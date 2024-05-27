#include <string>
#include "nlohmann/json.hpp"
#include "../Log.h"

namespace GakumasLocal::Config {
    bool isConfigInit = false;

    bool enabled = true;
    bool textTest = false;
    bool enableFreeCamera = false;
    int targetFrameRate = 0;
    bool unlockAllLive = false;

    bool enableLiveCustomeDress = false;
    std::string liveCustomeHeadId = "";
    std::string liveCustomeCostumeId = "";

    bool useCustomeGraphicSettings = false;
    float renderScale = 0.77f;
    int qualitySettingsLevel = 3;
    int volumeIndex = 3;
    int maxBufferPixel = 3384;
    int reflectionQualityLevel = 4;
    int lodQualityLevel = 4;

    void LoadConfig(const std::string& configStr) {
        try {
            const auto config = nlohmann::json::parse(configStr);

            #define GetConfigItem(name) if (config.contains(#name)) name = config[#name]

            GetConfigItem(enabled);
            GetConfigItem(textTest);
            GetConfigItem(targetFrameRate);
            GetConfigItem(enableFreeCamera);
            GetConfigItem(unlockAllLive);
            GetConfigItem(enableLiveCustomeDress);
            GetConfigItem(liveCustomeHeadId);
            GetConfigItem(liveCustomeCostumeId);
            GetConfigItem(useCustomeGraphicSettings);
            GetConfigItem(renderScale);
            GetConfigItem(qualitySettingsLevel);
            GetConfigItem(volumeIndex);
            GetConfigItem(maxBufferPixel);
            GetConfigItem(reflectionQualityLevel);
            GetConfigItem(lodQualityLevel);

        }
        catch (std::exception& e) {
            Log::ErrorFmt("LoadConfig error: %s", e.what());
        }
        isConfigInit = true;
    }
}
