import history.LogSqLite;

import java.io.File;
import java.io.IOException;

import jibble.simplewebserver.SimpleWebServer;

import org.jdom2.DataConversionException;
import org.jdom2.Element;
import rights.Right;
import web.LookUp;
import web.Setter;
import web.VisJQuery;
import web.VisUpdater;
import common.CallbackInst;
import common.HAObject;
import common.Output;


public class HARoot extends HAObject {

	public HARoot() {
		super(Right.getGlobalUser("ui"),Right.getGlobalUser("ui").getFirstGroup(), null);
	}

	public boolean _readXML(Element el) {
		
		VisUpdater visup = new VisUpdater(this, 10000);
		LogSqLite history = new LogSqLite(this, "./history.db");
		
		CallbackInst.addDefaultCallback(history);
		CallbackInst.addDefaultCallback(visup);
		
		boolean r = super._readXML(el);
		
		int port=8080;
		String visxml="", auth="";
		boolean restore = true;
		
		org.jdom2.Attribute t = el.getAttribute("port");
		if(t!=null)
			try {
				port = t.getIntValue();
			} catch (DataConversionException e1) {
				Output.error(e1);
				return false;
			}
		
		t = el.getAttribute("visxml");
		if(t!=null)
			visxml = t.getValue();
		t = el.getAttribute("auth");
		if(t!=null)
			auth = t.getValue();
		t = el.getAttribute("restore");
		try {
		if(t!=null)
			restore = Boolean.parseBoolean(t.getValue());
		} catch(Exception e) {Output.error(e);}
		
		try {
			SimpleWebServer server = new SimpleWebServer(new File("../UI Sw"),port, new rights.Authentification(new File(auth)));
			
			server.addHandle(new LookUp(this));
			server.addHandle(new Setter(this));
			server.addHandle(new VisJQuery(new File(visxml)));
			server.addHandle(visup);
		} catch (IOException e) {
			Output.error(e);
		}
		
		System.out.println("Started...");
		
		if(restore) history.restore(Right.getGlobalUser("ui"));
		
		return r;
	}

}
