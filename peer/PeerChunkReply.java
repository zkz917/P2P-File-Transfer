package netw.lab1.peer;

import java.io.Serializable;

/* Used for reply the chunk request. */
public class PeerChunkReply implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1109442102771496225L;
	public int port;
	public byte[] data;
	
	public PeerChunkReply(int p, byte [] d) {
		this.port = p;
		this.data = d;
	}
}
