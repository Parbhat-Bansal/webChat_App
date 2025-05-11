package WebChat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client2 {

    private final String host = "localhost";
    private final int port = 8008;

    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    // Thread-safe queue to hold received messages
    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();

    public Client2() {
        try {
            this.socket = new Socket(host, port);
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder message = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                message.append(line);
            }
            String msg = message.toString();
            System.out.println("Received via HTTP: " + msg);

            if (msg.equalsIgnoreCase("QUIT")) {
                socket.close();
            } else {
                printWriter.println(msg);
            }

            String response = "Message sent: " + msg;
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private class ReceiveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder response = new StringBuilder();

            // Drain all available messages from queue
            while (!messageQueue.isEmpty()) {
                response.append(messageQueue.poll()).append("\n");
            }

            String res = response.toString().isEmpty() ? "No new messages." : response.toString();

            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, res.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(res.getBytes());
            os.close();
        }
    }

    public void receiveMessage() {
        Thread receiveThread = new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = bufferedReader.readLine()) != null) {
                    System.out.println("Coming Message: " + serverMessage);
                    messageQueue.offer(serverMessage);  // store message
                }
            } catch (IOException e) {
                System.out.println("Connection closed.");
            }
        });
        receiveThread.start();
    }

    public void startHttpServer() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8081), 0);
        httpServer.createContext("/send", new SendHandler());
        httpServer.createContext("/receive", new ReceiveHandler());
        httpServer.setExecutor(null);
        httpServer.start();
        System.out.println("HTTP Server started at http://localhost:8081");
    }

    public static void main(String[] args) throws IOException {
        Client2 client = new Client2();
        client.receiveMessage();
        client.startHttpServer();
    }
}
