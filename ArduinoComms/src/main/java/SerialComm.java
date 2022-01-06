import com.fazecast.jSerialComm.SerialPort;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SerialComm {

    SerialPort port;
    String response = null;
    String command = null;
    InternalServer server = new InternalServer();

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

    public void startCon(SerialPort port) throws IOException {

        String strFileContents = "";

        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);

        if (port.openPort()) {
            // wait after connecting, so arduino/xbee bootloader can finish
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }

            System.out.println(">Comms started, port: " + port.getSystemPortName());


            port.setBaudRate(9600);
            PrintWriter output = new PrintWriter(port.getOutputStream());
            BufferedInputStream bis = new BufferedInputStream(port.getInputStream());
            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            ByteArrayOutputStream buf = new ByteArrayOutputStream();


            InputStream is = null;
            BufferedReader br = null;
            //output.print("p000");


            try {


                is = System.in;
                br = new BufferedReader(new InputStreamReader(is));

                String inString;
                while ((inString = br.readLine()) != null) {
                    if (inString.equalsIgnoreCase(("disconnect"))) {
                        break;
                    }


                    System.out.println("> Sent command " + inString + " to Arduino");


                    //uncomment this, when trying with arduino IRL
                    byte[] writeContent;

                    writeContent = inString.getBytes();

                    port.writeBytes(writeContent, 6, 0);
                    output.flush();


                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }


                }
            } catch (Exception e) {
            }

        } else {
            // disconnect from the serial port
            port.closePort();
            System.out.println("Conn closed");
        }
    }

    public void reader() {
        try {
            System.out.println(">Reader thread alive");
            while (true) {
                byte[] readBuffer;
                while (port.bytesAvailable() == 0)
                {Thread.sleep(15);}


                while (port.bytesAvailable() > 0) {
                    readBuffer = new byte[port.bytesAvailable()];
                    int numRead = port.readBytes(readBuffer, readBuffer.length);
                    System.out.print("Read " + numRead + " bytes. ");

                    String s = new String(readBuffer, StandardCharsets.UTF_8);
                    System.out.println("Arduino response: " + s);
                    response = s;
                    //getResponse();

                    if(s.contains("Alarm")){
                        server.SendMessage(s);
                        System.out.println("Sending alarm to SERVER");
                    }else if(s.startsWith("a0")||s.startsWith("a2")){
                        double t = Double.parseDouble(s.substring(2));
//                       int f =(int)Math.round(t);
                        int f = (int)(t*10);
                       s=s.substring(0,2);
                       s+=String.valueOf(f);
                        System.out.println(s+"    --");
                        server.SendMessage(s);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("server seems to be offline");
        }

        System.out.println("thread terminated");
    }

    public String getResponse() {
        String returnS = null;
        if (command == null && response != null) {
            server.SendMessage(response);
            response = null;
            System.out.println("1");
        } else if (command.equalsIgnoreCase(response)) {
            command = null;
            returnS = "ok";
            System.out.println("2");
        } else if (!command.equalsIgnoreCase(response)) {
            returnS = "not ok";
            System.out.println("3");
        }

        return returnS;

    }

    public void sendCommand(String command) {
        this.command = command;
        String response = ""; //response from arduino
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        port.setBaudRate(9600);

        if (port.openPort()) {
            // wait after connecting, so arduino/xbee bootloader can finish
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        // get streams from serial port
        PrintWriter output = new PrintWriter(port.getOutputStream());
        BufferedInputStream bis = new BufferedInputStream(port.getInputStream());

        boolean readyToReadResponse = true;

        try {
            //send command to arduino
            // output.print(command);
            byte[] writeContent;

            writeContent = command.getBytes();
            System.out.println(writeContent);

            port.writeBytes(writeContent, 6, 0);
            //output.flush();


            //debug sout
            System.out.println("> Sent command " + command + " to Arduino");

            // wait 100 ms for response.
            Thread.sleep(100);

            //read response from arduino
//            byte[] contents = new byte[1024];
//            int bytesRead = 0;
//            while (readyToReadResponse) {
//                try {
//                    bytesRead = bis.read(contents);
//                    Thread.sleep(100);
//                    readyToReadResponse = false;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                response += new String(contents, 0, bytesRead);
//            }
//
//            //debug response printout
//            System.out.println(">Response code: " + response);
//
        } catch (Exception e) {
            e.printStackTrace();
        }

//        return response; // return response from arduino to server
    }

}
