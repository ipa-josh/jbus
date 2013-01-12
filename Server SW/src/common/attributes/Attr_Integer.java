package common.attributes;

import java.util.Calendar;

import org.jdom2.Element;

import rights.Group;
import rights.User;
import common.Attribute;

public class Attr_Integer extends Attribute {
	long val_;

	public Attr_Integer(User usr, Group grp, Attribute parent) {
		super(usr, grp, parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean _set(User usr, Object v) {
		if(v==null)
			return false;

		long t;
		if(Integer.class.equals(v.getClass())) {
			t=(Integer)v;
		}
		else if(Long.class.equals(v.getClass())) {
			t=(Long)v;
		}
		else if(String.class.equals(v.getClass())) {
			t=Long.parseLong((String)v);
		}
		else
			return false;
		if(t==val_)
			return false;
		val_ = t;

		return true;
	}

	@Override
	protected Object _get(User usr) {
		return new Long(val_);
	}
	
	public boolean _readXML(Element el) {
		val_ = Long.parseLong(el.getValue());
		return true;
	}

}
