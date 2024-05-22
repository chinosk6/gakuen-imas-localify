#pragma once

#include "../deps/UnityResolve/UnityResolve.hpp"

enum LonMoveHState {
	LonMoveLeftAndRight,
	LonMoveForward,
	LonMoveBack
};

namespace BaseCamera {
    using Vector3_t = UnityResolve::UnityType::Vector3;

	extern float moveStep;
	extern float look_radius;  // 转向半径
	extern float moveAngel;  // 转向角度

	extern int smoothLevel;
	extern unsigned long sleepTime;
	

	class Camera {
	public:
		Camera();
		Camera(Vector3_t& vec, Vector3_t& lookAt);
		Camera(Vector3_t* vec, Vector3_t* lookAt);
		Camera(float x, float y, float z, float lx, float ly, float lz);

		void reset();
		void setPos(float x, float y, float z);
		void setLookAt(float x, float y, float z);

		void set_lon_move(float vertanglePlus, LonMoveHState moveState = LonMoveHState::LonMoveLeftAndRight);
		void updateVertLook();
		void setHoriLook(float vertangle);

		Vector3_t GetPos();
        Vector3_t GetLookAt();

		Vector3_t pos{0.5, 1.1, 1.3};
		Vector3_t lookAt{0.5, 1.1, -3.7};
		float fov = 60;

		float horizontalAngle = 0;  // 水平方向角度
		float verticalAngle = 0;  // 垂直方向角度

	};

}
