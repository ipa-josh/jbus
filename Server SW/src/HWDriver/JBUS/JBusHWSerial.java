package HWDriver.JBUS;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

import org.jdom2.Attribute;
import org.jdom2.Element;

import HWDriver.JBUS.JBusInterface.Message;

public class JBusHWSerial implements JBusHW, SerialPortEventListener {
	OutputStream out_ = null;
	InputStream in_ = null;
	JBusInterface intf_ = null;
	boolean debug_ = false;
	String portName_;
	int baud_;
	private int status_ = OK;

	public JBusHWSerial(JBusInterface intf, Element el) throws Exception {
		intf_ = intf;

		Attribute t1 = el.getAttribute("port");
		Attribute t2 = el.getAttribute("baud");
		if(t1!=null && t2!=null) {
			connect( portName_=t1.getValue(), baud_=t2.getIntValue() );
		}
		else
			throw new Exception("jbus serial: missing parameter port or baud");

		t1 = el.getAttribute("debug");
		if(t1!=null)
			debug_ = t1.getBooleanValue();		
		else
			debug_ = false;
	}

	public void connect ( String portName, int baud ) throws Exception
	{
		out_ = null;
		in_ = null;

		String SerialPortID = "/dev/ttyAMA0";
		System.setProperty("gnu.io.rxtx.SerialPorts", SerialPortID);

		CommPortIdentifier portIdentifier;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		} catch(gnu.io.NoSuchPortException e) {
			System.out.println("available serial ports");

			Enumeration<?> enumComm = CommPortIdentifier.getPortIdentifiers();
			while (enumComm.hasMoreElements()) {
				CommPortIdentifier serialPortId = (CommPortIdentifier) enumComm.nextElement();
				if(serialPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					System.out.println(serialPortId.getName());
				}
			}

			throw e;
		}

		if ( portIdentifier.isCurrentlyOwned() )
		{
			throw new Exception("Error: Port is currently in use");
		}
		else
		{
			CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);

			if ( commPort instanceof SerialPort )
			{
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
				
				serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

				in_ = serialPort.getInputStream();
				out_ = serialPort.getOutputStream();

				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);

			}
			else
			{
				throw new Exception("Error: Only serial ports are handled.");
			}
		}

	}

	private Vector<Byte> buffer_ = new Vector<Byte>();
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.DATA_AVAILABLE:
			status_ = OK;
			synchronized (intf_.getLock()) {
				try {
					int availableBytes = in_.available();
					if(debug_)
						System.out.println("got "+availableBytes+" bytes");
					if (availableBytes > 0) {
						byte readBuffer[] = new byte[availableBytes];
						in_.read(readBuffer, 0, availableBytes);
						synchronized (this) {
							for(int i=0; i<availableBytes; i++) {
								buffer_.add(readBuffer[i]);
								if(debug_)
									System.out.println("r "+Integer.toHexString(((int)readBuffer[i])&0xff));
							}
						}
					}
				} catch (IOException e) {
					status_ = ERROR;

					e.printStackTrace();
				}
				intf_.getLock().notify();
			}
			break;
		}
	}

	private static final int SEND_OK = 0x82;
	private static final int SEND_FAILED = 0x83;
	private static final int RECV_FAILED = 0x84;
	//private static final int RECV_CON = 0x12;
	//private static final int SET_TIME = 0xfc;
	//private static final int GET_ERROR = 0xfd;
	private static final int SEND_MASK = 0xc0;

	@Override
	public Message getMessage() throws Exception {
		Message msg = null;

		if(buffer_.size()>0) {
			synchronized (intf_.getLock()) {
				int status = buffer_.firstElement();
				status = status&0xff;

				if(status==SEND_OK) {
					buffer_.removeElementAt(0);
					return getMessage();
				}
				else if(status==SEND_FAILED)  {
					buffer_.removeElementAt(0);
					throw new Exception("jbus error (send failed)");
				}
				else if(status==RECV_FAILED)  {
					buffer_.removeElementAt(0);
					throw new Exception("jbus error (receive failed)");
				}

				int len = (status+7)/8;
				if(len+1>buffer_.size()) {
					return null;
				}
				buffer_.removeElementAt(0);
				if(len==0)
					return null;

				msg = new Message();
				for(int i=0; i<len; i++)
				{
					int l = i+1==len?(status%8):8;
					if(l==0)
						l=8;
					msg.add(buffer_.firstElement(), l);
					buffer_.removeElementAt(0);
				}
			}
		}

		return msg;
	}
	
	private int last_send_id_ = -1;
	protected long ts_next_send_same_id_=0;

	@Override
	public void sendMessage(Message msg) throws Exception {

		if(last_send_id_ == msg.getId()) {
			long tm = ts_next_send_same_id_-Calendar.getInstance().getTimeInMillis();
			if(tm>0)
				Thread.sleep(tm);
		}
		
		int size = msg.length();
		if( (size&SEND_MASK) > 0)
			throw new Exception("too long for sending");

		int len = (size+7)/8;
		byte b[] = new byte[1+len];
		b[0] = (byte) size;
		for(int i=0; i<len; i++) {
			int l = i+1==len?(size%8):8;
			if(l==0)
				l=8;
			b[1+i] = (byte) msg.read(8*i, l);
		}
		out_.write(b);
		out_.flush();
		
		last_send_id_ = msg.getId();
		ts_next_send_same_id_ = Calendar.getInstance().getTimeInMillis()+3*(size*20+50);

		if(debug_)
			for (byte theByte : b)
			{
				System.out.println("w "+Integer.toHexString(theByte));
			}
	}

	@Override
	public int getStatus() {
		return status_;
	}

	@Override
	public int getLastId() {
		return last_send_id_;
	}

	@Override
	public void clear() {
		/*try {
			Thread.sleep(100);
			connect ( portName_, baud_ );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
