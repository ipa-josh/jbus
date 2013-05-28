package web;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import common.Output;

import jibble.simplewebserver.SimpleWebServer.PathHandle;

public class VisJQuery implements PathHandle {

	private Map<String, String> jsons_ = new HashMap<String, String>();

	public VisJQuery(File visxml) {
		Element root;
		try {
			root = (new SAXBuilder()).build( visxml ).getRootElement();
		} catch (JDOMException e) {
			Output.error(e);
			return;
		} catch (IOException e) {
			Output.error(e);
			return;
		}

		for(Element el : root.getChildren()) {
			jsons_.put(el.getName().replace("__", "/"), buildJSon(el));
		}
	}

	private String buildJSon(Element el) {
		if(el.getChildren().size()==0)
			return "\""+el.getValue()+"\"";

		int i=0;
		String r="{";
		for(Element e:el.getChildren()) {
			if(i!=0)
				r+=",";
			++i;
			r+="\""+e.getName()+"\":"+buildJSon(e);
		}
		return r+"}";
	}

	@Override
	public String getPath() {
		return "/vis/";
	}

	@Override
	public String onRequest(String url, rights.User user) {
		if(user==null || url==null) return "";
		
		String key = url.substring(getPath().length());
		if(jsons_.containsKey(key))
			return jsons_.get(key);
		return "";
	}

}
