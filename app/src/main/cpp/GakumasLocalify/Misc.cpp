#include "Misc.h"

#include <codecvt>
#include <locale>
#include <jni.h>


extern JavaVM* g_javaVM;


namespace GakumasLocal::Misc {
    std::u16string ToUTF16(const std::string_view& str) {
        std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> utf16conv;
        return utf16conv.from_bytes(str.data(), str.data() + str.size());
    }

    std::string ToUTF8(const std::u16string_view& str) {
        std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> utf16conv;
        return utf16conv.to_bytes(str.data(), str.data() + str.size());
    }

    JNIEnv* GetJNIEnv() {
        if (!g_javaVM) return nullptr;
        JNIEnv* env = nullptr;
        if (g_javaVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
            int status = g_javaVM->AttachCurrentThread(&env, nullptr);
            if (status < 0) {
                return nullptr;
            }
        }
        return env;
    }

} // namespace UmaPyogin::Misc
