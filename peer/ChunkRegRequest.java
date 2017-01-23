package netw.lab1.peer;

import java.io.Serializable;
import java.util.*;

import netw.lab1.common.*;

public class ChunkRegRequest extends CommuObj implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3912955419813254480L;
	
	public String filename;
	public Set<ChunkInfo> chunkSet;

	public ChunkRegRequest(String filename, Chunk[] chunks, int port) {
		this.type = "ChunkRegRequest";
		this.filename = filename;
		this.chunkSet = new HashSet<ChunkInfo>();
		this.port = port;
		
		ChunkInfo chunkInfo;
		
		for (int i = 0; i < chunks.length; i++) {
			chunkInfo = new ChunkInfo(chunks[i].sequenceNumber, chunks[i].hashVal);
			this.chunkSet.add(chunkInfo);
		}
	}
}
