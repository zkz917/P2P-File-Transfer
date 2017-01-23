package netw.lab1.peer;

import java.util.*;

import netw.lab1.common.PeerInfo;

public class ChunkLocationInfo {
	public Map<Integer, ArrayList<PeerInfo>> chunkLocationList;
	public Map<Integer, String> hashValMap;
	
	public ChunkLocationInfo() {
		this.chunkLocationList = new HashMap<Integer, ArrayList<PeerInfo>>();
		this.hashValMap = new HashMap<Integer, String>();
	}
}
