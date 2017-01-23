package netw.lab1.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

import netw.lab1.common.PeerInfo;

/**
 * Created by tianxiat on 10/6/16.
 */
public class PeerServerThread extends Thread{
    private Socket connection;
    private Peer peer;
    private PeerInfo pinfo;

    PeerServerThread(Peer p, Socket connection) {
        this.connection = connection;
        this.peer = p;
        this.pinfo = new PeerInfo(connection.getInetAddress().getHostAddress(), connection.getPort());
        
    }
    
    @Override
    public void run() {
    	System.out.println("A peer server thread is created.");
    	
    	try {
    		ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
    		PeerChunkRequest request = (PeerChunkRequest) ois.readObject();
    		this.pinfo.port =request.port;
    		this.peer.addConnectedPeer(this.pinfo);
    		
    		String filename = request.filename;
    		int sequenceNumber = request.sequenceNumber;
    		Chunk chunk = this.peer.chunkManager.getChunk(filename, sequenceNumber);
   
			ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
			
			if (chunk == null) {
    			System.out.println("The chunk can not be found.");
    			oos.writeObject(new PeerChunkReply(this.peer.peerServerPort, null));
    			return;
    		}
			
			int total = chunk.data.length;
			int sent = 0;
			int limit = 0;
			int end = 0;
			byte[] data = chunk.data;
			
			while (sent < total) {
//				System.out.println(String.format("Transfering the file: %s(seq: %d) to"
//						+ " %s:%d", filename, sequenceNumber, connection.getInetAddress().getHostAddress(),
//						connection.getPort()));
				limit = peer.getUploadLimit(this.pinfo);
//				System.out.println(String.format("limit: %d", limit));
				end = sent + limit > total - 1 ? total : sent + limit;
				PeerChunkReply reply = new PeerChunkReply(this.peer.peerServerPort, 
						Arrays.copyOfRange(data, sent, end));
				sent += limit;
				oos.writeObject(reply);
				sleep(500);
			}
			
			/* Sent a null data array to indicate the transfer is over. */
			PeerChunkReply reply = new PeerChunkReply(this.peer.peerServerPort, null);
			oos.writeObject(reply);
			System.out.print("Transfer complete.");
			
			connection.close();
			System.out.println("connection close.");
		} catch (IOException e) {
			System.out.println("Can not connect to peer, peer may be down.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Can not connect to peer, peer may be down.");
		}
    	finally {
			this.peer.removeConnectedPeer(this.pinfo);
		}
    }
}
