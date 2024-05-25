#ifndef GAKUMAS_LOCALIFY_CONFIG_HPP
#define GAKUMAS_LOCALIFY_CONFIG_HPP


namespace GakumasLocal::Config {
    extern bool isConfigInit;

    extern bool enabled;
    extern bool enableFreeCamera;
    extern int targetFrameRate;
    extern bool unlockAllLive;

    extern bool enableLiveCustomeDress;
    extern std::string liveCustomeHeadId;
    extern std::string liveCustomeCostumeId;

    void LoadConfig(const std::string& configStr);
}


#endif //GAKUMAS_LOCALIFY_CONFIG_HPP
