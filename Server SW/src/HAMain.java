import java.io.File;
import java.io.IOException;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import HWDriver.COMMAND.Command;

import common.Output;


public class HAMain {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*if(args.length<1) {
			System.out.println("spedify file to load");
			return;
		}
		String fn = args[0];*/
		String fn = "demo.xml";
		
		Command.sFn_ = fn;
		
		HARoot root = new HARoot();
		try {
			root.readXML( (new SAXBuilder()).build( new File(fn) ).getRootElement() );
		} catch (JDOMException e) {
			Output.error(e);
		} catch (IOException e) {
			Output.error(e);
		}
	}

}
