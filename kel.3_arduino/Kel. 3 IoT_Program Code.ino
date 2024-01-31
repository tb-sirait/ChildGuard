#include <Arduino.h>
#include <stdlib.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <TinyGPS++.h>
#include <SoftwareSerial.h>

SoftwareSerial gpsSerial(12, 14); // RX, TX
SoftwareSerial sim800lSerial(4, 5); // RX, TX
TinyGPSPlus gps;
LiquidCrystal_I2C lcd(0x27, 16, 2);

void setup() {
  Serial.begin(9600);
  gpsSerial.begin(9600);
  sim800lSerial.begin(9600);
  lcd.begin(16, 2);
}

void displayGPSInfo() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Lat: ");
  lcd.print(gps.location.lat(), 6);
  lcd.setCursor(0, 1);
  lcd.print("Lon: ");
  lcd.print(gps.location.lng(), 6);
}


void sendDataToAPI(float lat, float lon)
{
  HTTPClient http;

  // Construct the API URL with latitude and longitude parameters
  String apiUrl = String(apiEndpoint) + "?id_parent=0&koordinat_lattitude=" + lat + "&koordinat_longtitude=" + lon;

  // Make a PUT request to the API
  http.begin(apiUrl);
  int httpResponseCode = http.PUT("");

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
  }
  else
  {
    Serial.println("Error in HTTP request");
  }

  http.end();  // Close connection
}

void loop()
{
  while (SerialGPS.available() > 0)
  {
    if (gps.encode(SerialGPS.read()))
    {
      if (gps.location.isValid())
      {
        latitude = gps.location.lat();
        lat_str = String(latitude, 6);
        longitude = gps.location.lng();
        lng_str = String(longitude, 6);

        // Display data on OLED
        display.clear();
        display.setTextAlignment(TEXT_ALIGN_LEFT);
        display.setFont(ArialMT_Plain_16);
        display.drawString(0, 23, "Lat:");
        display.drawString(45, 23, lat_str);
        display.drawString(0, 38, "Lng:");
        display.drawString(45, 38, lng_str);
        display.display();

        // Send data to API
        sendDataToAPI(latitude, longitude);
      }
      delay(1000);
      Serial.println();
    }
  }
}
