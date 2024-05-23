#include "baseCamera.hpp"
#include <thread>


namespace BaseCamera {
	using Vector3_t = UnityResolve::UnityType::Vector3;

	float moveStep = 0.05;
	float look_radius = 5;  // 转向半径
	float moveAngel = 1;  // 转向角度

	int smoothLevel = 1;
	unsigned long sleepTime = 0;
	

	Camera::Camera() {
		Camera(0, 0, 0, 0, 0, 0);
	}

	Camera::Camera(Vector3_t* vec, Vector3_t* lookAt) {
		Camera(vec->x, vec->y, vec->z, lookAt->x, lookAt->y, lookAt->z);
	}

	Camera::Camera(Vector3_t& vec, Vector3_t& lookAt) {
		Camera(vec.x, vec.y, vec.z, lookAt.x, lookAt.y, lookAt.z);
	}

	Camera::Camera(float x, float y, float z, float lx, float ly, float lz) {
		pos.x = x;
		pos.y = y;
		pos.z = z;
		lookAt.x = lx;
		lookAt.y = ly;
		lookAt.z = lz;
	}

	void Camera::setPos(float x, float y, float z) {
		pos.x = x;
		pos.y = y;
		pos.z = z;
	}

	void Camera::setLookAt(float x, float y, float z) {
		lookAt.x = x;
		lookAt.y = y;
		lookAt.z = z;
	}

	void Camera::reset() {
		setPos(0.5, 1.1, 1.3);
		setLookAt(0.5, 1.1, -3.7);
		fov = 60;
		verticalAngle = 0;
		horizontalAngle = 0;
	}

	Vector3_t Camera::GetPos() {
		return pos;
	}

	Vector3_t Camera::GetLookAt() {
		return lookAt;
	}

	void Camera::set_lon_move(float vertanglePlus, LonMoveHState moveState) {  // 前后移动
		auto radian = (verticalAngle + vertanglePlus) * M_PI / 180;
		auto radianH = (double)horizontalAngle * M_PI / 180;

		auto f_step = cos(radian) * moveStep * cos(radianH) / smoothLevel;  // ↑↓
		auto l_step = sin(radian) * moveStep * cos(radianH) / smoothLevel;  // ←→
		// auto h_step = tan(radianH) * sqrt(pow(f_step, 2) + pow(l_step, 2));
		auto h_step = sin(radianH) * moveStep / smoothLevel;

		switch (moveState)
		{
		case LonMoveForward: break;
		case LonMoveBack: h_step = -h_step; break;
		default: h_step = 0; break;
		}

		for (int i = 0; i < smoothLevel; i++) {
			pos.z -= f_step;
			lookAt.z -= f_step;
			pos.x += l_step;
			lookAt.x += l_step;
			pos.y += h_step;
			lookAt.y += h_step;
			std::this_thread::sleep_for(std::chrono::milliseconds(sleepTime));
		}
	}

	void Camera::updateVertLook() {  // 上+
		auto radian = verticalAngle * M_PI / 180;
		auto radian2 = ((double)horizontalAngle - 90) * M_PI / 180;  // 日

		auto stepX1 = look_radius * sin(radian2) * cos(radian) / smoothLevel;
		auto stepX2 = look_radius * sin(radian2) * sin(radian) / smoothLevel;
		auto stepX3 = look_radius * cos(radian2) / smoothLevel;

		for (int i = 0; i < smoothLevel; i++) {
			lookAt.z = pos.z + stepX1;
			lookAt.y = pos.y + stepX3;
			lookAt.x = pos.x - stepX2;
			std::this_thread::sleep_for(std::chrono::milliseconds(sleepTime));
		}
	}

	void Camera::setHoriLook(float vertangle) {  // 左+
		auto radian = vertangle * M_PI / 180;
		auto radian2 = horizontalAngle * M_PI / 180;

		auto stepBt = cos(radian) * look_radius * cos(radian2) / smoothLevel;
		auto stepHi = sin(radian) * look_radius * cos(radian2) / smoothLevel;
		auto stepY = sin(radian2) * look_radius / smoothLevel;

		for (int i = 0; i < smoothLevel; i++) {
			lookAt.x = pos.x + stepHi;
			lookAt.z = pos.z - stepBt;
			lookAt.y = pos.y + stepY;
			std::this_thread::sleep_for(std::chrono::milliseconds(sleepTime));
		}
	}


}
