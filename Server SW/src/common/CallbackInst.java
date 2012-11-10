package common;

import rights.User;

public abstract class CallbackInst {
	
	protected User user_ = null;
	
	public CallbackInst(User usr) {
		user_ = usr;
	}

}
