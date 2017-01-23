package netw.lab1.peer;

import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer extends Thread {
    private ServerSocket serverSocket;
    private Peer peer;
    private int port;
    
    @Override
    public void run() {
    	try {
    		System.out.println("The peer server thread starts.");
            serverSocket = new ServerSocket(this.port);

            while(true) {
                Socket connection = serverSocket.accept();
                Thread session = new PeerServerThread(peer, connection);
                session.start();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    PeerServer(int port, Peer p) {
    	this.peer = p;
    	this.port = port;
    }
}
