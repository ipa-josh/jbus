package common.attributes;

import java.util.Calendar;

import org.jdom2.Element;

import rights.Group;
import rights.User;
import common.Attribute;

public class Attr_Double extends Attribute {
	double val_;

	public Attr_Double(User usr, Group grp, Attribute parent) {
		super(usr, grp, parent);
		// TODO Auto-generated constructor stub
	}

	public static double toDouble(Object v) throws Exception {
		double t;
		if(Double.class.equals(v.getClass())) {
			t=(Double)v;
		}
		else if(Float.class.equals(v.getClass())) {
			t=(Float)v;
		}
		else if(Integer.class.equals(v.getClass())) {
			t=(Integer)v;
		}
		else if(Long.class.equals(v.getClass())) {
			t=(Long)v;
		}
		else if(String.class.equals(v.getClass())) {
			t=Double.parseDouble((String)v);
		}
		else if(Boolean.class.equals(v.getClass())) {
			if( Boolean.parseBoolean((String)v) )
				t=1;
			else
				t=0;
		}
		else
			throw new Exception("unsupported");
		return t;
	}

	@Override
	protected boolean _set(User usr, Object v) {
		if(v==null)
			return false;

		double t;
		try {
			t = toDouble(v);
		}
		catch(Exception e) {
			return false;
		}

		if(t==val_)
			return false;
		val_ = t;

		return true;
	}

	@Override
	protected Object _get(User usr) {
		return new Double(val_);
	}

	public boolean _readXML(Element el) {
		val_ = Double.parseDouble(el.getValue());
		return true;
	}

}
