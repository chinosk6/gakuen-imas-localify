#pragma once

namespace GakumasLocal::Config {
    extern bool isConfigInit;

    extern bool dbgMode;
    extern bool enabled;
    extern bool forceExportResource;
    extern bool textTest;
    extern bool dumpText;
    extern bool enableFreeCamera;
    extern int targetFrameRate;
    extern bool unlockAllLive;

    extern bool enableLiveCustomeDress;
    extern std::string liveCustomeHeadId;
    extern std::string liveCustomeCostumeId;

    extern bool useCustomeGraphicSettings;
    extern float renderScale;
    extern int qualitySettingsLevel;
    extern int volumeIndex;
    extern int maxBufferPixel;

    extern int reflectionQualityLevel;
    extern int lodQualityLevel;

    void LoadConfig(const std::string& configStr);
}
