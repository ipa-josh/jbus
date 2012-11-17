package history;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import HWDriver.AVRNETIO.AvrNetIo;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;

import rights.Right;
import rights.User;

import common.Attribute;
import common.Callback;
import common.HAObject;
import common.Path;
import common.PathWithData;

import jibble.simplewebserver.SimpleWebServer.PathHandle;

public class LogSqLite implements Callback {
	Attribute root_ = null;
	SQLiteQueue queue_ = null;
	boolean capture_ = false;

	public LogSqLite(Attribute root, String fn) {
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
		//YYYY-MM-DD HH:MM:SS.SSS
		queue_.execute(new SQLiteJob<Object>() {
			protected Object job(SQLiteConnection connection) throws SQLiteException {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				//System.out.println("INSERT INTO history VALUES('"+attr.getAbsoluteId()+"','"+attr.get(usr)+"',julianday('"+fmt.format(utime)+"'))");
				
			    SQLiteStatement st = connection.prepare("INSERT INTO history VALUES(?,?,julianday('"+fmt.format(utime)+"'))");
			    st.bind(1, attr.getAbsoluteId());
			    st.bind(2, attr.get(usr).toString());
			    st.stepThrough();

				return null;
			}
		});
	}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		System.out.println(attr.getAbsoluteId());
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
