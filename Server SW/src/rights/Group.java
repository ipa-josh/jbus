package rights;

public class Group {
	private String name_;
	
	public Group(String n) {
		name_=n;
	}

	public boolean equals(Group grp) {
		return name_.equals(grp.name_);
	}

	public boolean equals(Object obj) {
		if(obj!=null && obj.getClass().equals(String.class))
			return equals(new Group((String)obj));
		else if(obj!=null && obj.getClass().equals(Group.class))
			return equals((Group)obj);
		return false;
	}
}
