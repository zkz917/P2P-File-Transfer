package netw.lab1.common;

import java.io.Serializable;

public abstract class CommuObj implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6889351630884969539L;
	
	protected String type;
	public int port;
	
	public String getType() {
		return type;
	}
}
