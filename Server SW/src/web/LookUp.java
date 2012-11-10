package web;

import java.util.Vector;

import rights.Right;
import rights.User;

import common.Attribute;
import common.HAObject;
import common.Path;

import jibble.simplewebserver.SimpleWebServer.PathHandle;

public class LookUp implements PathHandle {
	Attribute root_ = null;
	
	public LookUp(Attribute root) {
		root_ = root;
	}
	

	@Override
	public String getPath() {
		return "/get";
	}
	
	private String Attribute2JSon(Attribute attr, Vector<String> adds){
		String r="{\"id\":\""+attr.getId()+"\", \"vis\":\""+attr.getVisualization()+"\", \"base\":\""+attr.getClass().getSimpleName()+"\"";
		for(int i=0; i<adds.size(); i++)
			r+=", "+adds.get(i);
		return r+"}";
	}

	@Override
	public String onRequest(String url, rights.User user) {
		Vector<String> adds = new Vector<String>();
		Path p = new Path();
		p.parseString(url.substring(getPath().length()));
		
		if(root_!=null) {
			Object o = root_.get(user, p);
			if(o==null)
				return "{}";
			
			if(o.getClass().equals(HAObject.class)||o.getClass().equals(root_.getClass())) {
				Vector<Path> paths = ((HAObject)o).getSubs(user);
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
				return "{\"base\":\"data\", \"data\":\""+o.toString()+"\"}";
		}
		
		return "{}";
	}

}
