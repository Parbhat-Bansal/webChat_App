package WebChat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Server {
    public static void main(String[] args) {
        boolean sw = false ; 
        int port = 8008;  // Define the port number
        Queue<String> queueA = new LinkedList<>();
        Queue<String> queueB = new LinkedList<>();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                try {
                    // socket represents a client here
                    Socket socket = serverSocket.accept();
                    sw = !sw ;
                    ProxyServer proxyServer;
                    
                    if (sw) {
                        proxyServer = new ProxyServer(socket, queueA,queueB);
                    } else {
                        proxyServer = new ProxyServer(socket, queueB,queueA);
                    }
                    proxyServer.start();  
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
