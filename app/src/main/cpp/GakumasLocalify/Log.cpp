#include "Log.h"
#include <android/log.h>
#include <Misc.hpp>
#include <sstream>
#include <string>
#include <thread>

extern JavaVM* g_javaVM;
extern jclass g_gakumasHookMainClass;
extern jmethodID showToastMethodId;

#define GetParamStringResult(name)\
    va_list args;\
    va_start(args, fmt);\
    va_list args_copy;\
    va_copy(args_copy, args);\
    int size = vsnprintf(nullptr, 0, fmt, args_copy) + 1;\
    va_end(args_copy);\
    char* buffer = new char[size];\
    vsnprintf(buffer, size, fmt, args);\
    va_end(args);\
    std::string name(buffer);\
    delete[] buffer


namespace GakumasLocal::Log {
    void Log(int prio, const char* msg) {
        __android_log_write(prio, "GakumasLocal-Native", msg);
    }

    void LogFmt(int prio, const char* fmt, ...) {
        GetParamStringResult(result);
        Log(prio, result.c_str());
    }

    void Info(const char* msg) {
        Log(ANDROID_LOG_INFO, msg);
    }

    void InfoFmt(const char* fmt, ...) {
        GetParamStringResult(result);
        Info(result.c_str());
    }

    void Error(const char* msg) {
        Log(ANDROID_LOG_ERROR, msg);
    }

    void ErrorFmt(const char* fmt, ...) {
        GetParamStringResult(result);
        Error(result.c_str());
    }

    void Debug(const char* msg) {
        Log(ANDROID_LOG_DEBUG, msg);
    }

    void DebugFmt(const char* fmt, ...) {
        GetParamStringResult(result);
        Debug(result.c_str());
    }

    void LogUnityLog(int prio, const char* fmt, ...) {
        GetParamStringResult(result);
        __android_log_write(prio, "GakumasLog", result.c_str());
    }

    void ShowToast(const std::string& text) {
        DebugFmt("Toast: %s", text.c_str());

        std::thread([text](){
            auto env = Misc::GetJNIEnv();
            if (!env) {
                return;
            }
            g_javaVM->AttachCurrentThread(&env, nullptr);

            jclass& kotlinClass = g_gakumasHookMainClass;
            if (!kotlinClass) {
                g_javaVM->DetachCurrentThread();
                return;
            }
            jmethodID& methodId = showToastMethodId;
            if (!methodId) {
                g_javaVM->DetachCurrentThread();
                return;
            }
            jstring param = env->NewStringUTF(text.c_str());
            env->CallStaticVoidMethod(kotlinClass, methodId, param);

            g_javaVM->DetachCurrentThread();
        }).detach();
    }

    void ShowToastFmt(const char* fmt, ...) {
        GetParamStringResult(result);
        ShowToast(result);
    }
}
