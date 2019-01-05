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

#define LED0_IN   A0      // PC0
#define LED1_IN   A1      // PC1
#define LED2_IN   A2      // PC2
#define LED3_IN   A3      // PC3
#define LED4_IN   A4      // PC4

#define LED0_OUT  2       // PD2
#define LED1_OUT  3       // PD3
#define LED2_OUT  4       // PD4
#define LED3_OUT  5       // PD5
#define LED4_OUT  6       // PD6

#define SIG0_OUT  7       // PD7
#define SIG1_OUT  8       // PB0
#define SIG2_OUT  9       // PB1
#define SIG3_OUT  10      // PB2
#define SIG4_OUT  11      // PB3

#define ACTIVE_LED 12                   // flashes every 500 times through the loop
#define TRIGGERED_LED LED_BUILTIN       // lights when a beam is broken

uint8_t led_out[NUM_LEDS] = { LED0_IN, LED1_IN, LED2_IN, LED3_IN, LED4_IN };
uint8_t led_in[NUM_LEDS] = { LED0_OUT, LED1_OUT, LED2_OUT, LED3_OUT, LED4_OUT };
uint8_t sig_out[NUM_LEDS] = { SIG0_OUT, SIG1_OUT, SIG2_OUT, SIG3_OUT, SIG4_OUT };

#define LED_ON_VALUE  300

boolean active = false;
int active_count = 0;

void setup() {
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(ACTIVE_LED, OUTPUT);
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
    
    digitalWrite(led_out[pin], v);
    delay(1);
    r = digitalRead(led_in[pin]);
    res = res && (v == r);    // res is true while v always matches sensorValue
  }
  digitalWrite(led_out[pin], LOW);      // leave it off to reduce current flow
  return res;
}

/*
 * This shoudl be a Linear Feedback Shift Register to ensure that there is no interference
 * with other beam breaks. For now a simple on-off pattern will do.
 */
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
  }
  digitalWrite(TRIGGERED_LED, any);
  if (active_count++ == 10) {
    active = !active;
    active_count = 0;
    digitalWrite(ACTIVE_LED, active);
  }
}