package netw.lab1.peer;

import java.io.Serializable;

import netw.lab1.common.CommuObj;

public class FileListRequest extends CommuObj implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 795433078133832634L;

	public FileListRequest(int port){
		this.type = "FileListRequest";
		this.port = port;
	}
}
