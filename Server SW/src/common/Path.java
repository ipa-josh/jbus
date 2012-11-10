package common;

import java.util.Vector;

public class Path {
	
	private Vector<String> nodes_ = new Vector<String>();
	private boolean absolute_ = false;
	private boolean attribute_ = false;
	
	public String toString() {
		String r="";
		if(nodes_!=null) {
			for(int i=0; i<nodes_.size(); i++)
				r+=(i>0?"/":"")+nodes_.get(i);
		}
		return r;
	}
	
	public void setAttribute() {
		attribute_=true;
	}
	public boolean getAttribute() {
		return attribute_;
	}
	
	public void parseString(String s) {
		nodes_.clear();
		String n[] = s.split("/");
		for(int i=0; i<n.length; i++) {
			if(i==0 && n[i].length()==0) {
				absolute_=true;
			}
			else
				nodes_.add(n[i]);
		}
	}
	
	public String popFront() {
		if(nodes_.size()<1)
			return "";
		String s=nodes_.get(0);
		nodes_.remove(0);
		return s;
	}
	
	public String popBack() {
		if(nodes_.size()<1)
			return "";
		String s=nodes_.get(nodes_.size()-1);
		nodes_.remove(nodes_.size()-1);
		return s;
	}
	
	public boolean empty() {
		return nodes_.size()==0;
	}

}
