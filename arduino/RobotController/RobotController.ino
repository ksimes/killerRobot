/*
  HC-SR04 Ping distance sensor:
  VCC to arduino 5v
  GND to arduino GND
  Echo to Arduino pin x
  Trig to Arduino pin y

  This sketch originates from Virtualmix: http://goo.gl/kJ8Gl
  Has been modified by Winkle ink here: http://winkleink.blogspot.com.au/2012/05/arduino-hc-sr04-ultrasonic-distance.html
  And modified further by ScottC here: http://arduinobasics.blogspot.com.au/2012/11/arduinobasics-hc-sr04-ultrasonic-sensor.html
  on 10 Nov 2012.
  converted to a class on 15th Oct 2016
*/

#define SERIAL_SPEED 115200 

// The positions of the three detectors
#define LEFT 0 
#define CENTRE 1
#define RIGHT 2

// The position in the array of the restrospecive Trigger and echo pins.
#define TRIGPIN 0 // Trigger Pin
#define ECHOPIN 1 // Echo Pin

int Pins[3][2] = { { 2, 3 },
                   { 4, 5 },
                   { 6, 7 } };


class RangeFinder {
  private :
    const int maximumRange = 200; // Maximum range needed
    const int minimumRange = 0; // Minimum range needed
    int trigPin;
    int echoPin;
    long duration, distance; // Duration used to calculate distance

  public :
    RangeFinder(int trig, int echo)
    {
      trigPin = trig;
      echoPin = echo;

      pinMode(trigPin, OUTPUT);
      pinMode(echoPin, INPUT);
    }

    long getDistance()
    {
      /* The following trigPin/echoPin cycle is used to determine the
        distance of the nearest object by bouncing soundwaves off of it. */
      digitalWrite(trigPin, LOW);
      delayMicroseconds(2);

      digitalWrite(trigPin, HIGH);
      delayMicroseconds(10);

      digitalWrite(trigPin, LOW);
      duration = pulseIn(echoPin, HIGH);

      //Calculate the distance (in cm) based on the speed of sound.
      distance = duration / 58.2;

      if (distance >= maximumRange || distance <= minimumRange) {
        return -1;
      }
      else {
        return distance;
      }
    }
};

RangeFinder *finder[3];

void setup() {
  Serial.begin (SERIAL_SPEED);

  finder[LEFT] = new RangeFinder(Pins[LEFT][TRIGPIN], Pins[LEFT][ECHOPIN]);
  finder[CENTRE] = new RangeFinder(Pins[CENTRE][TRIGPIN], Pins[CENTRE][ECHOPIN]);
  finder[RIGHT] = new RangeFinder(Pins[RIGHT][TRIGPIN], Pins[RIGHT][ECHOPIN]);
}

int distance[3];

void loop() {
  
  distance[LEFT] = finder[LEFT]->getDistance();
  distance[CENTRE] = finder[CENTRE]->getDistance();
  distance[RIGHT] = finder[RIGHT]->getDistance();

  /* Send the distances to the computer using Serial protocol in JSON format */
  String postData = "{\"Distance\": {";
  postData = postData + "\"left\":" + String(distance[LEFT]) + ",";
  postData = postData + "\"centre\":" + String(distance[CENTRE]) + ",";
  postData = postData + "\"right\":" + String(distance[RIGHT]) + "}";
  postData = postData + "}";

  Serial.println(postData);

  //Delay 1/10 of a second before next reading.
  delay(100);
}

