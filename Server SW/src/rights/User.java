package rights;

import java.util.Vector;

public class User {
	private Vector<Group> groups_ = new Vector<Group>();
	private String name_;

	public User(String n) {
		name_=n;
	}

	public boolean equals(Object obj) {
		if(obj!=null && obj.getClass().equals(String.class))
			return equals(new User((String)obj));
		else if(obj!=null && obj.getClass().equals(User.class))
			return equals((User)obj);
		return false;
	}
	
	public boolean equals(User usr) {
		return name_.equals(usr.name_);
	}

	public boolean hasGroup(Group grp) {
		for(int i=0; i<groups_.size(); i++)
			if( groups_.get(i).equals(grp) )
				return true;
		return false;
	}

	public void addGroup(Group grp) {
		groups_.add(grp);
	}

	public Group getFirstGroup() {
		if(groups_.size()>0)
			return groups_.get(0);
		return null;
	}
}
