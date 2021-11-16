
import java.io.*;
import java.util.ArrayList;

import com.fazecast.jSerialComm.SerialPort;

public class Main {

    static SerialComm sCom = new SerialComm();

    public static void main(String[] args) {
        System.out.println("Arduino Xbee SMART HOUSE v0.3 PRE-ALPHA");

        consoleDebugInput();
    }

    public static void consoleDebugInput() {

        SerialPort[] portNames = SerialPort.getCommPorts();
        ArrayList<String> ports = new ArrayList<>();

        System.out.println(">Console debug menu<");
        System.out.println(">Select COM PORT:");

        for (int i = 0; i < portNames.length; i++) {
            ports.add(portNames[i].getSystemPortName());
            System.out.println(i + 1 + ": " + ports.get(i));
        }

        BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
        int temp;

        try {
            temp = Integer.parseInt(bin.readLine());
            System.out.println("Selected port " + temp);
            sCom.startCon(SerialPort.getCommPort(ports.get(temp - 1)));
        } catch (Exception io) {
            io.printStackTrace();
        }



    }
}