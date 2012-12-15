package common.attributes;

import java.util.Calendar;

import org.jdom2.Element;

import rights.Group;
import rights.Right;
import rights.User;
import common.Attribute;
import common.HAObject;
import common.Output;
import common.Path;

public class Attr_Error extends Attribute {
	public enum STATUS {
		ERROR("e"), WARNING("w"), OK("o");
		
		String s;
		STATUS(String str) {s=str;}
		
		public static STATUS toSTATUS(String s) {
			if(s.equals(OK))
				return OK;
			if(s.equals(WARNING))
				return WARNING;
			return ERROR;
		}
	};
	
	STATUS status_ = STATUS.OK;
	String descr_ = "";

	public Attr_Error(User usr, Group grp, Attribute parent) {
		super(usr, grp, (HAObject)(parent.getRoot().get(usr, (new Path()).parseString("/error_handler"))) );

		((HAObject)parent_).add(this);
	}

	//prevent from replacing error handler
	public void setId(String id) {
		if(parent_.get(getUser(),(new Path()).parseString(id))!=null)
			setId(id+"_");
		else
			id_=id;
	}
	
	public void setStatus(User usr, STATUS s, String descr) {
		switch(s) {
		case ERROR:
			Output.error(descr);
			break;
		case WARNING:
			Output.warning(descr);
			break;
		default:
			break;
		}
		set(usr, s+","+descr);
	}

	@Override
	protected boolean _set(User usr, Object v) {
		if(v==null)
			return false;

		STATUS status;
		String descr;
		if(String.class.equals(v.getClass())) {
			String str[] = ((String)v).split(",", 2);
			status = STATUS.toSTATUS(str[0]);
			descr  = str[1];
		}
		else
			return false;
		if(status_==status && descr_.equals(descr))
			return false;
		status_ = status;
		descr_  = descr;

		ts_change_=Calendar.getInstance().getTimeInMillis();
		return true;
	}

	@Override
	protected Object _get(User usr) {
		return new String(status_+","+descr_);
	}
	
	public boolean _readXML(Element el) {
		//val_ = Long.parseLong(el.getValue());
		return true;
	}

	public void clear(User usr) {
		setStatus(usr, STATUS.OK, "");
	}

}
