#include <ESP8266WiFi.h>
#include <Firebase_ESP_Client.h>
#include <ArduinoJson.h>
//Provide the token generation process info.
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"
#include "ServoMotorRun.h"
#include <TimeLib.h>
#include <WiFiUdp.h>
#include <NTPClient.h>
#include <DS3232RTC.h>      // https://github.com/JChristensen/DS3232RTC
#include <Streaming.h>      // https://github.com/janelia-arduino/Streaming

// REFER TO: 
// https://github.com/mobizt/Firebase-ESP8266?tab=readme-ov-file#realtime-database
// https://github.com/mobizt/Firebase-ESP-Client/tree/main/examples/RTDB

#define API_KEY "AIzaSyDEihkhy5tYd2cebL288qEIGiyccHpPfN0"
#define DATABASE_URL "https://rhodes-db-default-rtdb.asia-southeast1.firebasedatabase.app/"

const char* ssid="MSG";
const char* password="Toutou@181017";

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

unsigned long sendDataPrevMillis = 0;
bool boolValue;
bool signupOK = false;

DS3232RTC myRTC;
ServoMotorRun motor1(14, 0, 150); // pin, minAngle, maxAngle

// Setting up the device time using NTP server 
const long utcOffsetInSeconds = 28800;
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "asia.pool.ntp.org", utcOffsetInSeconds);
bool gateActivated = false;  // Variable to track if the gate has been activated

void setup() {
  time_t t;
  tmElements_t tm;

  motor1.setup();
  myRTC.begin();
  setSyncProvider(myRTC.get);
  if(timeStatus() != timeSet)
      Serial.println("Unable to sync with the RTC");
    else
      Serial.println("RTC has set the system time");

  Serial.begin(115200);
  Serial.println("");
  Serial.println("");
  Serial.print("Connecting to WiFi: ");
  Serial.print(ssid);
  WiFi.begin(ssid, password);
  Serial.println("");
  Serial.print("Connecting");

  while(WiFi.status() != WL_CONNECTED){
    delay(500);
    Serial.print(".");
  }


  Serial.print("... DONE");
  Serial.println("");
  Serial.println("DEVICE IP ADDRESS: ");
  Serial.print(WiFi.localIP());
  /* Assign the api key (required) */
  config.api_key = API_KEY;
  /* Assign the RTDB URL (required) */
  config.database_url = DATABASE_URL;
  /* Sign up */
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("");
    Serial.println("Connecting to database... DONE");
    signupOK = true;
  }
  else {
    Serial.printf("%s\n", config.signer.signupError.message.c_str());
  }


  timeClient.begin();
  timeClient.update();
  Serial.println("");


  // Only use this to synchronize the device time to current time. If not needed anymore, comment it out
  // Serial.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
  // int y = 2024;
  // if (y >= 100 && y < 1000)
  //   Serial << F("Error: Year must be two digits or four digits!") << endl;
  // else {
  //   if (y >= 1000)
  //       tm.Year = CalendarYrToTm(y);
  //   else    // (y < 100)
  //       tm.Year = y2kYearToTm(y);
  //       tm.Month = 3;
  //       tm.Day = 15;
  //       tm.Hour = timeClient.getHours();
  //       tm.Minute = timeClient.getMinutes();
  //       tm.Second = timeClient.getSeconds();
  //       t = makeTime(tm);
  //       myRTC.set(t);
  //       setTime(t);
  //       Serial.print("Updating device clock to : ");
  //       digitalClockDisplay();
  //       Serial.println("");
  // }

  // Serial.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");

  Serial.print("Updating device clock to : ");
  digitalClockDisplay();
  Serial.println("");

  /* Assign the callback function for the long running token generation task */
  config.token_status_callback = tokenStatusCallback; //see addons/TokenHelper.h
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  // FirebaseJson json;
  // json.set("Talipapa", "Elaina");


  // FirebaseJsonArray defaultSchedArray;
  // defaultSchedArray.add("6:00");
  // defaultSchedArray.add("12:00");
  // defaultSchedArray.add("19:00");
  // defaultSchedArray.add("22:53");
  // if (!Firebase.RTDB.get(&fbdo, "/feedingSchedules")) {
  //   Serial.println("No past record found.. Setting up /feedingSchedules default schedule!");
  //   Serial.printf("Schedules: %s\n", Firebase.RTDB.setArray(&fbdo, "/feedingSchedules", &defaultSchedArray) ? fbdo.to<FirebaseJsonArray>().raw() : fbdo.errorReason().c_str());
  // } else{
  //   Serial.println("Searching for schedule.... FOUND");
  // }

  if (!Firebase.RTDB.get(&fbdo, "/feedSignal")) {
    Serial.println("No past record found.. Setting up /feedSignal default value to FALSE");
    Serial.printf("Feed signal setup status: %s\n", Firebase.RTDB.setBool(&fbdo, "/feedSignal", false) ? "SUCCESS" : "FAILED");
  } else{
    Serial.println("Searching for feed signal status.... FOUND");
  }




  Serial.println("");
  Serial.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
  Serial.println("Device is now ready!");
  Serial.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");

  // Examples!!!
  // REFER TO: 
  // https://github.com/mobizt/Firebase-ESP8266?tab=readme-ov-file#realtime-database
  // https://github.com/mobizt/Firebase-ESP-Client/tree/main/examples/RTDB
  // Serial.printf("Set object data..... %s\n", Firebase.RTDB.setJSON(&fbdo, "/test/set/data", &json) ? fbdo.to<FirebaseJson>().raw() : fbdo.errorReason().c_str());
  // Serial.printf("Set string data..... %s\n", Firebase.RTDB.setString(&fbdo, "/test/set/data2", "TEST STRING") ? fbdo.to<const char *>() : fbdo.errorReason().c_str());
  
}

void loop() {
  timeClient.update(); // Keep this in loop to synchrounously update the device clock
  


  FirebaseJson scheduleArr;
  FirebaseJsonData result;
  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 2000 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();
    if (Firebase.RTDB.get(&fbdo, "/feedSignal")) {
      if (fbdo.dataType()== "boolean") {
        bool gateFeedingStatus = fbdo.to<bool>();
        if (gateFeedingStatus == true){
          Serial.println("Activating motor");
          motor1.activateMotor(2, 300); // activationCount, activationDelay
          Serial.println("Motor is now deactivated");
          Firebase.RTDB.setBool(&fbdo, "/feedSignal", false);
        }
      }
    }

    if (Firebase.RTDB.get(&fbdo, "/feedingSchedules")) {
      if (fbdo.dataType()== "json") {
    
        scheduleArr = fbdo.to<FirebaseJson>();

        result.get<FirebaseJson>(scheduleArr);
        size_t len = scheduleArr.iteratorBegin();
        FirebaseJson::IteratorValue value;

        for (size_t i = 0; i < len; i++)
        {
          value = scheduleArr.valueAt(i);
          char formattedTime[6];
          sprintf(formattedTime, "%02d:%02d", hour(), minute());
          String firebaseTime = value.value.substring(1, value.value.length() - 1);

          // Serial.printf("%d, Type: %s, Name: %s, Value: %s\n", i, value.type == FirebaseJson::JSON_OBJECT ? "object" : "array", value.key.c_str(), value.value.c_str());

          if (String(formattedTime) == firebaseTime && !gateActivated){
            Serial.println("Its time to feed");
            Serial.println("Activating motor");
            motor1.activateMotor(2, 300); // activationCount, activationDelay
            Serial.println("Motor is now deactivated");

            gateActivated = true;
          } 
        }
        // Clear all list to free memory
        scheduleArr.iteratorEnd();
      }
    }
    
    



  }

  if (timeClient.getSeconds() == 0) {
    gateActivated = false;
  }

}

void digitalClockDisplay()
{
    // digital clock display of the time
    Serial.print(month());
    Serial.print(' ');
    Serial.print(day());
    Serial.print(' ');
    Serial.print(year());
    Serial.print(' ');
    Serial.print(hour());
    printDigits(minute());
    printDigits(second());
}

void printDigits(int digits)
{
    // utility function for digital clock display: prints preceding colon and leading 0
    Serial.print(':');
    if(digits < 10)
        Serial.print('0');
    Serial.print(digits);
}

      
