import com.google.gson.Gson;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;

public class InternalServer {
    private static WebSocket webSocket;
    private static String SERVER_PATH = "[REDACTED]"
    private static Gson gson;
    private static SerialComm serialComm = new SerialComm();

    public static void main(String[] args) {
        gson = new Gson();
        connectWebsocket();
        serialComm.sendCommand("a2");
        serialComm.sendCommand("a0");
    }

    //Connecting to external webserver
    private static void connectWebsocket() {
        OkHttpClient client = new OkHttpClient();
        System.out.println("connect: " + SERVER_PATH);
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new SocketListener());
        new Thread(() -> serialComm.reader()).start();
        new Thread(() -> threadedTasks()).start();
    }

    //Method for sending messages from Arduino to external server
    public void SendMessage(String command) {
        ArrayList<String> info = new ArrayList<>();
        if (command.contains("Alarm") || command.equalsIgnoreCase("wLeakage")) {
            info.add(command + "-alarm"); // måste göra såhär
        } else if (command.contains("a0") || command.contains("a2")) {
            info.add(command.substring(0, 2) + "-" + command.substring(2));
        } else if (command.contains("p")) {
            info.add(command.substring(1));
        } else {
            info.add(command + "-ok");
        }
        String text123 = gson.toJson(info);
        webSocket.send(text123);
        System.out.println(">[sendMessage] Command: '" + text123 + "' sent to server!");
    }

    //SocketListener House, listen on external server
    private static class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            System.out.println(">Socket Connection successful!");

        }

        //receive method
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text); // bry er inte
            System.out.println("<------------------------------->");
            System.out.println(">onMessage Method entered");
            System.out.println(webSocket.request());

            ArrayList<String> info = (ArrayList<String>) gson.fromJson(text, ArrayList.class);

            // make loop for bigger list of commands ---------------
            String[] response = info.get(0).split("-");

            String command = response[1];

            //response[0];
            if (command.startsWith("p")) {
                if (command.length() == 2) {
                    command = "p00" + command.substring(1);
                } else if (command.length() == 3) {
                    command = "p0" + command.substring(2);
                }
            }
            serialComm.sendCommand(command);

            ArrayList<String> responseList = new ArrayList<String>();

            responseList.add(serialComm.getResponse());

            System.out.println("Response from arduino to server: " + responseList.get(0));
            if (command.contains("p") || command.contains("t")) {
                info.set(0, info.get(0) + "-" + command.substring(1));
            } else {
                info.set(0, info.get(0) + "-" + responseList.get(0));
            }

            String text123 = gson.toJson(info); // gör om till ett objekt som kan skickas
            webSocket.send(text123); // skicka  tillbaka
        }

    }

    // Automatic tasks to read temp and electricity consumption with polling
    private static void threadedTasks() {
        System.out.println(">Timer thread alive"
                );
        int pingTime = LocalTime.now().getMinute() + 5;
        while (true) {
            if (LocalTime.now().getMinute() == pingTime||LocalTime.now().getMinute()==pingTime+1) {
                System.out.println(">Sending temp & el auto");
                pingTime = LocalTime.now().getMinute() + 5;
                if (pingTime > 59) {
                    pingTime -= 60;
                }
                serialComm.sendCommand("a2");
                serialComm.sendCommand("a0");
            }
        }
    }

}
