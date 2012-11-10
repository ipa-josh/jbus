package rights;

import java.util.Vector;

import org.jdom2.Element;

public class Right {
	
	public static class RightSet {
		private boolean usr_readable_=false;
		private boolean usr_writeable_=false;
		
		public RightSet(boolean read, boolean write) {
			usr_readable_ =read;
			usr_writeable_=write;
		}
		public boolean getReadable()  {return usr_readable_;}
		public boolean getWriteable() {return usr_writeable_;}

		public void setReadable(boolean v)  {usr_readable_=v;}
		public void setWriteable(boolean v) {usr_writeable_=v;}
		
		RightSet getClone() {try {
			return (RightSet) clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}}
	}
	
	private User owner_;
	private Group group_;
	
	private RightSet grp_rs_=new RightSet(true,true);
	private RightSet usr_rs_=new RightSet(true,true);
	
	public Right(User usr, Group grp) {
		owner_=usr;
		group_=grp;
	}
	
	RightSet getUserRights() {return usr_rs_.getClone();}
	RightSet getGroupRights() {return grp_rs_.getClone();}
	
	protected final User getUser() {
		return owner_;
	}
	
	protected final Group getGroup() {
		return group_;
	}
	
	public boolean canRead(User usr) {
		if(usr.equals(owner_)&&usr_rs_.getReadable())
			return true;
		if(usr.hasGroup(group_)&&grp_rs_.getReadable())
			return true;
		return false;
	}
	
	public boolean canWrite(User usr) {
		if(usr.equals(owner_)&&usr_rs_.getWriteable())
			return true;
		if(usr.hasGroup(group_)&&grp_rs_.getWriteable())
			return true;
		return false;
	}

	public boolean readXML(Element el) {
		org.jdom2.Attribute t; //TODO: 
		
		owner_ = new User(el.getAttribute("user").getValue());
		group_ = new Group(el.getAttribute("group").getValue());
		int r=Integer.parseInt(el.getAttribute("rights").getValue());
		usr_rs_.setReadable( (((r/10)%10)&1)==1 );
		usr_rs_.setWriteable( (((r/10)%10)&2)==2 );
		grp_rs_.setReadable( (((r)%10)&1)==1 );
		grp_rs_.setWriteable( (((r)%10)&2)==2 );
		return true;
	}
	
	static private class UserList {
		Vector<User> users_ = new Vector<User>();
		
		UserList() {
			users_.add(new User("interlayer"));
			users_.add(new User("ui"));
			users_.add(new User("hw"));
			
			users_.get(0).addGroup(new Group("ui"));
			users_.get(0).addGroup(new Group("hw"));

			users_.get(1).addGroup(new Group("ui"));
			
			users_.get(2).addGroup(new Group("hw"));
		}
	}

	private static UserList _ul_ = new UserList();
	
	public static User getGlobalUser(String string) {
		for(int i=0; i<_ul_.users_.size(); i++)
			if(_ul_.users_.get(i).equals(string))
				return _ul_.users_.get(i);
		return null;
	}
	
	public static User getGlobalUser(User string) {
		for(int i=0; i<_ul_.users_.size(); i++)
			if(_ul_.users_.get(i).equals(string))
				return _ul_.users_.get(i);
		return null;
	}
	
	public static boolean addUser(User u)
	{
		if(getGlobalUser(u)!=null)
			return false;
		_ul_.users_.add(u);
		return true;
	}
}
