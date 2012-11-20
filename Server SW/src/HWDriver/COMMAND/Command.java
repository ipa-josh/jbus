package HWDriver.COMMAND;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import common.*;
import rights.*;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class Command extends HAObject implements Callback {

	public static String sFn_ = "";

	private static String _descr_ =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>"+
					"<Attr_Boolean id=\"reboot\">0</Attr_Boolean>"+
					"</root>";

	public Command(User usr, Group grp, Attribute parent) {
		super(Right.getGlobalUser("admin"), Right.getGlobalUser("admin").getFirstGroup(), parent);
	}

	@Override
	public boolean _readXML(Element el) {;
	try {
		if(!super._readXML( (new SAXBuilder()).build( new StringReader(_descr_) ).getRootElement()))
			return false;
		for(int i=0; i<list_.size(); i++)
			list_.get(i).getNotifier().addCallback(this);
		return true;
	} catch (JDOMException e) {
		Output.error(e);
	} catch (IOException e) {
		Output.error(e);
	}
	return false;
	}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		if(attr.getId().startsWith("reboot")) {
			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

			/* Build command: java -jar application.jar */
			final ArrayList<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add("JHA.jar");
			command.add(sFn_);

			final ProcessBuilder builder = new ProcessBuilder(command);
			try {
				builder.start();
			} catch (IOException e) {
				Output.error(e);
			}
			System.exit(0);
		}
		return false;
	}

	@Override
	public boolean required() {
		return true;
	}

}
