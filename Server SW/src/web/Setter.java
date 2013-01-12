package web;

import java.net.URLDecoder;

import common.Attribute;
import common.PathWithData;

import jibble.simplewebserver.SimpleWebServer.PathHandle;

public class Setter implements PathHandle {
	private Attribute root_ = null;
	
	public Setter(Attribute root) {
		root_ = root;
	}
	

	@Override
	public String getPath() {
		return "/set";
	}

	@Override
	public String onRequest(String url, rights.User user) {
		PathWithData p = new PathWithData();
		p.parseString(url.substring(getPath().length()));
		p.fromPath();
		
		p.setData( URLDecoder.decode((String) p.getData()) );
		
		if(root_!=null) {
			return ""+root_.set(user, p);
		}
		
		return "false";
	}

}
