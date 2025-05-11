package WebChat;
import java.io.*;
import java.net.Socket;
import java.util.Queue;

public class ProxyServer extends Thread {
    Socket socket;
    Queue<String> inQueue;  // messages from the client
    Queue<String> outQueue; // messages to the client

    public ProxyServer(Socket s, Queue<String> inQueue, Queue<String> outQueue) {
        this.socket = s;
        this.inQueue = inQueue;
        this.outQueue = outQueue;
    }

    @Override
    public void run() {
        System.out.println("Forward Server is Running: " + socket);
        try {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Thread for reading messages from this client and pushing to inQueue
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = br.readLine()) != null) {
                        inQueue.offer(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Client disconnected: " + socket);
                }
            }).start();

            // Thread for sending messages from outQueue to this client
            while (true) {
                if (!outQueue.isEmpty()) {
                    String outgoing = outQueue.poll();
                    pw.println(outgoing);
                }
                Thread.sleep(100); // avoid busy waiting
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}