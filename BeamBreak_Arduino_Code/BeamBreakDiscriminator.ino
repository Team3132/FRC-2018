/*
 * This system takes five beam breaks, and removes false positives.
 * 
 * We oscillate the output LED and compare the sending pattern and the received pattern.
 * If they are identical then the beam is intact. Otherwise it is broken.
 */


/*
 * The five sensors are connected to A0-A4
 * The five LEDs are connected to 2-6
 * Each sensor has an individual output
 * The five outputs are conencted to 7-11
 */

#define NUM_LEDS 5
#define NUM_CHECK 5         // number of LEDs to check

#define LED0_IN   A0
#define LED1_IN   A1
#define LED2_IN   A2
#define LED3_IN   A3
#define LED4_IN   A4

#define LED0_OUT  2
#define LED1_OUT  3
#define LED2_OUT  4
#define LED3_OUT  5
#define LED4_OUT  6

#define SIG0_OUT  7
#define SIG1_OUT  8
#define SIG2_OUT  9
#define SIG3_OUT  10
#define SIG4_OUT  11

uint8_t led_in[NUM_LEDS] = { LED0_IN, LED1_IN, LED2_IN, LED3_IN, LED4_IN };
uint8_t led_out[NUM_LEDS] = { LED0_OUT, LED1_OUT, LED2_OUT, LED3_OUT, LED4_OUT };
uint8_t sig_out[NUM_LEDS] = { SIG0_OUT, SIG1_OUT, SIG2_OUT, SIG3_OUT, SIG4_OUT };

#define LED_ON_VALUE  300

void setup() {
  //Serial.begin(9600);
  pinMode(LED_BUILTIN, OUTPUT);
  for (uint8_t i = 0; i < NUM_LEDS; i++) {
    pinMode(led_in[i], INPUT);
    pinMode(led_out[i], OUTPUT);
    pinMode(sig_out[i], OUTPUT);
  }
}

boolean pinCheck(uint8_t pin) {
  boolean res = true;
  for (uint8_t i = 0; i < 8; i++) {
    boolean v = getNextBit();   // next "random" bit value
    boolean r;
    int sensorValue;
    
    digitalWrite(led_out[pin], v);
    sensorValue = analogRead(led_in[pin]);
    delay(5);
    sensorValue = analogRead(led_in[pin]);  // read a second time for better accuracy
    r = (sensorValue > LED_ON_VALUE);
    //Serial.print(sensorValue); Serial.print(",");
    res = res && (v == r);    // res is true while v always matches sensorValue
  }
  digitalWrite(led_out[pin], LOW);      // leave it off to reduce current flow
  //Serial.print("  ");
  return res;
}

boolean getNextBit() {      // first cut, just oscillate
  static uint8_t state = 0;

  state = !state;
  return state & 1;
}

void loop() {
  boolean any = false;
  for (uint8_t i = 0; i < NUM_CHECK; i++) {
    boolean p = pinCheck(i);
    any = any || p;
    digitalWrite(sig_out[i], p);
    //Serial.print(p?"1":"0");
  }
  digitalWrite(LED_BUILTIN, any);
  //Serial.println(any?" Y":" N");
}
