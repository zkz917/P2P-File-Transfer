package netw.lab1.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Enumeration;

import netw.lab1.common.CommuObj;
import netw.lab1.common.PeerInfo;
import netw.lab1.server.ChunkRegReply;

/**
 * Created by tianxiat on 10/6/16.
 */
public class PeerClientThread extends Thread{
	private String filename;
	private long length;
	private int sequenceNumber;
	private Peer peer;
	private PeerInfo peerInfo;
	private Socket serverSocket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private PeerClient peerClient;
	private String hashVal;
	
	public PeerClientThread(Peer p, String filename, long length, int seqNum, PeerInfo pif, PeerClient pc,
			String hashVal) {
		this.filename = filename;
		this.length = length;
		this.sequenceNumber = seqNum;
		this.peer = p;
		this.peerInfo = pif;
		this.peerClient = pc;
		this.hashVal = hashVal;
	}
	
	@Override
	public void run() {
		System.out.println("A peer thread starts.");
		fetchChunkFromPeer(this.peerInfo.IP, this.peerInfo.port, this.filename, this.sequenceNumber);
		System.out.println("A peer thread terminates.");
	}
	
	private void fetchChunkFromPeer(String ip, int port, String filename, int sequenceNumber) {
		System.out.println(String.format("Download %s: %d, from %s - %d", filename, sequenceNumber,
				ip, port));
		try {
			Socket socket = new Socket(ip, port);
			this.peer.addConnectedPeer(new PeerInfo(ip, port));
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
			
			PeerChunkRequest request = new PeerChunkRequest(filename, sequenceNumber, peer.peerServerPort);
			
			System.out.println("Send out a request.");
			outputStream.writeObject(request);
			
			Chunk chunk = new Chunk(filename, sequenceNumber, 0, null);
			
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			// Download chunk.
			while (true) {
				PeerChunkReply reply = (PeerChunkReply) inputStream.readObject();
				if (reply.data == null) {
					break;
				}
				else {
					System.out.println(String.format("Receive seq-%d data len: %d from %s:%d.", 
							sequenceNumber, reply.data.length, peerInfo.IP, peerInfo.port));
					chunk.chunkAppend(reply.data);
					this.peer.recordPeerContribution(peerInfo, reply.data.length);
				}
			}
			
			// Check the integrity.
			if (chunk != null && chunk.hashVal.equals(this.hashVal)) {
				peer.chunkManager.saveChunkToList(chunk, filename, sequenceNumber);
				Chunk[] chunks = new Chunk[1];
				chunks[0] = chunk;
				peerClient.downloaded[sequenceNumber] = true;
				peerClient.downloadedNum += 1;
				registerChunk(filename, chunks);	
			}
			else {
				System.out.println(String.format("The chunk %d did not arrive correctly.", sequenceNumber));
			}
			
			peerClient.pifSet.remove(this.peerInfo);
			this.peer.removeConnectedPeer(new PeerInfo(ip, port));
			socket.close();
		} catch (IOException e) {
			System.out.println("Can not connect to peer, peer may be down.");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	private boolean initConnect() {
		try {
			System.out.println("Initialize connection to server.");
			if (serverSocket != null) {
				serverSocket.close();
			}
			serverSocket = new Socket(peer.SERVER_HOST, peer.SERVER_PORT);
			outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
			inputStream = new ObjectInputStream(serverSocket.getInputStream());
			return true;
		} catch (IOException e1) {
			System.out.println("Can not connect to peer, peer may be down.");
			return false;
		}
	}
	
	public boolean registerChunk(String key, Chunk[] chunks) {
		try {
			if (!initConnect()) {
				System.out.println("The connection can not be established");
				return false;
			}
			ChunkRegRequest regRequest2 = new ChunkRegRequest(key, chunks, peer.peerServerPort);
			
			outputStream.writeObject(regRequest2);
			CommuObj commuObj = (CommuObj) inputStream.readObject();
			ChunkRegReply reply = (ChunkRegReply) commuObj;
			
			System.out.println("Chunk Register Reply");
			System.out.println(String.format("File: %s", reply.name));
			
			Enumeration<Integer> enumeration = reply.successList.keys();
			while (enumeration.hasMoreElements()) {
				int ki = enumeration.nextElement();
				boolean s = reply.successList.get(ki);
				String ss = s ? "success" : "fail";
				System.out.println(String.format("%d: %s", ki, ss));
			}
		} catch (IOException e1) {
			System.out.println("Can not connect to server, server may be down.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return true;
	}
}
