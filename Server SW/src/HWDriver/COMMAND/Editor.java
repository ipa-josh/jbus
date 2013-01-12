package HWDriver.COMMAND;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Calendar;

import common.*;
import common.attributes.Attr_Boolean;
import common.attributes.Attr_Error;
import common.attributes.Attr_String;
import rights.*;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class Editor extends HAObject implements Callback {

	public static String sFn_ = "";
	private Attr_String response_ = null;
	private Attr_Boolean active_ = null;

	private static String _descr_ =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>"+
					"<Attr_String id=\"response\"></Attr_String>"+	//output

					//inputs
					"<Attr_Boolean id=\"active\"></Attr_Boolean>"+
					"<Attr_Boolean id=\"list_images\"></Attr_Boolean>"+
					"<Attr_String id=\"create\"></Attr_String>"+
					"<Attr_String id=\"remove\"></Attr_String>"+

					"</root>";

	private Attr_Error status_;
	private long timeout_ = 0;
	private long timeout_val_ = 30*60*1000;	//30 min
	private String img_dir_ = "../UI Sw/usr_imgs";
	private String base_dir_= "../UI Sw";

	public Editor(User usr, Group grp, Attribute parent) {
		super(Right.getGlobalUser("admin"), Right.getGlobalUser("admin").getFirstGroup(), parent);
		
		setVisualization("editor");

		status_ = new Attr_Error(getUser(), getGroup(), parent_);
		status_.setId("editor_status");
	}

	@Override
	public boolean _readXML(Element el) {;
	try {
		if(!super._readXML( (new SAXBuilder()).build( new StringReader(_descr_) ).getRootElement()))
			return false;
		
		org.jdom2.Attribute t = el.getAttribute("image_dir");
		if(t!=null)
			img_dir_  = t.getValue();

		t = el.getAttribute("base_dir");
		if(t!=null)
			base_dir_  = t.getValue();
		
		for(int i=0; i<list_.size(); i++) {
			if(list_.get(i).getId().equals("response"))
				response_ = (Attr_String) list_.get(i);
			else if(list_.get(i).getId().equals("active"))
				active_ = (Attr_Boolean) list_.get(i);
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

			/*
			 * first activate editor to prevent heavy load/unwanted behaviour
			 * then editor is active for X ms
			 */
			if(attr.getId().startsWith("active")) {
				timeout_  = Calendar.getInstance().getTimeInMillis()+timeout_val_;
			}
			if(timeout_-Calendar.getInstance().getTimeInMillis()<0)
				return false;

			if(attr.getId().startsWith("list_images")) {
				String l="";
				File d = new File(img_dir_);
				File[] lf = d.listFiles();
				URI base = new File(base_dir_).toURI();
				for(int i=0; i<lf.length; i++) {
					if(lf[i].isFile()) {
						if(l.length()==0) l+="\""+base.relativize(lf[i].toURI()).getPath()+"\"";
						else l+=",\""+base.relativize(lf[i].toURI()).getPath()+"\"";
					}
				}
				l = l.replace("\\","\\\\");
				response_.set(getUser(), "{\"imgs\":["+l+"]}");
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
