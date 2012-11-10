package web;

import java.util.Vector;

import rights.Right;
import rights.User;

import common.Attribute;
import common.HAObject;
import common.Path;
import common.PathWithData;

import jibble.simplewebserver.SimpleWebServer.PathHandle;

public class Setter implements PathHandle {
	Attribute root_ = null;
	
	public Setter(Attribute root) {
		root_ = root;
	}
	

	@Override
	public String getPath() {
		return "/set";
	}

	@Override
	public String onRequest(String url, rights.User user) {
		Vector<String> adds = new Vector<String>();
		PathWithData p = new PathWithData();
		p.parseString(url.substring(getPath().length()));
		p.fromPath();
		
		if(root_!=null) {
			return ""+root_.set(user, p);
		}
		
		return "false";
	}

}
