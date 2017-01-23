package netw.lab1.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import netw.lab1.common.*;

public class FileListReply extends CommuObj implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5219468717367067568L;
	public List<FileInfo> fileList;
	
	public FileListReply() {
		this.type = "FileListReply";
		fileList = new ArrayList<FileInfo>();
	}
	
	public void setList(List<FileInfo> list) {
		fileList = list;	//may have some problems
	}
}
