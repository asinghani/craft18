#define PORT0 21
#define PORT1 20
#define PORT2 19
#define PORT3 18
#define PORT4 17
#define PORT5 16
#define PORT6 15
#define PORT7 14

void setup() {
  digitalWrite(PORT0, LOW);
  digitalWrite(PORT1, LOW);
  digitalWrite(PORT2, LOW);
  digitalWrite(PORT3, LOW);
  digitalWrite(PORT4, LOW);
  digitalWrite(PORT5, LOW);
  digitalWrite(PORT6, LOW);
  digitalWrite(PORT7, LOW);
  Serial.begin(115200);
}

int lastRead = 0;
void loop() {
  int x = lastRead;
  while (Serial.available()) {
    x = Serial.read();
  }
  if (x != lastRead) {
    pinMode(PORT0, (x & (1 << 0)) ? OUTPUT : INPUT);
    pinMode(PORT1, (x & (1 << 1)) ? OUTPUT : INPUT);
    pinMode(PORT2, (x & (1 << 2)) ? OUTPUT : INPUT);
    pinMode(PORT3, (x & (1 << 3)) ? OUTPUT : INPUT);
    pinMode(PORT4, (x & (1 << 4)) ? OUTPUT : INPUT);
    pinMode(PORT5, (x & (1 << 5)) ? OUTPUT : INPUT);
    pinMode(PORT6, (x & (1 << 6)) ? OUTPUT : INPUT);
    pinMode(PORT7, (x & (1 << 7)) ? OUTPUT : INPUT);
    lastRead = x;
  }
  Serial.write(((!digitalRead(PORT0) << 0) |
                (!digitalRead(PORT1) << 1) |
                (!digitalRead(PORT2) << 2) |
                (!digitalRead(PORT3) << 3) |
                (!digitalRead(PORT4) << 4) |
                (!digitalRead(PORT5) << 5) |
                (!digitalRead(PORT6) << 6) |
                (!digitalRead(PORT7) << 7)) & ~lastRead);
  delay(2);
}
