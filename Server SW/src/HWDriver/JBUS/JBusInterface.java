package HWDriver.JBUS;

import java.util.*;
import java.util.Map.Entry;

import org.jdom2.DataConversionException;
import org.jdom2.Element;
import rights.Group;
import rights.Right;
import rights.User;

import common.Attribute;
import common.HAObject;
import common.Output;
import common.attributes.Attr_Error;
import common.attributes.Attr_Error.STATUS;

public class JBusInterface extends HAObject implements Runnable {

	private Object lock_ = new Object();
	
	public Object getLock() {return lock_;}

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

		public Message() {
		}

		public int getId() throws Exception {
			return read(0,6);
		}

		public void add(byte val, int no_bits) throws Exception {
			if(no_bits>8)
				throw new Exception("max. 8 bits are allowed");

			if(size_%8==0)
				bytes_.add( val );
			else {
				bytes_.set(bytes_.size()-1, (byte) (bytes_.lastElement().byteValue() | (val<<(size_%8))) );
				if( 8-size_%8 < no_bits )
					bytes_.add( (byte) (val>>(8-size_%8)) );
			}
			size_ += no_bits;
		}

		public int length() {
			return size_;
		}

		public void print() {
			int len = (size_+7)/8;
			byte b[] = new byte[1+len];
			b[0] = (byte) size_;
			for(int i=0; i<len; i++) {
				int l = i+1==len?(size_%8):8;
				if(l==0)
					l=8;
				try {
					b[1+i] = (byte) read(8*i, l);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (byte theByte : b)
			{
				System.out.println("b "+Integer.toHexString(theByte));
			}
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
					r = ( (int)bytes_.get(i)&0xff)>>(8-j);
				}
				else if(i+1==(off+len+7)/8) {
					r |= ((( (int)bytes_.get(i)&0xff)&( 0xFF>>((off+len)%8) ))<<j);
				}
				else {
					r |= ( (int)bytes_.get(i)&0xff)<<j;
					j+=8;
				}
			}

			int mask=0;
			for(int i=0; i<len; i++)
				mask|=(1<<i);

			return r&mask;
		}
	}

	public JBusInterface(User usr, Group grp, Attribute parent) {
		super(Right.getGlobalUser("hw"), Right.getGlobalUser("hw").getFirstGroup(), parent);

		setVisualization("jbus_interface");

		status_ = new Attr_Error(Right.getGlobalUser("ui"), Right.getGlobalUser("ui").getFirstGroup(), parent_);
		status_.setId(getId()+"_status");

		//JBusNode node = new JBusNode(getUser(), getGroup(), this, 6, this);
	}


	private Map<Integer, JBusNode> connections_ = new HashMap<Integer, JBusNode>();
	private Vector<Message> buffer_ = new Vector<Message>();
	private Attr_Error status_;
	private JBusHW driver_ = null;
	private int polling_interval_ = 250;
	private int discoverID_ = -1;

	private void doDiscover() {
		if(discoverID_<0) return;
		Message msg = new Message(discoverID_);
		addMessage(msg);
		++discoverID_;
		if(discoverID_>=(1<<6))
			discoverID_ = -1;
	}

	public void discover() {
		discoverID_ = 0;
		doDiscover();
	}

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

			t = el.getAttribute("discover");
			if(t!=null) {
				try {
					if( t.getBooleanValue() )
						discover();
				} catch (DataConversionException e) {
					Output.error(e);
				}
			}

			try {
				if(hw.equals("serial")) {
					driver_ = new JBusHWSerial(this, el);
				}
				else if(hw.equals("console")) {
					driver_ = new JBusHWConsole(this, el);
				}
			} catch (Exception e) {
				e.printStackTrace();
				status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.ERROR, e.toString());
			}

			if(driver_==null) {
				status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.ERROR, "missing driver");
				return false;
			}
		}

		(new Thread(this)).start();

		/*while(discoverID_>=0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}*/
		return true;
	}


	public void connect(JBusNode obj) {
		if(connections_.containsKey(obj.getHwId()))
			status_.setStatus(Right.getGlobalUser("ui"), STATUS.WARNING, "hardware id of jbus node already exists");
		connections_.put(obj.getHwId(), obj);

		synchronized (lock_) {
			lock_.notify();
		}
	}

	public void addMessage(Message msg) {
		synchronized (lock_) {
			buffer_.add(msg);
			lock_.notify();
		}
	}


	private void addMessage(Vector<Message> msgs) {
		synchronized (lock_) {
			for(Message msg : msgs)
				buffer_.add(msg);
		}
	}

	private void sendBuffer() {
		if(driver_==null)
			status_.setStatus(Right.getGlobalUser("ui"), STATUS.WARNING, "not connected");

		int last_id = driver_.getLastId();
		boolean send_anyway = false;
		while(buffer_.size()>0) {
			for(int i=0; i<buffer_.size(); i++) {
				
				try {
					if(!send_anyway && buffer_.get(i).getId()==last_id)
						continue;
				} catch (Exception e1) {
					Output.error(e1);
				}
				
				try {
					driver_.sendMessage(buffer_.get(i));
				} catch (Exception e) {
					e.printStackTrace();
					status_.setStatus(Right.getGlobalUser("ui"), STATUS.WARNING, e.toString());
				}
				buffer_.remove(i);
				break;
			}
			
			send_anyway = true;
		}
	}

	@Override
	public void run() {
		long ms = 0;

		while(true) {

			synchronized (lock_) {
				if(ms>0)
					try {
						lock_.wait(ms);
					} catch (InterruptedException e) {
					}
				
				if(Calendar.getInstance().getTimeInMillis()-status_.getLastChanged()>10*1000)
					status_.setStatus(Right.getGlobalUser("ui"), STATUS.OK, "");
				
				//check for lost devices (timeout)
				for(Entry<Integer, JBusNode> c : connections_.entrySet()) {
					if(  Calendar.getInstance().getTimeInMillis()-c.getValue().getLastReceived() > 10*c.getValue().getPollingMs())
						status_.setStatus(Right.getGlobalUser("ui"), STATUS.ERROR, "jbus node "+c.getKey()+" timed out");
				}

				Message msg;
				try {
					while( driver_!=null && (msg=driver_.getMessage())!=null ) {

						try {
							int id = msg.getId();
							if( connections_.containsKey(id)) {
								JBusNode con = connections_.get(id);

								con.parseMessage(msg);
							}
							else {	//not here yet
								System.out.println("new node "+id);
								
								JBusNode node = new JBusNode(getUser(), getGroup(), this, id, this);
								node.setPollingMs(polling_interval_);
								node.setId("node"+id);
								add(node);
								connections_.put(id, node);
							}
						} catch (Exception e) {
							e.printStackTrace();
							status_.setStatus(Right.getGlobalUser("ui"), STATUS.WARNING, e.toString());
						}
					}
				} catch (Exception e) {
					if(driver_!=null)
						driver_.clear();
					
					e.printStackTrace();
					status_.setStatus(Right.getGlobalUser("ui"), STATUS.ERROR, e.toString());
				}

				ms = 100000;
				for(Map.Entry<Integer, JBusNode> n : connections_.entrySet()) {
					if( n.getValue().getPolled()+n.getValue().getPollingMs() - Calendar.getInstance().getTimeInMillis() <= 0) {
						try {
							addMessage(n.getValue().createPollingMsg());
						} catch (Exception e) {
							e.printStackTrace();
							status_.setStatus(Right.getGlobalUser("ui"), STATUS.WARNING, e.toString());
						}
					}
				}

				sendBuffer();

				doDiscover();

				for(Map.Entry<Integer, JBusNode> n : connections_.entrySet()) {
					ms = Math.min(ms, n.getValue().getPolled()+n.getValue().getPollingMs() - Calendar.getInstance().getTimeInMillis());
				}

				if(buffer_.size()>0)
					ms = 0;
			}

		}

	}

}
