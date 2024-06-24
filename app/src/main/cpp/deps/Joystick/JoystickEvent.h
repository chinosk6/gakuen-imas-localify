//
// Created by RanKaeder on 2024/6/18.
//

#ifndef GAKUMAS_LOCALIFY_JOYSTICKEVENT_H
#define GAKUMAS_LOCALIFY_JOYSTICKEVENT_H

class JoystickEvent {
public:
    JoystickEvent(int message, float leftStickX, float leftStickY, float rightStickX,
                  float rightStickY, float leftTrigger, float rightTrigger,
                  float hatX, float hatY)
            : message(message), leftStickX(leftStickX), leftStickY(leftStickY),
              rightStickX(rightStickX), rightStickY(rightStickY), leftTrigger(leftTrigger),
              rightTrigger(rightTrigger), hatX(hatX), hatY(hatY) {
    }

    // Getter 方法
    int getMessage() const {
        return message;
    }

    float getLeftStickX() const {
        return leftStickX;
    }

    float getLeftStickY() const {
        return leftStickY;
    }

    float getRightStickX() const {
        return rightStickX;
    }

    float getRightStickY() const {
        return rightStickY;
    }

    float getLeftTrigger() const {
        return leftTrigger;
    }

    float getRightTrigger() const {
        return rightTrigger;
    }

    float getHatX() const {
        return hatX;
    }

    float getHatY() const {
        return hatY;
    }

private:
    int message;
    float leftStickX;
    float leftStickY;
    float rightStickX;
    float rightStickY;
    float leftTrigger;
    float rightTrigger;
    float hatX;
    float hatY;
};

#endif //GAKUMAS_LOCALIFY_JOYSTICKEVENT_H