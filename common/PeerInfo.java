package netw.lab1.common;

import java.io.Serializable;

public class PeerInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2632560669397727137L;
	
	public String IP;
	public int port;
	
	public PeerInfo(String IP, int port) {
		this.IP = IP;
		this.port = port;
	}
	
	/* we override this function to let different objects with same values
	 * be equal so that a Hashtable lookup will function as we want */
	public boolean equals(Object obj) {
		if(!(obj instanceof PeerInfo))
			return false;
		PeerInfo pi = (PeerInfo)obj;
		return this.IP.equals(pi.IP) &&
		this.port == pi.port;
	}
	
	/* we override this function to let different objects with same values
	 * be equal so that a Hashtable lookup will function as we want */
	public int hashCode() {
		return 1;
	}
}
