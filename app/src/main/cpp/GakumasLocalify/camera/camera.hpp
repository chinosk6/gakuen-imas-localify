#pragma once
#include "baseCamera.hpp"

namespace GKCamera {
    enum class CameraMode {
        FREE,
        FIRST_PERSON,
        FOLLOW
    };

    enum class FirstPersonRoll {
        ENABLE_ROLL,
        DISABLE_ROLL
    };

    void SetCameraMode(CameraMode mode);
    CameraMode GetCameraMode();
    void SetFirstPersonRoll(FirstPersonRoll mode);
    FirstPersonRoll GetFirstPersonRoll();

    extern BaseCamera::Camera baseCamera;
    extern UnityResolve::UnityType::Vector3 firstPersonPosOffset;
    extern UnityResolve::UnityType::Vector3 followPosOffset;
    extern int followCharaIndex;
    extern GakumasLocal::Misc::CSEnum bodyPartsEnum;

    UnityResolve::UnityType::Vector3 CalcPositionFromLookAt(const UnityResolve::UnityType::Vector3& target,
                                                            const UnityResolve::UnityType::Vector3& offset);

    UnityResolve::UnityType::Vector3 CalcFirstPersonPosition(const UnityResolve::UnityType::Vector3& position,
                                                             const UnityResolve::UnityType::Vector3& forward,
                                                             const UnityResolve::UnityType::Vector3& offset);

    void on_cam_rawinput_keyboard(int message, int key);
	void initCameraSettings();
}
