import com.google.gson.Gson;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class InternalServer {
    private static WebSocket webSocket;
    private static String SERVER_PATH = "ws://85.197.159.150:1338/arduino";
    //private static String SERVER_PATH = "ws://85.197.159.131:1337/arduino; //bogges IP
    private static Gson gson;
    private static SerialComm serialComm = new SerialComm();

    public static void main(String[] args) {
        gson=new Gson();
        connectWebsocket();
    }

    private static void connectWebsocket() {
        OkHttpClient client = new OkHttpClient();
        System.out.println("connect: " + SERVER_PATH);
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new SocketListener());
        new Thread(() -> serialComm.reader()).start();
    }
    public void SendMessage(String command){ //  när ni ska skicka till oss direkt behöver inte ändras alls.
        ArrayList<String> info = new ArrayList<>();
        info.add(command+"-ok"); // måste göra såhär
        String text123 = gson.toJson(info); // gör om till ett objekt som kan skickas
        webSocket.send(text123); // skicka  tillbaka
        System.out.println("Message sent");

    }

    //SocketListener House
    private static class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            System.out.println("Socket Connection successful");
            System.out.println("onOpen Thread id: " + Thread.currentThread().getId());

        }
        //receive method
        @Override
        public void onMessage(WebSocket webSocket, String text) {  //// Det är här ni ska pilla era muppar
            super.onMessage(webSocket, text); // bry er inte
            System.out.println("onMessage Method"); // bry er inte
            System.out.println(webSocket.request()); // bry er inte

            ArrayList<String> info  = (ArrayList<String>) gson.fromJson(text,ArrayList.class); // hämtar en arraylist med all info

            // make loop for bigger list of commands ---------------
            String[] response = info.get(0).split("-"); // splitar stringer för att få olika detaljer om meddelandet ni behöver inte bry er om detta

            String command = response[1]; // commandot ni ska använda

            //FelixPlaceholder fp = new FelixPlaceholder();
            serialComm.sendCommand(command);

            ArrayList<String> responseList = new ArrayList<String>();
            responseList.add(serialComm.getResponse());

            System.out.println("Response from arduino to server: " + responseList.get(0));


            info.set(0,info.get(0) +"-"+ responseList.get(0)); // omdet går egenom

            //info.set(0,info.get(0) + "-fail"); // om det inte går egenom

            String text123 = gson.toJson(info); // gör om till ett objekt som kan skickas
            webSocket.send(text123); // skicka  tillbaka
            System.out.println("onMessage END");
        }

    }


//        @Override
//        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
//            super.onFailure(webSocket, t, response);
//            connectWebsocket();
//        }

}