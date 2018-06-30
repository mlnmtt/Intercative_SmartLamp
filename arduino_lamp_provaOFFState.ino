#include <FastLED.h>
#include <Servo.h>
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <EEPROM.h>

// VARIABILI LAMPADA
String lampName = "Studio Smartlamp";
String lampImg = "https://i.imgur.com/xRgIEAf.png";

// VARIABILI PIN
const int ledPin = 5;
const int sensorPin = D6; 
const int builtinLed = 2; 

// VARIABILI LED
const int numLED = 5;                   // numero di led sulla striscia
CRGB leds[numLED];
int ledColor = 100;
int ledIntensity = 25;    
int ledIncrement = 5;

// VARIBIABILI TEMPO
const int ciclo = numLED + 1;  
const int waitTime = 50;
const int TIMEOUT = 5000;
int i=0;
int r=0;
int nLoops = 1;
int udpTimeInit = 0;
int udpTimeEnd = 0;

// VARIABILI SENSORE IR
int sensorState = HIGH; 
int oldSensorState = LOW;

// VARIABILI MOTORI
Servo motoreSx;
Servo motoreDx;
int angleSx = 90;
int angleDx = 90;

// VARIABILI STATO
int oldState = 0;                         // si userà vecchio_val per conservare lo stato del pin di input al passo precedente  
int ledState = 0;   //io                  // ricorda lo stato in cui si trova il led, stato = 0 led spento, stato = 1 led acceso  
int intensityMode = 0;

// VARIABILI WIFI
IPAddress ip;
WiFiUDP Udp;
WiFiServer server(2048);
unsigned int localUdpPort = 4096;
const char* ssid = "iPhone di Mattia";
const char* password = "mlnmtt92";
String rx_buffer = "";                    // Buffer to store INPUT data
String tx_buffer = "";                    // Buffer to store OUTPUT data
const int INPUT_SIZE = 5;                 // 5 byte: 1 per il char del comando e 4 per l'int del valore
byte incomingPacket[INPUT_SIZE];
String initParam = "";
char outPacket[128] = "";
char command;
String value = "";

// VARIABILI MEMORIA
int memAddrIo = 0;
int memAddrIntensity = 1;
int memAddrColor = 2;
int memAddrAngleSx = 3;
int memAddrAngleDx = 4;

// FUNZIONI

// LED ON
void ledOn() {
  for (int j=0; j<numLED; j++)
    leds[j] = CHSV(ledColor,255,ledIntensity); 
  FastLED.show(); 
  delay(100);
  tx_buffer = "y1";                   // yes
  ledState = 1;
} 

// LED OFF
void ledOff() {
  for (int j=0; j<numLED; j++)
    leds[j] = CHSV(0,0,0); 
  FastLED.show();
  delay(100); 
  tx_buffer = "n0";                   // no
  ledState = 0;
}

// CAMBIA COLORE
void setColor(int val) {
  if (ledState == 0)                  
    return;
  ledColor = val;
  for (int j=0; j<numLED; j++)
      leds[j] = CHSV(ledColor,255,ledIntensity); 
  FastLED.show(); 
  delay(100); 
  Serial.println("New color "+ val);
  String tmp = String(val);           // da int a String    
  tx_buffer = "c"+tmp;                // aggiorna buffer OUT per rispondere ad Android
}

// CAMBIA INTENSITA
void setIntensity(int val) {
  if (ledState == 0)
    return;
  ledIntensity = val;
  for (int j=0; j<numLED; j++)
      leds[j] = CHSV(ledColor,255,ledIntensity); 
  FastLED.show(); 
  Serial.println("New intensity");
  String tmp = String(ledIntensity);           // da int a String    
  tx_buffer = "i"+tmp;                       // aggiorna buffer OUT per rispondere ad Android
}

// CAMBIO ANGOLO SX
void setAngleSx(int val) {
  if (ledState == 0)
    return;
  angleSx = val;
  motoreSx.write(angleSx);
  delay(100);                         // aspetta che il motore agisca
  Serial.println("New angle "+ val);
  String tmp = String(val);           // da int a String    
  tx_buffer = "s"+tmp;                // aggiorna buffer OUT per rispondere ad Android
}

// CAMBIO ANGOLO DX
void setAngleDx(int val) {
  if (ledState == 0)
    return;
  angleDx = val;
  motoreDx.write(angleDx);
  delay(100);                         // aspetta che il motore agisca
  Serial.println("New angle "+ val);
  String tmp = String(val);           // da int a String    
  tx_buffer = "d"+tmp;                // aggiorna buffer OUT per rispondere ad Android
}

// SALVA STATO LAMAPDA
void saveState() {
  EEPROM.write(memAddrIo, ledState);
  EEPROM.write(memAddrColor, ledColor);
  EEPROM.write(memAddrIntensity, ledIntensity);
  EEPROM.write(memAddrAngleSx, angleSx);
  EEPROM.write(memAddrAngleDx, angleDx);
  EEPROM.commit();
}

// RECUPERA STATO LAMPADA
void loadState() {
  ledState = EEPROM.read(0);
  ledIntensity = EEPROM.read(1);
  ledColor = EEPROM.read(2);
  angleSx = EEPROM.read(3);
  angleDx = EEPROM.read(4);
}

// SPLIT: DIVIDE IL PACCHETTO IN INGRESSO
void split (String msg) {                                // Splits message in 2 parts: commands and values
 command = msg[0];                                       // primo char per il comando da interpretare
 value = msg.substring(1);
}

// INVIA TX
void send_message(WiFiClient client, String msg) {        // Send a message to the given client
  client.println(msg);
  Serial.println("[TX] " + msg);
}

// GESTIONE RX
void got_message(String msg) {        // Called whenever a newline-delimited message is received
  Serial.println("[RX] " + msg);
  split(msg);
  int newVal = value.toInt();         // da stringa a int

  // CASI DI UPDATE
  switch (command) {
    case 'y' : ledOn(); break;
    case 'n' : ledOff(); break;
    case 'c' : setColor(newVal); break;
    case 'i' : setIntensity(newVal); break;
    case 's' : setAngleSx(newVal); break;
    case 'd' : setAngleDx(newVal); break; 
  }
}

// INIZIALIZZAZIONE
void setup() {
  // LED
  FastLED.addLeds<NEOPIXEL, ledPin>(leds, numLED); 

  // led interno per debug udp
  pinMode(LED_BUILTIN,OUTPUT);
  pinMode(builtinLed, OUTPUT);

  // sensore IR
  pinMode(sensorPin, INPUT);
  
  // MOTORI
  /*
  motoreSx.attach(); // MANCANO I PIN DEI MOTORI
  motoreDx.attach();
  */

  // MEMORIA
  EEPROM.begin(10);
  loadState();
  
  // MONITOR SERIALE PER DEBUG
  Serial.begin(115200);
  Serial.println(ip);
  Serial.printf("Connecting to %s ", ssid);

  // Wi-Fi
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED)
    delay(500);
  Serial.println("Connected to IP: ");
  Serial.println(WiFi.localIP());
  IPAddress ip = WiFi.localIP();         // indirizzo assegnato

  // UDP BROADCAST
  Udp.begin(localUdpPort);
  initParam = lampName + ";https://i.imgur.com/xRgIEAf.png";
  initParam.toCharArray(outPacket, 128);
  Serial.printf("Now listening at IP %s, UDP port %d\n", WiFi.localIP().toString().c_str(), localUdpPort);
  udpTimeInit = millis();                // attiva timer per mandare pacchetto ogni 5 secondi

  // SERVER TCP
  server.begin();
  Serial.println("WiFi server initialized!");
}

// RUN
void loop() { 

  // UDP BROADCAST
  udpTimeEnd = millis() - udpTimeInit;  // timer che manda un pacchetto UDP broadcast ogni 5 secondi
  if (udpTimeEnd >= TIMEOUT) {          // ogni 5 secondi
    digitalWrite(builtinLed, LOW);      // accendi il led integrato di arduino
    Udp.beginPacket("255.255.255.255", 4096);
    Udp.write(outPacket);
    Udp.endPacket();
    delay(500);
    Serial.println(outPacket);          // print su console
    digitalWrite(builtinLed, HIGH);
    udpTimeInit = millis();             // ripristina timeout
  }

  // CONNESSIONE TCP
  WiFiClient client = server.available();
  if (client) {                         // Arduino rimane sempre in ascolto e Android gli chiede come sta ogni mezzo secondo, ricevendo sempre una risposta
    while (client.connected()){
      // RX
      if (client.available()) {         // Read data from the client till we hit a newline
        while (client.available()) {
          char c = client.read();
          if (c == '\n') {
            got_message(rx_buffer);
            rx_buffer = ""; 
          } else rx_buffer += c;
        }
        // TX
        if (tx_buffer.equals(""))
          break;
        else {
          send_message(client, tx_buffer); // se il buffer è vuoto Android non fa nulla, altrimenti lo interpreta
          tx_buffer = "";  
        } 
      }
    }
  } 
  
  // STATI DEL SENSORE PER DISTINGUERE TRA ON/OFF E INTENSITY
  sensorState = digitalRead(sensorPin); // sensore IR
  if ((sensorState == LOW) && (ledState == 0)) {
  // ON MODE
    ledOn();
    delay(50);
  }
  else if ((sensorState == LOW) && (ledState == 1)) {
    delay(250);
    if (digitalRead(sensorPin) == LOW) {
      while (digitalRead(sensorPin) == LOW) {
        // INTENSITA MODE
        ledIntensity = (ledIntensity+ledIncrement);
        setIntensity(ledIntensity);
        //FastLED.show(); 
        delay(100); 
        // DECREMENTA
        if (ledIntensity == 255) {
          ledIncrement = -5;
        }
        // INCREMENTA
        if (ledIntensity == 0) {
          ledIncrement = +5;
        }
      }
    }
    else {
      // OFF MODE
      ledOff();
      delay(50);
    }
    
  }

  // SALVA STATO IN MEMORIA
  saveState();
  delay(100);

  // close connection. Give the client time to receive data before closing
  // Serial.println("Disconnecting from client");
  delay(100);
  client.flush();
  client.stop(); 
    
} // end loop
  

