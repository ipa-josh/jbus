package web;

import java.util.Vector;

import org.json.simple.JSONValue;

import common.Attribute;
import common.HAObject;
import common.HAObject.SubPaths;
import common.Path;

import jibble.simplewebserver.SimpleWebServer.PathHandle;

public class LookUp implements PathHandle {
	private Attribute root_ = null;
	
	public LookUp(Attribute root) {
		root_ = root;
	}
	

	@Override
	public String getPath() {
		return "/get";
	}
	
	static private String Attribute2JSon(Attribute attr, Vector<String> adds){
		String r="{\"id\":\""+attr.getId()+"\", \"vis\":\""+attr.getVisualization()+"\", \"base\":\""+attr.getClass().getSimpleName()+"\"";
		for(int i=0; i<adds.size(); i++)
			r+=", "+adds.get(i);
		return r+"}";
	}

	@Override
	public String onRequest(String url, rights.User user) {
		if(user==null || url==null) return "";
		
		Path p = new Path();
		p.parseString(url.substring(getPath().length()));
		
		if(root_!=null) {
			Object o = root_.get(user, p);
			return Object2JSon(o, user, root_.getClass());
		}
		
		return "{}";
	}
	
	static public String Object2JSon(Object o, rights.User user, Object root_class) {
		if(o==null)
			return "{}";

		Vector<String> adds = new Vector<String>();
		Object data = null;
		if(HAObject.class.isAssignableFrom(o.getClass()))
			data = ((HAObject)o).get(user,null);
		//if(o.getClass().equals(HAObject.class)||o.getClass().equals(root_class)) {
			//Vector<Path> paths = ((HAObject)o).getSubs(user);
		if( data!=null && data.getClass().equals(SubPaths.class)) {
			Vector<Path> paths = ((SubPaths)data).paths;
			String r="";
			for(int i=0; i<paths.size(); i++)
				r+=(i==0?"":",")+"\""+paths.get(i).toString()+"\"";
			adds.add("\"subs\":["+r+"]");
			return Attribute2JSon((HAObject)o,adds);
		}
		else if(o.getClass().equals(Attribute.class)) {
			return Attribute2JSon((Attribute)o,adds);
		}
		else
			return "{\"base\":\"data\",\"data\":\""+JSONValue.escape(o.toString())+"\"}";
	}

}
