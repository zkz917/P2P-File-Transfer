package netw.lab1.server;

import java.io.Serializable;

import netw.lab1.common.CommuObj;

public class LeaveReply extends CommuObj implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8475754849578511299L;
	public boolean success;
	
	public LeaveReply() {
		this.type = "LeaveReply";
		success = true;
	}
}
