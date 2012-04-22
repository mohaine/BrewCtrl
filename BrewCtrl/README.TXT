First we need to program the Arduino.  We only need to do this once from any computer.

1) Download Arduino development environment (Arduino DE)
	http://arduino.cc/en/Main/Software
	
2) Extract the Arduino Zip to your desktop.	 You should now have a folder named something like arduino-1.0 on your desktop.

3) Download the Arduino OneWire Library.
	http://arduino.cc/playground/Learning/OneWire

4) Extract the Arduino OneWire Library into your arduino-1.0/libraries folder.  You should now have a arduino-1.0/libraries/OneWire folder

6) Extract the BrewCtrl Zip to your desktop.

7) Launch the Arduino DE arduino-1.0/arduino.exe

8) Select File->Open in the Arduino DE.  Browse to and select the BrewCtrl/BrewCtrlArduino/BrewCtrlArduino.ino file.

9) Compile and Download the program to your arduino board.  To get this to work make sure you have your board and com port selected under the tools menu in the Arduino DE.

Now you can lanuch the Control interface on any PC

1) Download and install a java runtime  (JRE)  
	http://www.oracle.com/technetwork/java/javase/downloads/index.html
	
2) Launch BrewCtrl/run.cmd  If you have everything hooked up, you should see the status go to OK.  Exit the app.


3) After exiting the app, you will have a BrewControllerConfig.json file. 

This file has your brewery layout, your predefined mash schedules and automaticly controls and other configuration options.

If you are having issues connecting to the arduino board, you will want to add the selected COM port from the Arduino DE to the commPorts selection of this file.



