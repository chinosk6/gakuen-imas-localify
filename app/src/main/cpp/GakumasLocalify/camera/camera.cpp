#include "baseCamera.hpp"
#include "camera.hpp"
#include <thread>
#include "Misc.hpp"
#include "../BaseDefine.h"


namespace GKCamera {
	BaseCamera::Camera baseCamera{};
    CameraMode cameraMode = CameraMode::FREE;
    FirstPersonRoll firstPersonRoll = FirstPersonRoll::ENABLE_ROLL;
    FollowModeY followModeY = FollowModeY::SMOOTH_Y;

    UnityResolve::UnityType::Vector3 firstPersonPosOffset{0, 0.064f, 0.000f};
    UnityResolve::UnityType::Vector3 followPosOffset{0, 0, 1.5};
    UnityResolve::UnityType::Vector2 followLookAtOffset{0, 0};
    float offsetMoveStep = 0.008;
    int followCharaIndex = 0;
    float l_sensitivity = 0.5f;
    float r_sensitivity = 0.5f;
    bool showToast = true;
    GakumasLocal::Misc::CSEnum bodyPartsEnum("Head", 0xa);

	// bool rMousePressFlg = false;

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
        followLookAtOffset = {0, 0};
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
	void camera_back(float multiplier = 1.0f) {  // 后退
        switch (cameraMode) {
            case CameraMode::FREE: {
                baseCamera.set_lon_move(180, LonMoveHState::LonMoveBack, multiplier);
            } break;
            case CameraMode::FIRST_PERSON: {
                firstPersonPosOffset.z -= offsetMoveStep * multiplier;
            } break;
            case CameraMode::FOLLOW: {
                followPosOffset.z += offsetMoveStep * multiplier;
            }
        }
	}
	void camera_left() {  // 向左
        switch (cameraMode) {
            case CameraMode::FREE: {
                baseCamera.set_lon_move(90);
            } break;
            case CameraMode::FOLLOW: {
                // followPosOffset.x += 0.8;
                followLookAtOffset.x += offsetMoveStep;
            }
            default:
                break;
        }

	}
	void camera_right(float multiplier = 1.0f) {  // 向右
        switch (cameraMode) {
            case CameraMode::FREE: {
                baseCamera.set_lon_move(-90, LonMoveLeftAndRight, multiplier);
            } break;
            case CameraMode::FOLLOW: {
                // followPosOffset.x -= 0.8;
                followLookAtOffset.x -= offsetMoveStep * multiplier;
            }
            default:
                break;
        }
	}

	void camera_down(float multiplier = 1.0f) {  // 向下
        switch (cameraMode) {
            case CameraMode::FREE: {
                float preStep = BaseCamera::moveStep / BaseCamera::smoothLevel * multiplier;

                for (int i = 0; i < BaseCamera::smoothLevel; i++) {
                    baseCamera.pos.y -= preStep;
                    baseCamera.lookAt.y -= preStep;
                    std::this_thread::sleep_for(std::chrono::milliseconds(BaseCamera::sleepTime));
                }
            } break;
            case CameraMode::FIRST_PERSON: {
                firstPersonPosOffset.y -= offsetMoveStep * multiplier;
            } break;
            case CameraMode::FOLLOW: {
                // followPosOffset.y -= offsetMoveStep;
                followLookAtOffset.y -= offsetMoveStep * multiplier;
            }
        }
	}

	void camera_up(float multiplier = 1.0f) {  // 向上
        switch (cameraMode) {
            case CameraMode::FREE: {
                float preStep = BaseCamera::moveStep / BaseCamera::smoothLevel * multiplier;

                for (int i = 0; i < BaseCamera::smoothLevel; i++) {
                    baseCamera.pos.y += preStep;
                    baseCamera.lookAt.y += preStep;
                    std::this_thread::sleep_for(std::chrono::milliseconds(BaseCamera::sleepTime));
                }
            } break;
            case CameraMode::FIRST_PERSON: {
                firstPersonPosOffset.y += offsetMoveStep * multiplier;
            } break;
            case CameraMode::FOLLOW: {
                // followPosOffset.y += offsetMoveStep;
                followLookAtOffset.y += offsetMoveStep * multiplier;
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
                GakumasLocal::Log::Info("CameraMode: FIRST_PERSON");
            } break;
            case CameraMode::FIRST_PERSON: {
                cameraMode = CameraMode::FREE;
                GakumasLocal::Log::Info("CameraMode: FREE");

            } break;
        }
    }

    void SwitchCameraSubMode() {
        switch (cameraMode) {
            case CameraMode::FIRST_PERSON: {
                if (firstPersonRoll == FirstPersonRoll::ENABLE_ROLL) {
                    firstPersonRoll = FirstPersonRoll::DISABLE_ROLL;
                    GakumasLocal::Log::Info("FirstPersonRoll: DISABLE_ROLL");
                }
                else {
                    firstPersonRoll = FirstPersonRoll::ENABLE_ROLL;
                    GakumasLocal::Log::Info("FirstPersonRoll: ENABLE_ROLL");
                }
            } break;

            case CameraMode::FOLLOW: {
                if (followModeY == FollowModeY::APPLY_Y) {
                    followModeY = FollowModeY::SMOOTH_Y;
                    GakumasLocal::Log::Info("FollowModeY: SMOOTH_Y");
                }
                else {
                    followModeY = FollowModeY::APPLY_Y;
                    GakumasLocal::Log::Info("FollowModeY: APPLY_Y");
                }
            } break;

            default: break;
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

    void ChangeLiveFollowCameraOffsetY(const float value) {
        if (cameraMode == CameraMode::FOLLOW) {
            followPosOffset.y += value;
        }
    }

    void ChangeLiveFollowCameraOffsetX(const float value) {
        if (cameraMode == CameraMode::FOLLOW) {
            followPosOffset.x += value;
        }
    }

    void ShowToast(const char *text) {
        if (showToast) {
            GakumasLocal::Log::ShowToast(text);
        }
    }

    void JLThumbRight(float value) {
        camera_right(value * l_sensitivity * baseCamera.fov / 60);
    }

    void JLThumbDown(float value) {
        camera_back(value * l_sensitivity * baseCamera.fov / 60);
    }

    void JRThumbRight(float value) {
        cameraLookat_right(value * r_sensitivity * baseCamera.fov / 60);
        ChangeLiveFollowCameraOffsetX(-1 * value * r_sensitivity * baseCamera.fov / 60);
    }

    void JRThumbDown(float value) {
        cameraLookat_down(value * r_sensitivity * baseCamera.fov / 60);
        ChangeLiveFollowCameraOffsetY(-0.1 * value * r_sensitivity * baseCamera.fov / 60);
    }

    void JDadUp(){
        reset_camera();
        ShowToast("重置镜头");
    }

    void JDadDown(){
        ShowToast("关闭通知，再次点击开启");
        showToast = !showToast;
    }

    void JDadLeft(){
        l_sensitivity = 1.0f;
        ShowToast("重置移动灵敏度");
    }

    void JDadRight(){
        r_sensitivity = 1.0f;
        ShowToast("重置镜头灵敏度");
    }

    void JAKeyDown() {
        if (cameraMode == CameraMode::FOLLOW) {
            const auto currPart = bodyPartsEnum.Next();
            if (showToast) {
                GakumasLocal::Log::ShowToastFmt("看向：%s (0x%x)", currPart.first.c_str(),
                                                currPart.second);
            }
        } else {
            r_sensitivity *= 0.8f;
        }
    }

    void JBKeyDown() {
        if (cameraMode == CameraMode::FOLLOW) {
            const auto currPart = bodyPartsEnum.Last();
            if (showToast) {
                GakumasLocal::Log::ShowToastFmt("看向：%s (0x%x)", currPart.first.c_str(),
                                                currPart.second);
            }
        } else {
            r_sensitivity *= 1.2f;
        }
    }

    void JXKeyDown() {
        if (cameraMode == CameraMode::FOLLOW) {
            OnLeftDown();
            if (showToast) {
                GakumasLocal::Log::ShowToastFmt("注视：%d", followCharaIndex);
            }
        } else {
            l_sensitivity *= 0.8f;
        }
    }

    void JYKeyDown() {
        if (cameraMode == CameraMode::FOLLOW) {
            OnRightDown();
            if (showToast) {
                GakumasLocal::Log::ShowToastFmt("注视：%d", followCharaIndex);
            }
        } else {
            l_sensitivity *= 1.2f;
        }
    }

    void JSelectKeyDown() {
        switch (cameraMode) {
            case CameraMode::FREE: {
                cameraMode = CameraMode::FOLLOW;
                ShowToast("跟随模式");
            } break;
            case CameraMode::FOLLOW: {
                cameraMode = CameraMode::FIRST_PERSON;
                ShowToast("第一人称模式");
            } break;
            case CameraMode::FIRST_PERSON: {
                cameraMode = CameraMode::FREE;
                ShowToast("无人机模式");

            } break;
        }
    }

    void JStartKeyDown() {
        switch (cameraMode) {
            case CameraMode::FIRST_PERSON: {
                if (firstPersonRoll == FirstPersonRoll::ENABLE_ROLL) {
                    firstPersonRoll = FirstPersonRoll::DISABLE_ROLL;
                    ShowToast("镜头水平固定");
                }
                else {
                    firstPersonRoll = FirstPersonRoll::ENABLE_ROLL;
                    ShowToast("镜头水平摇动");
                }
            } break;

            case CameraMode::FOLLOW: {
                if (followModeY == FollowModeY::APPLY_Y) {
                    followModeY = FollowModeY::SMOOTH_Y;
                    ShowToast("平滑升降");
                }
                else {
                    followModeY = FollowModeY::APPLY_Y;
                    ShowToast("即时升降");
                }
            } break;

            default: break;
        }
    }

    UnityResolve::UnityType::Vector3 CalcPositionFromLookAt(const UnityResolve::UnityType::Vector3& target,
                                                            const UnityResolve::UnityType::Vector3& offset) {
        // offset: z 远近, y 高低, x角度
        const float angleX = offset.x;
        const float distanceZ = offset.z;
        const float angleRad = angleX * (M_PI / 180.0f);
        const float newX = target.x + distanceZ * std::sin(angleRad);
        const float newZ = target.z + distanceZ * std::cos(angleRad);
        const float newY = target.y + offset.y;
        return UnityResolve::UnityType::Vector3(newX, newY, newZ);
    }

    float CheckNewY(const UnityResolve::UnityType::Vector3& targetPos, const bool recordY,
                    GakumasLocal::Misc::FixedSizeQueue<float>& recordsY) {
        const auto currentY = targetPos.y;
        static auto lastRetY = currentY;

        if (followModeY == FollowModeY::APPLY_Y) {
            lastRetY = currentY;
            return currentY;
        }

        const auto currentAvg = recordsY.Average();
        // GakumasLocal::Log::DebugFmt("currentY: %f, currentAvg: %f, diff: %f", currentY, currentAvg, abs(currentY - currentAvg));

        if (recordY) {
            recordsY.Push(currentY);
        }

        if (abs(currentY - currentAvg) < 0.02) {
            return lastRetY;
        }

        const auto retAvg = recordsY.Average();
        lastRetY = lastRetY + (retAvg - lastRetY) / 8;
        return lastRetY;
    }

    UnityResolve::UnityType::Vector3 CalcFollowModeLookAt(const UnityResolve::UnityType::Vector3& targetPos,
                                                          const UnityResolve::UnityType::Vector3& posOffset,
                                                          const bool recordY) {
        static GakumasLocal::Misc::FixedSizeQueue<float> recordsY(60);

        const float angleX = posOffset.x;
        const float angleRad = (angleX + (followPosOffset.z >= 0 ? 90.0f : -90.0f)) * (M_PI / 180.0f);

        UnityResolve::UnityType::Vector3 newTargetPos = targetPos;
        newTargetPos.y = CheckNewY(targetPos, recordY, recordsY);

        const float offsetX = followLookAtOffset.x * sin(angleRad);
        const float offsetZ = followLookAtOffset.x * cos(angleRad);

        newTargetPos.x += offsetX;
        newTargetPos.z += offsetZ;
        newTargetPos.y += followLookAtOffset.y;

        return newTargetPos;
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
        float thumb_l_right = 0.0f;
        float thumb_l_down = 0.0f;
        bool thumb_l_button = false;
        float thumb_r_right = 0.0f;
        float thumb_r_down = 0.0f;
        bool thumb_r_button = false;
        bool dpad_up = false;
        bool dpad_down = false;
        bool dpad_left = false;
        bool dpad_right = false;
        bool a_button = false;
        bool b_button = false;
        bool x_button = false;
        bool y_button = false;
        bool lb_button = false;
        float lt_button = 0.0f;
        bool rb_button = false;
        float rt_button = 0.0f;
        bool select_button = false;
        bool start_button = false;
        bool share_button = false;
        bool xbox_button = false;
		bool threadRunning = false;

		void resetAll() {
            // 获取当前对象的指针并转换为 unsigned char* 类型
            unsigned char* p = reinterpret_cast<unsigned char*>(this);

            // 遍历对象的每个字节
            for (size_t offset = 0; offset < sizeof(*this); ) {
                if (offset + sizeof(bool) <= sizeof(*this) && reinterpret_cast<bool*>(p + offset) == reinterpret_cast<bool*>(this) + offset / sizeof(bool)) {
                    // 如果当前偏移量适用于 bool 类型，则将其设置为 false
                    *reinterpret_cast<bool*>(p + offset) = false;
                    offset += sizeof(bool);
                } else if (offset + sizeof(float) <= sizeof(*this) && reinterpret_cast<float*>(p + offset) == reinterpret_cast<float*>(this) + offset / sizeof(float)) {
                    // 如果当前偏移量适用于 float 类型，则将其设置为 0.0
                    *reinterpret_cast<float*>(p + offset) = 0.0f;
                    offset += sizeof(float);
                } else {
                    // 处理未定义的情况（例如混合类型数组或其他类型成员）
                    // 可以根据实际情况调整逻辑或添加更多类型检查
                    offset += 1; // 跳过一个字节
                }
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
				if (cameraMoveState.i) ChangeLiveFollowCameraOffsetY(offsetMoveStep);
				if (cameraMoveState.k) ChangeLiveFollowCameraOffsetY(-offsetMoveStep);
				if (cameraMoveState.j) ChangeLiveFollowCameraOffsetX(0.8);
				if (cameraMoveState.l) ChangeLiveFollowCameraOffsetX(-0.8);
                // 手柄操作响应
                // 左摇杆
                if (std::abs(cameraMoveState.thumb_l_right) > 0.1f)
                    JLThumbRight(cameraMoveState.thumb_l_right);
                if (std::abs(cameraMoveState.thumb_l_down) > 0.1f)
                    JLThumbDown(cameraMoveState.thumb_l_down);
                // 右摇杆
                if (std::abs(cameraMoveState.thumb_r_right) > 0.1f)
                    JRThumbRight(cameraMoveState.thumb_r_right);
                if (std::abs(cameraMoveState.thumb_r_down) > 0.1f)
                    JRThumbDown(cameraMoveState.thumb_r_down);
                // 左扳机
                if (std::abs(cameraMoveState.lt_button) > 0.1f)
                    camera_down(cameraMoveState.lt_button * l_sensitivity * baseCamera.fov / 60);
                // 右扳机
                if (std::abs(cameraMoveState.rt_button) > 0.1f)
                    camera_up(cameraMoveState.rt_button * l_sensitivity * baseCamera.fov / 60);
                // 左肩键
                if (cameraMoveState.lb_button) changeCameraFOV(0.5f * r_sensitivity);
                // 右肩键
                if (cameraMoveState.rb_button) changeCameraFOV(-0.5f * r_sensitivity);
                // 十字键
                if (cameraMoveState.dpad_up) JDadUp();
//                if (cameraMoveState.dpad_down) JDadDown();
                if (cameraMoveState.dpad_left) JDadLeft();
                if (cameraMoveState.dpad_right) JDadRight();
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
            case KEY_V: if (message == WM_KEYDOWN) SwitchCameraSubMode(); break;
                // 手柄操作响应
                case BTN_A:
                    cameraMoveState.a_button = message == WM_KEYDOWN;
                    if (message == WM_KEYDOWN) JAKeyDown();
                    break;
                case BTN_B:
                    cameraMoveState.b_button = message == WM_KEYDOWN;
                    if (message == WM_KEYDOWN) JBKeyDown();
                    break;
                case BTN_X:
                    cameraMoveState.x_button = message == WM_KEYDOWN;
                    if (message == WM_KEYDOWN) JXKeyDown();
                    break;
                case BTN_Y:
                    cameraMoveState.y_button = message == WM_KEYDOWN;
                    if (message == WM_KEYDOWN) JYKeyDown();
                    break;
                case BTN_LB:
                    cameraMoveState.lb_button = message == WM_KEYDOWN;
                    break;
                case BTN_RB:
                    cameraMoveState.rb_button = message == WM_KEYDOWN;
                    break;
                case BTN_THUMBL:
                    cameraMoveState.thumb_l_button = message == WM_KEYDOWN;
                    break;
                case BTN_THUMBR:
                    cameraMoveState.thumb_r_button = message == WM_KEYDOWN;
                    break;
                case BTN_SELECT:
                    cameraMoveState.select_button = message == WM_KEYDOWN;
                    if (message == WM_KEYDOWN) JSelectKeyDown();
                    break;
                case BTN_START:
                    cameraMoveState.start_button = message == WM_KEYDOWN;
                    if (message == WM_KEYDOWN) JStartKeyDown();
                    break;
                case BTN_SHARE:
                    cameraMoveState.share_button = message == WM_KEYDOWN;
                    break;
                case BTN_XBOX:
                    cameraMoveState.xbox_button = message == WM_KEYDOWN;
                    break;

			default: break;
			}
		}
	}

    void
    on_cam_rawinput_joystick(JoystickEvent event) {
        int message = event.getMessage();
        float leftStickX = event.getLeftStickX();
        float leftStickY = event.getLeftStickY();
        float rightStickX = event.getRightStickX();
        float rightStickY = event.getRightStickY();
        float leftTrigger = event.getLeftTrigger();
        float rightTrigger = event.getRightTrigger();
        float hatX = event.getHatX();
        float hatY = event.getHatY();

        cameraMoveState.thumb_l_right = (std::abs(leftStickX) > 0.1f) ? leftStickX : 0;
        cameraMoveState.thumb_l_down = (std::abs(leftStickY) > 0.1f) ? leftStickY : 0;
        cameraMoveState.thumb_r_right = (std::abs(rightStickX) > 0.1f) ? rightStickX : 0;
        cameraMoveState.thumb_r_down = (std::abs(rightStickY) > 0.1f) ? rightStickY : 0;
        cameraMoveState.lt_button = (std::abs(leftTrigger) > 0.1f) ? leftTrigger : 0;
        cameraMoveState.rt_button = (std::abs(rightTrigger) > 0.1f) ? rightTrigger : 0;
        cameraMoveState.dpad_up = hatY == -1.0f;
        cameraMoveState.dpad_down = hatY == 1.0f;
        cameraMoveState.dpad_left = hatX == -1.0f;
        cameraMoveState.dpad_right = hatX == 1.0f;

        if (cameraMoveState.dpad_down) {
            JDadDown();
        }

//        GakumasLocal::Log::InfoFmt(
//                "Motion event: action=%d, leftStickX=%.2f, leftStickY=%.2f, rightStickX=%.2f, rightStickY=%.2f, leftTrigger=%.2f, rightTrigger=%.2f, hatX=%.2f, hatY=%.2f",
//                message, leftStickX, leftStickY, rightStickX, rightStickY, leftTrigger,
//                rightTrigger, hatX, hatY);
    }

	void initCameraSettings() {
		reset_camera();
		cameraRawInputThread();
	}

}
