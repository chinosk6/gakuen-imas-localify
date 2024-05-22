#include <string>
#include "nlohmann/json.hpp"
#include "../Log.h"

namespace GakumasLocal::Config {
    bool isConfigInit = false;

    bool enabled = true;
    bool enableFreeCamera = false;

    void LoadConfig(const std::string& configStr) {
        try {
            const auto config = nlohmann::json::parse(configStr);

            enabled = config["enabled"];
            enableFreeCamera = config["enableFreeCamera"];

        }
        catch (std::exception& e) {
            Log::ErrorFmt("LoadConfig error: %s", e.what());
        }
        isConfigInit = true;
    }
}
