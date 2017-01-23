package netw.lab1.server;

import java.io.Serializable;
import java.util.Hashtable;

import netw.lab1.common.CommuObj;

public class ChunkRegReply extends CommuObj implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6746010306171410893L;
	public String name;	//filename
	public Hashtable<Integer, Boolean> successList;
	
	public ChunkRegReply() {
		this.type = "ChunkRegReply";
		this.successList = new Hashtable<Integer, Boolean>();
	}
	
	public void addChunkInfo(int nr, boolean flag) {
		successList.put(new Integer(nr), new Boolean(flag));
	}
}
