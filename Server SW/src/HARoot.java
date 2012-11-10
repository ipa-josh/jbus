import history.LogSqLite;

import java.io.File;
import java.io.IOException;

import jibble.simplewebserver.SimpleWebServer;

import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import rights.Group;
import rights.Right;
import rights.User;
import web.LookUp;
import web.Setter;
import web.VisJQuery;
import web.VisUpdater;
import common.CallbackInst;
import common.HAObject;
import common.Output;


public class HARoot extends HAObject {

	public HARoot() {
		super(Right.getGlobalUser("ui"),Right.getGlobalUser("ui").getFirstGroup());
	}

	public boolean _readXML(Element el) {
		
		VisUpdater visup = new VisUpdater();
		
		CallbackInst.addDefaultCallback(new LogSqLite(this, "./history.db"));
		CallbackInst.addDefaultCallback(visup);
		
		boolean r = super._readXML(el);
		
		int port=8080;
		String visxml="", auth="";
		
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
		
		try {
			SimpleWebServer server = new SimpleWebServer(new File("./ui"),port, new rights.Authentification(new File(auth)));
			
			server.addHandle(new LookUp(this));
			server.addHandle(new Setter(this));
			server.addHandle(new VisJQuery(new File(visxml)));
		} catch (IOException e) {
			Output.error(e);
		}
		
		System.out.println("Started...");
		
		return r;
	}

}
