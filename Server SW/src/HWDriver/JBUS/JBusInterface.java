package HWDriver.JBUS;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import HWDriver.AVRNETIO.AvrNetIo;

import rights.Group;
import rights.Right;
import rights.User;

import common.Attribute;
import common.HAObject;
import common.Output;
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

		public int getId() throws Exception {
			return read(0,6);
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

		public int length() {
			return size_;
		}

		public int read(int off, int len) throws Exception {
			int r=0;
			if(off+len>size_)
				throw new Exception("trying to read after bounds ("+off+"+"+len+" is after "+size_+")");
			if(len>31)
				throw new Exception("can only read 31 bits");

			int j=0;
			for(int i=(off/8); i<(off+len+7)/8; i++) {
				if(j==0) {
					j = 8-off%8;
					r = bytes_.get(i)>>(8-j);
				}
				else if(i+1==(off+len+7)/8) {
					r |= (bytes_.get(i)<<j)&( 0xFF>>((off+len)%8) );
				}
				else {
					r |= bytes_.get(i)<<j;
					j+=8;
				}
			}
			return r;
		}
	}

	public JBusInterface(User usr, Group grp, Attribute parent) {
		super(usr, grp, parent);

		status_ = new Attr_Error(Right.getGlobalUser("ui"), Right.getGlobalUser("ui").getFirstGroup(), parent_);
		status_.setId(parent_.getId()+"_status");
	}


	private Map<Integer, JBusNode> connections_ = new HashMap<Integer, JBusNode>();
	private Vector<Message> buffer_ = new Vector<Message>();
	private Attr_Error status_;
	private JBusHW driver_ = null;
	private int polling_interval_ = 250;

	@Override
	public boolean _readXML(Element el) {
		synchronized(this) {
			status_.clear(Right.getGlobalUser("ui"));

			String hw="";

			org.jdom2.Attribute t = el.getAttribute("hw");
			if(t!=null) {
				hw=t.getValue();
			}

			t = el.getAttribute("polling_interval");
			if(t!=null) {
				try {
					polling_interval_  = t.getIntValue();
				} catch (DataConversionException e) {
					Output.error(e);
				}
			}

			try {
				if(hw.equals("serial")) {
					driver_ = new JBusHWSerial(this, el);
				}
			} catch (Exception e) {
				status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.ERROR, e.toString());
			}

			if(driver_==null) {
				status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.ERROR, "missing driver");
				return false;
			}
		}

		(new Thread(this)).start();
		return true;
	}


	public void connect(JBusNode obj) {
		if(connections_.containsKey(obj.getHwId()))
			status_.setStatus(Right.getGlobalUser("ui"), STATUS.WARNING, "hardware id of jbus node already exists");
		connections_.put(obj.getHwId(), obj);

		synchronized (this) {
			notify();
		}
	}

	public void addMessage(Message msg) {
		synchronized (this) {
			buffer_.add(msg);
			notify();
		}
	}


	private void addMessage(Vector<Message> msgs) {
		synchronized (this) {
			for(Message msg : msgs)
				buffer_.add(msg);
		}
	}

	private void sendBuffer() {
		if(driver_==null)
			status_.setStatus(Right.getGlobalUser("ui"), STATUS.WARNING, "not connected");

		for(Message msg : buffer_) {
			try {
				driver_.sendMessage(msg);
			} catch (Exception e) {
				status_.setStatus(Right.getGlobalUser("ui"), STATUS.WARNING, e.toString());
			}
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
				
				Message msg;
				while( driver_!=null && (msg=driver_.getMessage())!=null ) {
					f
				}

				ms = 100000;
				for(Map.Entry<Integer, JBusNode> n : connections_.entrySet()) {
					if( n.getValue().getPolled()+n.getValue().getPollingMs() - Calendar.getInstance().getTimeInMillis() <= 0) {
						addMessage(n.getValue().createPollingMsg());
					}
				}

				sendBuffer();

				for(Map.Entry<Integer, JBusNode> n : connections_.entrySet()) {
					ms = Math.min(ms, n.getValue().getPolled()+n.getValue().getPollingMs() - Calendar.getInstance().getTimeInMillis());
				}
			}

		}

	}

}
