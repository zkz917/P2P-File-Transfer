package netw.lab1.peer;

import java.util.*;
import java.io.*;
import java.net.*;
import java.security.acl.LastOwnerException;

import netw.lab1.common.*;
import netw.lab1.server.*;

public class Peer {
	private File peerFolder;
	
	// For server connection.
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private Socket serverSocket;
	private PeerServer peerServer;
	public int peerServerPort;
	public String SERVER_HOST = "127.0.0.1";
	public final int SERVER_PORT = 2000;
	public PeerChunkManager chunkManager;
	public Map<String, ChunkLocationInfo> fileChunkList;
	public List<String> fileList;
	public HashMap<String, Long> fileLengthList;
	volatile public HashMap<PeerInfo, Double> peerContributionList;
	volatile public HashMap<PeerInfo, Integer> updatedContributionList;
	public HashMap<PeerInfo, Integer> peerUploadLimit;
	public int uploadLimitation;
	volatile public List<PeerInfo> connectedPeer;
	volatile public HashMap<PeerInfo, Long> updateTime;
	public int port;
	public String peerFolderPrefix;
	

	public Peer(String server_ip, int port, int peerServerPort, int uploadLimit, String peerFolderPrefix) {
		System.out.println("Initializing the peer.");
		this.port = port;
		this.chunkManager = new PeerChunkManager();
		this.updateTime = new HashMap<PeerInfo, Long>();
		this.updatedContributionList = new HashMap<PeerInfo, Integer>();
		this.fileList = new ArrayList<String>();
		this.fileChunkList = new HashMap<String, ChunkLocationInfo>();
		this.fileLengthList = new HashMap<String, Long>();
		this.peerServerPort = peerServerPort;
		this.peerContributionList = new HashMap<PeerInfo, Double>();
		this.uploadLimitation = uploadLimit;
		this.connectedPeer = new ArrayList<PeerInfo>();
		this.peerUploadLimit = new HashMap<PeerInfo, Integer>();

		this.peerServer = new PeerServer(this.peerServerPort, this);
		this.peerServer.start();
		this.peerFolderPrefix = peerFolderPrefix;
		this.SERVER_HOST = server_ip;
		
		// Initialize the peer folder and output the file in the folder.
		peerFolder = new File(this.peerFolderPrefix);
		for (File fileEntry : peerFolder.listFiles()) {
			if (fileEntry.isFile()) {
				this.chunkManager.loadFiles(fileEntry);
				System.out.println(fileEntry.getName());
			}
		}
	}
	
	/* The peer begins to work. */
	public void startPeer() throws InterruptedException {
		System.out.println("Hi, the peer has started.");
		Scanner scanner = new Scanner(System.in);
		boolean sessionPersist = true;
		String userOrder;
		
		requestFileList();
		
		/* Read the input of the user. */
		while(sessionPersist) {
			System.out.println("Input an order:");
			userOrder = scanner.nextLine();
			String[] parts = userOrder.split(" ");
				
			if (parts[0].equals("regf")) {
				registerFile();
			} else if (parts[0].equals("flist")) {
				requestFileList();
			} else if (parts[0].equals("floc")) {
				this.getFileLoc(parts[1]);
			} else if (parts[0].equals("lreq")) {
				sessionPersist = false;
				this.leaveRequest();
			} else if (parts[0].equals("download")) {
				downloadFile(parts[1]);
			}
		}
	}
	
	private boolean leaveRequest() {
		initConnect();
		
		LeaveRequest leaveRequest = new LeaveRequest(this.peerServerPort);
		LeaveReply leaveReply;
		
		try {
			outputStream.writeObject(leaveRequest);
			CommuObj commuObj = (CommuObj) inputStream.readObject();
			leaveReply = (LeaveReply) commuObj;
			System.out.println("Leave.");
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	/* Initialize the connection to the server. */
	private boolean initConnect() {
		try {
			System.out.println("Initialize connection to server.");
			if (serverSocket != null) {
				serverSocket.close();
			}
			serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
			outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
			inputStream = new ObjectInputStream(serverSocket.getInputStream());
			return true;
		} catch (IOException e1) {
			System.out.println("Can not connect to server, server may be down.");
			return false;
		}
	}
	
	private boolean registerFile() {
		if (!initConnect()) {
			System.out.println("The connection can not be established");
			return false;
		}
		
		PeerRegRequest regRequest = new PeerRegRequest(this.SERVER_PORT);
		List<FileInfo> fileList = new ArrayList<FileInfo>();
			
		for (File fileEntry : peerFolder.listFiles()) {
			FileInfo fileInfo = new FileInfo(fileEntry.getName(), fileEntry.length());
			this.chunkManager.loadFiles(fileEntry);
			fileList.add(fileInfo);
		}
			
		regRequest.fileList = fileList;
		try {
			outputStream.writeObject(regRequest);
			CommuObj commuObj = (CommuObj) inputStream.readObject();
			PeerRegReply peerRegReply = (PeerRegReply) commuObj;
			
			/* Output the response from the server. */
			for (String key : peerRegReply.successMap.keySet()) {
				boolean s = peerRegReply.successMap.get(key);
				if (s) {
					System.out.println(String.format("%s: success", key));
				}
				else {
					System.out.println(String.format("%s: fail", key));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("Register chunks for the files.");
		
		for (String key : this.chunkManager.fileChunkList.keySet()) {
			Chunk[] chunks = this.chunkManager.getFileChunks(key);
			this.registerChunk(key, chunks);
		}
		
		return true;
	}
	
	public boolean registerChunk(String key, Chunk[] chunks) {
		try {
			if (!initConnect()) {
				System.out.println("The connection can not be established");
				return false;
			}
			ChunkRegRequest regRequest2 = new ChunkRegRequest(key, chunks, this.peerServerPort);
			
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
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	/* Request the file list stored on the server. */
	private boolean requestFileList() {
		if (!initConnect()) {
			System.out.println("The connection can not be established");
			return false;
		}
		
		FileListRequest listRequest = new FileListRequest(this.peerServerPort);
		try {
			outputStream.writeObject(listRequest);
			CommuObj commuObj = (CommuObj) inputStream.readObject();
			FileListReply fileListReply = (FileListReply) commuObj;
			
			System.out.println(String.format("Num of files: %d", fileListReply.fileList.size()));
			for (FileInfo fileInfo : fileListReply.fileList) {
				System.out.println(String.format("file: %s, length: %d", 
						fileInfo.name, fileInfo.length));
				
				if (!this.fileList.contains(fileInfo.name)) {
					this.fileList.add(fileInfo.name);
					this.fileLengthList.put(fileInfo.name, fileInfo.length);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	/* Get the location of chunks of a file. */
	private boolean getFileLoc(String filename) {
		FileLocationRequest locationRequest = new FileLocationRequest(filename, this.peerServerPort);
		
		if (!initConnect()) {
			System.out.println("The connection can not be established");
			return false;
		}
		
		/* Get the file location from the server. */
		try {
			outputStream.writeObject(locationRequest);
			CommuObj commuObj = (CommuObj) inputStream.readObject();
			FileLocationReply fileLocationReply = (FileLocationReply) commuObj;
			
			System.out.println("Location reply:");
			Hashtable<PeerInfo, Set<ChunkInfo>> chunkset = fileLocationReply.peerChunkSet;
			
			if (chunkset == null) {
				System.out.println("The file is not available.");
				return false;
			}
			
			Enumeration<PeerInfo> pif = chunkset.keys();
			this.fileChunkList.remove(filename);
			ChunkLocationInfo locationInfo = new ChunkLocationInfo();
			
			if (!pif.hasMoreElements()) {
				System.out.println(String.format("The file %s is not existed.", filename));
				return true;
			}
			
			while (pif.hasMoreElements()) {
				PeerInfo info = pif.nextElement();
				System.out.println(String.format("IP: %s, port: %d", info.IP, info.port));
				
				Set<ChunkInfo> infoSet = chunkset.get(info);
				for (ChunkInfo cf : infoSet) {
					System.out.println(String.format("seq: %d, hash: %s", cf.sequenceNumber, cf.hashVal));
					if (!locationInfo.chunkLocationList.containsKey(cf.sequenceNumber)) {
						locationInfo.chunkLocationList.put(cf.sequenceNumber, new ArrayList<PeerInfo>());
						locationInfo.hashValMap.put(cf.sequenceNumber, cf.hashVal);
					}
					
					List<PeerInfo> pList = locationInfo.chunkLocationList.get(cf.sequenceNumber);
					pList.add(info);
				}
			}
			this.fileChunkList.put(filename, locationInfo);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private boolean downloadFile(String filename) {
		
		this.getFileLoc(filename);
		
		if (!initConnect()) {
			System.out.println("The connection can not be established");
			return false;
		}
		
		long length = 0;
		for (String s : this.fileList) {
			if (s.equals(filename)) {
				length = (long) this.fileLengthList.get(s);
			}
		}
		this.chunkManager.fileChunkList.put(filename, new ArrayList<Chunk>());
		PeerClient peerClient = new PeerClient(this, filename, length);
		peerClient.beginDownload();
		
		return true;
	}
	
	public void recordPeerContribution(PeerInfo pif, int contribution) {
		System.out.println(String.format("Record contribution: %d", contribution));
		if (peerContributionList.containsKey(pif)) {
			long last = this.updateTime.get(pif);
			long now = System.currentTimeMillis();
			double speed = ((double)contribution) / (double)(now - last);
			System.out.println(String.format("Contribution: %f", (double)(contribution)));
			System.out.println(String.format("Time gap: %f", (double)(now - last)));
			System.out.println(String.format("Record contribution speed: %f", speed));
			this.peerContributionList.put(pif, speed * 500);
			this.updateTime.put(pif, now);
		}
	}
	
	synchronized public int getUploadLimit(PeerInfo pif) {
		System.out.println(String.format("This is %s:%d", pif.IP, pif.port));
		int result = 0;
		
		double sum = 0;
		double part = 1;
		
		/* Remove the old contributions. */
		Set<PeerInfo> keys = this.updateTime.keySet();
		for (PeerInfo info : keys) {
			long last = this.updateTime.get(info);
			if (System.currentTimeMillis() - last > 10000) {
				this.peerContributionList.put(info, 0.0);
			}
		}
		
//		Set<PeerInfo> keys = this.peerContributionList.keySet();
		for (PeerInfo info : keys) {
			sum += this.peerContributionList.get(info);
			if (info.equals(pif)) {
				System.out.println(String.format("Contribution: %f", this.peerContributionList.get(pif)));
				part = Math.max(this.peerContributionList.get(info), 1);
			}
		}

		System.out.println(String.format("Total Contribution: %f", sum));
		if (sum == 0) {
			result = (int) ((double) this.uploadLimitation / (double) this.peerUploadLimit.size());
			System.out.print(String.format("Limit:", result));
		}
		else {
			int otherUpload = 0;
			for (PeerInfo peerInfo : this.peerUploadLimit.keySet()) {
				if (!peerInfo.equals(pif)) {
					otherUpload += this.peerUploadLimit.get(peerInfo);
				}
			}
//			System.out.println(String.format("Contribution: %f, sum: %f", part, sum));
			result = Math.min((int) ((double)part * (double)this.uploadLimitation / (double)(sum + this.peerContributionList.size())), 
					this.uploadLimitation - otherUpload);
			System.out.println(String.format("Limit: %d", result));
		}

//		System.out.println(String.format("The upload limitation is  %d to peer at %s:%d", result,
//				pif.IP, pif.port));
		
		return result;
	}
	
	synchronized public void addConnectedPeer(PeerInfo pif) {
		this.connectedPeer.add(pif);
		this.peerUploadLimit.put(pif, 0);
		this.updateTime.put(pif, System.currentTimeMillis());
		this.updatedContributionList.put(pif, 0);
		this.peerContributionList.put(pif, 0.0);
	}
	
	synchronized public void removeConnectedPeer(PeerInfo pif) {
		this.connectedPeer.remove(pif);
		this.peerUploadLimit.remove(pif);
		this.updateTime.remove(pif);
		this.peerContributionList.remove(pif);
		this.peerContributionList.remove(pif);
	}
}
