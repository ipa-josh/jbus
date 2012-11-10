package HWDriver.AVRNETIO;

import java.io.IOException;
import java.io.StringReader;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import rights.Group;
import rights.Right;
import rights.User;
import common.HAObject;
import common.Output;

public class AvrNetIoBoard extends HAObject implements Runnable {

	private static String _descr_ =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>"+
					"<Attr_Boolean id=\"out1\">0</Attr_Boolean>"+
					"<Attr_Boolean id=\"out2\">0</Attr_Boolean>"+
					"<Attr_Boolean id=\"out3\">0</Attr_Boolean>"+
					"<Attr_Boolean id=\"out4\">0</Attr_Boolean>"+
					"<Attr_Boolean id=\"out5\">0</Attr_Boolean>"+
					"<Attr_Boolean id=\"out6\">0</Attr_Boolean>"+
					"<Attr_Boolean id=\"out7\">0</Attr_Boolean>"+
					"<Attr_Boolean id=\"out8\">0</Attr_Boolean>"+

					"<Attr_Boolean id=\"in1\">0</Attr_Boolean>"+
					"<Attr_Boolean id=\"in2\">0</Attr_Boolean>"+
					"<Attr_Boolean id=\"in3\">0</Attr_Boolean>"+
					"<Attr_Boolean id=\"in4\">0</Attr_Boolean>"+
					"</root>";

	private HWDriver driver_ = null;
	private Thread worker_ = null;
	
	public AvrNetIoBoard(User usr, Group grp) {
		super(Right.getGlobalUser("hw"), Right.getGlobalUser("hw").getFirstGroup());
		
		driver_ = new HWDriver(Right.getGlobalUser("hw"));
		worker_ = new Thread(this);
	}

	@Override
	public boolean _readXML(Element el) {;
		try {
			if(!super._readXML( (new SAXBuilder()).build( new StringReader(_descr_) ).getRootElement()))
				return false;
			synchronized(this) {
				if(!driver_.readXML(el))
					return false;
			}
			for(int i=0; i<list_.size(); i++)
				list_.get(i).getNotifier().addCallback(driver_);
			worker_.start();
			return true;
		} catch (JDOMException e) {
			Output.error(e);
		} catch (IOException e) {
			Output.error(e);
		}
		return false;
	}

	@Override
	public void run() {
		
		while(true) {
			synchronized(this) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
				
				driver_.polling(this);
			}
		}
		
	}

}
