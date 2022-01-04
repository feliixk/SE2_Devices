/*
 * Written by Simon Sörensen @ Kristianstad University
 */

#include <SoftwareSerial.h>
SoftwareSerial XBee(0, 1);
bool cont = true; // global var for alarms
bool cont0 = true; // global var for burglar alarm (and window)
bool cont1 = true; // global var for fire alarm
bool cont2 = true; // ... waterleakage
bool cont3 = true; // ... power outage 
bool cont4 = true; // global var for automatic light override
bool cont5 = true; // global var for automatic indoor heating
bool cont6, cont8 = true; // prevents spamming of outside light
bool cont7, cont9 = true; // prevents spamming of heating functionality
volatile long currentMillis = 0;
volatile long previousMillis = 0;
volatile long stoveOnTime = 0;
volatile long previousStoveOnTime = 0;
volatile long currentStoveMillis = 0;
volatile long currentTempMillis = 0;
volatile long previousTempMillis = 0;
const long tempCheckIntervalMS = 10000;
const long lightCheckIntervalMS = 4000; 
float desiredTemp = 20.0;

void setup()
{
  XBee.begin(9600);
  mux(1,1,0,0); // timer1 has to be initially turned off
  delay(500);
  mux(1,1,1,1); // outdoor lighting ...
  delay(500);
  mux(1,1,0,1); // heating element ...
  delay(500);
}

void loop()
{
  systemsChecker();
  xbeeChecker();
}

void writeD()
{
  while (XBee.available() < 2)
    ; 
  char pin = XBee.read(); // regularly used variables can be accessed by pointers/references instead
  char in = XBee.read();
  char hl = ASCIItoHL((int)in);
  String pinString  = "d";
  pinString += String(pin);
  String valueString = String(in);

  response(pinString, valueString);

  pin = ASCIItoInt(pin);
  pinMode(pin, OUTPUT); 
  digitalWrite(pin, hl); 
}

void writePWM(char c)
{
 while(XBee.available() < 3)
    ; 
    if(c == 'p')
    {
      int value = ASCIItoInt(XBee.read()) * 100; 
      value += ASCIItoInt(XBee.read()) * 10;     
      value += ASCIItoInt(XBee.read());          
      value = constrain(value, 0, 255); 

      String valueRes;
      if(value < 10)
      {
        valueRes = "00" + String(value);
      }
      else if(value < 100)
      {
        valueRes = "0" + String(value);
      }
      else
      {
        valueRes = String(value);
      }
      response("p", valueRes);
      
      pinMode(10, OUTPUT); 
      analogWrite(10, value); 
    }
    else if(c == 't')
    {
      // not yet tested
      String rr = String(XBee.read());
      rr += String(XBee.read());
      rr += ".";
      rr += String(XBee.read());
      
      response("t", rr);

      desiredTemp = rr.toFloat();
    }
}

void writeM()
{
  while (XBee.available() < 4)
    ;
  char h11 = XBee.read();
  char h22 = XBee.read();
  char h33 = XBee.read();
  char h44 = XBee.read();
  int h1 = ASCIItoHL((int)h11);
  int h2 = ASCIItoHL((int)h22);
  int h3 = ASCIItoHL((int)h33);
  int h4 = ASCIItoHL((int)h44);

  String r = String(h11) + String(h22) + String(h33) + String(h44);
  response("m", r);

  checkMuxCommands(h11, h22, h33, h44);
  
  mux(h1, h2, h3, h4);
}

void checkMuxCommands(char h11, char h22, char h33, char h44)
{
  // commands här som overridar saker om vi vill ha det
}

void writeA()
{
  while (XBee.available() < 4)
    ; 
  char pin = XBee.read();
  int value = ASCIItoInt(XBee.read()) * 100; 
  value += ASCIItoInt(XBee.read()) * 10;     
  value += ASCIItoInt(XBee.read());          
  value = constrain(value, 0, 255); 

  response("w" + String(pin), String(value));

  pin = ASCIItoInt(pin);
  pinMode(pin, OUTPUT); 
  analogWrite(pin, value);
}

void readD()
{
  while (XBee.available() < 1)
    ;
  char pin = XBee.read(); 
  pin = ASCIItoInt(pin); 
  
  if(pin == 9)
  {
    pinMode(pin, INPUT);
    response("", String(tempconverter(digitalRead(pin))));
  }
  else if(pin == 8)
  {
     // not yet tested
    response(String(pin), String(stoveOnTime));
  } 
  else 
  {
    pinMode(pin, INPUT);
    response("", String(digitalRead(pin)));
  }
}

void readA()
{
  while (XBee.available() < 1)
    ; 
  char pin = XBee.read(); 
  pin = ASCIItoInt(pin); 
  pinMode(pin, INPUT);
  if(pin == 1 || pin == 2) 
  {
    response("", String(tempconverter(analogRead(pin))));
  } 
  else if(pin == 3)
  {
    response("", String(lightconverter(analogRead(pin))));
  }
  /*
  else if(pin == 0)
  {
    LÄGG TILL OM DET BEHÖVS SPECIELL FUNKTIONALITET HÄR FÖR ATT KOLLA ELFÖRBRUKNING
  }
  */
  else {
    response("", String(analogRead(pin))); 
  }
}

int ASCIItoHL(char c)
{
  if ((c == '0') || (c == 0) || (c == 'L') || (c == 'l'))
    return LOW;
  else if ((c == '1') || (c == 1) || (c == 'H') || (c == 'h'))
    return HIGH;
  else
    return -1;
}

int ASCIItoInt(char c)
{
  if ((c >= '0') && (c <= '9'))
    return c - 0x30; 
  else if ((c >= 'A') && (c <= 'F'))
    return c - 0x37; 
  else if ((c >= 'a') && (c <= 'f'))
    return c - 0x57; 
  else
    return -1;
}

void mux(int a, int b, int c, int d)
{
  pinMode(12, OUTPUT);
  pinMode(13, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(8, OUTPUT); 
  digitalWrite(12, a);
  digitalWrite(13, b);
  digitalWrite(11, c);
  digitalWrite(8, d);
}

float tempconverter(int val)
{
  float mv = ( val/1024.0)*5000;
  float cel = mv/10;
  return cel;
}

int lightconverter(int light)
{
  return map(light, 0, 1023, 0, 100);
}

void response(String pin, String value)
{
  XBee.print(pin);
  XBee.print(value);
}

void burglaryCheck(int pin, bool cont0)
{
  if(cont0){
    soundwhenLOW(pin, "bAlarm"); 
  }
}

void windowCheck(int pin, bool cont0)
{
  if(cont0){
    soundwhenHIGH(pin, "wAlarm");
  }
}

void fireAlarmCheck(int pin, bool cont1)
{
  if(cont1){
    soundwhenHIGH(pin, "fAlarm"); 
  }
}

void waterAlarmCheck(int pin, bool cont2)
{
  if(cont2){
    soundwhenHIGH(pin, "wLeakage");
  }
}

void stoveCheck()
{
  pinMode(5, INPUT);
  currentStoveMillis = millis();
  if(digitalRead(5) == 1)
  {
    if(previousStoveOnTime == 0)
    {
      stoveOnTime = 0;
      previousStoveOnTime = currentStoveMillis;
    }
    else
    {
      stoveOnTime += (currentStoveMillis - previousStoveOnTime);
      previousStoveOnTime = currentStoveMillis; 
    }
    if(stoveOnTime >= 7200000)
    {
      response("", "sAlarm");
      stoveOnTime = 0;
      previousStoveOnTime = 0;
    }
  }
  else if(digitalRead(5) == 0)
  {
    stoveOnTime = 0;
    previousStoveOnTime = 0;
  }
}

void tempCheck()
{
  pinMode(1, INPUT);
  currentTempMillis = millis();
  if(currentTempMillis - previousTempMillis >= tempCheckIntervalMS && cont5)
  {
    previousTempMillis = currentTempMillis;
    if(tempconverter(analogRead(1)) < (desiredTemp - 1.0))
    {
      if(cont7)
      {
        mux(0,1,0,1);
        cont7 = false;
        cont9 = true;
        response("", "ihOn");
      } 
    }
    else
    {
      if(cont9)
      {
        mux(1,1,0,1);
        cont9 = false;
        cont7 = true;
        response("", "ihOff"); 
      } 
    } 
  }
}

void lightCheck()
{
  pinMode(3, INPUT);
  currentMillis = millis();
  if(currentMillis - previousMillis >= lightCheckIntervalMS && cont4)
  {
    previousMillis = currentMillis;
      if(lightconverter(analogRead(3)) < 50)
      {
        if(cont6)
        {
         mux(0,1,1,1);
         cont6 = false;
         cont8 = true;
         response("", "outOn"); 
        }
      } 
      else
      {
        if(cont8)
        {
         mux(1,1,1,1);
         cont8 = false;
         cont6 = true;
         response("", "outOff"); 
        }
      }
  }
}

void soundwhenHIGH(int pin, String r){
  // sätt in intervaller, så den inte spammar digitalRead
  if(digitalRead(pin) == 1 && cont){
    mux(1,0,0,0);
    cont = false;
    response("", r);
  } 
}

void soundwhenLOW(int pin, String r){
  // sätt in intervaller, så den inte spammar digitalRead
  if(digitalRead(pin) == 0 && cont){
    mux(1,0,0,0);
    cont = false;
    response("", r);
  }
}

void trueOrNah(int trueOrNah)
{
  if(trueOrNah == '1'){
    cont = true;
  } 
  else if(trueOrNah == '0')
  {
    cont = false;
    mux(0,0,0,0);
  }
}

void alarmToggle()
{
  while (XBee.available() < 1)
    ;
  char onOffchar = XBee.read();
  trueOrNah(onOffchar);
  String sendMeBack = String("b") + String(onOffchar);
  response("", sendMeBack);
}

void individualToggle()
{
  while (XBee.available() < 2)
    ;
  char type = XBee.read();
  char contOrNot = XBee.read();

  if(type == '0')
  {
    if(contOrNot == '0')
    {
      cont0 = false;
      mux(0,0,0,0);
    } 
    else if(contOrNot == '1')
    {
      cont0 = true;
    }
  } 
  else if(type == '1')
  {
    if(contOrNot == '0')
    {
      cont1 = false;
      mux(0,0,0,0);
    } 
    else if(contOrNot == '1')
    {
      cont1 = true;
    }
  } 
  else if(type == '2')
  {
    if(contOrNot == '0')
    {
      cont2 = false;
      mux(0,0,0,0);
    } 
    else if(contOrNot == '1')
    {
      cont2 = true;
    }
  }
  else if(type == '3')
  {
    if(contOrNot == '0')
    {
      cont3 = false;
      mux(0,0,0,0);
    } else if(contOrNot == '1')
    {
      cont3 = true;
    }
  }
  else if(type == '4')
  {
    if(contOrNot == '0')
    {
      cont4 = false;
      //mux(1,1,1,1);
    }
    else if(contOrNot == '1')
    {
      cont4 = true;
    }
  }
  else if(type == '5')
  {
    if(contOrNot == '0')
    {
      cont5 = false;
      //mux(1,1,0,1);
    }
    else if(contOrNot == '1')
    {
      cont5 = true;
    }
  }

  String qwerty = String("i") + String(type) + String(contOrNot);
  response("",qwerty);
}

// checks the XBee for commands
void xbeeChecker(){
  if (XBee.available())
  {
    char c = XBee.read();
    switch (c)
    {
    case 'w':         
      writeA(); 
      break;
    case 'd':    
      writeD(); 
      break;
    case 'r':           
      readD();  
      break;
    case 'a':    
      readA();  
      break;
    case 'm':
      writeM();
      break;
    case 'p':
    case 't':
      writePWM(c);
      break;
    case 'b':
      alarmToggle();
      break;
    case 'i':
      individualToggle();
      break;  
    }
  }
}

// checks the internal house systems for alarm
void systemsChecker()
{
  /*
  if(cont)
  {
   // alla alarm?
  }
  */
  
  burglaryCheck(3, cont0);
  windowCheck(6, cont0);
  fireAlarmCheck(2, cont1);
  waterAlarmCheck(4, cont2);
  lightCheck();
  stoveCheck();
  tempCheck(); 
}
