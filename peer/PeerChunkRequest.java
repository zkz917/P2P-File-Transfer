package netw.lab1.peer;

import java.io.Serializable;

/* Used to request chunks on another node. */
public class PeerChunkRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1689655058056419067L;

	public String filename;
	public int sequenceNumber;
	public int port;
	
	public PeerChunkRequest(String filename, int sequenceNumber, int port) {
		this.filename = filename;
		this.sequenceNumber = sequenceNumber;
		this.port = port;
	}

}
