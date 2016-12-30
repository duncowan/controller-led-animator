/*
 * This is the firmware that runs on the controller. 
 *
 * It controls the loading of the previously set LED animation into RAM,
 * the saving of an uploaded animation into EEPROM, and the brightness 
 * of the LEDs based on the current animation stored in RAM.
 *
 * Note: This code was written for the Atmel ATmega328P microcontroller 
 *       running the Arduino 1.6.6 bootloader.
 *
 * @author Duncan Cowan
 */
#include <EEPROM.h>
#include <avr/wdt.h>

/*
 * This table remaps linear input values (the numbers we’d like to use; e.g. 127 = half brightness) 
 * to nonlinear gamma-corrected output values (numbers producing the desired effect on the LED; 
 * e.g. 36 = half brightness) [1]. This has to be done because the human perception of brightness follows 
 * an approximate power function, with greater sensitivity to relative differences between darker tones 
 * than between lighter ones [2]. In other words, if this table wasn't used, a LED starting off and fading 
 * on over a time period would look like it was fading on linearly until about half way through where it 
 * would seem to have reached it's maximum brightness. Because we want the LED to fade linearly over the 
 * entire time period, this table has to be used to account for the human perception of brightness.
 *
 * Sources:
 * [1] https://learn.adafruit.com/led-tricks-gamma-correction/the-quick-fix
 * [2] https://en.wikipedia.org/wiki/Gamma_correction#Explanation
 */
const uint8_t PROGMEM gammaCorrection[] = {
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  1,  1,  1,
    1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  2,  2,  2,  2,  2,  2,
    2,  3,  3,  3,  3,  3,  3,  3,  4,  4,  4,  4,  4,  5,  5,  5,
    5,  6,  6,  6,  6,  7,  7,  7,  7,  8,  8,  8,  9,  9,  9, 10,
   10, 10, 11, 11, 11, 12, 12, 13, 13, 13, 14, 14, 15, 15, 16, 16,
   17, 17, 18, 18, 19, 19, 20, 20, 21, 21, 22, 22, 23, 24, 24, 25,
   25, 26, 27, 27, 28, 29, 29, 30, 31, 32, 32, 33, 34, 35, 35, 36,
   37, 38, 39, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 50,
   51, 52, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 66, 67, 68,
   69, 70, 72, 73, 74, 75, 77, 78, 79, 81, 82, 83, 85, 86, 87, 89,
   90, 92, 93, 95, 96, 98, 99,101,102,104,105,107,109,110,112,114,
  115,117,119,120,122,124,126,127,129,131,133,135,137,138,140,142,
  144,146,148,150,152,154,156,158,160,162,164,167,169,171,173,175,
  177,180,182,184,186,189,191,193,196,198,200,203,205,208,210,213,
  215,218,220,223,225,228,231,233,236,239,241,244,247,249,252,255
};
// Defines the PWM enabled pins for each LED.
const int LED[4] = {3, 5, 6, 9};
// How many times the animation will be updated per second in microseconds.
// (default 12500 (80 times a second))
const int UPDATES_PER_SECOND_US = 12500;  

// Stores the number of timelines the current animation has.
// (i.e. how many LEDs the current animation uses).
byte numOfTimelines;
// Stores the time the current animation ends.
byte animEndTime;
// Stores how many sections are in each timeline.
byte numOfSectionsInTimeline[4];
// Stores the sections' data (i.e. the current animation).
unsigned int sections[400];
// Buffer to store the most recently uploaded animation.
byte bytesReceived[806];
// Stores the position in the sections array where 
// the current timeline's sections start.
// (e.g. if the first timeline contains 4 sections, 
// the offset for the first section in the second timeline
// would be 4.)
int offset = 0;
// Stores how far through the current animation we are.
int curTime = 0;

/*
 * First method to get called.
 */
void setup() {
    MCUSR = 0;
    Serial.begin(9600);

    // Load the stored animation.
    loadAnimation();
}

/*
 * Soft resets the microcontroller.
 */
void reset() {
    wdt_enable(WDTO_15MS);
    while(1);
}

/*
 * Load the animation that's stored in the EEPROM into RAM.
 */
void loadAnimation() {
    // Get the number of timelines in the animation.
    numOfTimelines = EEPROM.read(0);
    // Get the end time of the animation.
    animEndTime = EEPROM.read(numOfTimelines+1);

    // Get the number of sections in each timeline.
    for(byte b = 0; b < numOfTimelines; b++) {
        numOfSectionsInTimeline[b] = EEPROM.read(1+b);
        // Here offset is being used to temporarily store
        // the total number of sections in the animation.
        // This is done to save space in the RAM.
        offset += int(numOfSectionsInTimeline[b]);
    }

    // Get the section data for the animation.
    for(int i = 0; i < offset*2; i+=2)
        sections[i/2] = (EEPROM.read(i+(numOfTimelines+2)) << 8) |  EEPROM.read((i+1)+(numOfTimelines+2));

    // Turn off all the LEDs.
    for(byte i = 0; i < numOfTimelines; i++) {
        pinMode(LED[i], OUTPUT);
        digitalWrite(LED[i], LOW);
    }
}

/*
 * Accepts, validates and sotres an uploaded animation into EEPROM.
 */
void getAnimation() {
    // Only accept data if an 'R' is sent first.
    if(serialRead(3000) == 'R') {
        // Get size of receved data.
        byte animSize = serialRead(3000);
        // Store the receved data in the buffer.
        for(int i = 0; i < animSize; i++) {
            byte b = 0;
            if((b = serialRead(3000)) == 255) {
                break;
            } else {
                bytesReceived[i] = b;
            }
        }
        
        // Send data back for verification.
        Serial.write(bytesReceived, animSize);  
        
        // If data is valid, save it to EEPROM and reset.
        if(serialRead(10000) == '1') {          
            for(int i = 0; i < animSize; i++)
                EEPROM.update(i, bytesReceived[i]);
            reset();
        }
    }
}

/*
 * Read serial data (1 byte) with a timeout.
 */
byte serialRead(unsigned int timeout) {
    unsigned long startTime = millis();
    while(Serial.available() < 1) {
        if((millis() - startTime) > timeout)
            // If there isn't at least 1 byte serial data when the timeout expires,
            // return the error code (255).
            return 255;
     }
     // If there is at least 1 byte serial data before the timeout expires,
     // return it.
     return Serial.read();
}

/*
 * Sets the brightness of the LEDs for the current time in the animation.
 * (i.e. it draws the current frame of animation).
 */
void updateAnimation() {
   offset = 0;
   for(byte t = 0; t < numOfTimelines; t++) {
      for(byte s = 0; s < numOfSectionsInTimeline[t]; s++) {
        // Get the data for section s in timeline t.
        int startOn = int(sections[s+offset]&0x01);
        int fade = int((sections[s+offset]>>1)&0x01);
        int startTime = int((sections[s+offset]>>2)&0x7F)*10;
        int endTime = int((sections[s+offset]>>9)&0x7F)*10;
        
        // If timeline t's corrosponding LED's brightness should be
        // updated at this time in the animation, update it.
        if(curTime >= startTime && curTime <= endTime) {
          int brightness = int((255.0/(endTime-startTime))*(curTime-startTime));
          analogWrite(LED[t], pgm_read_byte(&gammaCorrection[ (startOn*255) + fade*(brightness - (brightness*2*startOn)) ]));
        } 
        // Turn off timeline t's corrosponding LED if it shouldn't
        // be on at this time in the animation.
        if(curTime == endTime+1) {
            // ...Unless we are at the end of the animation and timeline t's corrosponding LED
            // is updated at time 0 of the animation.
            // This makes the looping of the animation seemless, without it there would
            // be a noticeable flash of any LED that is on at the very end and very start of
            // the animation.
            if(endTime == animEndTime*10 && int((sections[offset]>>2)&0x7F) == 0) {}
            else
                analogWrite(LED[t], 0);
        }
      }
      // Set the offset for the beginning of the next timeline.
      offset += numOfSectionsInTimeline[t];
    }
    // Increment the animation time, loop back when we get to the end.
    curTime = (curTime < (animEndTime*10+1)) ? curTime+1 : 0;
}

// Keeps track of time (in microseconds) since the animation was last updated.
unsigned long count = micros()+UPDATES_PER_SECOND_US*2;
/*
 * Loops forever.
 */
void loop() {
    // Update animation every 0.0125 seconds (80 times a second).
    if((micros()-count) >= UPDATES_PER_SECOND_US) {
        count = micros();
        updateAnimation();
    }

    // When serial data is available, treat it as a new animation.
    if(Serial.available() > 0)
        getAnimation();
}
