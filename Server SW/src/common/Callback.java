package common;

public interface Callback {

	boolean onAttributeChanged(Attribute attr);
	
	//first check all requiered callbacks, if all succeed notify rest
	//e.g: HW is down --> don't change UI
	boolean required();
}
