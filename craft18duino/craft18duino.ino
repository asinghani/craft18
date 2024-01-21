#define PORT0 2
#define PORT1 3
#define PORT2 4
#define PORT3 5
#define PORT4 6

void setup() {
  digitalWrite(PORT0, LOW);
  digitalWrite(PORT1, LOW);
  digitalWrite(PORT2, LOW);
  digitalWrite(PORT3, LOW);
  digitalWrite(PORT4, LOW);
  Serial.begin(115200);
}

int lastRead = 0;
void loop() {
  int x = lastRead;
  while (Serial.available()) {
    x = Serial.read();
  }
  if (x != lastRead) {
    pinMode(PORT0, (x & (1 << 0)) ? OUTPUT : INPUT_PULLUP);
    pinMode(PORT1, (x & (1 << 1)) ? OUTPUT : INPUT_PULLUP);
    pinMode(PORT2, (x & (1 << 2)) ? OUTPUT : INPUT_PULLUP);
    pinMode(PORT3, (x & (1 << 3)) ? OUTPUT : INPUT_PULLUP);
    pinMode(PORT4, (x & (1 << 4)) ? OUTPUT : INPUT_PULLUP);
    lastRead = x;
  }
  Serial.write(((!digitalRead(PORT0) << 0) |
                (!digitalRead(PORT1) << 1) |
                (!digitalRead(PORT2) << 2) |
                (!digitalRead(PORT3) << 3) |
                (!digitalRead(PORT4) << 4)) & ~lastRead);
  delay(2);
}
