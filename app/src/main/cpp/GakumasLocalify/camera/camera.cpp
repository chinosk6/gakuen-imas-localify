#include "baseCamera.hpp"
#include <thread>

#define KEY_W  51
#define KEY_S  47
#define KEY_A  29
#define KEY_D  32
#define KEY_R  46
#define KEY_Q  45
#define KEY_E  33
#define KEY_I  37
#define KEY_K  39
#define KEY_J  38
#define KEY_L  40
#define KEY_R  46
#define KEY_UP  19
#define KEY_DOWN  20
#define KEY_LEFT  21
#define KEY_RIGHT  22
#define KEY_CTRL  113
#define KEY_SHIFT  59
#define KEY_ALT  57
#define KEY_SPACE  62

#define WM_KEYDOWN 0
#define WM_KEYUP 1


namespace GKCamera {
	BaseCamera::Camera baseCamera{};

	bool rMousePressFlg = false;

	void reset_camera() {
		baseCamera.reset();
	}

	void camera_forward() {  // 向前
		baseCamera.set_lon_move(0, LonMoveHState::LonMoveForward);
	}
	void camera_back() {  // 后退
		baseCamera.set_lon_move(180, LonMoveHState::LonMoveBack);
	}
	void camera_left() {  // 向左
		baseCamera.set_lon_move(90);
	}
	void camera_right() {  // 向右
		baseCamera.set_lon_move(-90);
	}
	void camera_down() {  // 向下
		float preStep = BaseCamera::moveStep / BaseCamera::smoothLevel;

		for (int i = 0; i < BaseCamera::smoothLevel; i++) {
			baseCamera.pos.y -= preStep;
			baseCamera.lookAt.y -= preStep;
            std::this_thread::sleep_for(std::chrono::milliseconds(BaseCamera::sleepTime));
		}
	}
	void camera_up() {  // 向上
		float preStep = BaseCamera::moveStep / BaseCamera::smoothLevel;

		for (int i = 0; i < BaseCamera::smoothLevel; i++) {
			baseCamera.pos.y += preStep;
			baseCamera.lookAt.y += preStep;
            std::this_thread::sleep_for(std::chrono::milliseconds(BaseCamera::sleepTime));
		}
	}
	void cameraLookat_up(float mAngel, bool mouse = false) {
		baseCamera.horizontalAngle += mAngel;
		if (baseCamera.horizontalAngle >= 90) baseCamera.horizontalAngle = 89.99;
		baseCamera.updateVertLook();
	}
	void cameraLookat_down(float mAngel, bool mouse = false) {
		baseCamera.horizontalAngle -= mAngel;
		if (baseCamera.horizontalAngle <= -90) baseCamera.horizontalAngle = -89.99;
		baseCamera.updateVertLook();
	}
	void cameraLookat_left(float mAngel) {
		baseCamera.verticalAngle += mAngel;
		if (baseCamera.verticalAngle >= 360) baseCamera.verticalAngle = -360;
		baseCamera.setHoriLook(baseCamera.verticalAngle);
	}
	void cameraLookat_right(float mAngel) {
		baseCamera.verticalAngle -= mAngel;
		if (baseCamera.verticalAngle <= -360) baseCamera.verticalAngle = 360;
		baseCamera.setHoriLook(baseCamera.verticalAngle);
	}
	void changeCameraFOV(float value) {
		baseCamera.fov += value;
	}

	struct CameraMoveState {
		bool w = false;
		bool s = false;
		bool a = false;
		bool d = false;
		bool ctrl = false;
		bool space = false;
		bool up = false;
		bool down = false;
		bool left = false;
		bool right = false;
		bool q = false;
		bool e = false;
		bool i = false;
		bool k = false;
		bool j = false;
		bool l = false;
		bool threadRunning = false;

		void resetAll() {
			auto p = reinterpret_cast<bool*>(this);
			const auto numMembers = sizeof(*this) / sizeof(bool);
			for (size_t idx = 0; idx < numMembers; ++idx) {
				p[idx] = false;
			}
		}
	} cameraMoveState;


	void cameraRawInputThread() {
		using namespace BaseCamera;

		std::thread([]() {
			if (cameraMoveState.threadRunning) return;
			cameraMoveState.threadRunning = true;
			while (true) {
				if (cameraMoveState.w) camera_forward();
				if (cameraMoveState.s) camera_back();
				if (cameraMoveState.a) camera_left();
				if (cameraMoveState.d) camera_right();
				if (cameraMoveState.ctrl) camera_down();
				if (cameraMoveState.space) camera_up();
				if (cameraMoveState.up) cameraLookat_up(moveAngel);
				if (cameraMoveState.down) cameraLookat_down(moveAngel);
				if (cameraMoveState.left) cameraLookat_left(moveAngel);
				if (cameraMoveState.right) cameraLookat_right(moveAngel);
				if (cameraMoveState.q) changeCameraFOV(0.5f);
				if (cameraMoveState.e) changeCameraFOV(-0.5f);
				// if (cameraMoveState.i) changeLiveFollowCameraOffsetY(moveStep / 3);
				// if (cameraMoveState.k) changeLiveFollowCameraOffsetY(-moveStep / 3);
				// if (cameraMoveState.j) changeLiveFollowCameraOffsetX(moveStep * 10);
				// if (cameraMoveState.l) changeLiveFollowCameraOffsetX(-moveStep * 10);
                std::this_thread::sleep_for(std::chrono::milliseconds(10));
			}
			}).detach();
	}

	void on_cam_rawinput_keyboard(int message, int key) {
		if (message == WM_KEYDOWN || message == WM_KEYUP) {
			switch (key) {
			case KEY_W:
				cameraMoveState.w = message == WM_KEYDOWN; break;
			case KEY_S:
				cameraMoveState.s = message == WM_KEYDOWN; break;
			case KEY_A:
				cameraMoveState.a = message == WM_KEYDOWN; break;
			case KEY_D:
				cameraMoveState.d = message == WM_KEYDOWN; break;
			case KEY_CTRL:
				cameraMoveState.ctrl = message == WM_KEYDOWN; break;
			case KEY_SPACE:
				cameraMoveState.space = message == WM_KEYDOWN; break;
			case KEY_UP:
				cameraMoveState.up = message == WM_KEYDOWN; break;
			case KEY_DOWN:
				cameraMoveState.down = message == WM_KEYDOWN; break;
			case KEY_LEFT:
				cameraMoveState.left = message == WM_KEYDOWN; break;
			case KEY_RIGHT:
				cameraMoveState.right = message == WM_KEYDOWN; break;
			case KEY_Q:
				cameraMoveState.q = message == WM_KEYDOWN; break;
			case KEY_E:
				cameraMoveState.e = message == WM_KEYDOWN; break;
			case KEY_I:
				cameraMoveState.i = message == WM_KEYDOWN; break;
			case KEY_K:
				cameraMoveState.k = message == WM_KEYDOWN; break;
			case KEY_J:
				cameraMoveState.j = message == WM_KEYDOWN; break;
			case KEY_L:
				cameraMoveState.l = message == WM_KEYDOWN; break;
			case KEY_R: {
				if (message == WM_KEYDOWN) reset_camera();
			}; break;
			default: break;
			}
		}
	}

	void initCameraSettings() {
		reset_camera();
		cameraRawInputThread();
	}

}
