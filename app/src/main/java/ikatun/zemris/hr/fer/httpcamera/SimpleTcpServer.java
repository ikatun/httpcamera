package ikatun.zemris.hr.fer.httpcamera;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ikatun on 15.1.2015..
 */
public class SimpleTcpServer {
    private boolean discardAccepted = false;
    private boolean listeningStopped = false;
    Thread listenThread = null;
    ServerSocket serverSocket = null;

    public void startListening(final ConnectedHandler connectedHandler, final Runnable listeningTerminated) throws IOException {
        discardAccepted = false;
        listeningStopped = false;

        serverSocket = new ServerSocket(12345);
        serverSocket.setSoTimeout(1000);
        listenThread = new Thread(new Runnable() {
            public void run() {
                while (!listeningStopped) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        if (!discardAccepted) {
                            connectedHandler.clientConnected(clientSocket);
                            //discardAccepted = true;
                        }
                    } catch (SocketTimeoutException e) {
                    } catch (Exception e) {
                        e.printStackTrace();
                        stopListening();
                        if (listeningTerminated != null)
                            listeningTerminated.run();
                    }
                }
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listenThread.start();
    }

    public void stopListening() {
        discardAccepted = true;
        listeningStopped = true;
    }

    public static interface ConnectedHandler {
        public void clientConnected(Socket clientSocket);
    }
}
