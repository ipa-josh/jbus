package rights;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import common.Output;

import HWDriver.AVRNETIO.AvrNetIo;

public class Authentification implements jibble.simplewebserver.Authentification {
	
	static class AuthUser {
		private String user_, pswd_;
		
		AuthUser(String user, String pswd, Vector<String> groups)
		{
			user_ = user;
			pswd_ = pswd;
			
			User u = new User(user_);
			for(int i=0; i<groups.size(); i++)
				u.addGroup(new Group(groups.get(i)));
			Right.addUser(u);
		}
		
		public String getUser() {return user_;}
		
		public boolean check(String pswd) {return pswd_.toUpperCase().equals(pswd.toUpperCase());}
	}
	
	Map<String, AuthUser> users_ = new HashMap<String, AuthUser>();

	public Authentification(File visxml) {
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
			if(el.getName().equals("user"))
				addUser(parseUser(el));
		}
	}

	private AuthUser parseUser(Element el) {
		String user = null, pswd = null;
		Vector<String> groups = new Vector<String>();
		for(Element e:el.getChildren()) {
			if(e.getName().equals("user"))
				user = e.getValue();
			else if(e.getName().equals("password"))
				pswd = e.getValue();
			else if(e.getName().equals("group"))
				groups.add( e.getValue() );
		}
		if(user==null || pswd==null)
		{
			Output.error("Authentification: invalid XML file");
			return null;
		}
		return new AuthUser(user, pswd, groups);
	}
	
	private void addUser(AuthUser u)
	{
		if(users_.containsKey(u.getUser()))
		{
			Output.error("Authentification: username exists twice...ignoring");
			return;
		}
		users_.put(u.getUser(), u);
	}
	
    private static String calculateHash(String str) throws Exception{
		MessageDigest algorithm = MessageDigest.getInstance("SHA1");

        DigestInputStream   dis = new DigestInputStream(new StringBufferInputStream(str), algorithm);

        // read the file and update the hash calculation
        while (dis.read() != -1);

        // get the hash value as byte array
        byte[] hash = algorithm.digest();

        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }


	@Override
	public User login(String user, String pswd) {
		if(users_.containsKey(user))
		{
			try {
				if(users_.get(user).check(calculateHash(pswd)))
					return Right.getGlobalUser(user);
			} catch (Exception e) {
				Output.error(e);
			}
		}
		return null;
	}

}
