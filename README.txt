	       ***Protocol by Simon Sörensen for arduino controlled devices***
*********************************************************************************************
LAST UPDATED: 2021-11-11
---------------------------------------------------------------------------------------------
Send corresponding strings to devices-server for ACTIVATION.
Ex: turning on the outdoor lighting is done by sendig string "m0111" to devices-server

Outdoor lighting on/off              --> m0111/m1111 *
Timer 1 on/off                       --> m0100/m1100
Heating element on/off               --> m0101/m1101
Heating element wind on/off 	     --> m0110/m1110
Indication burlgar alarm lamp on/off --> m0011/m1011
Indoor lighting on/off               --> m0010/m1010
Timer 2 on/off 			     --> m0001/m1001
Sound on/off			     --> m1000/m0000 **

All alarms on/off                    --> b1/b0
Burglar alarm on/off                 --> i01/i00 (includes window check)
Fire alarm on/off                    --> i11/i10
Water leakage alarm on/off           --> i21/i20
Power outage alarm on/off            --> i31/i30 (not yet implemented)
----------------------------------------------------------------------------------------------
Send corresponding strings to devices-server for FEEDBACK.
Ex: checking the outdoor temperature is done by sending the string "d9" to the devices-server

Check outside temperature 	     --> d9
Check inside temperature	     --> a1
Check inside temperature attic       --> a2
Check LDR (light reader)	     --> a3

**Correspons to 1/0**
Fire alarm active/inactive 	     --> r2
Burglar alarm inactive/active	     --> r3
Waterleakage active/inactive	     --> r4
Stove on/off			     --> r5 
Window open/closed		     --> r6
Power outage active/inactive 	     --> r7 (not yet implemented)
----------------------------------------------------------------------------------------------
Send corresponding strings to devices-server for MODULATING INPUT AMOUNT (0-255 = 0-100%).
Ex: turning on the inside fan at roughly 50% speed is done by sending string "p127"

Fan 				     --> p[value 0-255]
----------------------------------------------------------------------------------------------
Additional information regarding devices.

As of (2021-11-11) alarms now do two things upon activation; they trigger the alarm sound
in the house (can be manually accessed by "m1000/m0000") and they also send a message to
the internal server that an alarm has been activated. This message can then be used to
inform further up the chain that an alarm has been activated. The arduino informs about
what alarm has gone off according to the scheme below:

Burglary alarm has gone off          --> "bAlarm" will be sent if activated by door opening
				     --> "wAlarm" will be sent if activated by window opening
Fire alarm has gone off              --> "fAlarm" will be sent
Water leakage has been detected      --> "wLeakage" will be sent

If an alarm is activated, it can either be turned off using "b0" (as seen in the ACTIVATION
part above) which turns off all alarms, or by using the alarms individual alarm activation
command such as "i00" for the burglary alarm. Alarms are by default on and will be off until
re-activation.

*Outdoor lighting is mainly controlled via a lightsensor that is checked at a certain
interval. This automatic light control overrides manually sent commands. This may be changed
for in future versions of the software.
**Turning on sound will immediately beep the alarm sound in the smart house without an actual
alarm being triggered. This may interfere with alarm functionality. Use at own risk.
----------------------------------------------------------------------------------------------
BEWARE!
There are more commands that can be sent, but has no useful functionality. Such commands could for
example be write commands for controllers that only support reading. Digital pin 2 can be read
by sending 'r2' as seen from the protocol above, however you can also send 'd21' to write value
1 to digital pin 2, but since pin 2 does not have any device accepting inputs, it is useless.
Do not send useless commands to the arduino.
