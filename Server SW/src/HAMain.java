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
		if(args.length<1) {
			System.out.println("spedify file to load");
			return;
		}
		String fn = args[0];
		
		Command.sFn_ = fn;
		
		if(args.length>=2) {
			try {
				Thread.sleep( Long.parseLong(args[1]) );
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
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
