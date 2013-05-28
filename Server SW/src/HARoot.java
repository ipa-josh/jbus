import history.LogSqLite;

import java.io.File;
import java.io.IOException;

import jibble.simplewebserver.SimpleWebServer;

import org.jdom2.DataConversionException;
import org.jdom2.Element;
import rights.Right;
import web.History;
import web.LookUp;
import web.Setter;
import web.VisJQuery;
import web.VisUpdater;
import common.CallbackInst;
import common.ConnectionInst;
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
		
		//add error handler
		HAObject error_handler = new HAObject(Right.getGlobalUser("ui"),Right.getGlobalUser("ui").getFirstGroup(), this);
		error_handler.setId("error_handler");
		error_handler.setVisualization("error");
		add( error_handler );
		
		boolean r = super.addXML(el);
		
		//add handler for editor
		/*HAObject editor_handler = new HAObject(Right.getGlobalUser("admin"),Right.getGlobalUser("admin").getFirstGroup(), this);
		editor_handler.setId("error_handler");
		editor_handler.setVisualization("error");
		add( editor_handler );*/
		
		int port=8080;
		String visxml="", auth="";
		boolean restore = false;
		int restore_ms = 1000;
		
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
			restore = true;
			restore_ms = Integer.parseInt(t.getValue());
		} catch(Exception e) {Output.warning("restore not set");}
		
		try {
			SimpleWebServer server = new SimpleWebServer(new File("../UI Sw"),port, new rights.Authentification(new File(auth)));
			
			server.addHandle(new LookUp(this));
			server.addHandle(new Setter(this));
			server.addHandle(new VisJQuery(new File(visxml)));
			server.addHandle(visup);
			server.addHandle(new History(history, this));
		} catch (IOException e) {
			Output.error(e);
		}
		
		System.out.println("Started...");
		
		if(restore) {
			try {
				Thread.sleep(restore_ms);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			history.restore(Right.getGlobalUser("ui"));
		}
		
		history.capture();
		
		return r;
	}

}
