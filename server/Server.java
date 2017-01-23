package netw.lab1.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import netw.lab1.common.*;
import netw.lab1.peer.*;

public class Server {
	private ServerSocket serverSocket;
	volatile private Hashtable<String, Long> fileList;	//this variable needs exclusive access
	volatile private Hashtable<String, Hashtable<PeerInfo, Set<ChunkInfo>>> fileLocation;	/* for
	each file, record every peer that has part of it and the chunks the peer has*/
	volatile private Hashtable<PeerInfo, Set<FileInfo>> peerFileSet;	/* records files that 
	each peer possesses */
	volatile private Hashtable<String, Hashtable<Integer, String>> chunkChecksum;	/* for
	every file, record the checksum of every chunk in it */
	
	public Server() {
		fileList = new Hashtable<String, Long>();
		fileLocation = new Hashtable<String, Hashtable<PeerInfo,Set<ChunkInfo>>>();
		peerFileSet = new Hashtable<PeerInfo, Set<FileInfo>>();
		chunkChecksum = new Hashtable<String, Hashtable<Integer,String>>();
	}
	
	public void startServer() {
		try {
			serverSocket = new ServerSocket(2000);
			System.out.println(String.format("IP: %s, Port: %s", 
					serverSocket.getInetAddress().getLocalHost().getHostAddress(),
					serverSocket.getLocalPort()));
			
			while(true) {
				Socket connection = serverSocket.accept();
				System.out.println("server receives a new incoming connection");
				Thread session = new ServerThread(connection, this);
				session.start();
			}
		} catch (IOException e) {
			System.out.println("The server can not be started.");
			e.printStackTrace();
		}
	}
	
	private void printTables() {
		
		System.out.println("===============begin of tables============================");
		Enumeration<String> str_keys = fileList.keys();
		
		System.out.println("fileList:");
		while(str_keys.hasMoreElements()) {
			String name = str_keys.nextElement();
			System.out.println(name + ":" + fileList.get(name));
		}
		
		System.out.println("\n\n");
		
		System.out.println("fileLocation:");
		Hashtable<PeerInfo, Set<ChunkInfo>> fileChunks;
		
		str_keys = fileLocation.keys();
		while(str_keys.hasMoreElements()) {
			String name = str_keys.nextElement();
			System.out.println(name + ":");
			fileChunks = fileLocation.get(name);
			if(fileChunks != null) {
				Enumeration<PeerInfo>  pinfo_keys = fileChunks.keys();
				PeerInfo pi;
				while(pinfo_keys.hasMoreElements()) {
					pi = pinfo_keys.nextElement();
					System.out.print("peer ip " + pi.IP + " port " + pi.port + ":");
					Set<ChunkInfo> chunkSet = fileChunks.get(pi);
					
					if(chunkSet != null) {
						Iterator<ChunkInfo> iter = chunkSet.iterator();
						while(iter.hasNext()) {
							ChunkInfo ci = iter.next();
							System.out.print(ci.sequenceNumber + " ");
						}
					}
					
					System.out.println("\n");
				}
			}
			
			System.out.println("\n");
		}
		
		System.out.println("peerFileSet:");
		Enumeration<PeerInfo> pinfo_keys = peerFileSet.keys();
		while(pinfo_keys.hasMoreElements()) {
			PeerInfo pi = pinfo_keys.nextElement();
			System.out.println("peer ip " + pi.IP + " port " + pi.port + ":");
			Set<FileInfo> fileSet = peerFileSet.get(pi);
			if (fileSet != null) {
				Iterator<FileInfo> iter = fileSet.iterator();
				while(iter.hasNext()) {
					FileInfo fi = iter.next();
					System.out.print(fi.name + " ");
				}
			}
			System.out.println();
		}
		
		System.out.println("\n");
		
		System.out.println("chunkChecksum:");
		str_keys = chunkChecksum.keys();
		while(str_keys.hasMoreElements()) {
			String name = str_keys.nextElement();
			System.out.println(name + ": ");
			
			Hashtable<Integer, String> chksum_table = chunkChecksum.get(name);
			if (chksum_table != null) {
				Enumeration<Integer> int_keys = chksum_table.keys();
				
				while(int_keys.hasMoreElements()) {
					int chunkNr = int_keys.nextElement();
					System.out.println(chunkNr + ":" + chksum_table.get(chunkNr) + " ");
				}
			}
			
			System.out.println();
		}
		
		System.out.println("===============END of tables============================");
	}
	
	//add a file to fileset
	public boolean addFile(FileInfo file) {
		if(fileList.containsKey(file.name))
			return false;
		
		fileList.put(file.name, file.length);
		return true;
	}
	
	//generate reply to peer registration
	public PeerRegReply dealPeerRegRequest(List<FileInfo> files, PeerInfo pinfo) {
		
		PeerRegReply reply = new PeerRegReply();
		
		FileInfo f;
		
		if (peerFileSet.get(pinfo) == null) {
			peerFileSet.put(pinfo, new HashSet<FileInfo>());
		}
		
		Set<FileInfo> fileSet = peerFileSet.get(pinfo);
		
		for (int i = 0; i < files.size(); ++i) {
			f = files.get(i);
			
			if (addFile(f)) {
				reply.addResult(f.name, true);
				fileSet.add(f);
			} else{
				reply.addResult(f.name, false);
			}
		}
		
		printTables();
		
		return reply;
	}
	
	public FileListReply dealFileListRequest() {
		FileListReply reply = new FileListReply();
		
		List<FileInfo> list = new ArrayList<FileInfo>();
		Enumeration<String> keys= fileList.keys();
		for (int i = 0; i < fileList.size(); ++i) {
			String name = keys.nextElement();
			list.add(new FileInfo(name, fileList.get(name)));
		}
		reply.setList(list);
		
		printTables();
		
		return reply;
	}
	
	public FileLocationReply dealFileLocationRequest(FileLocationRequest req) {
		FileLocationReply reply = new FileLocationReply();
		reply.peerChunkSet = fileLocation.get(req.name);
		
		printTables();
		
		return reply;
	}
	
	/* Deal with the request of chunk register for a file.*/
	public ChunkRegReply dealChunkRegRequest(ChunkRegRequest req, PeerInfo pinfo) {
		ChunkRegReply reply = new ChunkRegReply();
		reply.name = req.filename;
		
		if(fileList.get(req.filename) == null) {	//file does not exist
			reply.successList = null;
		} else {
			if (fileLocation.get(req.filename) == null) {	//there's no information relating this file yet
				Hashtable<PeerInfo, Set<ChunkInfo>> ht = new Hashtable<PeerInfo, Set<ChunkInfo>>();
				ht.put(pinfo, new HashSet<ChunkInfo>());
				fileLocation.put(req.filename, ht);
			} else if (fileLocation.get(req.filename).get(pinfo) == null) {	//there is no information relating this peer yet
				fileLocation.get(req.filename).put(pinfo, new HashSet<ChunkInfo>());
			}
				
			Set<ChunkInfo> chunkSet = fileLocation.get(req.filename).get(pinfo);	//get the non-null chunk set
			if (req.chunkSet.size() == 0) {	//there's no chunk number in the request 
				reply.successList = null;
			} else {
				Object[] chunkArray = req.chunkSet.toArray();
				
				for (int i = 0; i < chunkArray.length; ++i) {
					int chunkNr =  ((ChunkInfo)chunkArray[i]).sequenceNumber;
					if (chunkChecksum.get(req.filename) == null) {	//there's no information relating this file yet
						reply.addChunkInfo(chunkNr, chunkSet.add((ChunkInfo)chunkArray[i]));
						
						chunkChecksum.put(req.filename, new Hashtable<Integer, String>());
						chunkChecksum.get(req.filename).put(chunkNr, ((ChunkInfo)chunkArray[i]).hashVal);
						
					} else if (chunkChecksum.get(req.filename).get(chunkNr) == null) {
						//there's no information relating this chunk yet
						reply.addChunkInfo(chunkNr, chunkSet.add((ChunkInfo)chunkArray[i]));
						chunkChecksum.get(req.filename).put(chunkNr, ((ChunkInfo)chunkArray[i]).hashVal);
					} else {
						String checkSum = chunkChecksum.get(req.filename).get(chunkNr);
						
						if (checkSum.equals(((ChunkInfo)chunkArray[i]).hashVal)) {	//checksum is correct
							reply.addChunkInfo(chunkNr, chunkSet.add((ChunkInfo)chunkArray[i]));
						} else {
							reply.addChunkInfo(chunkNr, false);	//checksum is not correct
						}
					}
				}
			}
			
		}
		
		printTables();
		
		return reply;
	}
	
	/* delete the info relating this peer in chunkList */
	public LeaveReply dealLeaveRequest(LeaveRequest req, PeerInfo pinfo) {
		LeaveReply reply = new LeaveReply();
		reply.success = true;
		
		//TODO: will this situation be possible?
		if(peerFileSet.get(pinfo) == null) {	//the sever has no information about this peer
			reply.success = false;
			return reply;
		}
		
		Object[] fileArray = peerFileSet.get(pinfo).toArray();
		if (fileArray != null) {
			String file;
			
			for (int i = 0; i < fileArray.length; ++i) {
				file = ((FileInfo)fileArray[i]).name;
				fileLocation.get(file).remove(pinfo);
				
				//TODO:is this really necessary?
				if(fileLocation.get(file).size() == 0) {	//no peer ever holds this file now
					fileLocation.remove(file);
				}
			}
		}
		
		peerFileSet.remove(pinfo);
		
		printTables();
		
		return reply;
	}
}
