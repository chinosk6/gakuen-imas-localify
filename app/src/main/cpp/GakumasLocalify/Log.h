#ifndef GAKUMAS_LOCALIFY_LOG_H
#define GAKUMAS_LOCALIFY_LOG_H

#include <string>
#include <jni.h>

namespace GakumasLocal::Log {
    std::string StringFormat(const char* fmt, ...);
    void LogUnityLog(int prio, const char* fmt, ...);
    void LogFmt(int prio, const char* fmt, ...);
    void Info(const char* msg);
    void InfoFmt(const char* fmt, ...);
    void Error(const char* msg);
    void ErrorFmt(const char* fmt, ...);
    void Debug(const char* msg);
    void DebugFmt(const char* fmt, ...);

    void ShowToast(const char* text);
    void ShowToastFmt(const char* fmt, ...);

    void ToastLoop(JNIEnv *env, jclass clazz);
}

#endif //GAKUMAS_LOCALIFY_LOG_H
