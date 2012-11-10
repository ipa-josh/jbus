package ui;

import java.io.File;
import java.io.IOException;

import jibble.simplewebserver.SimpleWebServer;

public class Webserver {

	SimpleWebServer server_;
	
	Webserver() {
		try {
			server_ = new SimpleWebServer(new File("./ui/"), 80, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//server_.addHandle(ph)
	}
}
