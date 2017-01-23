package netw.lab1.peer;

import java.io.Serializable;
import java.util.*;

import netw.lab1.common.*;

public class PeerRegRequest extends CommuObj implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5482233910885102289L;
	
	public List<FileInfo> fileList;
	
	public PeerRegRequest(int port) {
		this.type = "PeerRegRequest";
		fileList = new ArrayList<FileInfo>();
		this.port = port;
	}
}
