package netw.lab1.common;

import java.io.Serializable;

public class FileInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4100374123228216737L;
	
	public String name;
	public long length;
	
	public FileInfo(String name, long length) {
		// TODO Auto-generated constructor stub
		this.length = length;	//in bytes
		this.name = name;
	}
	
	public String toString() {
		return String.format("Filename: %s, length: %d", name, length);
	}
	
	/* we override this function to let different objects with same values
	 * be equal so that a Hashtable lookup will function as we want */
	public boolean equals(Object obj) {
		if(!(obj instanceof FileInfo))
			return false;
		
		FileInfo fi = (FileInfo)obj;
		return this.name.equals(fi.name)
		&& this.length == fi.length;
	}
	
	/* we override this function to let different objects with same values
	 * be equal so that a Hashtable lookup will function as we want */
	public int hashCode() {
		return 1;
	}
}
