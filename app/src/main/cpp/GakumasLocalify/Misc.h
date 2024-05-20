#ifndef GAKUMAS_LOCALIFY_MISC_H
#define GAKUMAS_LOCALIFY_MISC_H

#include <string>
#include <string_view>

namespace GakumasLocal {
    using OpaqueFunctionPointer = void (*)();

    namespace Misc {
        std::u16string ToUTF16(const std::string_view& str);
        std::string ToUTF8(const std::u16string_view& str);
    }
}

#endif //GAKUMAS_LOCALIFY_MISC_H
