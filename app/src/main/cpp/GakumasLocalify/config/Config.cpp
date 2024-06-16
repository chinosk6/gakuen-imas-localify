#include <string>
#include "nlohmann/json.hpp"
#include "../Log.h"

namespace GakumasLocal::Config {
    bool isConfigInit = false;

    bool dbgMode = false;
    bool enabled = true;
    bool replaceFont = true;
    bool forceExportResource = true;
    bool textTest = false;
    int gameOrientation = 0;
    bool dumpText = false;
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

    bool enableBreastParam = false;
    float bUseLimit = 1.0f;
    float bDamping = 0.33f;
    float bStiffness = 0.08f;
    float bSpring = 1.0f;
    float bPendulum = 0.055f;
    float bPendulumRange = 0.15f;
    float bAverage = 0.20f;
    float bRootWeight = 0.5f;
    bool bUseArmCorrection = true;
    bool bUseScale = false;
    float bScale = 1.0f;

    void LoadConfig(const std::string& configStr) {
        try {
            const auto config = nlohmann::json::parse(configStr);

            #define GetConfigItem(name) if (config.contains(#name)) name = config[#name]

            GetConfigItem(dbgMode);
            GetConfigItem(enabled);
            GetConfigItem(replaceFont);
            GetConfigItem(forceExportResource);
            GetConfigItem(gameOrientation);
            GetConfigItem(textTest);
            GetConfigItem(dumpText);
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
            GetConfigItem(enableBreastParam);
            GetConfigItem(bUseLimit);
            GetConfigItem(bDamping);
            GetConfigItem(bStiffness);
            GetConfigItem(bSpring);
            GetConfigItem(bPendulum);
            GetConfigItem(bPendulumRange);
            GetConfigItem(bAverage);
            GetConfigItem(bRootWeight);
            GetConfigItem(bUseArmCorrection);
            GetConfigItem(bUseScale);
            GetConfigItem(bScale);

        }
        catch (std::exception& e) {
            Log::ErrorFmt("LoadConfig error: %s", e.what());
        }
        isConfigInit = true;
    }
}
