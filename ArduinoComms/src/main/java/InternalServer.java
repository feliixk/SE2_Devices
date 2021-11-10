import com.fazecast.jSerialComm.SerialPort;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class InternalServer {
    public static void main(String[] args) throws IOException {
        SerialPort[] ports = SerialPort.getCommPorts();
        ArrayList<String> portsS = new ArrayList<>();

        for (int i = 0; i < ports.length;i++){
            portsS.add(ports[i].getSystemPortName());
        }
        Socket client = new Socket("85.197.159.131", 2222);
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

        SerialComm sc = new SerialComm();
        ArrayList<String> responses = new ArrayList<>();
        SerialPort sPort = SerialPort.getCommPort(portsS.get(0));
        while (true) {
            try {

                ArrayList<String> commandList = (ArrayList<String>) ois.readObject();
                for(String s : commandList){
                    System.out.println(s);
                    String response = sc.sendCommand(sPort,s);
                    responses.add(response);
                    Thread.sleep(300);
                }
                String ok = "ok";
                ArrayList<String> temp = new ArrayList<>();
                temp.add(ok);
                oos.writeObject(temp);

//                oos.close();
//                ois.close();
//                bw.close();
//                br.close();
//                client.close();


            } catch (Exception ex) {
//                ex.printStackTrace();

            }
        }

    }
}