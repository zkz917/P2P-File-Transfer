package netw.lab1.common;

import java.io.Serializable;

public class ChunkInfo implements Serializable {
/**
	 * 
	 */
	private static final long serialVersionUID = -3646801785188642032L;
	//	public String filename;
	public int sequenceNumber;
	public String hashVal;
	
	public ChunkInfo(int seq, String hashVal) {
//		this.filename = filename;
		this.sequenceNumber = seq;
		this.hashVal = hashVal;
	}
	
	/* we override this function to let different objects with same values
	 * be equal so that a Hashtable lookup will function as we want */
	public boolean equals(Object obj) {
		if(!(obj instanceof ChunkInfo))
			return false;
		
		ChunkInfo ci = (ChunkInfo)obj;
		return this.sequenceNumber == ci.sequenceNumber
		&& this.hashVal.equals(ci.hashVal);
	}
	
	/* we override this function to let different objects with same values
	 * be equal so that a Hashtable lookup will function as we want */
	public int hashCode() {
		return 1;
	}
}
