
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ESP8266HTTPClient.h>

const char* ssid = "VrakRed";
const char* password = "Athiradi22#";

// Change to ESP32's IP address
const char* esp32Server = "http://10.122.97.204/carData";

ESP8266WebServer server(80);

void handleData() {
  if (server.hasArg("plain")) {
    String body = server.arg("plain");
    Serial.println("Received from Swing:");
    Serial.println(body);

    // Forward this data to ESP32
    WiFiClient client;
    HTTPClient http;
    http.begin(client, esp32Server);
    http.addHeader("Content-Type", "application/json");
    int code = http.POST(body);
    Serial.printf("Forwarded to ESP32 | HTTP Code: %d\n", code);
    http.end();

    server.send(200, "text/plain", "Received and forwarded to ESP32");
  } else {
    server.send(400, "text/plain", "Bad Request");
  }
}

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nConnected!");
  Serial.print("NodeMCU IP: ");
  Serial.println(WiFi.localIP());

  server.on("/data", HTTP_POST, handleData);
  server.begin();
  Serial.println("NodeMCU server started!");
}

void loop() {
  server.handleClient();
}

