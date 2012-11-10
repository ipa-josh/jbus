package common;

import java.util.Vector;

public class Notifier {

	private Vector<Callback> notifier_ = new Vector<Callback>();
	
	public boolean notify(Attribute attr) {
		for(int i=0; i<notifier_.size(); i++) {
			if(notifier_.get(i).required())
				if(!notifier_.get(i).onAttributeChanged(attr))
					return false;
		}
		
		for(int i=0; i<notifier_.size(); i++) {
			if(!notifier_.get(i).required())
				notifier_.get(i).onAttributeChanged(attr);
		}
		
		return true;
	}
	
	public void addCallback(Callback c) {
		notifier_.add(c);
	}
}
