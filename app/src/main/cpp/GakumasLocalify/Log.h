#ifndef GAKUMAS_LOCALIFY_LOG_H
#define GAKUMAS_LOCALIFY_LOG_H

namespace GakumasLocal::Log {
    void LogUnityLog(int prio, const char* fmt, ...);
    void LogFmt(int prio, const char* fmt, ...);
    void Info(const char* msg);
    void InfoFmt(const char* fmt, ...);
    void Error(const char* msg);
    void ErrorFmt(const char* fmt, ...);
    void Debug(const char* msg);
    void DebugFmt(const char* fmt, ...);
}

#endif //GAKUMAS_LOCALIFY_LOG_H
