#include <WiFi.h>
#include <WebServer.h>
#include <HTTPClient.h>

const char* ssid = "VrakRed";
const char* password = "Athiradi22#";

const char* crashServer = "http://10.122.97.121:9080/report";

WebServer server(80);

// LED pins
const int whiteLED = 4;
const int whiteLED2 = 18;
const int whiteLED3 = 19;
const int whiteLED4 = 21;
const int redLED = 5;

String carBrand = "";
String plate = "";
String location = "";

void setNormalMode() {
  digitalWrite(whiteLED, HIGH);  // White ON
  digitalWrite(whiteLED2, HIGH);  // White ON
  digitalWrite(whiteLED3, HIGH);  // White ON
  digitalWrite(whiteLED4, HIGH);  // White ON
  digitalWrite(redLED, LOW);     // Red OFF
}

void setAlertMode() {
  digitalWrite(whiteLED, LOW);   // White OFF
  digitalWrite(redLED, HIGH);    // Red ON
}

void sendToDatabase() {
  if (carBrand == "" || plate == "" || location == "") {
    Serial.println(" Waiting for all fields before sending...");
    return;
  }

  String json = "{\"carBrand\":\"" + carBrand + "\",\"plate\":\"" + plate + "\",\"location\":\"" + location + "\"}";
  Serial.println("Sending to CrashServer: " + json);

  WiFiClient client;
  HTTPClient http;
  http.begin(client, crashServer);
  http.addHeader("Content-Type", "application/json");
  int code = http.POST(json);
  Serial.printf("DB Server Response: %d\n", code);
  http.end();

  setAlertMode();
  delay(10000); 
  setNormalMode();

  carBrand = "";
  plate = "";
  location = "";
}

void handleCarData() {
  if (server.hasArg("plain")) {
    String body = server.arg("plain");
    Serial.println("Received car data: " + body);

    int brandStart = body.indexOf("carBrand\":\"") + 11;
    int brandEnd = body.indexOf("\"", brandStart);
    carBrand = body.substring(brandStart, brandEnd);

    int plateStart = body.indexOf("plate\":\"") + 8;
    int plateEnd = body.indexOf("\"", plateStart);
    plate = body.substring(plateStart, plateEnd);

    sendToDatabase();
    server.send(200, "text/plain", "Car data received");
  } else {
    server.send(400, "text/plain", "Bad Request");
  }
}

void handleLocation() {
  if (server.hasArg("plain")) {
    location = server.arg("plain");
    Serial.println("Received location: " + location);
    sendToDatabase();
    server.send(200, "text/plain", "Location received");
  } else {
    server.send(400, "text/plain", "Bad Request");
  }
}

void setup() {
  Serial.begin(115200);

  // Setup LEDs
  pinMode(whiteLED, OUTPUT);
  pinMode(whiteLED2, OUTPUT);
  pinMode(whiteLED3, OUTPUT);
  pinMode(whiteLED4, OUTPUT);
  pinMode(redLED, OUTPUT);
  setNormalMode();

  WiFi.begin(ssid, password);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n WiFi connected!");
  Serial.print(" ESP32 IP: ");
  Serial.println(WiFi.localIP());

  server.on("/carData", HTTP_POST, handleCarData);
  server.on("/location", HTTP_POST, handleLocation);
  server.begin();
  Serial.println(" ESP32 server started!");
}

void loop() {
  server.handleClient();
}
