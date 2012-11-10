package common;

import java.util.Calendar;

import org.jdom2.*;
import rights.Group;
import rights.Right;
import rights.User;

public abstract class Attribute extends Right {
	protected Notifier notifier_ = new Notifier();
	protected String id_="";
	protected String visualization_ = "none";
	protected long ts_creation_=Calendar.getInstance().getTimeInMillis();
	protected long ts_change_=Calendar.getInstance().getTimeInMillis();

	public Attribute(User usr, Group grp) {
		super(usr, grp);
		CallbackInst.addDefaultCallbacks(this);
	}
	public Object get(User usr) {
		synchronized(this) {
			if(!canRead(usr))
				return null;
			return _get(usr);
		}
	}
	public Object get(User usr, Object v) {
		synchronized(this) {
			if(!canRead(usr))
				return null;
			return _get(usr,v);
		}
	}
	public boolean set(User usr, Object v) {
		synchronized(this) {
			if(!canWrite(usr))
				return false;

			if(!_set(usr,v))
				return false;

			notifier_.notify(this);
			return true;
		}
	}

	public Notifier getNotifier() {
		return notifier_;
	}

	public String getId() {
		return id_;
	}

	public void setId(String id) {
		id_=id;
	}

	public String getVisualization() {
		return visualization_;
	}
	
	public boolean readXML(Element el) {
		//if(!super.readXML(el))
		//	return false;
		org.jdom2.Attribute t = el.getAttribute("id");
		if(t==null) {
			Output.error("missing id for Attribute");
			return false;
		}
		id_ = t.getValue();
		
		t=el.getAttribute("visualization");
		if(t!=null)
			visualization_ = t.getValue();
		
		return _readXML(el);
	}

	protected Object _get(User usr, Object v) {return null;}
	protected abstract boolean _set(User usr, Object v);
	protected abstract Object _get(User usr);
	protected abstract boolean _readXML(Element el);
	//protected abstract Object _read();

}
