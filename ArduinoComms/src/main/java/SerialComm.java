import com.fazecast.jSerialComm.SerialPort;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SerialComm {

    private   byte[] sendingPack;
    private   byte[] receivingPack;
    SerialPort port;
    String response = null;
    String command = null;
    InternalServer server = new InternalServer();

    public SerialComm(){
        SerialPort[] portNames = SerialPort.getCommPorts();
        ArrayList<String> ports = new ArrayList<>();
        for (int i = 0; i < portNames.length; i++) {
            ports.add(portNames[i].getSystemPortName());
        }
        port = SerialPort.getCommPort(ports.get(2));
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

            boolean readyToReadResponse = true;
            String response = "";


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
            }catch (Exception e) {
            }

        } else{
            // disconnect from the serial port
            port.closePort();
            System.out.println("Conn closed");
        }
    }

    public void reader(){
        try {
            System.out.println("thread alive");
            while (true) {
                byte[] readBuffer;
                while (port.bytesAvailable() == 0)
                    Thread.sleep(20);


                while (port.bytesAvailable() > 0) {
                    readBuffer = new byte[port.bytesAvailable()];
                    int numRead = port.readBytes(readBuffer, readBuffer.length);
                    System.out.print("Read " + numRead + " bytes. ");

                    String s = new String(readBuffer, StandardCharsets.UTF_8);
                    System.out.println("Arduino response: " + s);
                    response = s;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        System.out.println("thread terminated");
    }

    public String getResponse(){
        String returnS = null;
        if(command == null && response != null) {
            server.SendMessage(response);
            response = null;
        }
        else if(command.equalsIgnoreCase(response)){
            command = null;
            returnS =  "ok";
        }else if(!command.equalsIgnoreCase(response)){
            returnS =  "not ok";
        }

        return returnS;

    }

    public String readCommands(){
        SerialPort comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
        byte[] readBuffer = new byte[1024];
        try {
            while (true)
            {
                while (comPort.bytesAvailable() == 0)
                    Thread.sleep(20);

                readBuffer = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                System.out.println("Read " + numRead + " bytes.");
            }
        } catch (Exception e) { e.printStackTrace(); }
        comPort.closePort();
        return readBuffer.toString();
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

    public static void sendData(SerialPort arduinoPort, byte[] buffer){
        byte[] sendingPack = new byte[6];
        byte[] receivingPack= new byte[36];

        arduinoPort.writeBytes(sendingPack,6,0);

        //System.out.println("Sending"+bytesToHexString(sendingPack));
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
