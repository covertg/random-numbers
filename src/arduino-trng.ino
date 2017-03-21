#include <EventManager.h>

/* Concept inspired by http://www.fourmilab.ch/hotbits/how3.html - John Walker
   Don't forget to hack EventManager to use unsigned long instead of int as its parameter
   Some lines can be uncommented to print average bits/sec and/or print in decimal instead of binary
*/

EventManager eventMan;

unsigned char counts, bits, flipper;
unsigned char randByte[8]; // 8 random bits form a random byte
unsigned long times[4]; // recorded time of each count ... max value 2^32-1 (not meant to run for >49 days)
unsigned long diff1, diff2; // time difference between counts

unsigned long start, totalbits; // used to monitor bits/sec

void setup() {
  Serial.begin(9600);
  attachInterrupt(digitalPinToInterrupt(2), pinEvent, RISING); // execute pinEvent() whenever pin 2 (connected to counter) is rising (i.e pulse)
  eventMan.addListener(0, processCount); // processCount() will run whenever a queued event w/ id 0 is executed
  start = millis();
}

// ISR called when pin 2 detects a pulse - queues a processCount() event (id 0) and passes the current time in milliseconds
void pinEvent() {
  eventMan.queueEvent(0, millis());
}

// main loop; simply processes the oldest event in the queue - in this case, always runs processCount() if there's been a count
void loop() {
  eventMan.processEvent(); // Note that millis() has already been recorded by the ISR.
}

void processCount(int eventID, unsigned long t) {
  // millis() doesn't advance during interrupt, and sometimes processCount() is called with same system time if the counter is pulsing rapidly. ignore these instances
  if (counts > 0 && times[counts - 1] == t) {
    return;
  }
  times[counts++] = t; // otherwise, store system time of count in times[4]
  if (counts >= 4) { // every 4 valid counts, compute the differences between times
    diff1 = times[1] - times[0];
    diff2 = times[3] - times[2];
    if (diff1 != diff2) { // we will use the inherent randomness in these differences to generate bits (see below) - BUT ignore if diffs happen to be equal
      generateBit();
    }
    counts = 0;
  }
}

void generateBit() {
  // 'flipper' is a bit to address potential non-randomness that may creep in (bias in measuring equipment/method, etc)
  // if flipper = 0 it has no effect; if flipper = 1, it will flip the generated bit
  randByte[bits++] = (diff1 > diff2) ^ flipper; // if diff1 > diff2, generate 1 (with respect to flipper), and vice versa
  if (bits >= 8) {
    printByte(); // after 8 bits are generated, send them through serial as a byte
    bits = 0;
  }
  flipper ^= 1; // xor operation to flip the flipper bit
  
  // uncomment these lines to measure average bits/sec
  //totalbits++;
  //if (totalbits > 400) {
  //  printStats();
  //  totalbits = 0;
  //  start = millis();
  //}
}

void printByte() {
  //String s = ""; // uncomment these lines to print in decimal
  for (int i = 7; i >= 0; i--) {
    Serial.print(randByte[i]);
    //s += randByte[i];
  }
  //Serial.println(strtol(s.c_str(), NULL, 2));
}

void printStats() {
  float timeElapsed = (millis() - start) / 1000.0f;
  Serial.print("bits/sec over ");
  Serial.print(timeElapsed);
  Serial.print(" sec: ");
  Serial.println(totalbits / timeElapsed);
}
