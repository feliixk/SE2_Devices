import com.fazecast.jSerialComm.SerialPort;

import java.io.*;

public class SerialComm {

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

            PrintWriter output = new PrintWriter(port.getOutputStream());
            BufferedInputStream bis = new BufferedInputStream(port.getInputStream());
            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            ByteArrayOutputStream buf = new ByteArrayOutputStream();

            boolean loop;
            boolean loop2 = true;

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            output.print("m0111");
            output.flush();

            byte[] contents = new byte[1024];

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }

            int bytesRead = 0;
            while (loop2) {
                try {
                    bytesRead = bis.read(contents);

                    if (bytesRead == 0) {
                        loop2 = false;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                    loop2 = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                strFileContents += new String(contents, 0, bytesRead);
            }


            InputStream is = null;
            BufferedReader br = null;
            //output.print("p000");


            try {


                is = System.in;
                br = new BufferedReader(new InputStreamReader(is));

                String inString = null;

                while ((inString = br.readLine()) != null) {

                    loop = true;
                    loop2 = true;

                    if (inString.equalsIgnoreCase(("disconnect"))) {
                        break;
                    }

                    System.out.println("> Sent command " + inString + " to Arduino");


                    //uncomment this, when trying with arduino IRL
                    output.print(inString);
                    output.flush();

                    contents = new byte[1024];

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }

                    bytesRead = 0;
                    while (loop2) {
                        try {
                            bytesRead = bis.read(contents);

                            if (bytesRead == 0) {
                                loop2 = false;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                            }
                            loop2 = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        strFileContents += new String(contents, 0, bytesRead);
                    }

                    System.out.println(strFileContents);

                    System.out.println("------------------");
                    strFileContents = "";
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            // disconnect from the serial port
            port.closePort();
            System.out.println("Conn closed");
        }

    }

    public String sendCommand(SerialPort port, String command) {
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
            output.print(command);
            output.flush();

            //debug sout
            System.out.println("> Sent command " + command + " to Arduino");

            // wait 100 ms for response.
            Thread.sleep(100);

            //read response from arduino
            byte[] contents = new byte[1024];
            int bytesRead = 0;
            while (readyToReadResponse) {
                try {
                    bytesRead = bis.read(contents);
                    Thread.sleep(100);
                    readyToReadResponse = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                response += new String(contents, 0, bytesRead);
            }

            //debug response printout
            System.out.println(">Response code: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response; // return response from arduino to server
    }
}
