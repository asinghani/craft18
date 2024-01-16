#define PORT0 21
#define PORT1 20
// TODO: PORT2-PORT7

void setup() {
  digitalWrite(PORT0, LOW);
  digitalWrite(PORT1, LOW);

  Serial.begin(115200);
}

int lastRead = 0;
void loop() {
  int x = lastRead;
  while (Serial.available()) {
    x = Serial.read();
  }
  if (x != lastRead) {
    pinMode(PORT0, (x & 1) ? OUTPUT : INPUT);
    pinMode(PORT1, (x & 2) ? OUTPUT : INPUT);
    lastRead = x;
  }
  Serial.write((!digitalRead(PORT0) | (!digitalRead(PORT1)<<1)) & ~lastRead);
  delay(25);
}
