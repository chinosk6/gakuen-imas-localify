#ifndef GAKUMAS_LOCALIFY_MISC_H
#define GAKUMAS_LOCALIFY_MISC_H

#include <string>
#include <string_view>
#include <jni.h>

namespace GakumasLocal {
    using OpaqueFunctionPointer = void (*)();

    namespace Misc {
        std::u16string ToUTF16(const std::string_view& str);
        std::string ToUTF8(const std::u16string_view& str);
        JNIEnv* GetJNIEnv();

        class CSEnum {
        public:
            CSEnum(const std::string& name, const int value);

            CSEnum(const std::vector<std::string>& names, const std::vector<int>& values);

            int GetIndex();

            int GetTotalLength();

            void Add(const std::string& name, const int value);

            std::pair<std::string, int> GetCurrent();

            std::pair<std::string, int> Last();

            std::pair<std::string, int> Next();

            int GetValueByName(const std::string& name);

        private:
            int currIndex = 0;
            std::vector<std::string> names{};
            std::vector<int> values{};

        };
    }
}

#endif //GAKUMAS_LOCALIFY_MISC_H
