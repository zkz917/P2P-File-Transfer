package netw.lab1.peer;

import java.io.*;
import java.io.IOException;
import java.util.*;

public class PeerChunkManager {
	volatile Map<String, List<Chunk>> fileChunkList;

	public PeerChunkManager() {
		fileChunkList = new HashMap<String, List<Chunk>>();
	}
	
	/* Load the file into memory and represent it as chunks. */
	public void loadFiles(File file)
	{
		List<Chunk> chunkList = new ArrayList<Chunk>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			int size = (int) file.length();
			byte [] fileData = new byte[size];
			fileInputStream.read(fileData);
			fileInputStream.close();
			
			int seqNum = 0;
			for (int i = 0; i < size; i += Chunk.CHUNK_SIZE) {
				int chunkSize;
				if (i + Chunk.CHUNK_SIZE - 1 < size) {
					chunkSize = Chunk.CHUNK_SIZE;
				}
				else {
					chunkSize = size - i + 1;
				}
				Chunk chunk = new Chunk(file.getName(), seqNum, chunkSize,
						Arrays.copyOfRange(fileData, i, i + chunkSize - 1));
				chunkList.add(chunk);
				seqNum += 1;
			}
			this.fileChunkList.put(file.getName(), chunkList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Save the chunks of a file to the disk. */
	private void saveChunkListToFile(String filename, List<Chunk> chunkList) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			int [] seqNums = new int[chunkList.size()];
			int index = 0;
			for (Chunk c : chunkList) {
				seqNums[index] = c.sequenceNumber;
				index += 1;
			}
			Arrays.sort(seqNums);
			
			for(int i = 0; i < chunkList.size(); i++) {
				if (seqNums[i] != i) {
					System.out.println("The file is not complete.");
					fileOutputStream.close();
					return;
				}
			}

			for(int i = 0; i < chunkList.size(); i++) {
				Chunk chunk = this.findChunkBySequenceNumber(chunkList, i);
				fileOutputStream.write(chunk.data);
			}
			
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Save the a file's chunks to the file on the disk. */
	public void saveFile(String filename, String prefix) {
		List<Chunk> chunks = this.fileChunkList.get(filename);
		this.saveChunkListToFile(filename, chunks);
		try{
			File afile =new File(filename);
			if(afile.renameTo(new File(prefix + afile.getName()))){
				System.out.println("File is moved successful!");
			}
			else{
	    		System.out.println("File is failed to move!");
	    	}

	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}

	private Chunk findChunkBySequenceNumber(List<Chunk> chunkList, int sequenceNumber)
	{
		Chunk result = null;
		
		System.out.println(String.format("The len of chunk list: %d.", chunkList.size()));

		for (Chunk c : chunkList) {
			if (c!= null && c.sequenceNumber == sequenceNumber) {
				result = c;
				break;
			}
		}

		return result;
	}

	public Chunk getChunk(String filename, int sequenceNumber)
	{
		List<Chunk> chuckList = fileChunkList.get(filename);
		Chunk result = findChunkBySequenceNumber(chuckList, sequenceNumber);
		return result;
	}
	
	public Chunk[] getFileChunks(String filename) {
		List<Chunk> chuckList = fileChunkList.get(filename);
		Chunk[] chunks = new Chunk[chuckList.size()];
		
		int i = 0;
		for (Object chunk : chuckList.toArray()) {
			Chunk chunk2 = (Chunk) chunk;
			chunks[i] = chunk2;
			i++;
		}
		
		return chunks;
	}
	
	public boolean saveChunkToList(Chunk chunk, String filename, int sequenceNumber) 
	{
		List<Chunk> chuckList = fileChunkList.get(filename);

		if (chuckList == null) {
			System.out.println("Create a new chunk list.");
			chuckList = new ArrayList<Chunk>();
		}
		
		Chunk c = findChunkBySequenceNumber(chuckList, sequenceNumber);

		if (c == null) {
			chuckList.add(chunk);
		}
		
		return true;
	}

	public boolean saveChunk(byte []data, String filename, int sequenceNumber)
	{
		List<Chunk> chuckList = fileChunkList.get(filename);

		if (chuckList == null) {
			return false;
		}
		else {
			Chunk c = findChunkBySequenceNumber(chuckList, sequenceNumber);

			if (c != null) {
				return false;
			}
			else {
				Chunk chunk = new Chunk(filename, sequenceNumber, Chunk.CHUNK_SIZE, data);
				chuckList.add(chunk);
				return true;
			}
		}
	}
}
