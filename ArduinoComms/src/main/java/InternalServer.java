import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class InternalServer {
    public static void main(String[] args) {
        SerialComm sc = new SerialComm();

        while (true) {
            try {
                Socket client = new Socket(InetAddress.getLocalHost(), 10013);
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

                ArrayList<String> commandList = (ArrayList<String>) ois.readObject();
                for(String s : commandList){
                    String response = sc.sendCommand(s);
                    bw.write(response.trim()+"\n");
                    bw.flush();
                }


                ois.close();
                bw.close();
                br.close();
                client.close();


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}