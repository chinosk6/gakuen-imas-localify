#include "GakumasLocalify/Plugin.h"
#include "GakumasLocalify/Log.h"

#include <jni.h>
#include <android/log.h>
#include "string"
#include "shadowhook.h"
#include "xdl.h"
#include "GakumasLocalify/camera/camera.hpp"
#include "GakumasLocalify/config/Config.hpp"

JavaVM* g_javaVM = nullptr;
jclass g_gakumasHookMainClass = nullptr;
jmethodID showToastMethodId = nullptr;

namespace
{
    class AndroidHookInstaller : public GakumasLocal::HookInstaller
    {
    public:
        explicit AndroidHookInstaller(const std::string& il2cppLibraryPath, const std::string& localizationFilesDir)
                : m_Il2CppLibrary(xdl_open(il2cppLibraryPath.c_str(), RTLD_LAZY))
        {
            this->m_il2cppLibraryPath = il2cppLibraryPath;
            this->localizationFilesDir = localizationFilesDir;
        }

        ~AndroidHookInstaller() override {
            xdl_close(m_Il2CppLibrary);
        }

        void* InstallHook(void* addr, void* hook, void** orig) override
        {
            return shadowhook_hook_func_addr(addr, hook, orig);
        }

        GakumasLocal::OpaqueFunctionPointer LookupSymbol(const char* name) override
        {
            return reinterpret_cast<GakumasLocal::OpaqueFunctionPointer>(xdl_sym(m_Il2CppLibrary, name, NULL));
        }

    private:
        void* m_Il2CppLibrary;
    };
}

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_javaVM = vm;
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_chinosk_gakumas_localify_GakumasHookMain_initHook(JNIEnv *env, jclass clazz, jstring targetLibraryPath,
                                                                 jstring localizationFilesDir) {
    g_gakumasHookMainClass = clazz;
    showToastMethodId = env->GetStaticMethodID(clazz, "showToast", "(Ljava/lang/String;)V");

    const auto targetLibraryPathChars = env->GetStringUTFChars(targetLibraryPath, nullptr);
    const std::string targetLibraryPathStr = targetLibraryPathChars;

    const auto localizationFilesDirChars = env->GetStringUTFChars(localizationFilesDir, nullptr);
    const std::string localizationFilesDirCharsStr = localizationFilesDirChars;

    auto& plugin = GakumasLocal::Plugin::GetInstance();
    plugin.InstallHook(std::make_unique<AndroidHookInstaller>(targetLibraryPathStr, localizationFilesDirCharsStr));
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_chinosk_gakumas_localify_GakumasHookMain_keyboardEvent(JNIEnv *env, jclass clazz, jint key_code, jint action) {
    GKCamera::on_cam_rawinput_keyboard(action, key_code);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_chinosk_gakumas_localify_GakumasHookMain_loadConfig(JNIEnv *env, jclass clazz,
                                                                   jstring config_json_str) {
    const auto configJsonStrChars = env->GetStringUTFChars(config_json_str, nullptr);
    const std::string configJson = configJsonStrChars;
    GakumasLocal::Config::LoadConfig(configJson);
}