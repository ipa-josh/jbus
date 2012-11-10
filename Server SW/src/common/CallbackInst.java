package common;

import java.util.Vector;

import rights.User;

public abstract class CallbackInst {
	
	protected User user_ = null;
	
	public CallbackInst(User usr) {
		user_ = usr;
	}
	
	private static Vector<Callback> def_cb_ = new Vector<Callback>();
	
	static public void addDefaultCallback(Callback cb) {
		def_cb_.add(cb);
	}
	
	static public void addDefaultCallbacks(Attribute attr) {
		for(Callback cb : def_cb_) attr.getNotifier().addCallback(cb);
	}

}
