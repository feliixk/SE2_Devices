/**
 * Code modified from and inspired by https://learn.sparkfun.com/tutorials/xbee-shield-hookup-guide/all
 * Last accessed 2021-10-04
 * Last modified 2021-10-11
 * Written by Simon Sörensen @ Kristianstad University
 */

#include <SoftwareSerial.h>
SoftwareSerial XBee(0, 1);

void setup()
{
  XBee.begin(9600); 
}

void loop()
{
  xbeeChecker();
}

void writeDPin()
{
  while (XBee.available() < 2)
    ; // Wait for pin and value to become available
  char pin = XBee.read();
  char hl = ASCIItoHL(XBee.read());

  XBee.print("Setting pin ");
  XBee.print(pin);
  XBee.print(" to ");
  XBee.println(hl ? "HIGH" : "LOW");

  pin = ASCIItoInt(pin); // Convert ASCCI to a 0-13 value
  pinMode(pin, OUTPUT); // Set pin as an OUTPUT
  digitalWrite(pin, hl); // Write pin accordingly
}

void writePWM()
{
 while(XBee.available() < 3)
    ; 
    
 int value = ASCIItoInt(XBee.read()) * 100; 
 value += ASCIItoInt(XBee.read()) * 10;     
 value += ASCIItoInt(XBee.read());          
 value = constrain(value, 0, 255); 

 XBee.print("Setting pin ");
 XBee.print(10);
 XBee.print(" to ");
 XBee.println(value);

 pinMode(10, OUTPUT); 
 analogWrite(10, value); 
}

void writeMpin()
{
  while (XBee.available() < 4)
    ;
  char h1 = ASCIItoHL(XBee.read());
  char h2 = ASCIItoHL(XBee.read());
  char h3 = ASCIItoHL(XBee.read());
  char h4 = ASCIItoHL(XBee.read());

  XBee.print("Setting mux to ");
  XBee.print(h1 + h2 + h3 + h4);

  pinMode(12, OUTPUT);
  pinMode(13, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(8, OUTPUT); 
  
  mux(h1, h2, h3, h4);
}

void writeAPin()
{
  while (XBee.available() < 4)
    ; 
  char pin = XBee.read();
  int value = ASCIItoInt(XBee.read()) * 100; 
  value += ASCIItoInt(XBee.read()) * 10;     
  value += ASCIItoInt(XBee.read());          
  value = constrain(value, 0, 255); 

  XBee.print("Setting pin ");
  XBee.print(pin);
  XBee.print(" to ");
  XBee.println(value);

  pin = ASCIItoInt(pin);
  pinMode(pin, OUTPUT); 
  analogWrite(pin, value);
}

void readDPin()
{
  while (XBee.available() < 1)
    ;
  char pin = XBee.read(); 
  XBee.print("Pin ");
  XBee.print(pin);
  pin = ASCIItoInt(pin); 
  
  if(pin == 9)
  {
    pinMode(pin, INPUT);
    XBee.print(" = "); 
    XBee.println(tempconverter(digitalRead(pin)));
  } else {
    pinMode(pin, INPUT);
    XBee.print(" = "); 
    XBee.println(digitalRead(pin));
  }
}

void readAPin()
{
  while (XBee.available() < 1)
    ; 
  char pin = XBee.read(); 
  XBee.print("Pin A");
  XBee.print(pin);
  pin = ASCIItoInt(pin); 
  
  if(pin == 1 || pin == 2)
  {
    XBee.print(" = ");
    XBee.println(tempconverter(analogRead(pin)));
  } else if(pin == 2)
  {
    XBee.print(" = ");
    XBee.println(tempconverter(analogRead(pin))); 
  }else {
    XBee.print(" = ");
    XBee.println(analogRead(pin)); 
  }
}

int ASCIItoHL(char c)
{
  // If received 0, byte value 0, L, or l: return LOW
  // If received 1, byte value 1, H, or h: return HIGH
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
    return c - 0x30; // Minus 0x30
  else if ((c >= 'A') && (c <= 'F'))
    return c - 0x37; // Minus 0x41 plus 0x0A
  else if ((c >= 'a') && (c <= 'f'))
    return c - 0x57; // Minus 0x61 plus 0x0A
  else
    return -1;
}

void mux(int a, int b, int c, int d){
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

void xbeeChecker(){
  if (XBee.available())
  {
    char c = XBee.read();
    switch (c)
    {
    case 'w':      
    case 'W':     
      writeAPin(); 
      break;
    case 'd':    
    case 'D':     
      writeDPin(); 
      break;
    case 'r':      
    case 'R':      
      readDPin();  
      break;
    case 'a':    
    case 'A':     
      readAPin();  
      break;
    case 'm':
    case 'M':
      writeMpin();
      break;
    case 'p':
    case 'P':
      writePWM();
      break;
    }
  }
}
