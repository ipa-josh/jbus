package jibble.simplewebserver;

import rights.User;

public interface Authentification {

	User login(String user, String pswd);

}
