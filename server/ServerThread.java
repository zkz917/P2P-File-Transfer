package netw.lab1.server;
	
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import netw.lab1.common.*;
import netw.lab1.peer.*;

public class ServerThread extends Thread {
	private Socket connection;
	private Server sver; 
	
	public ServerThread(Socket connection, Server s) {
		this.connection = connection;
		sver = s;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("A server thread is created.");
			System.out.println("Reading connection input.");
			
			/* Read in the requests. */
			ObjectInputStream inputStream = new ObjectInputStream(connection.getInputStream());
			System.out.println("ois established for thread " + connection);
			ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
			System.out.println("oos established for thread " + connection);
			CommuObj commuObj = (CommuObj) inputStream.readObject();
			
			PeerInfo pinfo = new PeerInfo(connection.getInetAddress().getHostAddress(), 
					commuObj.port);
			
			System.out.println(String.format("Server receives request type: %s.", commuObj.getType()));
			String pktType = commuObj.getType();
			
			/* Determine the type of the request and the corresponding handler.*/
			if (pktType.equals("PeerRegRequest")) {
				oos.writeObject(sver.dealPeerRegRequest(((PeerRegRequest)commuObj).fileList, pinfo));
				
			} else if (pktType.equals("FileListRequest")) {
				oos.writeObject(sver.dealFileListRequest());
				
			} else if (pktType.equals("FileLocationRequest")) {
				oos.writeObject(sver.dealFileLocationRequest((FileLocationRequest)commuObj));
				
			} else if (pktType.equals("ChunkRegRequest")) {
				oos.writeObject(sver.dealChunkRegRequest((ChunkRegRequest)commuObj, pinfo));
				
			} else if (pktType.equals("LeaveRequest")) {
				oos.writeObject(sver.dealLeaveRequest((LeaveRequest)commuObj, pinfo));
			} else {
				System.err.println("Invalid packet type!");
			}
			
			connection.close();
			System.out.println("A server thread terminated.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
