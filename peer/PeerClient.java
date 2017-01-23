package netw.lab1.peer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import netw.lab1.common.Pair;
import netw.lab1.common.PeerInfo;

/* Used to manage the peer's download. */
public class PeerClient {
	private Peer peer;
	private String filename;
	private long length;
	public int downloadedNum;
	volatile public Set<PeerInfo> pifSet;
	public boolean[] downloaded;
	
	public PeerClient(Peer p, String filename, long length2) {
		this.peer = p;
		this.filename = filename;
		this.length = length2;
		this.downloadedNum = 0;
		this.pifSet = new HashSet<PeerInfo>();
	}
	
	public boolean beginDownload() {
		System.out.println("Begin download file.");
		ChunkLocationInfo locationInfo = this.peer.fileChunkList.get(this.filename);
		
		int len = locationInfo.chunkLocationList.size();
//		int[] downloadOffset = new int[len];
		
		Pair[] pairs = new Pair[len];
		
		for (int i = 0; i < len; i++) {
			pairs[i] = new Pair();
			pairs[i].index = i;
			pairs[i].value = locationInfo.chunkLocationList.get(i).size();
//			downloadOffset[i] = 0;
		}
		
		Arrays.sort(pairs);
		
		System.out.println("Chunk order:");
		for (int i = 0; i < len; i++) {
			System.out.println(String.format("%d. index: %d, available peer: %d.", 
					i, pairs[i].index, pairs[i].value));
		}
		
		downloaded = new boolean[len];
		for (int i = 0; i < downloaded.length; i++){
			downloaded[i] = false;
		}
		
		do {
			ArrayList<Thread> threads = new ArrayList<Thread>();
			this.pifSet.clear();
			
			// Check whether the file is complete ornote.
			for (int j=0; j < len; j++) {
				int i = pairs[j].index;
				
				if (this.downloaded[i]) {
					continue;
				}
				
				ArrayList<PeerInfo> peerInfos = locationInfo.chunkLocationList.get(i);
				
				if (peerInfos.size() == 0) {
					System.out.println("This file is incomplete in the network.");
					for (Thread thread : threads) {
						try {
							thread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					return false;
				}
				
				// Create threads to download the chunk.
				int index = 0;
				do {
					PeerInfo peerInfo = peerInfos.get(index);
					
					if (!this.pifSet.contains(peerInfo)) {
						this.pifSet.add(peerInfo);
						peerInfos.remove(index);
//						downloadOffset[i] += 1;
						System.out.println(String.format("Go to peer: %s, port: %d.", peerInfo.IP, peerInfo.port));
						PeerClientThread peerClientThread = new PeerClientThread(peer, filename, length, i, peerInfo,
								this, locationInfo.hashValMap.get(i));
						threads.add(peerClientThread);
						peerClientThread.start();
					}
					
					index += 1;
				}while(index <= peerInfos.size() - 1);
			}
			
			for (Thread thread : threads) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					System.out.println("Can not connect to the peer, it may be down.");
				}
			}
		} while(this.downloadedNum < len);
		
		System.out.println("Save chunks to file.");
		peer.chunkManager.saveFile(filename, this.peer.peerFolderPrefix);
		System.out.println("Download complte.");
		return true;
	}
}
