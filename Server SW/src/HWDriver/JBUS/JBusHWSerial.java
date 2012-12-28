package HWDriver.JBUS;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.jdom2.Attribute;
import org.jdom2.Element;

import HWDriver.JBUS.JBusInterface.Message;

public class JBusHWSerial implements JBusHW, SerialPortEventListener {
	OutputStream out_ = null;
	InputStream in_ = null;
	JBusInterface intf_ = null;
	private int status_ = OK;

	public JBusHWSerial(JBusInterface intf, Element el) throws Exception {
		intf_ = intf;

		Attribute t1 = el.getAttribute("port");
		Attribute t2 = el.getAttribute("baud");
		if(t1!=null && t2!=null) {
			connect( t1.getValue(), t2.getIntValue() );
		}
		else
			throw new Exception("jbus serial: missing parameter port or baud");
	}

	public void connect ( String portName, int baud ) throws Exception
	{
		out_ = null;
		in_ = null;

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
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
	public void serialEvent(SerialPortEvent arg0) {
		status_ = OK;
		synchronized (intf_) {
			int d;
			try {
				while( (d=in_.read())>-1 ) {
					synchronized (this) {
						buffer_.add((byte) d);
					}
				}
			} catch (IOException e) {
				status_ = ERROR;
			}
			intf_.notify();
		}
	}

	private static final int SEND_OK = 0x82;
	private static final int SEND_FAILED = 0x83;
	private static final int RECV_CON = 0x12;
	private static final int SET_TIME = 0xfc;
	private static final int GET_ERROR = 0xfd;
	private static final int SEND_MASK = 0xc0;

	@Override
	public Message getMessage() throws Exception {
		Message msg = null;
		
		synchronized (this) {
			int status = buffer_.firstElement();

			if(status==SEND_OK) {
				buffer_.remove(0);
				return getMessage();
			}
			else if(status==SEND_FAILED)  {
				buffer_.remove(0);
				throw new Exception("jbus error");
			}

			int len = (status+7)/8;
			if(len+1>buffer_.size()) {
				return null;
			}
			buffer_.remove(0);

			msg = new Message(0);
			for(int i=0; i<len; i++)
			{
				msg.add(buffer_.firstElement(), i+1==len?status%8:8);
				buffer_.remove(0);
			}
		}
		
		return msg;
	}

	@Override
	public void sendMessage(Message msg) throws Exception {
		int size = msg.length();
		if( (size&SEND_MASK) > 0)
			throw new Exception("too long for sending");
		
		int len = (size+7)/8;
		byte b[] = new byte[1+len];
		b[0] = (byte) size;
		for(int i=0; i<len; i++)
			b[1+i] = (byte) msg.read(8*i, i+1==len?size%8:8);
		out_.write(b);
	}

	@Override
	public int getStatus() {
		return status_;
	}
}
