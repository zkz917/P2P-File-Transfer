package netw.lab1.common;

public class Pair implements Comparable<Pair>{
	public int index;
	public int value;
	
	public Pair() {
		
	}
	
	public Pair(int i, int j) {
		this.index = i;
		this.value = j;
	}

	@Override
	public int compareTo(Pair o) {
		return this.value - o.value;
	}
}
