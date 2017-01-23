package netw.lab1.peer;

import java.io.Serializable;
import java.security.*;

public class Chunk implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5148018258130088111L;
	
	static public final int CHUNK_SIZE = 4 * 1024 * 1024;
	public String filename;
	public int sequenceNumber;
	public String hashVal;
	public int length;
	public byte[] data;
	
	public Chunk(String filename, int sequenceNumber, int length, byte[] data)
	{
		this.filename = filename;
		this.length = length;
		this.data = data;
		this.sequenceNumber = sequenceNumber;
		this.hashVal = "";
		this.computeHashValue();
	}
	
	/* add the byte array to the end of the chunk. */
	public void chunkAppend(byte[] data) {
		if (this.data != null) {
			byte[] newData = new byte[data.length + this.data.length];
			System.arraycopy(this.data, 0, newData, 0, this.data.length);
			System.arraycopy(data, 0, newData, this.data.length, data.length);
			this.data = newData;
		}
		else {
			this.data = data;
		}
		this.computeHashValue();
	}
	
	/* Use md5 to compute the hash value. */
	private void computeHashValue() {
		/* Compute the hash value of */
		if (this.data == null) {
			return;
		}
		
		try {
//			System.out.println(String.format("data length: %d", this.data.length));
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] hashBytes = messageDigest.digest(data);
			hashVal = "";
			for (int i = 0; i < hashBytes.length; i++) {
				hashVal += String.format("%02X", hashBytes[i]);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
