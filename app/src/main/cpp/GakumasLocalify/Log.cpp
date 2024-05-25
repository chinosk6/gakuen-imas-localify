#include "Log.h"
#include <android/log.h>
#include <sstream>
#include <string>

std::string format(const char* fmt, ...) {
    va_list args;
    va_start(args, fmt);
    va_list args_copy;
    va_copy(args_copy, args);

    // 计算格式化后的字符串长度
    int size = vsnprintf(nullptr, 0, fmt, args_copy) + 1; // 加上额外的终止符空间
    va_end(args_copy);

    // 动态分配缓冲区
    char* buffer = new char[size];

    // 格式化字符串
    vsnprintf(buffer, size, fmt, args);

    va_end(args);

    std::string result(buffer);
    delete[] buffer; // 释放缓冲区
    return result;
}


namespace GakumasLocal::Log {
    void Log(int prio, const char* msg) {
        __android_log_write(prio, "GakumasLocal-Native", msg);
    }

    void LogFmt(int prio, const char* fmt, ...) {
        va_list args;
        va_start(args, fmt);
        va_list args_copy;
        va_copy(args_copy, args);

        // 计算格式化后的字符串长度
        int size = vsnprintf(nullptr, 0, fmt, args_copy) + 1; // 加上额外的终止符空间
        va_end(args_copy);

        // 动态分配缓冲区
        char* buffer = new char[size];

        // 格式化字符串
        vsnprintf(buffer, size, fmt, args);

        va_end(args);

        std::string result(buffer);
        delete[] buffer; // 释放缓冲区

        Log(prio, result.c_str());
    }

    void Info(const char* msg) {
        Log(ANDROID_LOG_INFO, msg);
    }

    void InfoFmt(const char* fmt, ...) {
        va_list args;
        va_start(args, fmt);
        va_list args_copy;
        va_copy(args_copy, args);

        // 计算格式化后的字符串长度
        int size = vsnprintf(nullptr, 0, fmt, args_copy) + 1; // 加上额外的终止符空间
        va_end(args_copy);

        // 动态分配缓冲区
        char* buffer = new char[size];

        // 格式化字符串
        vsnprintf(buffer, size, fmt, args);

        va_end(args);

        std::string result(buffer);
        delete[] buffer; // 释放缓冲区

        Info(result.c_str());
    }

    void Error(const char* msg) {
        Log(ANDROID_LOG_ERROR, msg);
    }

    void ErrorFmt(const char* fmt, ...) {
        va_list args;
        va_start(args, fmt);
        va_list args_copy;
        va_copy(args_copy, args);

        // 计算格式化后的字符串长度
        int size = vsnprintf(nullptr, 0, fmt, args_copy) + 1; // 加上额外的终止符空间
        va_end(args_copy);

        // 动态分配缓冲区
        char* buffer = new char[size];

        // 格式化字符串
        vsnprintf(buffer, size, fmt, args);

        va_end(args);

        std::string result(buffer);
        delete[] buffer; // 释放缓冲区

        Error(result.c_str());
    }

    void Debug(const char* msg) {
        Log(ANDROID_LOG_DEBUG, msg);
    }

    void DebugFmt(const char* fmt, ...) {
        va_list args;
        va_start(args, fmt);
        va_list args_copy;
        va_copy(args_copy, args);

        // 计算格式化后的字符串长度
        int size = vsnprintf(nullptr, 0, fmt, args_copy) + 1; // 加上额外的终止符空间
        va_end(args_copy);

        // 动态分配缓冲区
        char* buffer = new char[size];

        // 格式化字符串
        vsnprintf(buffer, size, fmt, args);

        va_end(args);

        std::string result(buffer);
        delete[] buffer; // 释放缓冲区

        Debug(result.c_str());
    }

    void LogUnityLog(int prio, const char* fmt, ...) {
        va_list args;
        va_start(args, fmt);
        va_list args_copy;
        va_copy(args_copy, args);

        // 计算格式化后的字符串长度
        int size = vsnprintf(nullptr, 0, fmt, args_copy) + 1; // 加上额外的终止符空间
        va_end(args_copy);

        // 动态分配缓冲区
        char* buffer = new char[size];

        // 格式化字符串
        vsnprintf(buffer, size, fmt, args);

        va_end(args);

        std::string result(buffer);
        delete[] buffer; // 释放缓冲区

        __android_log_write(prio, "GakumasLog", result.c_str());
    }
}
