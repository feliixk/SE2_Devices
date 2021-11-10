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
        String strFileContents = "";
        //Not implemented yet
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);

        if (port.openPort()) {
            // wait after connecting, so arduino/xbee bootloader can finish
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }


        }

            System.out.println(">Comms started, port: " + port.getSystemPortName());

            PrintWriter output = new PrintWriter(port.getOutputStream());
            BufferedInputStream bis = new BufferedInputStream(port.getInputStream());
            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            ByteArrayOutputStream buf = new ByteArrayOutputStream();

            InputStream is = null;
            BufferedReader br = null;
            //output.print("p000"); //FUCK YEAH this owrks


            boolean loop;
            boolean loop2;

            try {


                is = System.in;
                br = new BufferedReader(new InputStreamReader(is));

                String inString = null;


                loop = true;
                loop2 = true;


                System.out.println("> Sent command " + command + " to Arduino");


                //uncomment this, when trying with arduino IRL
                output.print(command);
                output.flush();
                //System.out.println();
                //System.out.print(bis.readAllBytes());

                byte[] contents = new byte[1024];

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }

                int bytesRead = 0;
                while (loop2) {
                    try {
                        bytesRead = bis.read(contents);
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


            } catch (Exception e) {
//            e.printStackTrace();
            }

        return strFileContents;
    }
}
