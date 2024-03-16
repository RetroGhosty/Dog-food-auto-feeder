#ifndef ServoMotorRun_h
#define ServoMotorRun_h

#include "Arduino.h"

class ServoMotorRun{
  public:
    ServoMotorRun(int assignedPin, int minAngle, int maxAngle);
    void setup();
    void activateMotor(int activationCount, int activationDelay);
  private:
    int _pin;
    int _minAngle;
    int _maxAngle;
};


#endif