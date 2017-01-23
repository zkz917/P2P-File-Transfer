package netw.lab1.server;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import netw.lab1.common.ChunkInfo;
import netw.lab1.common.CommuObj;
import netw.lab1.common.PeerChunkInfo;
import netw.lab1.common.PeerInfo;

public class FileLocationReply extends CommuObj implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8127033684996745218L;
	public Hashtable<PeerInfo, Set<ChunkInfo>> peerChunkSet;
	
	public FileLocationReply() {
		this.type = "FileLocationReply";
		peerChunkSet = new Hashtable<PeerInfo, Set<ChunkInfo>>();
	}
}
