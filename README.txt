	       ***Protocol by Simon Sörensen for arduino controlled devices***
*********************************************************************************************
---------------------------------------------------------------------------------------------
Send corresponding strings to devices-server for activation.
Ex: turning on the outdoor lighting is done by sendig string "m0111" to devices-server

Outdoor lighting on/off              --> m0111/m1111
Timer 1 on/off                       --> m0100/m1100
Heating element on/off               --> m0101/m1101
Heating element wind on/off 	     --> m0110/m1110
Indication burlgar alarm lamp on/off --> m0011/m1011
Indoor lighting on/off               --> m0010/m1010
Timer 2 on/off 			     --> m0001/m1001
Sound on/off			     --> m1000/m0000

Fire alarm active/inactive 	     --> d21/d20
Burglar alarm active/inactive	     --> d30/d31
Waterleakage active/inactive	     --> d41/d40
Stove on/off			     --> d51/d50
Window open/closed		     --> d61/d60
Power outage active/inactive 	     --> d71/d70
----------------------------------------------------------------------------------------------
Send corresponding strings to devices-server for feedback.
Ex: checking the outdoor temperature is done by sending the string "d9" to the devices-server

Check outside temperature 	     --> d9
Check inside temperature	     --> a1
Check inside temperature attic       --> a2
Check LDR (light reader)	     --> a3
----------------------------------------------------------------------------------------------
Send corresponding strings to devices-server for modulating input amount (0-255 = 0-100%).
Ex: turning on the inside fan at roughly 50% speed is done by sending string "p127"

Fan 				     --> p[value 0-255]
----------------------------------------------------------------------------------------------

