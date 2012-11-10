package common;

public class PathWithData extends Path {
	
	Object obj_=null;

	public void setData(Object obj) {obj_=obj;}
	public Object getData() {return obj_;}
	
	public Object get() {
		if(empty())
			return obj_;
		return this;
	}
	
	public void fromPath() {
		obj_ = popBack();
	}
}
