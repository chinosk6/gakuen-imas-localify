#include <android/log.h>
#include "Hook.h"
#include "Plugin.h"
#include "Log.h"
#include "../deps/UnityResolve/UnityResolve.hpp"
#include "Il2cppUtils.hpp"
#include "Local.h"
#include <unordered_set>
#include "camera/camera.hpp"
#include "config/Config.hpp"
#include "shadowhook.h"
#include <jni.h>
#include <thread>
#include <map>


std::unordered_set<void*> hookedStubs{};

#define DEFINE_HOOK(returnType, name, params)                                                      \
	using name##_Type = returnType(*) params;                                                      \
	name##_Type name##_Addr = nullptr;                                                             \
	name##_Type name##_Orig = nullptr;                                                             \
	returnType name##_Hook params


#define ADD_HOOK(name, addr)                                                                       \
	name##_Addr = reinterpret_cast<name##_Type>(addr);                                             \
	if (addr) {                                                                                    \
    	auto stub = hookInstaller->InstallHook(reinterpret_cast<void*>(addr),                      \
                                               reinterpret_cast<void*>(name##_Hook),               \
                                               reinterpret_cast<void**>(&name##_Orig));            \
        if (stub == NULL) {                                                                        \
            int error_num = shadowhook_get_errno();                                                \
            const char *error_msg = shadowhook_to_errmsg(error_num);                               \
            Log::ErrorFmt("ADD_HOOK: %s at %p failed: %s", #name, addr, error_msg);                \
        }                                                                                          \
        else {                                                                                     \
            hookedStubs.emplace(stub);                                                             \
            GakumasLocal::Log::InfoFmt("ADD_HOOK: %s at %p", #name, addr);                         \
        }                                                                                          \
    }                                                                                              \
    else GakumasLocal::Log::ErrorFmt("Hook failed: %s is NULL", #name, addr)

void UnHookAll() {
    for (const auto i: hookedStubs) {
        int result = shadowhook_unhook(i);
        if(result != 0)
        {
            int error_num = shadowhook_get_errno();
            const char *error_msg = shadowhook_to_errmsg(error_num);
            GakumasLocal::Log::ErrorFmt("unhook failed: %d - %s", error_num, error_msg);
        }
    }
}

namespace GakumasLocal::HookMain {
    using Il2cppString = UnityResolve::UnityType::String;

    UnityResolve::UnityType::String* environment_get_stacktrace() {
        /*
        static auto mtd = Il2cppUtils::GetMethod("mscorlib.dll", "System",
                                                 "Environment", "get_StackTrace");
        return mtd->Invoke<UnityResolve::UnityType::String*>();*/
        const auto pClass = Il2cppUtils::GetClass("mscorlib.dll", "System.Diagnostics",
                                                  "StackTrace");

        const auto ctor_mtd = Il2cppUtils::GetMethod("mscorlib.dll", "System.Diagnostics",
                                                     "StackTrace", ".ctor");
        const auto toString_mtd = Il2cppUtils::GetMethod("mscorlib.dll", "System.Diagnostics",
                                                         "StackTrace", "ToString");

        const auto klassInstance = pClass->New<void*>();
        ctor_mtd->Invoke<void>(klassInstance);
        return toString_mtd->Invoke<Il2cppString*>(klassInstance);
    }

    DEFINE_HOOK(void, Internal_LogException, (void* ex, void* obj)) {
        Internal_LogException_Orig(ex, obj);
        static auto Exception_ToString = Il2cppUtils::GetMethod("mscorlib.dll", "System", "Exception", "ToString");
        Log::LogUnityLog(ANDROID_LOG_ERROR, "UnityLog - Internal_LogException:\n%s", Exception_ToString->Invoke<Il2cppString*>(ex)->ToString().c_str());
    }

    DEFINE_HOOK(void, Internal_Log, (int logType, int logOption, UnityResolve::UnityType::String* content, void* context)) {
        Internal_Log_Orig(logType, logOption, content, context);
        // 2022.3.21f1
        Log::LogUnityLog(ANDROID_LOG_VERBOSE, "Internal_Log:\n%s", content->ToString().c_str());
    }

    bool IsNativeObjectAlive(void* obj) {
        static UnityResolve::Method* IsNativeObjectAliveMtd = nullptr;
        if (!IsNativeObjectAliveMtd) IsNativeObjectAliveMtd = Il2cppUtils::GetMethod("UnityEngine.CoreModule.dll", "UnityEngine",
                                                                                     "Object", "IsNativeObjectAlive");
        return IsNativeObjectAliveMtd->Invoke<bool>(obj);
    }

    UnityResolve::UnityType::Camera* mainCameraCache = nullptr;
    UnityResolve::UnityType::Transform* cameraTransformCache = nullptr;
    void CheckAndUpdateMainCamera() {
        if (!Config::enableFreeCamera) return;
        if (IsNativeObjectAlive(mainCameraCache)) return;

        mainCameraCache = UnityResolve::UnityType::Camera::GetMain();
        cameraTransformCache = mainCameraCache->GetTransform();
    }

    Il2cppUtils::Resolution_t GetResolution() {
        static auto GetResolution = Il2cppUtils::GetMethod("UnityEngine.CoreModule.dll", "UnityEngine",
                                                           "Screen", "get_currentResolution");
        return GetResolution->Invoke<Il2cppUtils::Resolution_t>();
    }

    DEFINE_HOOK(void, Unity_set_fieldOfView, (UnityResolve::UnityType::Camera* self, float value)) {
        if (Config::enableFreeCamera) {
            if (self == mainCameraCache) {
                value = GKCamera::baseCamera.fov;
            }
        }
        Unity_set_fieldOfView_Orig(self, value);
    }

    DEFINE_HOOK(float, Unity_get_fieldOfView, (UnityResolve::UnityType::Camera* self)) {
        if (Config::enableFreeCamera) {
            if (self == mainCameraCache) {
                static auto get_orthographic = reinterpret_cast<bool (*)(void*)>(Il2cppUtils::il2cpp_resolve_icall(
                        "UnityEngine.Camera::get_orthographic()"
                ));
                static auto set_orthographic = reinterpret_cast<bool (*)(void*, bool)>(Il2cppUtils::il2cpp_resolve_icall(
                        "UnityEngine.Camera::set_orthographic(System.Boolean)"
                ));

                for (const auto& i : UnityResolve::UnityType::Camera::GetAllCamera()) {
                    // Log::DebugFmt("get_orthographic: %d", get_orthographic(i));
                    // set_orthographic(i, false);
                    Unity_set_fieldOfView_Orig(i, GKCamera::baseCamera.fov);
                }
                Unity_set_fieldOfView_Orig(self, GKCamera::baseCamera.fov);

                // Log::DebugFmt("main - get_orthographic: %d", get_orthographic(self));
                return GKCamera::baseCamera.fov;
            }
        }
        return Unity_get_fieldOfView_Orig(self);
    }

    UnityResolve::UnityType::Transform* cacheTrans = nullptr;
    UnityResolve::UnityType::Quaternion cacheRotation{};
    UnityResolve::UnityType::Vector3 cachePosition{};
    UnityResolve::UnityType::Vector3 cacheForward{};
    UnityResolve::UnityType::Vector3 cacheLookAt{};

    DEFINE_HOOK(void, Unity_set_rotation_Injected, (UnityResolve::UnityType::Transform* self, UnityResolve::UnityType::Quaternion* value)) {
        if (Config::enableFreeCamera) {
            static auto lookat_injected = reinterpret_cast<void (*)(void*self,
                                                                    UnityResolve::UnityType::Vector3* worldPosition, UnityResolve::UnityType::Vector3* worldUp)>(
                    Il2cppUtils::il2cpp_resolve_icall(
                            "UnityEngine.Transform::Internal_LookAt_Injected(UnityEngine.Vector3&,UnityEngine.Vector3&)"));
            static auto worldUp = UnityResolve::UnityType::Vector3(0, 1, 0);

            if (cameraTransformCache == self) {
                const auto cameraMode = GKCamera::GetCameraMode();
                if (cameraMode == GKCamera::CameraMode::FIRST_PERSON) {
                    if (cacheTrans && IsNativeObjectAlive(cacheTrans)) {
                        if (GKCamera::GetFirstPersonRoll() == GKCamera::FirstPersonRoll::ENABLE_ROLL) {
                            *value = cacheRotation;
                        }
                        else {
                            static GakumasLocal::Misc::FixedSizeQueue<float> recordsY(60);
                            const auto newY = GKCamera::CheckNewY(cacheLookAt, true, recordsY);
                            UnityResolve::UnityType::Vector3 newCacheLookAt{cacheLookAt.x, newY, cacheLookAt.z};
                            lookat_injected(self, &newCacheLookAt, &worldUp);
                            return;
                        }
                    }
                }
                else if (cameraMode == GKCamera::CameraMode::FOLLOW) {
                    auto newLookAtPos = GKCamera::CalcFollowModeLookAt(cachePosition,
                                                                       GKCamera::followPosOffset, true);
                    lookat_injected(self, &newLookAtPos, &worldUp);
                    return;
                }
                else {
                    auto& origCameraLookat = GKCamera::baseCamera.lookAt;
                    lookat_injected(self, &origCameraLookat, &worldUp);
                    // Log::DebugFmt("fov: %f, target: %f", Unity_get_fieldOfView_Orig(mainCameraCache), GKCamera::baseCamera.fov);
                    return;
                }
            }
        }
        return Unity_set_rotation_Injected_Orig(self, value);
    }

    DEFINE_HOOK(void, Unity_set_position_Injected, (UnityResolve::UnityType::Transform* self, UnityResolve::UnityType::Vector3* data)) {
        if (Config::enableFreeCamera) {
            CheckAndUpdateMainCamera();

            if (cameraTransformCache == self) {
                const auto cameraMode = GKCamera::GetCameraMode();
                if (cameraMode == GKCamera::CameraMode::FIRST_PERSON) {
                    if (cacheTrans && IsNativeObjectAlive(cacheTrans)) {
                        *data = GKCamera::CalcFirstPersonPosition(cachePosition, cacheForward, GKCamera::firstPersonPosOffset);
                    }

                }
                else if (cameraMode == GKCamera::CameraMode::FOLLOW) {
                    auto newLookAtPos = GKCamera::CalcFollowModeLookAt(cachePosition, GKCamera::followPosOffset);
                    auto pos = GKCamera::CalcPositionFromLookAt(newLookAtPos, GKCamera::followPosOffset);
                    data->x = pos.x;
                    data->y = pos.y;
                    data->z = pos.z;
                }
                else {
                    //Log::DebugFmt("MainCamera set pos: %f, %f, %f", data->x, data->y, data->z);
                    auto& origCameraPos = GKCamera::baseCamera.pos;
                    data->x = origCameraPos.x;
                    data->y = origCameraPos.y;
                    data->z = origCameraPos.z;
                }
            }
        }

        return Unity_set_position_Injected_Orig(self, data);
    }

    DEFINE_HOOK(void*, InternalSetOrientationAsync, (void* self, int type, void* c, void* tc, void* mtd)) {
        switch (Config::gameOrientation) {
            case 1: type = 0x2; break;  // FixedPortrait
            case 2: type = 0x3; break;  // FixedLandscape
            default: break;
        }
        return InternalSetOrientationAsync_Orig(self, type, c, tc, mtd);
    }

    DEFINE_HOOK(void, EndCameraRendering, (void* ctx, void* camera, void* method)) {
        EndCameraRendering_Orig(ctx, camera, method);

        if (Config::enableFreeCamera && mainCameraCache) {
            Unity_set_fieldOfView_Orig(mainCameraCache, GKCamera::baseCamera.fov);
            if (GKCamera::GetCameraMode() == GKCamera::CameraMode::FIRST_PERSON) {
                mainCameraCache->SetNearClipPlane(0.001f);
            }
        }
    }

    DEFINE_HOOK(void, Unity_set_targetFrameRate, (int value)) {
        const auto configFps = Config::targetFrameRate;
        return Unity_set_targetFrameRate_Orig(configFps == 0 ? value: configFps);
    }

    std::unordered_map<void*, std::string> loadHistory{};

    DEFINE_HOOK(void*, AssetBundle_LoadAssetAsync, (void* self, Il2cppString* name, void* type)) {
        // Log::InfoFmt("AssetBundle_LoadAssetAsync: %s, type: %s", name->ToString().c_str());
        auto ret = AssetBundle_LoadAssetAsync_Orig(self, name, type);
        loadHistory.emplace(ret, name->ToString());
        return ret;
    }

    DEFINE_HOOK(void*, AssetBundleRequest_GetResult, (void* self)) {
        auto result = AssetBundleRequest_GetResult_Orig(self);
        if (const auto iter = loadHistory.find(self); iter != loadHistory.end()) {
            const auto name = iter->second;
            loadHistory.erase(iter);

            // const auto assetClass = Il2cppUtils::get_class_from_instance(result);
            // Log::InfoFmt("AssetBundleRequest_GetResult: %s, type: %s", name.c_str(), static_cast<Il2CppClassHead*>(assetClass)->name);
        }
        return result;
    }

    DEFINE_HOOK(void*, Resources_Load, (Il2cppString* path, void* systemTypeInstance)) {
        auto ret = Resources_Load_Orig(path, systemTypeInstance);

        // if (ret) Log::DebugFmt("Resources_Load: %s, type: %s", path->ToString().c_str(), Il2cppUtils::get_class_from_instance(ret)->name);

        return ret;
    }

    DEFINE_HOOK(void, I18nHelper_SetUpI18n, (void* self, Il2cppString* lang, Il2cppString* localizationText, int keyComparison)) {
        // Log::InfoFmt("SetUpI18n lang: %s, key: %d text: %s", lang->ToString().c_str(), keyComparison, localizationText->ToString().c_str());
        // TODO 此处为 dump 原文 csv
        I18nHelper_SetUpI18n_Orig(self, lang, localizationText, keyComparison);
    }

    DEFINE_HOOK(void, I18nHelper_SetValue, (void* self, Il2cppString* key, Il2cppString* value)) {
        // Log::InfoFmt("I18nHelper_SetValue: %s - %s", key->ToString().c_str(), value->ToString().c_str());
        std::string local;
        if (Local::GetI18n(key->ToString(), &local)) {
            I18nHelper_SetValue_Orig(self, key, UnityResolve::UnityType::String::New(local));
            return;
        }
        Local::DumpI18nItem(key->ToString(), value->ToString());
        if (Config::textTest) {
            I18nHelper_SetValue_Orig(self, key, Il2cppString::New("[I18]" + value->ToString()));
        }
        else {
            I18nHelper_SetValue_Orig(self, key, value);
        }
    }

    void* fontCache = nullptr;
    void* GetReplaceFont() {
        static std::string fontName = Local::GetBasePath() / "local-files" / "gkamsZHFontMIX.otf";
        if (!std::filesystem::exists(fontName)) {
            return nullptr;
        }

        static auto CreateFontFromPath = reinterpret_cast<void (*)(void* self, Il2cppString* path)>(
                Il2cppUtils::il2cpp_resolve_icall("UnityEngine.Font::Internal_CreateFontFromPath(UnityEngine.Font,System.String)")
        );
        static auto Font_klass = Il2cppUtils::GetClass("UnityEngine.TextRenderingModule.dll",
                                                       "UnityEngine", "Font");
        static auto Font_ctor = Il2cppUtils::GetMethod("UnityEngine.TextRenderingModule.dll",
                                                       "UnityEngine", "Font", ".ctor");
        if (fontCache) {
            if (IsNativeObjectAlive(fontCache)) {
                return fontCache;
            }
        }

        const auto newFont = Font_klass->New<void*>();
        Font_ctor->Invoke<void>(newFont);

        CreateFontFromPath(newFont, Il2cppString::New(fontName));
        fontCache = newFont;
        return newFont;
    }

    std::unordered_set<void*> updatedFontPtrs{};
    void UpdateFont(void* TMP_Textself) {
        if (!Config::replaceFont) return;
        static auto get_font = Il2cppUtils::GetMethod("Unity.TextMeshPro.dll",
                                                      "TMPro", "TMP_Text", "get_font");
        static auto set_font = Il2cppUtils::GetMethod("Unity.TextMeshPro.dll",
                                                      "TMPro", "TMP_Text", "set_font");

        static auto set_sourceFontFile = Il2cppUtils::GetMethod("Unity.TextMeshPro.dll", "TMPro",
                                                                "TMP_FontAsset", "set_sourceFontFile");
        static auto UpdateFontAssetData = Il2cppUtils::GetMethod("Unity.TextMeshPro.dll", "TMPro",
                                                                 "TMP_FontAsset", "UpdateFontAssetData");

        auto newFont = GetReplaceFont();
        if (!newFont) return;
        auto fontAsset = get_font->Invoke<void*>(TMP_Textself);
        if (fontAsset) {
            set_sourceFontFile->Invoke<void>(fontAsset, newFont);
            if (!updatedFontPtrs.contains(fontAsset)) {
                updatedFontPtrs.emplace(fontAsset);
                UpdateFontAssetData->Invoke<void>(fontAsset);
            }
            if (updatedFontPtrs.size() > 200) updatedFontPtrs.clear();
        }
        set_font->Invoke<void>(TMP_Textself, fontAsset);
    }

    DEFINE_HOOK(void, TMP_Text_PopulateTextBackingArray, (void* self, UnityResolve::UnityType::String* text, int start, int length)) {
        if (!text) {
            return TMP_Text_PopulateTextBackingArray_Orig(self, text, start, length);
        }

        static auto Substring = Il2cppUtils::GetMethod("mscorlib.dll", "System", "String", "Substring",
                                                       {"System.Int32", "System.Int32"});

        const std::string origText = Substring->Invoke<Il2cppString*>(text, start, length)->ToString();
        std::string transText;
        if (Local::GetGenericText(origText, &transText)) {
            const auto newText = UnityResolve::UnityType::String::New(transText);
            return TMP_Text_PopulateTextBackingArray_Orig(self, newText, 0, newText->length);
        }

        if (Config::textTest) {
            TMP_Text_PopulateTextBackingArray_Orig(self, UnityResolve::UnityType::String::New("[TP]" + text->ToString()), start, length + 4);
        }
        else {
            TMP_Text_PopulateTextBackingArray_Orig(self, text, start, length);
        }
        UpdateFont(self);
    }

    DEFINE_HOOK(void, TextMeshProUGUI_Awake, (void* self, void* method)) {
        // Log::InfoFmt("TextMeshProUGUI_Awake at %p, self at %p", TextMeshProUGUI_Awake_Orig, self);

        const auto TMP_Text_klass = Il2cppUtils::GetClass("Unity.TextMeshPro.dll",
                                                                     "TMPro", "TMP_Text");
        const auto get_Text_method = TMP_Text_klass->Get<UnityResolve::Method>("get_text");
        const auto set_Text_method = TMP_Text_klass->Get<UnityResolve::Method>("set_text");
        const auto currText = get_Text_method->Invoke<UnityResolve::UnityType::String*>(self);
        if (currText) {
            //Log::InfoFmt("TextMeshProUGUI_Awake: %s", currText->ToString().c_str());
            std::string transText;
            if (Local::GetGenericText(currText->ToString(), &transText)) {
                if (Config::textTest) {
                    set_Text_method->Invoke<void>(self, UnityResolve::UnityType::String::New("[TA]" + transText));
                }
                else {
                    set_Text_method->Invoke<void>(self, UnityResolve::UnityType::String::New(transText));
                }
            }
        }

        // set_font->Invoke<void>(self, font);
        UpdateFont(self);
        TextMeshProUGUI_Awake_Orig(self, method);
    }

    // TODO 文本未hook完整
    DEFINE_HOOK(void, TextField_set_value, (void* self, Il2cppString* value)) {
        Log::DebugFmt("TextField_set_value: %s", value->ToString().c_str());
        TextField_set_value_Orig(self, value);
    }

    DEFINE_HOOK(Il2cppString*, OctoCaching_GetResourceFileName, (void* data, void* method)) {
        auto ret = OctoCaching_GetResourceFileName_Orig(data, method);
        //Log::DebugFmt("OctoCaching_GetResourceFileName: %s", ret->ToString().c_str());
        return ret;
    }

    DEFINE_HOOK(void, OctoResourceLoader_LoadFromCacheOrDownload,
                (void* self, Il2cppString* resourceName, void* onComplete, void* onProgress, void* method)) {

        Log::DebugFmt("OctoResourceLoader_LoadFromCacheOrDownload: %s\n", resourceName->ToString().c_str());

        std::string replaceStr;
        if (Local::GetResourceText(resourceName->ToString(), &replaceStr)) {
            const auto onComplete_klass = Il2cppUtils::get_class_from_instance(onComplete);
            const auto onComplete_invoke_mtd = UnityResolve::Invoke<Il2cppUtils::MethodInfo*>(
                    "il2cpp_class_get_method_from_name", onComplete_klass, "Invoke", 2);
            if (onComplete_invoke_mtd) {
                const auto onComplete_invoke = reinterpret_cast<void (*)(void*, Il2cppString*, void*)>(
                        onComplete_invoke_mtd->methodPointer
                );
                onComplete_invoke(onComplete, UnityResolve::UnityType::String::New(replaceStr), nullptr);
                return;
            }
        }

        return OctoResourceLoader_LoadFromCacheOrDownload_Orig(self, resourceName, onComplete, onProgress, method);
    }

    DEFINE_HOOK(void, OnDownloadProgress_Invoke, (void* self, Il2cppString* name, uint64_t receivedLength, uint64_t contentLength)) {
        Log::DebugFmt("OnDownloadProgress_Invoke: %s, %lu/%lu", name->ToString().c_str(), receivedLength, contentLength);
        OnDownloadProgress_Invoke_Orig(self, name, receivedLength, contentLength);
    }

    // UnHooked
    DEFINE_HOOK(UnityResolve::UnityType::String*, UI_I18n_GetOrDefault, (void* self,
            UnityResolve::UnityType::String* key, UnityResolve::UnityType::String* defaultKey, void* method)) {

        auto ret = UI_I18n_GetOrDefault_Orig(self, key, defaultKey, method);

        // Log::DebugFmt("UI_I18n_GetOrDefault: key: %s, default: %s, result: %s", key->ToString().c_str(), defaultKey->ToString().c_str(), ret->ToString().c_str());

        return ret;
        // return UnityResolve::UnityType::String::New("[I18]" + ret->ToString());
    }

    DEFINE_HOOK(void, PictureBookLiveThumbnailView_SetData, (void* self, void* liveData, bool isUnlocked, bool isNew, void* ct, void* mtd)) {
        // Log::DebugFmt("PictureBookLiveThumbnailView_SetData: isUnlocked: %d, isNew: %d", isUnlocked, isNew);
        if (Config::dbgMode && Config::unlockAllLive) {
            isUnlocked = true;
        }
        PictureBookLiveThumbnailView_SetData_Orig(self, liveData, isUnlocked, isNew, ct, mtd);
    }

    bool needRestoreHides = false;
    DEFINE_HOOK(void*, PictureBookLiveSelectScreenPresenter_MoveLiveScene, (void* self, void* produceLive,
            Il2cppString* characterId, Il2cppString* costumeId, Il2cppString* costumeHeadId)) {
        needRestoreHides = false;
        Log::InfoFmt("MoveLiveScene: characterId: %s, costumeId: %s, costumeHeadId: %s,",
                     characterId->ToString().c_str(), costumeId->ToString().c_str(), costumeHeadId->ToString().c_str());

        /*
         characterId: hski, costumeId: hski-cstm-0002, costumeHeadId: costume_head_hski-cstm-0002,
         characterId: shro, costumeId: shro-cstm-0006, costumeHeadId: costume_head_shro-cstm-0006,
         */

        if (Config::dbgMode && Config::enableLiveCustomeDress) {
            // 修改 LiveFixedData_GetCharacter 可以更改 Loading 角色和演唱者名字，而不变更实际登台人
            return PictureBookLiveSelectScreenPresenter_MoveLiveScene_Orig(self, produceLive, characterId,
                                                                           Config::liveCustomeCostumeId.empty() ? costumeId : Il2cppString::New(Config::liveCustomeCostumeId),
                                                                           Config::liveCustomeHeadId.empty() ? costumeHeadId : Il2cppString::New(Config::liveCustomeHeadId));
        }

        return PictureBookLiveSelectScreenPresenter_MoveLiveScene_Orig(self, produceLive, characterId, costumeId, costumeHeadId);
    }

    // std::string lastMusicId;
    DEFINE_HOOK(void, PictureBookLiveSelectScreenPresenter_OnSelectMusic, (void* self, void* itemModel, void* ct, void* mtd)) {
        /*  // 修改角色后，Live 结束返回时, itemModel 为 null
        Log::DebugFmt("OnSelectMusic itemModel at %p", itemModel);

        static auto GetMusic = Il2cppUtils::GetMethod("Assembly-CSharp.dll", "Campus.OutGame",
                                                      "PlaylistMusicContext", "GetMusic");
        static auto GetCurrMusic = Il2cppUtils::GetMethod("Assembly-CSharp.dll", "Campus.OutGame.PictureBook",
                                                      "PictureBookLiveSelectMusicListItemModel", "get_Music");
        static auto GetMusicId = Il2cppUtils::GetMethod("Assembly-CSharp.dll", "Campus.Common.Proto.Client.Master",
                                                          "Music", "get_Id");

        static auto PictureBookLiveSelectMusicListItemModel_klass = Il2cppUtils::GetClass("Assembly-CSharp.dll", "Campus.OutGame.PictureBook",
                                                                                          "PictureBookLiveSelectMusicListItemModel");
        static auto PictureBookLiveSelectMusicListItemModel_ctor = Il2cppUtils::GetMethod("Assembly-CSharp.dll", "Campus.OutGame.PictureBook",
                                                                                          "PictureBookLiveSelectMusicListItemModel", ".ctor", {"*", "*"});

        if (!itemModel) {
            Log::DebugFmt("OnSelectMusic block", itemModel);
            auto music = GetMusic->Invoke<void*>(lastMusicId);
            auto newItemModel = PictureBookLiveSelectMusicListItemModel_klass->New<void*>();
            PictureBookLiveSelectMusicListItemModel_ctor->Invoke<void>(newItemModel, music, false);

            return PictureBookLiveSelectScreenPresenter_OnSelectMusic_Orig(self, newItemModel, isFirst, mtd);
        }

        if (itemModel) {
            auto currMusic = GetCurrMusic->Invoke<void*>(itemModel);
            auto musicId = GetMusicId->Invoke<Il2cppString*>(currMusic);
            lastMusicId = musicId->ToString();
        }*/
        if (!itemModel) return;
        return PictureBookLiveSelectScreenPresenter_OnSelectMusic_Orig(self, itemModel, ct, mtd);
    }

    DEFINE_HOOK(bool, VLDOF_IsActive, (void* self)) {
        if (Config::enableFreeCamera) return false;
        return VLDOF_IsActive_Orig(self);
    }

    DEFINE_HOOK(void, CampusQualityManager_set_TargetFrameRate, (void* self, float value)) {
        // Log::InfoFmt("CampusQualityManager_set_TargetFrameRate: %f", value);
        const auto configFps = Config::targetFrameRate;
        CampusQualityManager_set_TargetFrameRate_Orig(self, configFps == 0 ? value : (float)configFps);
    }

    DEFINE_HOOK(void, CampusQualityManager_ApplySetting, (void* self, int qualitySettingsLevel, int maxBufferPixel, float renderScale, int volumeIndex)) {
        if (Config::targetFrameRate != 0) {
            CampusQualityManager_set_TargetFrameRate_Orig(self, Config::targetFrameRate);
        }
        if (Config::useCustomeGraphicSettings) {
            static auto SetReflectionQuality = Il2cppUtils::GetMethod("campus-submodule.Runtime.dll", "Campus.Common",
                                                                      "CampusQualityManager", "SetReflectionQuality");
            static auto SetLODQuality = Il2cppUtils::GetMethod("campus-submodule.Runtime.dll", "Campus.Common",
                                                               "CampusQualityManager", "SetLODQuality");

            static auto Enum_GetValues = Il2cppUtils::GetMethod("mscorlib.dll", "System", "Enum", "GetValues");

            static auto QualityLevel_klass = Il2cppUtils::GetClass("campus-submodule.Runtime.dll", "", "QualityLevel");

            static auto values = Enum_GetValues->Invoke<UnityResolve::UnityType::Array<int>*>(QualityLevel_klass->GetType())->ToVector();
            if (values.empty()) {
                values = {0x0, 0xa, 0x14, 0x1e, 0x28, 0x64};
            }
            if (Config::lodQualityLevel >= values.size()) Config::lodQualityLevel = values.size() - 1;
            if (Config::reflectionQualityLevel >= values.size()) Config::reflectionQualityLevel = values.size() - 1;

            SetLODQuality->Invoke<void>(self, values[Config::lodQualityLevel]);
            SetReflectionQuality->Invoke<void>(self, values[Config::reflectionQualityLevel]);

            qualitySettingsLevel = Config::qualitySettingsLevel;
            maxBufferPixel = Config::maxBufferPixel;
            renderScale = Config::renderScale;
            volumeIndex = Config::volumeIndex;

            Log::ShowToastFmt("ApplySetting\nqualityLevel: %d, maxBufferPixel: %d\nenderScale: %f, volumeIndex: %d\nLODQualityLv: %d, ReflectionLv: %d",
                              qualitySettingsLevel, maxBufferPixel, renderScale, volumeIndex, Config::lodQualityLevel, Config::reflectionQualityLevel);
        }

        CampusQualityManager_ApplySetting_Orig(self, qualitySettingsLevel, maxBufferPixel, renderScale, volumeIndex);
    }

    DEFINE_HOOK(void, UIManager_UpdateRenderTarget, (UnityResolve::UnityType::Vector2 ratio, void* mtd)) {
        // const auto resolution = GetResolution();
        // Log::DebugFmt("UIManager_UpdateRenderTarget: %f, %f", ratio.x, ratio.y);
        return UIManager_UpdateRenderTarget_Orig(ratio, mtd);
    }

    DEFINE_HOOK(void, VLSRPCameraController_UpdateRenderTarget, (void* self, int width, int height, bool forceAlpha, void* method)) {
        // const auto resolution = GetResolution();
        // Log::DebugFmt("VLSRPCameraController_UpdateRenderTarget: %d, %d", width, height);
        return VLSRPCameraController_UpdateRenderTarget_Orig(self, width, height, forceAlpha, method);
    }

    DEFINE_HOOK(void*, VLUtility_GetLimitedResolution, (int32_t screenWidth, int32_t screenHeight,
            UnityResolve::UnityType::Vector2 aspectRatio, int32_t maxBufferPixel, float bufferScale, bool firstCall)) {

        if (Config::useCustomeGraphicSettings && (Config::renderScale > 1.0f)) {
            screenWidth *= Config::renderScale;
            screenHeight *= Config::renderScale;
        }
        //Log::DebugFmt("VLUtility_GetLimitedResolution: %d, %d, %f, %f", screenWidth, screenHeight, aspectRatio.x, aspectRatio.y);
        return VLUtility_GetLimitedResolution_Orig(screenWidth, screenHeight, aspectRatio, maxBufferPixel, bufferScale, firstCall);
    }


    DEFINE_HOOK(void, CampusActorModelParts_OnRegisterBone, (void* self, Il2cppString** name, UnityResolve::UnityType::Transform* bone)) {
        CampusActorModelParts_OnRegisterBone_Orig(self, name, bone);
        // Log::DebugFmt("CampusActorModelParts_OnRegisterBone: %s, %p", (*name)->ToString().c_str(), bone);
    }

    bool InitBodyParts() {
        static auto isInit = false;
        if (isInit) return true;

        const auto Enum_GetValues = Il2cppUtils::GetMethod("mscorlib.dll", "System", "Enum", "GetValues");
        const auto Enum_GetNames = Il2cppUtils::GetMethod("mscorlib.dll", "System", "Enum", "GetNames");

        const auto HumanBodyBones_klass = Il2cppUtils::GetClass(
                "UnityEngine.AnimationModule.dll", "UnityEngine", "HumanBodyBones");

        const auto values = Enum_GetValues->Invoke<UnityResolve::UnityType::Array<int>*>(HumanBodyBones_klass->GetType())->ToVector();
        const auto names = Enum_GetNames->Invoke<UnityResolve::UnityType::Array<Il2cppString*>*>(HumanBodyBones_klass->GetType())->ToVector();
        if (values.size() != names.size()) {
            Log::ErrorFmt("InitBodyParts Error: values count: %ld, names count: %ld", values.size(), names.size());
            return false;
        }

        std::vector<std::string> namesVec{};
        namesVec.reserve(names.size());
        for (auto i :names) {
            namesVec.push_back(i->ToString());
        }
        GKCamera::bodyPartsEnum = Misc::CSEnum(namesVec, values);
        GKCamera::bodyPartsEnum.SetIndex(GKCamera::bodyPartsEnum.GetValueByName("Head"));
        isInit = true;
        return true;
    }

    void HideHead(UnityResolve::UnityType::GameObject* obj, const bool isFace) {
        static UnityResolve::UnityType::GameObject* lastFaceObj = nullptr;
        static UnityResolve::UnityType::GameObject* lastHairObj = nullptr;

#define lastHidedObj (isFace ? lastFaceObj : lastHairObj)

       static auto get_activeInHierarchy = reinterpret_cast<bool (*)(void*)>(
                Il2cppUtils::il2cpp_resolve_icall("UnityEngine.GameObject::get_activeInHierarchy()"));

        const auto isFirstPerson = GKCamera::GetCameraMode() == GKCamera::CameraMode::FIRST_PERSON;

        if (isFirstPerson && obj) {
            if (obj == lastHidedObj) return;
            if (lastHidedObj && IsNativeObjectAlive(lastHidedObj) && get_activeInHierarchy(lastHidedObj)) {
                lastHidedObj->SetActive(true);
            }
            if (IsNativeObjectAlive(obj)) {
                obj->SetActive(false);
                lastHidedObj = obj;
            }
        }
        else {
            if (lastHidedObj && IsNativeObjectAlive(lastHidedObj)) {
                lastHidedObj->SetActive(true);
                lastHidedObj = nullptr;
            }
        }
    }

    DEFINE_HOOK(void, CampusActorController_LateUpdate, (void* self, void* mtd)) {
        static auto CampusActorController_klass = Il2cppUtils::GetClass("campus-submodule.Runtime.dll",
                                                                        "Campus.Common", "CampusActorController");
        static auto rootBody_field = CampusActorController_klass->Get<UnityResolve::Field>("_rootBody");
        static auto parentKlass = UnityResolve::Invoke<void*>("il2cpp_class_get_parent", CampusActorController_klass->address);

        if (!Config::enableFreeCamera || (GKCamera::GetCameraMode() == GKCamera::CameraMode::FREE)) {
            if (needRestoreHides) {
                needRestoreHides = false;
                HideHead(nullptr, false);
                HideHead(nullptr, true);
            }
            return CampusActorController_LateUpdate_Orig(self, mtd);
        }

        static auto GetHumanBodyBoneTransform_mtd = Il2cppUtils::il2cpp_class_get_method_from_name(parentKlass, "GetHumanBodyBoneTransform", 1);
        static auto GetHumanBodyBoneTransform = reinterpret_cast<UnityResolve::UnityType::Transform* (*)(void*, int)>(
                GetHumanBodyBoneTransform_mtd->methodPointer
                );
        static auto get_index_mtd = Il2cppUtils::il2cpp_class_get_method_from_name(CampusActorController_klass->address, "get_index", 0);
        static auto get_Index = get_index_mtd ? reinterpret_cast<int (*)(void*)>(
                get_index_mtd->methodPointer) : [](void*){return 0;};

        const auto currIndex = get_Index(self);
        if (currIndex == GKCamera::followCharaIndex) {
            static auto initPartsSuccess = InitBodyParts();
            static auto headBodyId = initPartsSuccess ? GKCamera::bodyPartsEnum.GetValueByName("Head") : 0xA;
            const auto isFirstPerson = GKCamera::GetCameraMode() == GKCamera::CameraMode::FIRST_PERSON;

            auto targetTrans = GetHumanBodyBoneTransform(self,
                                                         isFirstPerson ? headBodyId : GKCamera::bodyPartsEnum.GetCurrent().second);

            if (targetTrans) {
                cacheTrans = targetTrans;
                cacheRotation = cacheTrans->GetRotation();
                cachePosition = cacheTrans->GetPosition();
                cacheForward = cacheTrans->GetForward();
                cacheLookAt = cacheTrans->GetPosition() + cacheTrans->GetForward() * 3;

                auto rootBody = Il2cppUtils::ClassGetFieldValue<UnityResolve::UnityType::Transform*>(self, rootBody_field);
                auto rootModel = rootBody->GetParent();
                auto rootModelChildCount = rootModel->GetChildCount();
                for (int i = 0; i < rootModelChildCount; i++) {
                    auto rootChild = rootModel->GetChild(i);
                    const auto childName = rootChild->GetName();
                    if (childName == "Root_Face") {
                        for (int n = 0; n < rootChild->GetChildCount(); n++) {
                            auto vLSkinningRenderer = rootChild->GetChild(n);
                            if (vLSkinningRenderer->GetName() == "VLSkinningRenderer") {
                                HideHead(vLSkinningRenderer->GetGameObject(), true);
                                needRestoreHides = true;
                            }
                        }
                    }
                    else if (childName == "Root_Hair") {
                        HideHead(rootChild->GetGameObject(), false);
                        needRestoreHides = true;
                    }
                }
            }
            else {
                cacheTrans = nullptr;
            }

        }

        CampusActorController_LateUpdate_Orig(self, mtd);
    }

    void UpdateSwingBreastBonesData(void* initializeData) {
        if (!Config::enableBreastParam) return;
        static auto CampusActorAnimationInitializeData_klass = Il2cppUtils::GetClass("campus-submodule.Runtime.dll", "ActorAnimation",
                                                                                     "CampusActorAnimationInitializeData");
        static auto ActorSwingBreastBone_klass = Il2cppUtils::GetClass("ActorAnimation.Runtime.dll", "ActorAnimation",
                                                                       "ActorSwingBreastBone");
        static auto LimitInfo_klass = Il2cppUtils::GetClass("ActorAnimation.Runtime.dll", "ActorAnimation",
                                                            "LimitInfo");

        static auto Data_swingBreastBones_field = CampusActorAnimationInitializeData_klass->Get<UnityResolve::Field>("swingBreastBones");
        static auto damping_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("damping");
        static auto stiffness_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("stiffness");
        static auto spring_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("spring");
        static auto pendulum_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("pendulum");
        static auto pendulumRange_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("pendulumRange");
        static auto average_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("average");
        static auto rootWeight_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("rootWeight");
        static auto useArmCorrection_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("useArmCorrection");
        static auto isDirty_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("<isDirty>k__BackingField");
        static auto leftBreast_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("leftBreast");
        static auto rightBreast_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("rightBreast");
        static auto leftBreastEnd_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("leftBreastEnd");
        static auto rightBreastEnd_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("rightBreastEnd");
        static auto limitInfo_field = ActorSwingBreastBone_klass->Get<UnityResolve::Field>("limitInfo");

        static auto limitInfo_useLimit_field = LimitInfo_klass->Get<UnityResolve::Field>("useLimit");
        static auto limitInfo_axisX_field = LimitInfo_klass->Get<UnityResolve::Field>("axisX");
        static auto limitInfo_axisY_field = LimitInfo_klass->Get<UnityResolve::Field>("axisY");
        static auto limitInfo_axisZ_field = LimitInfo_klass->Get<UnityResolve::Field>("axisZ");

        auto swingBreastBones = Il2cppUtils::ClassGetFieldValue
                <UnityResolve::UnityType::List<UnityResolve::UnityType::MonoBehaviour*>*>(initializeData, Data_swingBreastBones_field);

        auto boneArr = swingBreastBones->ToArray();
        for (int i = 0; i < boneArr->max_length; i++) {
            auto bone = boneArr->At(i);
            if (!bone) continue;

            auto damping = Il2cppUtils::ClassGetFieldValue<float>(bone, damping_field);
            auto stiffness = Il2cppUtils::ClassGetFieldValue<float>(bone, stiffness_field);
            auto spring = Il2cppUtils::ClassGetFieldValue<float>(bone, spring_field);
            auto pendulum = Il2cppUtils::ClassGetFieldValue<float>(bone, pendulum_field);
            auto pendulumRange = Il2cppUtils::ClassGetFieldValue<float>(bone, pendulumRange_field);
            auto average = Il2cppUtils::ClassGetFieldValue<float>(bone, average_field);
            auto rootWeight = Il2cppUtils::ClassGetFieldValue<float>(bone, rootWeight_field);
            auto useArmCorrection = Il2cppUtils::ClassGetFieldValue<bool>(bone, useArmCorrection_field);
            auto isDirty = Il2cppUtils::ClassGetFieldValue<bool>(bone, isDirty_field);

            auto limitInfo = Il2cppUtils::ClassGetFieldValue<void*>(bone, limitInfo_field);
            auto useLimit = Il2cppUtils::ClassGetFieldValue<int>(limitInfo, limitInfo_useLimit_field);

            if (Config::bUseScale) {
                auto leftBreast = Il2cppUtils::ClassGetFieldValue<UnityResolve::UnityType::Transform*>(bone, leftBreast_field);
                auto rightBreast = Il2cppUtils::ClassGetFieldValue<UnityResolve::UnityType::Transform*>(bone, rightBreast_field);
                auto leftBreastEnd = Il2cppUtils::ClassGetFieldValue<UnityResolve::UnityType::Transform*>(bone, leftBreastEnd_field);
                auto rightBreastEnd = Il2cppUtils::ClassGetFieldValue<UnityResolve::UnityType::Transform*>(bone, rightBreastEnd_field);

                const auto setScale = UnityResolve::UnityType::Vector3(Config::bScale, Config::bScale, Config::bScale);
                leftBreast->SetLocalScale(setScale);
                rightBreast->SetLocalScale(setScale);
                leftBreastEnd->SetLocalScale(setScale);
                rightBreastEnd->SetLocalScale(setScale);
            }

            Log::DebugFmt("orig bone: damping: %f, stiffness: %f, spring: %f, pendulum: %f, "
                          "pendulumRange: %f, average: %f, rootWeight: %f, useLimit: %d, useArmCorrection: %d, isDirty: %d",
                          damping, stiffness, spring, pendulum, pendulumRange, average, rootWeight, useLimit, useArmCorrection, isDirty);
            if (!Config::bUseLimit) {
                Il2cppUtils::ClassSetFieldValue(limitInfo, limitInfo_useLimit_field, 0);
            }
            else {
                Il2cppUtils::ClassSetFieldValue(limitInfo, limitInfo_useLimit_field, 1);
                auto axisX = Il2cppUtils::ClassGetFieldValue<UnityResolve::UnityType::Vector2Int>(limitInfo, limitInfo_axisX_field);
                auto axisY = Il2cppUtils::ClassGetFieldValue<UnityResolve::UnityType::Vector2Int>(limitInfo, limitInfo_axisY_field);
                auto axisZ = Il2cppUtils::ClassGetFieldValue<UnityResolve::UnityType::Vector2Int>(limitInfo, limitInfo_axisZ_field);
                axisX.m_X *= Config::bLimitXx;
                axisX.m_Y *= Config::bLimitXy;
                axisY.m_X *= Config::bLimitYx;
                axisY.m_Y *= Config::bLimitYy;
                axisZ.m_X *= Config::bLimitZx;
                axisZ.m_Y *= Config::bLimitZy;
                Il2cppUtils::ClassSetFieldValue(limitInfo, limitInfo_axisX_field, axisX);
                Il2cppUtils::ClassSetFieldValue(limitInfo, limitInfo_axisY_field, axisY);
                Il2cppUtils::ClassSetFieldValue(limitInfo, limitInfo_axisZ_field, axisZ);

            }

            Il2cppUtils::ClassSetFieldValue(bone, damping_field, Config::bDamping);
            Il2cppUtils::ClassSetFieldValue(bone, stiffness_field, Config::bStiffness);
            Il2cppUtils::ClassSetFieldValue(bone, spring_field, Config::bSpring);
            Il2cppUtils::ClassSetFieldValue(bone, pendulum_field, Config::bPendulum);
            Il2cppUtils::ClassSetFieldValue(bone, pendulumRange_field, Config::bPendulumRange);
            Il2cppUtils::ClassSetFieldValue(bone, average_field, Config::bAverage);
            Il2cppUtils::ClassSetFieldValue(bone, rootWeight_field, Config::bRootWeight);
            Il2cppUtils::ClassSetFieldValue(bone, useArmCorrection_field, Config::bUseArmCorrection);
            // Il2cppUtils::ClassSetFieldValue(bone, isDirty_field, Config::bIsDirty);
        }
        // Log::DebugFmt("\n");
    }

    DEFINE_HOOK(void, CampusActorAnimation_Setup, (void* self, void* rootTrans, void* initializeData)) {
        UpdateSwingBreastBonesData(initializeData);
        return CampusActorAnimation_Setup_Orig(self, rootTrans, initializeData);
    }

    void StartInjectFunctions() {
        const auto hookInstaller = Plugin::GetInstance().GetHookInstaller();
        UnityResolve::Init(xdl_open(hookInstaller->m_il2cppLibraryPath.c_str(), RTLD_NOW), UnityResolve::Mode::Il2Cpp);

        ADD_HOOK(AssetBundle_LoadAssetAsync, Il2cppUtils::il2cpp_resolve_icall(
                "UnityEngine.AssetBundle::LoadAssetAsync_Internal(System.String,System.Type)"));
        ADD_HOOK(AssetBundleRequest_GetResult, Il2cppUtils::il2cpp_resolve_icall(
                "UnityEngine.AssetBundleRequest::GetResult()"));
        ADD_HOOK(Resources_Load, Il2cppUtils::il2cpp_resolve_icall(
                "UnityEngine.ResourcesAPIInternal::Load(System.String,System.Type)"));

        ADD_HOOK(I18nHelper_SetUpI18n, Il2cppUtils::GetMethodPointer("quaunity-ui.Runtime.dll", "Qua.UI",
                                                                     "I18nHelper", "SetUpI18n"));
        ADD_HOOK(I18nHelper_SetValue, Il2cppUtils::GetMethodPointer("quaunity-ui.Runtime.dll", "Qua.UI",
                                                                     "I18n", "SetValue"));

        //ADD_HOOK(UI_I18n_GetOrDefault, Il2cppUtils::GetMethodPointer("quaunity-ui.Runtime.dll", "Qua.UI",
        //                                                             "I18n", "GetOrDefault"));

        ADD_HOOK(TextMeshProUGUI_Awake, Il2cppUtils::GetMethodPointer("Unity.TextMeshPro.dll", "TMPro",
                                                                      "TextMeshProUGUI", "Awake"));

        ADD_HOOK(TMP_Text_PopulateTextBackingArray, Il2cppUtils::GetMethodPointer("Unity.TextMeshPro.dll", "TMPro",
                                                                  "TMP_Text", "PopulateTextBackingArray",
                                                                  {"System.String", "System.Int32", "System.Int32"}));

        ADD_HOOK(TextField_set_value, Il2cppUtils::GetMethodPointer("UnityEngine.UIElementsModule.dll", "UnityEngine.UIElements",
                                                                  "TextField", "set_value"));

        ADD_HOOK(OctoCaching_GetResourceFileName, Il2cppUtils::GetMethodPointer("Octo.dll", "Octo.Caching",
                                                                     "OctoCaching", "GetResourceFileName"));

        ADD_HOOK(OctoResourceLoader_LoadFromCacheOrDownload,
                 Il2cppUtils::GetMethodPointer("Octo.dll", "Octo.Loader",
                                               "OctoResourceLoader", "LoadFromCacheOrDownload",
                                               {"System.String", "System.Action<System.String,Octo.LoadError>", "Octo.OnDownloadProgress"}));

        ADD_HOOK(OnDownloadProgress_Invoke,
                 Il2cppUtils::GetMethodPointer("Octo.dll", "Octo",
                                               "OnDownloadProgress", "Invoke"));

        ADD_HOOK(PictureBookLiveThumbnailView_SetData,
                 Il2cppUtils::GetMethodPointer("Assembly-CSharp.dll", "Campus.OutGame.PictureBook",
                                               "PictureBookLiveThumbnailView", "SetDataAsync", {"*", "*", "*", "*"}));

        ADD_HOOK(PictureBookLiveSelectScreenPresenter_MoveLiveScene,
                 Il2cppUtils::GetMethodPointer("Assembly-CSharp.dll", "Campus.OutGame",
                                               "PictureBookLiveSelectScreenPresenter", "MoveLiveScene"));
        ADD_HOOK(PictureBookLiveSelectScreenPresenter_OnSelectMusic,
                 Il2cppUtils::GetMethodPointer("Assembly-CSharp.dll", "Campus.OutGame",
                                               "PictureBookLiveSelectScreenPresenter", "OnSelectMusicAsync"));

        ADD_HOOK(VLDOF_IsActive,
                 Il2cppUtils::GetMethodPointer("Unity.RenderPipelines.Universal.Runtime.dll", "VL.Rendering",
                                               "VLDOF", "IsActive"));

        ADD_HOOK(CampusQualityManager_ApplySetting,
                 Il2cppUtils::GetMethodPointer("campus-submodule.Runtime.dll", "Campus.Common",
                                               "CampusQualityManager", "ApplySetting"));

        ADD_HOOK(UIManager_UpdateRenderTarget,
                 Il2cppUtils::GetMethodPointer("ADV.Runtime.dll", "Campus.ADV",
                                               "UIManager", "UpdateRenderTarget"));
        ADD_HOOK(VLSRPCameraController_UpdateRenderTarget,
                 Il2cppUtils::GetMethodPointer("vl-unity.Runtime.dll", "VL.Rendering",
                                               "VLSRPCameraController", "UpdateRenderTarget",
                                               {"*", "*", "*"}));

        ADD_HOOK(VLUtility_GetLimitedResolution,
                 Il2cppUtils::GetMethodPointer("vl-unity.Runtime.dll", "VL",
                                               "VLUtility", "GetLimitedResolution",
                                               {"*", "*", "*", "*", "*", "*"}));

        ADD_HOOK(CampusActorModelParts_OnRegisterBone,
                 Il2cppUtils::GetMethodPointer("campus-submodule.Runtime.dll", "Campus.Common",
                                               "CampusActorModelParts", "OnRegisterBone"));
        ADD_HOOK(CampusActorController_LateUpdate,
                 Il2cppUtils::GetMethodPointer("campus-submodule.Runtime.dll", "Campus.Common",
                                               "CampusActorController", "LateUpdate"));
        /*
        static auto CampusActorController_klass = Il2cppUtils::GetClass("campus-submodule.Runtime.dll",
                                                                        "Campus.Common", "CampusActorController");
        for (const auto& i : CampusActorController_klass->methods) {
            Log::DebugFmt("CampusActorController.%s at %p", i->name.c_str(), i->function);
        }*/

        ADD_HOOK(CampusActorAnimation_Setup,
                 Il2cppUtils::GetMethodPointer("campus-submodule.Runtime.dll", "Campus.Common",
                                               "CampusActorAnimation", "Setup"));

        ADD_HOOK(CampusQualityManager_set_TargetFrameRate,
                 Il2cppUtils::GetMethodPointer("campus-submodule.Runtime.dll", "Campus.Common",
                                               "CampusQualityManager", "set_TargetFrameRate"));

        ADD_HOOK(Internal_LogException, Il2cppUtils::il2cpp_resolve_icall(
                "UnityEngine.DebugLogHandler::Internal_LogException(System.Exception,UnityEngine.Object)"));
        ADD_HOOK(Internal_Log, Il2cppUtils::il2cpp_resolve_icall(
                "UnityEngine.DebugLogHandler::Internal_Log(UnityEngine.LogType,UnityEngine.LogOption,System.String,UnityEngine.Object)"));

        ADD_HOOK(InternalSetOrientationAsync,
                 Il2cppUtils::GetMethodPointer("campus-submodule.Runtime.dll", "Campus.Common",
                                               "ScreenOrientationControllerBase", "InternalSetOrientationAsync"));

        ADD_HOOK(Unity_set_position_Injected, Il2cppUtils::il2cpp_resolve_icall(
                "UnityEngine.Transform::set_position_Injected(UnityEngine.Vector3&)"));
        ADD_HOOK(Unity_set_rotation_Injected, Il2cppUtils::il2cpp_resolve_icall(
                "UnityEngine.Transform::set_rotation_Injected(UnityEngine.Quaternion&)"));
        ADD_HOOK(Unity_get_fieldOfView, Il2cppUtils::GetMethodPointer("UnityEngine.CoreModule.dll", "UnityEngine",
                                                                      "Camera", "get_fieldOfView"));
        ADD_HOOK(Unity_set_fieldOfView, Il2cppUtils::GetMethodPointer("UnityEngine.CoreModule.dll", "UnityEngine",
                                                                      "Camera", "set_fieldOfView"));
        ADD_HOOK(Unity_set_targetFrameRate, Il2cppUtils::il2cpp_resolve_icall(
                "UnityEngine.Application::set_targetFrameRate(System.Int32)"));
        ADD_HOOK(EndCameraRendering, Il2cppUtils::GetMethodPointer("UnityEngine.CoreModule.dll", "UnityEngine.Rendering",
                                                                     "RenderPipeline", "EndCameraRendering"));
    }
    // 77 2640 5000

    DEFINE_HOOK(int, il2cpp_init, (const char* domain_name)) {
        const auto ret = il2cpp_init_Orig(domain_name);
        // InjectFunctions();

        Log::Info("Waiting for config...");

        while (!Config::isConfigInit) {
            std::this_thread::sleep_for(std::chrono::milliseconds(100));
        }
        if (!Config::enabled) {
            Log::Info("Plugin not enabled");
            return ret;
        }

        Log::Info("Start init plugin...");

        StartInjectFunctions();
        GKCamera::initCameraSettings();
        Local::LoadData();

        Log::Info("Plugin init finished.");
        return ret;
    }
}


namespace GakumasLocal::Hook {
    void Install() {
        const auto hookInstaller = Plugin::GetInstance().GetHookInstaller();

        Log::Info("Installing hook");

        ADD_HOOK(HookMain::il2cpp_init,
                  Plugin::GetInstance().GetHookInstaller()->LookupSymbol("il2cpp_init"));

        Log::Info("Hook installed");
    }
}
