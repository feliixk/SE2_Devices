/**
 * Last accessed 2021-10-04
 * Last modified 2021-10-22
 * Written by Simon Sörensen @ Kristianstad University
 */

#include <SoftwareSerial.h>
SoftwareSerial XBee(0, 1);
bool cont = true; // global cont var for alarm checkers

void setup()
{
  XBee.begin(9600);
}

void loop()
{
  systemsChecker();
  XBeeChecker();
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

void writePWM()
{
 while(XBee.available() < 3)
    ; 
    
 int value = ASCIItoInt(XBee.read()) * 100; 
 value += ASCIItoInt(XBee.read()) * 10;     
 value += ASCIItoInt(XBee.read());          
 value = constrain(value, 0, 255); 

 response("p", String(value));

 pinMode(10, OUTPUT); 
 analogWrite(10, value); 
}

void writeM()
{
  while (XBee.available() < 4)
    ;
  // maybe unnessersary usage of space just for sending a response but arduino is only at about 29% memusage so far
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

  pinMode(12, OUTPUT);
  pinMode(13, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(8, OUTPUT); 

  if(h11 == '0' && h22 == '0' && h33 == '0' && h44 == '0')
  {
    cont = true;
  }
  
  mux(h1, h2, h3, h4);
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
  } else {
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
  
  if(pin == 1 || pin == 2) 
  {
    response("", String(tempconverter(analogRead(pin))));
  } 
  else if(pin == 3)
  {
    response("", String(lightconverter(analogRead(pin))));
  }
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

void burglaryCheck(int pin){
  soundwhenLOW(pin);
}

void windowCheck(int pin){
  soundwhenHIGH(pin);
}

void fireAlarmCheck(int pin){
 soundwhenHIGH(pin);
}

void soundwhenHIGH(int pin){
  if(digitalRead(pin) == 1 && cont){
    mux(1,0,0,0);
    cont = false;
  } 
}

void soundwhenLOW(int pin){
  if(digitalRead(pin) == 0 && cont){
    mux(1,0,0,0);
    cont = false;
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

// checks the XBee for commands
void XBeeChecker(){
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
      writePWM();
      break;
    case 'b':
      alarmToggle();
      break;
    }
  }
}

// checks the internal house systems for alarm
void systemsChecker()
{
  burglaryCheck(3);
  windowCheck(6);
  fireAlarmCheck(2);
}
