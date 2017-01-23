package netw.lab1.main;

import java.io.File;

import netw.lab1.peer.Peer;
import netw.lab1.peer.PeerChunkManager;
import netw.lab1.server.*;

public class P2P {

	public static void main(String[] args) {
		System.out.println("Hi, the program has started.");

//		PeerChunkManager manager = new PeerChunkManager("./peerFolder2/");
//		
//		File file = new File("./peerFolder/sample");
//		manager.loadFiles(file);
//		manager.saveFile("./peerFolder/sample");
		try {	
			if (args[0].equals("server")) {
				Server server = new Server();
				server.startServer();
			}
			else if(args[0].equals("peer")) {
				Peer peer = new Peer(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]),
						Integer.parseInt(args[4]), args[5]);
				peer.startPeer();
			}
			else {
				System.out.println("The input is not valid.");
			}
		} catch (Exception e) {
			System.out.print(e);
		}
	}
}
