package common.attributes;

import java.util.Calendar;

import org.jdom2.Element;

import rights.Group;
import rights.User;
import common.Attribute;

public class Attr_String extends Attribute {
	String val_;

	public Attr_String(User usr, Group grp, Attribute parent) {
		super(usr, grp, parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean _set(User usr, Object v) {
		if(v==null)
			return false;

		String t = v.toString();
		if(t==val_)
			return false;
		val_ = t;

		ts_change_=Calendar.getInstance().getTimeInMillis();
		return true;
	}

	@Override
	protected Object _get(User usr) {
		return new Long(val_);
	}
	
	public boolean _readXML(Element el) {
		val_ = el.getValue();
		return true;
	}

}
