package web;

import java.util.Calendar;
import java.util.Vector;

import rights.User;
import jibble.simplewebserver.SimpleWebServer.PathHandle;
import common.Attribute;
import common.Callback;
import common.Path;

public class VisUpdater implements Callback, PathHandle {
	private long timeout_ = 10000;
	private Attribute root_ = null;

	public VisUpdater(Attribute root, long timeout_ms) {
		root_ = root;
		timeout_ = timeout_ms;
	}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		synchronized (this) {
			notify();
		}
		return false;
	}

	@Override
	public boolean required() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getPath() {
		return "/visupdate";
	}

	@Override
	public String onRequest(String url, User user) {
		long ts=0;
		try {
			ts = Long.parseLong(url.substring(getPath().length()));
		}
		catch(NumberFormatException e) {
			return toJSon(user, new Vector<Attribute>(), 0);
		}

		long ts_look = Calendar.getInstance().getTimeInMillis();
		Vector<Attribute> l = root_.getUpdate(user, ts);

		if(l.size()>0)
			return toJSon(user, l, ts_look);

		synchronized (this) {
			try {
				wait(timeout_);
			} catch (InterruptedException e) {
			}
		}

		ts_look = Calendar.getInstance().getTimeInMillis();
		l = root_.getUpdate(user, ts);

		return toJSon(user, l, ts_look);
	}

	private String toJSon(User user, Vector<Attribute> l, long ts_look) {
		String r = "{\"ts\":"+ts_look+",\"c\":{";
		int i=0;
		for(Attribute a : l) {
			if(i>0) r+=",";
			++i;
			r += "\""+a.getAbsoluteId()+"\":";
			r += LookUp.Object2JSon(a.get(user), user, root_.getClass());
		}
		return r+"}}";
	}

}
