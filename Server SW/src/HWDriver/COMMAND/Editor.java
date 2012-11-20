package HWDriver.COMMAND;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import common.*;
import common.attributes.Attr_Error;
import common.attributes.Attr_String;
import rights.*;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class Editor extends HAObject implements Callback {

	public static String sFn_ = "";
	private Attr_String response_ = null;

	private static String _descr_ =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>"+
					"<Attr_String id=\"response\"></Attr_String>"+	//output

					//inputs
					"<Attr_Boolean id=\"list_images\"></Attr_Boolean>"+
					"<Attr_String id=\"create\"></Attr_String>"+
					"<Attr_String id=\"remove\"></Attr_String>"+

					"</root>";

	private Attr_Error status_;

	public Editor(User usr, Group grp, Attribute parent) {
		super(Right.getGlobalUser("admin"), Right.getGlobalUser("admin").getFirstGroup(), parent);

		status_ = new Attr_Error(getUser(), getGroup(), parent_);
		status_.setId("editor_status");
	}

	@Override
	public boolean _readXML(Element el) {;
	try {
		if(!super._readXML( (new SAXBuilder()).build( new StringReader(_descr_) ).getRootElement()))
			return false;
		for(int i=0; i<list_.size(); i++) {
			if(list_.get(i).getId().equals("response"))
				response_ = (Attr_String) list_.get(i);
			list_.get(i).getNotifier().addCallback(this);
		}
		return true;
	} catch (JDOMException e) {
		Output.error(e);
	} catch (IOException e) {
		Output.error(e);
	}
	return false;
	}

	private HAObject getObjectFromPath(Path p) throws Exception {		
		Object a = getRoot().get(getUser(),p);

		if(HAObject.class.equals(a.getClass())) {
			return (HAObject)a;
		}
		else {			
			throw new Exception("something went wrong with "+p.toString());
		}
	}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		try {

			if(attr.getId().startsWith("list_images")) {
				response_.set(getUser(), "{\"imgs\":[]}");
			}

			else if(attr.getId().startsWith("create")) {
				String data[] = ((String)attr.get(getUser())).split("|",2);
				String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dummy>"+data[1]+"</dummy>";
				getObjectFromPath( (new Path()).parseString(data[0]) ).addXML(new SAXBuilder().build( xml ).getRootElement());
			}

			else if(attr.getId().startsWith("remove")) {
				Path p = new Path();
				p.parseString((String)attr.get(getUser()));
				String obj = p.popBack();

				getObjectFromPath(p).remove(obj);
			}

			else if(attr.getId().startsWith("response"))
				return true;
		} catch(Exception e) {
			status_.setStatus(getUser(), Attr_Error.STATUS.ERROR, e.toString());
		}
		return false;
	}

	@Override
	public boolean required() {
		return true;
	}

}
