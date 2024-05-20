#include "Misc.h"

#include <codecvt>
#include <locale>


namespace GakumasLocal::Misc {
    std::u16string ToUTF16(const std::string_view& str) {
        std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> utf16conv;
        return utf16conv.from_bytes(str.data(), str.data() + str.size());
    }

    std::string ToUTF8(const std::u16string_view& str) {
        std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> utf16conv;
        return utf16conv.to_bytes(str.data(), str.data() + str.size());
    }
} // namespace UmaPyogin::Misc
