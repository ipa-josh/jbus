package HWDriver.LOGIC;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import rights.Group;
import rights.User;

import common.Attribute;
import common.Callback;
import common.HAObject;
import common.Output;
import common.attributes.Attr_Boolean;

public class Delay extends HAObject implements Callback
{

	private Attribute out_ = null, in_ = null;
	private long delay_ms_ = 1000;

	public Delay(User usr, Group grp, Attribute parent) {
		super(usr, grp, parent);

		//setVisualization("delay");

		out_ = new Attr_Boolean(usr, grp, this);
		add(out_);
		out_.setId("out");

		in_ = new Attr_Boolean(usr, grp, this);
		in_.getNotifier().addCallback(this);
		add(in_);
		in_.setId("in");
	}

	@Override
	public boolean _readXML(Element el) {
		synchronized(this) {
			org.jdom2.Attribute t = el.getAttribute("delay_ms");
			if(t!=null)
				try {
					delay_ms_ = t.getLongValue();
				} catch (DataConversionException e) {
					Output.error(e);
				}
		}

		return true;
	}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		Object b = in_.get(getUser());
		if(b==null || !Boolean.class.equals(b.getClass()))
			return false;
		if( (Boolean)b )
			return out_.set(getUser(), b);
		else {
			new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						Thread.sleep(delay_ms_);
					} catch (InterruptedException e) {
					}
					out_.set(getUser(), new Boolean(false));
				}}).start();
			return true;
		}
	}

	@Override
	public boolean required() {
		return true;
	}

}