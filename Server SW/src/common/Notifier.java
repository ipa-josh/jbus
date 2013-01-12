package common;

import java.util.Vector;

public class Notifier {

	private Vector<Callback> notifier_ = new Vector<Callback>();

	public int notify(Attribute attr) {
		return notify(attr, -1);
	}
	
	public int notify(Attribute attr, int _size) {
		int size = notifier_.size();
		if(_size>=0)
			size = _size;
		for(int i=0; i<size; i++) {
			if(notifier_.get(i).required())
				if(!notifier_.get(i).onAttributeChanged(attr))
					return i;
		}

		if(_size<0)
			for(int i=0; i<size; i++) {
				if(!notifier_.get(i).required())
					notifier_.get(i).onAttributeChanged(attr);
			}
		
		return -1;
	}
	
	public void addCallback(Callback c) {
		notifier_.add(c);
	}
	
	public static interface RemovalCondition {
		public boolean canRemove(Callback c);
	}
	
	public void remove(String clname, RemovalCondition rc) {
		for(int i=0; i<notifier_.size(); i++) {
			if(notifier_.get(i)!=null && notifier_.get(i).getClass().getSimpleName().equals(clname) && rc.canRemove(notifier_.get(i)) ) {
				notifier_.remove(i);
				--i;
			}
		}
	}
}
