#ifndef GAKUMAS_LOCALIFY_CONFIG_HPP
#define GAKUMAS_LOCALIFY_CONFIG_HPP


namespace GakumasLocal::Config {
    extern bool isConfigInit;

    extern bool enabled;
    extern bool enableFreeCamera;

    void LoadConfig(const std::string& configStr);
}


#endif //GAKUMAS_LOCALIFY_CONFIG_HPP
