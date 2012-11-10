import java.io.File;
import java.io.IOException;

import jibble.simplewebserver.SimpleWebServer;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import rights.Right;
import web.*;

import common.HAObject;
import common.Output;


public class HAMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length<1) {
			System.out.println("spedify file to load");
			return;
		}
		
		HARoot root = new HARoot();
		try {
			root.readXML( (new SAXBuilder()).build( new File(args[0]) ).getRootElement() );
		} catch (JDOMException e) {
			Output.error(e);
		} catch (IOException e) {
			Output.error(e);
		}
	}

}
