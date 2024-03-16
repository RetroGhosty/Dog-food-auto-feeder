#include "ServoMotorRun.h"
#include "Arduino.h"
#include <Servo.h>


Servo servo;

ServoMotorRun::ServoMotorRun(int assignedPin, int minAngle, int maxAngle){
  _pin = assignedPin;
  _minAngle = minAngle;
  _maxAngle = maxAngle;
}

void ServoMotorRun::setup(){
  servo.attach(_pin);
  servo.write(_minAngle);
  delay(2000);
}

void ServoMotorRun::activateMotor(int activationCount, int activationDelay){
  for (int i = 0; i < activationCount; i++){
    servo.write(_maxAngle);
    delay(activationDelay);
    servo.write(_minAngle);
    delay(activationDelay);
  }

}