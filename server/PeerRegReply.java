package netw.lab1.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import netw.lab1.common.CommuObj;

public class PeerRegReply extends CommuObj implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9054208033950458239L;
	
	public Map<String, Boolean> successMap;
	
	public PeerRegReply() {
		this.type = "PeerRegReply";
		this.successMap = new HashMap<String, Boolean>();
	}
	
	public void setMap(Map<String, Boolean> map) {
		this.successMap = map;	//may have some problems
	}
	
	public void addResult(String str, Boolean success) {
		successMap.put(str, success);
	}
}
