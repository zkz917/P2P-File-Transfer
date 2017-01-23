package netw.lab1.common;

import java.io.Serializable;
import java.util.*;


public class PeerChunkInfo implements Serializable {
	/**
	 * A chunk is 4MB.
	 */
	private static final long serialVersionUID = 5509364710514689958L;
	
	public PeerInfo peerInfo;

	public Set<Integer> chunkSeqNumSet;
	
	public PeerChunkInfo(PeerInfo peerInfo, Set<Integer> set) {
		this.peerInfo = peerInfo;
		this.chunkSeqNumSet = set;
	}
}
