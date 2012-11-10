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

import rights.Right;
import rights.User;

import common.Attribute;
import common.Callback;
import common.HAObject;
import common.Path;

import jibble.simplewebserver.SimpleWebServer.PathHandle;

public class LogSqLite extends Thread implements Callback {
	private SQLiteConnection db_ = null;

	private static class LogEntry {
		Attribute attr;
		Date utime = new Date();
	}

	private Vector<LogEntry> buffer_ = new Vector<LogEntry>();

	public LogSqLite(String fn) {
		db_ = new SQLiteConnection(new File(fn));
		try {
			db_.open(true);
			db_.exec("create table if not exists history (id TEXT PRIMARY KEY ASC, data TEXT, utime REAL)");
		} catch (SQLiteException ex) {
			Logger.getLogger(LogSqLite.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		setPriority( Thread.MIN_PRIORITY );
		start();
	}

	private void log(LogEntry e) {
		//YYYY-MM-DD HH:MM:SS.SSS
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			db_.exec("INSERT INTO history ('"+e.attr.getId()+"','"+e.attr.toString()+"',julianday('"+fmt.format(e.utime)+"'))");
		} catch (SQLiteException ex) {
			Logger.getLogger(LogSqLite.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		LogEntry e = new LogEntry();
		e.attr = attr;
		synchronized(this) {
			buffer_.add(e);
		}
		notify();
		return false;
	}

	@Override
	public boolean required() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void run() {
		while(true) {
			synchronized (this) {
				while( buffer_.size()>0 ) {
					log(buffer_.get(0));
					buffer_.remove(0);
				}
			}
			
			try {
				wait();
			} catch (InterruptedException e) {
				try {
					sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
			
		}
	}

}
