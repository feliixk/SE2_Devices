import com.fazecast.jSerialComm.SerialPort;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SerialComm {

    SerialPort port;
    String response = null;
    String command = null;
    InternalServer server = new InternalServer();

    //Initializing serial communication interface
    public SerialComm() {
        try {
            SerialPort[] portNames = SerialPort.getCommPorts();
            ArrayList<String> ports = new ArrayList<>();
            for (int i = 0; i < portNames.length; i++) {
                ports.add(portNames[i].getSystemPortName());
            }
            port = SerialPort.getCommPort(ports.get(2));
        } catch (Exception e) {
            System.out.println("No serial communcation device found");
        }
    }

    //Method that runs in a separate thread to read all incoming messages from the Arduino
    public void reader() {
        try {
            System.out.println(">Reader thread alive");
            while (true) {
                byte[] readBuffer = null;
                while (port.bytesAvailable() == 0) {
                    Thread.sleep(150);
                }


                while (port.bytesAvailable() > 0) {
                    readBuffer = new byte[port.bytesAvailable()];
                    int numRead = port.readBytes(readBuffer, readBuffer.length);
                    System.out.print("Read " + numRead + " bytes. ");
                }
                if (readBuffer != null) {
                    String s = new String(readBuffer, StandardCharsets.UTF_8);
                    System.out.println("Arduino response: " + s);
                    response = s;

                    if (s.contains("Alarm")) {
                        server.SendMessage(s);
                        System.out.println("Sending alarm to SERVER");
                    } else if (s.contains("wLeakage")) {
                        server.SendMessage(s);
                        System.out.println("Sending water alarm to SERVER");
                    } else if (s.equalsIgnoreCase("m0110") || s.equalsIgnoreCase("m1110")) {
                        server.SendMessage(s);
                    } else if (s.startsWith("a0") || s.startsWith("a2")) {
                        double t = Double.parseDouble(s.substring(2));
//                       int f =(int)Math.round(t);
                        int f = (int) (t * 10);
                        s = s.substring(0, 2);
                        s += String.valueOf(f);
                        server.SendMessage(s);
                    } else if (s.equalsIgnoreCase("p000")) {
                        s.substring(2);
                    } else if (s.equalsIgnoreCase("m0111")) {
                        System.out.println("Sending out light ON");
                        server.SendMessage("m0111");
                    } else if (s.equalsIgnoreCase("m1111")) {
                        server.SendMessage("m1111");
                        System.out.println("Sending out light OFF");
                    }


                }
            }
        } catch (Exception e) {
            System.out.println("Error, no active connection to smart house");
        }

        System.out.println(">reader thread terminated");
    }

    // Method for getting responses from the Arduino
    public String getResponse() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String returnS = null;
        if (command == null && response != null) {
            server.SendMessage(response);
            response = null;
            System.out.println("Alarm OR Automatic lighting sent!");
        }
        if (command.equalsIgnoreCase(response)) {
            command = null;
            returnS = "ok";
            System.out.println("Ordinary command sent");
        } else if (!command.equalsIgnoreCase(response)) {
            returnS = "not ok";
            System.out.println("not ok response");
        }

        return returnS;

    }

    //Method for sending commands to Arduino
    public void sendCommand(String command) {
        this.command = command;
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        port.setBaudRate(9600);

        if (port.openPort()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        try {
            //send command to arduino
            byte[] writeContent;
            writeContent = command.getBytes();
            System.out.println(writeContent);
            port.writeBytes(writeContent, 6, 0);

            //debug sout
            System.out.println("> Sent command " + command + " to Arduino");


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
