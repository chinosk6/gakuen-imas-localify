#include "Misc.hpp"

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

    CSEnum::CSEnum(const std::string& name, const int value) {
        this->Add(name, value);
    }

    CSEnum::CSEnum(const std::vector<std::string>& names, const std::vector<int>& values) {
        if (names.size() != values.size()) return;
        this->names = names;
        this->values = values;
    }

    int CSEnum::GetIndex() {
        return currIndex;
    }

    void CSEnum::SetIndex(int index) {
        if (index < 0) return;
        if (index + 1 >= values.size()) return;
        currIndex = index;
    }

    int CSEnum::GetTotalLength() {
        return values.size();
    }

    void CSEnum::Add(const std::string &name, const int value) {
        this->names.push_back(name);
        this->values.push_back(value);
    }

    std::pair<std::string, int> CSEnum::GetCurrent() {
        return std::make_pair(names[currIndex], values[currIndex]);
    }

    std::pair<std::string, int> CSEnum::Last() {
        const auto maxIndex = this->GetTotalLength() - 1;
        if (currIndex <= 0) {
            currIndex = maxIndex;
        }
        else {
            currIndex--;
        }
        return this->GetCurrent();
    }

    std::pair<std::string, int> CSEnum::Next() {
        const auto maxIndex = this->GetTotalLength() - 1;
        if (currIndex >= maxIndex) {
            currIndex = 0;
        }
        else {
            currIndex++;
        }
        return this->GetCurrent();
    }

    int CSEnum::GetValueByName(const std::string &name) {
        for (int i = 0; i < names.size(); i++) {
            if (names[i].compare(name) == 0) {
                return values[i];
            }
        }
        return values[0];
    }

}
