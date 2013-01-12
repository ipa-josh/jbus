package history;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;

import rights.Right;
import rights.User;

import common.Attribute;
import common.Callback;
import common.PathWithData;

public class LogSqLite implements Callback {
	private Attribute root_ = null;
	private SQLiteQueue queue_ = null;
	private boolean capture_ = false;
	private static LogSqLite inst_ = null;

	public LogSqLite(Attribute root, String fn) {
		inst_ = this;
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.WARNING); 
		
		root_ = root;

		queue_ = new SQLiteQueue(new File(fn));
		queue_.start();
		queue_.execute(new SQLiteJob<Object>() {
			protected Object job(SQLiteConnection connection) throws SQLiteException {
				connection.exec("create table if not exists history (id TEXT, data TEXT, utime REAL)");
				return null;
			}
		});
	}

	private void log(final Attribute attr, final Date utime, final User usr) {
		synchronized (this) {
			if(!capture_)
				return;
		}
		
		if(!attr.canRead(usr))
			return;
		
		//YYYY-MM-DD HH:MM:SS.SSS
		queue_.execute(new SQLiteJob<Object>() {
			protected Object job(SQLiteConnection connection) throws SQLiteException {
				Object obj = attr.get(usr);
				if(obj==null)
					return null;
				
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

			    SQLiteStatement st = connection.prepare("INSERT INTO history VALUES(?,?,julianday('"+fmt.format(utime)+"'))");
			    st.bind(1, attr.getAbsoluteId());
			    st.bind(2, obj.toString());
			    st.stepThrough();

				return null;
			}
		});
	}

	public Vector<String> query(final Attribute attr, final User usr, final String condition) {
		if(!attr.canRead(usr))
			return new Vector<String>();
		
		//YYYY-MM-DD HH:MM:SS.SSS
		return queue_.execute(new SQLiteJob<Vector<String> >() {
			protected Vector<String> job(SQLiteConnection connection) throws SQLiteException {
			    SQLiteStatement st = connection.prepare("SELECT data, utime FROM history WHERE id=? "+condition);
			    st.bind(1, attr.getAbsoluteId());
			    
				Vector<String> r = new Vector<String>();
			    while(st.step()) {
			    	r.add( st.columnString(0) );
			    	r.add( st.columnString(1) );
			    }

				return r;
			}
		}).complete();
	}

	public Vector<String> queryex(final Attribute attr, final User usr, final long _from_secs, final long _to_secs, final boolean avg) {
		if(!attr.canRead(usr))
			return new Vector<String>();
		
		//YYYY-MM-DD HH:MM:SS.SSS
		return queue_.execute(new SQLiteJob<Vector<String> >() {
			protected Vector<String> job(SQLiteConnection connection) throws SQLiteException {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				double from_secs = _from_secs/(24.*3600.);
				double to_secs   = _to_secs/(24.*3600.);
				
			    SQLiteStatement st = connection.prepare("SELECT "+(avg?"AVG(CAST(data AS Float)), AVG(utime)":"data, utime")+" FROM history WHERE id=? AND utime BETWEEN (julianday('"+fmt.format(new Date())+"')"+(from_secs>=0?"+":"")+from_secs+") AND (julianday('"+fmt.format(new Date())+"')"+(to_secs>=0?"+":"")+to_secs+")");
			    st.bind(1, attr.getAbsoluteId());
			    
				Vector<String> r = new Vector<String>();
			    while(st.step()) {
			    	r.add( st.columnString(0) );
				    r.add( st.columnString(1) );
			    }

				return r;
			}
		}).complete();
	}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		Date d = new Date();
		synchronized(this) {
			log(attr, d, Right.getGlobalUser("ui"));
			notify();
		}
		return false;
	}

	@Override
	public boolean required() {
		// TODO Auto-generated method stub
		return false;
	}

	public void restore(rights.User user) {
		SQLiteStatement st;
		try {
			SQLiteConnection db_ = new SQLiteConnection(queue_.getDatabaseFile());
			try {
				db_.open(false);
			} catch (SQLiteException ex) {
				Logger.getLogger(LogSqLite.class.getName()).log(Level.SEVERE, null, ex);
			}
			st = db_.prepare("SELECT id, data FROM history h WHERE utime = (select Max(utime) FROM history WHERE id=h.id)");
			while(st.step()) {
				PathWithData p = new PathWithData();
				p.parseString(st.columnString(0));
				p.setData(st.columnString(1));
				root_.set(user, p);
			}
			db_.dispose();
		} catch (SQLiteException ex) {
			Logger.getLogger(LogSqLite.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void capture() {
		synchronized (this) {
			capture_ = true;
		}
	}

}
