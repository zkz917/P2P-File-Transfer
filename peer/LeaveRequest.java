package netw.lab1.peer;

import java.io.Serializable;

import netw.lab1.common.CommuObj;

public class LeaveRequest extends CommuObj implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3739352231374964283L;

	public LeaveRequest(int port) {
		this.type = "LeaveRequest";
		this.port = port;
	}
}
