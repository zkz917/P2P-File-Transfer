package netw.lab1.peer;

import java.io.Serializable;

import netw.lab1.common.CommuObj;

public class FileLocationRequest extends CommuObj implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 269614224184615480L;
	public String name;	//the name of the file it requests

	public FileLocationRequest(String filename, int port) {
		this.type = "FileLocationRequest";
		this.name = filename;
		this.port = port;
	}
}
