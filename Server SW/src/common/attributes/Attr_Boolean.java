package common.attributes;

import java.util.Calendar;

import org.jdom2.Element;

import rights.Group;
import rights.User;
import common.Attribute;

public class Attr_Boolean extends Attribute {
	boolean val_;

	public Attr_Boolean(User usr, Group grp) {
		super(usr, grp);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean _set(User usr, Object v) {
		if(v==null)
			return false;

		boolean t;
		if(Boolean.class.equals(v.getClass())) {
			t=(Boolean)v;
		}
		else if(String.class.equals(v.getClass())) {
			t=Boolean.parseBoolean((String)v);
		}
		else
			return false;
		
		if(t==val_)
			return false;
		val_ = t;

		ts_change_=Calendar.getInstance().getTimeInMillis();
		return true;
	}

	@Override
	protected Object _get(User usr) {
		return new Boolean(val_);
	}

	public boolean _readXML(Element el) {
		val_ = Boolean.parseBoolean(el.getValue());
		return true;
	}
}
