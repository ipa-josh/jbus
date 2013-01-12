package HWDriver.JBUS;

import HWDriver.JBUS.JBusInterface.Message;

public interface JBusHW {
	
	static final int ERROR = -1;
	static final int WARNING = 0;
	static final int OK = 1;
	
	void sendMessage(Message msg) throws Exception;
	Message getMessage() throws Exception;
	int getStatus();
	int getLastId();
	void clear();
}
