package common;

public class HWID {
	
	String [] nodes_=null;

	public HWID(String driver, String device) {
		nodes_=new String[2];
		nodes_[0]=driver;
		nodes_[1]=device;
	}
	
	public String getDriver() {
		if(nodes_==null||nodes_.length<2) return "no driver";
		return nodes_[0];
	}
	
	public String getDeviceID() {
		if(nodes_==null||nodes_.length<2) return "no device";
		return nodes_[nodes_.length-1];
	}
}
