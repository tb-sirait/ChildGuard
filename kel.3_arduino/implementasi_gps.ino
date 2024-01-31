void setup() {
  Serial.begin(9600);
  randomSeed(analogRead(A0)); // Menggunakan nilai analog sebagai seed
}

void loop() {
  float minValue_lat = -6.1; // Batas bawah
  float maxValue_lat = -6.2; // Batas atas
  
  float minValue_lon = 106.5; // Batas bawah
  float maxValue_lon = 106.9; // Batas atas

  // Menghasilkan nilai float acak antara minValue_lat dan maxValue_lat
  float lat = minValue_lat + (float)random(1000) / 1000.0 * (maxValue_lat - minValue_lat);

  // Menghasilkan nilai float acak antara minValue_lon dan maxValue_lon
  float lon = minValue_lon + (float)random(1000) / 1000.0 * (maxValue_lon - minValue_lon);

  // Menampilkan nilai float acak ke Serial Monitor
  Serial.print("{\"lat\":");
  Serial.print(lat,6);
  Serial.print(",\"lon\":");
  Serial.print(lon,6);
  Serial.println("}");

  delay(1000); // Tunggu 1 detik sebelum menghasilkan nilai acak berikutnya
}
