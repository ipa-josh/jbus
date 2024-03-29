package HWDriver.AVRNETIO;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import rights.Right;
import rights.User;

import common.Attribute;
import common.Callback;
import common.CallbackInst;
import common.Output;
import common.Path;
import common.attributes.Attr_Boolean;
import common.attributes.Attr_Error;

public class HWDriver extends CallbackInst implements Callback {
	public HWDriver(User usr, Attribute parent_) {
		super(usr);
		status_ = new Attr_Error(Right.getGlobalUser("ui"), Right.getGlobalUser("ui").getFirstGroup(), parent_);
		status_.setId(parent_.getId()+"_status");
	}

	private Attr_Error status_ = null;
	private AvrNetIo hw_ = null;
	
	public Attr_Error getStatus() {return status_;}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		if(hw_==null||attr==null)
			return false;

		if(!hw_.isConnected())
			return false;

		if(attr.getId().startsWith("out")) {
			int port=-1;
			try {
				port = Integer.parseInt( attr.getId().substring(3) );
			}
			catch(Exception e) {
				return false;
			}

			if(!Attr_Boolean.class.equals(attr.getClass()))
				return false;
			Object obj=((Attr_Boolean)attr).get(user_);
			if(obj==null || !Boolean.class.equals(obj.getClass()))
				return false;
			boolean level = (Boolean) obj;

			return hw_.setOutPort(port, level);
		}
		else if(attr.getId().startsWith("in")) {
			return true;
		}
		/*
		else if(attr.getId().startsWith("in")) {
				int port=-1;
				try {
					port = Integer.parseInt( attr.getId().substring(2) );
				}
				catch(Exception e) {
					return false;
				}

				if(!Attr_Boolean.class.equals(attr.getClass()))
					return false;
				Object obj=((Attr_Boolean)attr).get(user_);
				if(obj==null || !Boolean.class.equals(obj.getClass()))
					return false;
				boolean level = (Boolean) obj;

				return hw_.g(port, level);
			}*/

		return false;
	}

	@Override
	public boolean required() {
		return true;
	}

	public boolean check() {
		if(hw_==null) {
			status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.WARNING, "no AvrNetIo hardware found");
			return false;
		}

		if(!hw_.isConnected())
			if(!hw_.connect()) {
				status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.ERROR, "couldn't connect to device");
				return false;
			}
		
		status_.clear(Right.getGlobalUser("ui"));
		
		return true;
	}
	
	public void polling(AvrNetIoBoard avrNetIoBoard) {
		for(int port=1; port<=4; port++) {
			Path p = new Path();
			p.setAttribute();
			p.parseString("in"+port);
			Object o=avrNetIoBoard.get(user_,p);
			if(o==null||!Attribute.class.isAssignableFrom(o.getClass())) {
				status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.WARNING, "AvrNetIo misconfigured");
				continue;
			}
			Attribute a=(Attribute) o;
			boolean val = hw_.getPort(port);

			a.set(user_, val);
		}

	}

	public boolean readXML(Element el) {
		status_.clear(Right.getGlobalUser("ui"));
		
		org.jdom2.Attribute t;

		String ip="";
		int port=80;

		t=el.getAttribute("ip");
		if(t!=null) {
			ip=t.getValue();
		}

		t=el.getAttribute("port");
		if(t!=null) {
			try {
				port=t.getIntValue();
			} catch (DataConversionException e) {
				status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.ERROR, e.toString());
				Output.error(e);
				return false;
			}
		}

		if ( ip.matches( "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}" ) )
			hw_ = new AvrNetIo(ip,port);
		else {
			status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.ERROR, "ip misswritten: "+ip);
			return false;
		}

		t=el.getAttribute("gw");
		if(t!=null) {
			String gw=t.getValue();
			if ( gw.matches( "^.[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}" ) )
				hw_.setGW(gw);
			else
				status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.ERROR, "gw misswritten: "+gw);
		}

		t=el.getAttribute("mask");
		if(t!=null) {
			String mask=t.getValue();
			if ( mask.matches( "^.[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}" ) )
				hw_.setMask(mask);
			else
				status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.ERROR, "mask misswritten: "+mask);
		}

		if(!hw_.connect()) {
			status_.setStatus(Right.getGlobalUser("ui"), Attr_Error.STATUS.ERROR, "couldn't connect to device");
			//hw_ = null;
			//return false;
		}

		return true;
	}


}
