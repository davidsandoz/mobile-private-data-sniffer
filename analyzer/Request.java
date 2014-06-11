package analyzer;

public class Request {

	private int index;
	private String source;
	private String destination;
	private String load;
	
	public Request(int idx, String src, String dst, String ld) {
		index = idx;
		source = src;
		destination = dst;
		load = ld;
	}

	public int getIndex() {
		return index;
	}

	public String getSource() {
		return source;
	}

	public String getDestination() {
		return destination;
	}

	public String getLoad() {
		return load;
	}
}
