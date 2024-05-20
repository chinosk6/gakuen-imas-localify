#ifndef GAKUMAS_LOCALIFY_LOCAL_H
#define GAKUMAS_LOCALIFY_LOCAL_H

#include <string>
#include <filesystem>

namespace GakumasLocal::Local {
    std::filesystem::path GetBasePath();
    void LoadData();
    bool GetI18n(const std::string& key, std::string* ret);
    void DumpI18nItem(const std::string& key, const std::string& value);

    bool GetResourceText(const std::string& name, std::string* ret);
    bool GetGenericText(const std::string& origText, std::string* newStr);
}

#endif //GAKUMAS_LOCALIFY_LOCAL_H
