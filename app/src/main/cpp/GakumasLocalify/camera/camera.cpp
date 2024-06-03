#include "baseCamera.hpp"
#include "camera.hpp"
#include <thread>

#define KEY_W  51
#define KEY_S  47
#define KEY_A  29
#define KEY_D  32
#define KEY_R  46
#define KEY_Q  45
#define KEY_E  33
#define KEY_F  34
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
    CameraMode cameraMode = CameraMode::FREE;
    FirstPersonRoll firstPersonRoll = FirstPersonRoll::ENABLE_ROLL;

    UnityResolve::UnityType::Vector3 firstPersonPosOffset{0, 0.064f, 0.000f};
    UnityResolve::UnityType::Vector3 followPosOffset{0, 0, 1.5};
    float offsetMoveStep = 0.008;
    int followCharaIndex = 0;
    GakumasLocal::Misc::CSEnum bodyPartsEnum("Head", 0xa);

	bool rMousePressFlg = false;

    void SetCameraMode(CameraMode mode) {
        cameraMode = mode;
    }

    CameraMode GetCameraMode() {
        return cameraMode;
    }

    void SetFirstPersonRoll(FirstPersonRoll mode) {
        firstPersonRoll = mode;
    }

    FirstPersonRoll GetFirstPersonRoll() {
        return firstPersonRoll;
    }


    void reset_camera() {
        followCharaIndex = 0;
        firstPersonPosOffset = {0, 0.064f, 0.000f};  // f3: 0.008f
        followPosOffset = {0, 0, 1.5};
		baseCamera.reset();
	}

	void camera_forward() {  // 向前
        switch (cameraMode) {
            case CameraMode::FREE: {
                baseCamera.set_lon_move(0, LonMoveHState::LonMoveForward);
            } break;
            case CameraMode::FIRST_PERSON: {
                firstPersonPosOffset.z += offsetMoveStep;
            } break;
            case CameraMode::FOLLOW: {
                followPosOffset.z -= offsetMoveStep;
            }
        }

	}
	void camera_back() {  // 后退
        switch (cameraMode) {
            case CameraMode::FREE: {
                baseCamera.set_lon_move(180, LonMoveHState::LonMoveBack);
            } break;
            case CameraMode::FIRST_PERSON: {
                firstPersonPosOffset.z -= offsetMoveStep;
            } break;
            case CameraMode::FOLLOW: {
                followPosOffset.z += offsetMoveStep;
            }
        }
	}
	void camera_left() {  // 向左
        switch (cameraMode) {
            case CameraMode::FREE: {
                baseCamera.set_lon_move(90);
            } break;
            case CameraMode::FOLLOW: {
                followPosOffset.x += 0.8;
            }
            default:
                break;
        }

	}
	void camera_right() {  // 向右
        switch (cameraMode) {
            case CameraMode::FREE: {
                baseCamera.set_lon_move(-90);
            } break;
            case CameraMode::FOLLOW: {
                followPosOffset.x -= 0.8;
            }
            default:
                break;
        }
	}

	void camera_down() {  // 向下
        switch (cameraMode) {
            case CameraMode::FREE: {
                float preStep = BaseCamera::moveStep / BaseCamera::smoothLevel;

                for (int i = 0; i < BaseCamera::smoothLevel; i++) {
                    baseCamera.pos.y -= preStep;
                    baseCamera.lookAt.y -= preStep;
                    std::this_thread::sleep_for(std::chrono::milliseconds(BaseCamera::sleepTime));
                }
            } break;
            case CameraMode::FIRST_PERSON: {
                firstPersonPosOffset.y -= offsetMoveStep;
            } break;
            case CameraMode::FOLLOW: {
                followPosOffset.y -= offsetMoveStep;
            }
        }
	}

	void camera_up() {  // 向上
        switch (cameraMode) {
            case CameraMode::FREE: {
                float preStep = BaseCamera::moveStep / BaseCamera::smoothLevel;

                for (int i = 0; i < BaseCamera::smoothLevel; i++) {
                    baseCamera.pos.y += preStep;
                    baseCamera.lookAt.y += preStep;
                    std::this_thread::sleep_for(std::chrono::milliseconds(BaseCamera::sleepTime));
                }
            } break;
            case CameraMode::FIRST_PERSON: {
                firstPersonPosOffset.y += offsetMoveStep;
            } break;
            case CameraMode::FOLLOW: {
                followPosOffset.y += offsetMoveStep;
            }
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

    void SwitchCameraMode() {
        switch (cameraMode) {
            case CameraMode::FREE: {
                cameraMode = CameraMode::FOLLOW;
                GakumasLocal::Log::Info("CameraMode: FOLLOW");
            } break;
            case CameraMode::FOLLOW: {
                cameraMode = CameraMode::FIRST_PERSON;
                firstPersonRoll = FirstPersonRoll::ENABLE_ROLL;
                GakumasLocal::Log::Info("CameraMode: FIRST_PERSON");
            } break;
            case CameraMode::FIRST_PERSON: {
                if (firstPersonRoll == FirstPersonRoll::ENABLE_ROLL) {
                    firstPersonRoll = FirstPersonRoll::DISABLE_ROLL;
                }
                else {
                    cameraMode = CameraMode::FREE;
                    GakumasLocal::Log::Info("CameraMode: FREE");
                }
            } break;
        }
    }

    void OnLeftDown() {
        if (cameraMode == CameraMode::FREE) return;
        if (followCharaIndex >= 1) {
            followCharaIndex--;
        }
    }

    void OnRightDown() {
        if (cameraMode == CameraMode::FREE) return;
        followCharaIndex++;
    }

    void OnUpDown() {
        if (cameraMode == CameraMode::FOLLOW) {
            const auto currPart = bodyPartsEnum.Last();
            GakumasLocal::Log::InfoFmt("Look at: %s (0x%x)", currPart.first.c_str(), currPart.second);
        }
    }

    void OnDownDown() {
        if (cameraMode == CameraMode::FOLLOW) {
            const auto currPart = bodyPartsEnum.Next();
            GakumasLocal::Log::InfoFmt("Look at: %s (0x%x)", currPart.first.c_str(), currPart.second);
        }
    }

    UnityResolve::UnityType::Vector3 CalcPositionFromLookAt(const UnityResolve::UnityType::Vector3& target,
                                                            const UnityResolve::UnityType::Vector3& offset) {
        // offset: z 远近, y 高低, x角度
        float angleX = offset.x;
        float distanceZ = offset.z;
        float angleRad = angleX * (M_PI / 180.0f);
        float newX = target.x + distanceZ * std::sin(angleRad);
        float newZ = target.z + distanceZ * std::cos(angleRad);
        float newY = target.y + offset.y;
        return UnityResolve::UnityType::Vector3(newX, newY, newZ);
    }

    UnityResolve::UnityType::Vector3 CalcFirstPersonPosition(const UnityResolve::UnityType::Vector3& position,
                                                             const UnityResolve::UnityType::Vector3& forward,
                                                             const UnityResolve::UnityType::Vector3& offset) {
        using Vector3 = UnityResolve::UnityType::Vector3;

        // 计算角色的右方向
        Vector3 up(0, 1, 0); // Y轴方向
        Vector3 right = forward.cross(up).Normalize();
        Vector3 fwd = forward;
        Vector3 pos = position;

        // 计算角色的左方向
        Vector3 left = right * -1.0f;

        // 计算最终位置
        Vector3 backwardOffset = fwd * -offset.z;
        Vector3 leftOffset = left * offset.x;

        Vector3 finalPosition = pos + backwardOffset + leftOffset;
        finalPosition.y += offset.y;

        return finalPosition;

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
			case KEY_UP: {
                if (message == WM_KEYDOWN) {
                    OnUpDown();
                }
                cameraMoveState.up = message == WM_KEYDOWN;
            } break;
			case KEY_DOWN: {
                if (message == WM_KEYDOWN) {
                    OnDownDown();
                }
                cameraMoveState.down = message == WM_KEYDOWN;
            } break;
			case KEY_LEFT: {
                if (message == WM_KEYDOWN) {
                    OnLeftDown();
                }
                cameraMoveState.left = message == WM_KEYDOWN;
            } break;
			case KEY_RIGHT: {
                if (message == WM_KEYDOWN) {
                    OnRightDown();
                }
                cameraMoveState.right = message == WM_KEYDOWN;
            } break;
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
			} break;
            case KEY_F: if (message == WM_KEYDOWN) SwitchCameraMode(); break;
			default: break;
			}
		}
	}

	void initCameraSettings() {
		reset_camera();
		cameraRawInputThread();
	}

}
