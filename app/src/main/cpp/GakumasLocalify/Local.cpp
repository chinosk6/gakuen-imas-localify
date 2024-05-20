#include "Local.h"
#include "Log.h"
#include "Plugin.h"
#include <filesystem>
#include <iostream>
#include <fstream>
#include <unordered_map>
#include <unordered_set>
#include <nlohmann/json.hpp>
#include <thread>


namespace GakumasLocal::Local {
    std::unordered_map<std::string, std::string> i18nData{};
    std::unordered_map<std::string, std::string> i18nDumpData{};
    std::unordered_map<std::string, std::string> genericText{};
    std::unordered_map<std::string, std::string> genericTextDumpData{};
    std::unordered_set<std::string> translatedText{};

    std::filesystem::path GetBasePath() {
        return Plugin::GetInstance().GetHookInstaller()->localizationFilesDir;
    }

    void LoadJsonDataToMap(const std::filesystem::path& filePath, std::unordered_map<std::string, std::string>& dict,
                           const bool insertToTranslated = false) {
        if (!exists(filePath)) return;
        try {
            dict.clear();
            std::ifstream file(filePath);
            if (!file.is_open()) {
                Log::ErrorFmt("Load %s failed.\n", filePath.c_str());
                return;
            }
            std::string fileContent((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
            file.close();
            auto fileData = nlohmann::json::parse(fileContent);
            for (auto& i : fileData.items()) {
                const auto& key = i.key();
                const std::string value = i.value();
                if (insertToTranslated) translatedText.emplace(value);
                dict[key] = value;
            }
        }
        catch (std::exception& e) {
            Log::ErrorFmt("Load %s failed: %s\n", filePath.c_str(), e.what());
        }
    }

    void DumpMapDataToJson(const std::filesystem::path& dumpBasePath, const std::filesystem::path& fileName,
                           const std::unordered_map<std::string, std::string>& dict) {
        const auto dumpFilePath = dumpBasePath / fileName;
        try {
            if (!is_directory(dumpBasePath)) {
                std::filesystem::create_directories(dumpBasePath);
            }
            if (!std::filesystem::exists(dumpFilePath)) {
                std::ofstream dumpWriteLrcFile(dumpFilePath, std::ofstream::out);
                dumpWriteLrcFile << "{}";
                dumpWriteLrcFile.close();
            }

            std::ifstream dumpLrcFile(dumpFilePath);
            std::string fileContent((std::istreambuf_iterator<char>(dumpLrcFile)), std::istreambuf_iterator<char>());
            dumpLrcFile.close();
            auto fileData = nlohmann::ordered_json::parse(fileContent);
            for (const auto& i : dict) {
                fileData[i.first] = i.second;
            }
            const auto newStr = fileData.dump(4, 32, false);
            std::ofstream dumpWriteLrcFile(dumpFilePath, std::ofstream::out);
            dumpWriteLrcFile << newStr.c_str();
            dumpWriteLrcFile.close();
        }
        catch (std::exception& e) {
            Log::ErrorFmt("DumpMapDataToJson %s failed: %s", dumpFilePath.c_str(), e.what());
        }
    }

    void LoadData() {
        static auto localizationFile = GetBasePath() / "local-files" / "localization.json";
        static auto genericFile = GetBasePath() / "local-files" / "generic.json";

        if (!exists(localizationFile)) return;
        LoadJsonDataToMap(localizationFile, i18nData, true);
        Log::InfoFmt("%ld localization items loaded.", i18nData.size());

        LoadJsonDataToMap(genericFile, genericText, true);
        Log::InfoFmt("%ld generic text items loaded.", genericText.size());

        static auto dumpBasePath = GetBasePath() / "dump-files";
        static auto dumpFilePath = dumpBasePath / "localization.json";
        LoadJsonDataToMap(dumpFilePath, i18nDumpData);
    }

    bool GetI18n(const std::string& key, std::string* ret) {
        if (const auto iter = i18nData.find(key); iter != i18nData.end()) {
            *ret = iter->second;
            return true;
        }
        return false;
    }

    bool inDump = false;
    void DumpI18nItem(const std::string& key, const std::string& value) {
        if (i18nDumpData.contains(key)) return;
        i18nDumpData[key] = value;
        Log::DebugFmt("DumpI18nItem: %s - %s", key.c_str(), value.c_str());

        static auto dumpBasePath = GetBasePath() / "dump-files";

        if (inDump) return;
        inDump = true;
        std::thread([](){
            std::this_thread::sleep_for(std::chrono::seconds(5));
            DumpMapDataToJson(dumpBasePath, "localization.json", i18nDumpData);
            inDump = false;
        }).detach();
    }

    std::string readFileToString(const std::string& filename) {
        std::ifstream file(filename);
        if (!file.is_open()) {
            throw std::exception();
        }
        std::string content((std::istreambuf_iterator<char>(file)),
                            (std::istreambuf_iterator<char>()));
        file.close();
        return content;
    }

    bool GetResourceText(const std::string& name, std::string* ret) {
        static std::filesystem::path basePath = GetBasePath();

        try {
            const auto targetFilePath = basePath / "local-files" / "resource" / name;
            // Log::DebugFmt("GetResourceText: %s", targetFilePath.c_str());
            if (exists(targetFilePath)) {
                auto readStr = readFileToString(targetFilePath);
                *ret = readStr;
                return true;
            }
        }
        catch (std::exception& e) {
            Log::ErrorFmt("read file: %s failed.", name.c_str());
        }
        return false;
    }

    bool inDumpGeneric = false;
    bool GetGenericText(const std::string& origText, std::string* newStr) {
        if (const auto iter = genericText.find(origText); iter != genericText.end()) {
            *newStr = iter->second;
            return true;
        }

        if (translatedText.contains(origText)) return false;

        genericTextDumpData.emplace(origText, origText);
        static auto dumpBasePath = GetBasePath() / "dump-files";

        if (inDumpGeneric) return false;
        inDumpGeneric = true;
        std::thread([](){
            std::this_thread::sleep_for(std::chrono::seconds(5));
            DumpMapDataToJson(dumpBasePath, "generic.json", genericTextDumpData);
            genericTextDumpData.clear();
            inDumpGeneric = false;
        }).detach();

        return false;

    }

}
