#pragma once
#include "baseCamera.hpp"

namespace GKCamera {
	extern BaseCamera::Camera baseCamera;

    void on_cam_rawinput_keyboard(int message, int key);
	void initCameraSettings();
}
