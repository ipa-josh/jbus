package HWDriver.JBUS;

import java.util.*;

import rights.Group;
import rights.Right;
import rights.User;

import common.Attribute;
import common.HAObject;
import common.attributes.Attr_Error;
import common.attributes.Attr_Error.STATUS;

public class JBusInterface extends HAObject implements Runnable {
	
	public static class Message
	{
		int size_ = 0;
		Vector<Byte> bytes_ = new Vector<Byte>();
		
		public Message(int id) {
			try {
				add((byte)id, 6);
			} catch (Exception e) {
			}
		}
		
		public void add(byte val, int no_bits) throws Exception {
			if(no_bits>8)
				throw new Exception("max. 8 bits are allowed");
			val <<= (8-no_bits);
			if(size_%8==0)
				bytes_.add( val );
			else {
				bytes_.set(bytes_.size()-1, (byte) (bytes_.lastElement().byteValue() | (val>>(size_%8))) );
				if( 8-size_%8 < no_bits )
					bytes_.add( (byte) (val<<(8-size_%8)) );
			}
			size_ += no_bits;
		}
	}
	
	public JBusInterface(User usr, Group grp, Attribute parent) {
		super(usr, grp, parent);

		status_ = new Attr_Error(Right.getGlobalUser("ui"), Right.getGlobalUser("ui").getFirstGroup(), parent_);
		status_.setId(parent_.getId()+"_status");
	}


	private Map<Integer, JBusNode> connections_ = new HashMap<Integer, JBusNode>();
	private Attr_Error status_;

	
	public void connect(JBusNode obj) {
		if(connections_.containsKey(obj.getHwId()))
			status_.setStatus(Right.getGlobalUser("ui"), STATUS.WARNING, "hardware id of jbus node already exists");
		connections_.put(obj.getHwId(), obj);
		
		synchronized (this) {
			notify();
		}
	}
	
	public void addMessage() {
		synchronized (this) {
			notify();
		}
	}

	@Override
	public void run() {

		while(true) {
			long ms = 100000;

			synchronized (this) {
				try {
					wait(ms);
				} catch (InterruptedException e) {
				}
			}
			
		}

	}

}
