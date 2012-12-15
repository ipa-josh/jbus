package common;

import java.util.Calendar;
import java.util.Vector;

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
	protected long ts_change_subs_=Calendar.getInstance().getTimeInMillis();
	protected Attribute parent_ = null;

	public Attribute(User usr, Group grp, Attribute parent) {
		super(usr, grp);
		parent_ = parent;
		CallbackInst.addDefaultCallbacks(this);
	}
	public Object get(User usr) {
		synchronized(this) {
			if(!canRead(usr))
				return null;
			return _get(usr);
		}
	}
	public Vector<Attribute> getUpdate(User usr, long ts) {
		synchronized(this) {
			if(!canRead(usr) || (ts_change_<ts&&ts_creation_<ts&&ts_change_subs_<ts))
				return new Vector<Attribute>();
			return _getUpdate(usr, ts);
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

			Object o = _get(usr);
			if(!_set(usr,v))
				return false;

			if(!notifier_.notify(this)) {
				_set(usr, o);
				notifier_.notify(this);
			}
			
			return true;
		}
	}

	public Notifier getNotifier() {
		return notifier_;
	}

	public String getId() {
		return id_;
	}
	
	public String getAbsoluteId() {
		if(parent_==null)
			return "";
		return parent_.getAbsoluteId()+"/"+id_;
	}
	
	public Attribute getRoot() {
		if(parent_==null)
			return this;
		return parent_.getRoot();
	}

	public void setId(String id) {
		id_=id;
	}

	public String getVisualization() {
		return visualization_;
	}

	public void setVisualization(String vis) {
		visualization_ = vis;
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
	
	protected Vector<Attribute> _getUpdate(User usr, long ts) {
		Vector<Attribute> l = new Vector<Attribute>();
		l.add(this);
		return l;
	}

	protected Object _get(User usr, Object v) {return null;}
	protected abstract boolean _set(User usr, Object v);
	protected abstract Object _get(User usr);
	protected abstract boolean _readXML(Element el);
	//protected abstract Object _read();

}
