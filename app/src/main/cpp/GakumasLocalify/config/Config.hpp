#pragma once

namespace GakumasLocal::Config {
    extern bool isConfigInit;

    extern bool dbgMode;
    extern bool enabled;
    extern bool replaceFont;
    extern bool forceExportResource;
    extern int gameOrientation;
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

    extern bool enableBreastParam;
    extern float bUseLimit;
    extern float bDamping;
    extern float bStiffness;
    extern float bSpring;
    extern float bPendulum;
    extern float bPendulumRange;
    extern float bAverage;
    extern float bRootWeight;
    extern bool bUseArmCorrection;
    extern bool bUseScale;
    extern float bScale;

    void LoadConfig(const std::string& configStr);
}
