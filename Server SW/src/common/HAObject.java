package common;

import interlayer.AttrConvList;
import interlayer.AttributeConversion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.ServiceLoader;
import java.util.Vector;

import org.jdom2.Element;

import rights.Group;
import rights.Right;
import rights.User;

public class HAObject extends Attribute {
	protected Vector<Attribute> list_ = new Vector<Attribute>();

	public HAObject(User usr, Group grp, Attribute parent) {
		super(usr, grp, parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean _set(User usr, Object v) {
		if(v==null || !PathWithData.class.equals(v.getClass()))
			return false;

		PathWithData pwd = (PathWithData)v;
		String s=pwd.popFront();
		for(int i=0; i<list_.size(); i++)
			if(list_.get(i).getId().equals(s))
				if( list_.get(i).set(usr, pwd.get()) )
				{
					ts_change_subs_=Calendar.getInstance().getTimeInMillis();
					return true;
				}
				else
					return false;
		return false;
	}

	@Override
	public Object _get(User usr) {
		return this;
	}

	public Vector<Path> getSubs(User usr) {
		Vector<Path> r = new Vector<Path>();

		for(int i=0; i<list_.size(); i++) {
			if(!list_.get(i).canRead(usr))
				continue;
			Path p=new Path();
			p.parseString(list_.get(i).getId());
			r.add(p);
		}
		return r;
	}
	
	public static class SubPaths {
		public Vector<Path> paths;
		
		public SubPaths(Vector<Path> p) {
			paths=p;
		}
	}

	@Override
	protected Object _get(User usr, Object v) {
		if(v==null)
			return new SubPaths(getSubs(usr));
		
		if(!Path.class.equals(v.getClass()))
			return null;

		Path pth = (Path)v;
		String s=pth.popFront();
		if(s.length()==0)
			return _get(usr);

		for(int i=0; i<list_.size(); i++)
			if(list_.get(i).getId().equals(s)) {
				if(pth.empty()) {
					if(pth.getAttribute())
						return list_.get(i);
					else
						return list_.get(i).get(usr);
				}
				else
					return list_.get(i).get(usr, pth);
			}
		return null;
	}

	protected Vector<Attribute> _getUpdate(User usr, long ts) {
		Vector<Attribute> l = new Vector<Attribute>();
		if(ts_change_>=ts||ts_creation_>=ts)
			l.add(this);
		for(int i=0; i<list_.size(); i++)
			l.addAll( list_.get(i).getUpdate(usr, ts) );
		return l;
	}

	public boolean _readXML(Element el) {		
		list_.clear();
		return addXML(el);
	}

	public boolean addXML(Element el) {
		ConnectionInst.get().readXML(el, getAbsoluteId());
		
		ts_change_=Calendar.getInstance().getTimeInMillis();

		for( Element sub : el.getChildren())
		{
			if(sub.getName().equals("connection"))
				continue;
			boolean found=false;
			ServiceLoader<AttributeList> attributeServices = ServiceLoader.load( AttributeList.class );
			for ( AttributeList atts : attributeServices )  {
				Class [] list = atts.getList();
				for(int j=0; j<list.length; j++) {
					if(sub.getName().equals(list[j].getSimpleName()) && Attribute.class.isAssignableFrom(list[j])) {

						try {
							Constructor<? extends Attribute> constructor = list[j].getConstructor(new Class[] {User.class, Group.class, Attribute.class});
							Attribute temp = constructor.newInstance(new Object[] {getUser(),getGroup(), this});

							list_.add(temp);
							
							if(!temp.readXML(sub)) {
								temp.onRemove();
								list_.remove(temp);
								Output.error("Failed to load "+sub.getName());
								return false;
							}

							found=true;
							break;
						} catch (NoSuchMethodException e) {
							Output.error(e);
						} catch (InstantiationException e) {
							Output.error(e);
						} catch (IllegalAccessException e) {
							Output.error(e);
						} catch (InvocationTargetException e) {
							Output.error(e);
						}
					}
				}
			}
			if(!found) {
				Output.warning("Element "+sub.getName()+" not found");
			}
		}

		/*for( Element sub : el.getChildren())
		{
			if(!sub.getName().equals("connection"))
				continue;

			org.jdom2.Attribute ta=sub.getAttribute("a");
			org.jdom2.Attribute tb=sub.getAttribute("b");
			if(ta==null) {
				Output.error("missing a for connection");
				continue;
			}
			if(tb==null) {
				Output.error("missing b for connection");
				continue;
			}
			Path p = new Path();
			p.setAttribute();
			p.parseString(ta.getValue());
			Object a=get(Right.getGlobalUser("interlayer"), p);
			p.parseString(tb.getValue());
			Object b=get(Right.getGlobalUser("interlayer"), p);

			org.jdom2.Attribute t=sub.getAttribute("type");
			String type="";
			if(t!=null)
				type = t.getValue();

			if(a!=null && Attribute.class.isAssignableFrom(a.getClass()) && b!=null && Attribute.class.isAssignableFrom(b.getClass())) {
				if(type.equals("")) 
					new AttributeConversion((Attribute)a,(Attribute)b,Right.getGlobalUser("interlayer"));
				else {

					boolean found=false;
					ServiceLoader<AttrConvList> attributeServices = ServiceLoader.load( AttrConvList.class );
					for ( AttrConvList atts : attributeServices )  {
						Class [] list = atts.getList();
						for(int j=0; j<list.length; j++) {
							if(type.equals(list[j].getSimpleName()) && AttributeConversion.class.isAssignableFrom(list[j])) {

								try {
									Constructor<? extends AttributeConversion> constructor = list[j].getConstructor(new Class[] {Attribute.class, Attribute.class, User.class});
									AttributeConversion temp = constructor.newInstance(new Object[] {(Attribute)a,(Attribute)b,Right.getGlobalUser("interlayer")});

									found=true;
									break;
								} catch (NoSuchMethodException e) {
									Output.error(e);
								} catch (InstantiationException e) {
									Output.error(e);
								} catch (IllegalAccessException e) {
									Output.error(e);
								} catch (InvocationTargetException e) {
									Output.error(e);
								}
							}
						}
					}
					if(!found)
						Output.warning("Connection "+type+" not found");
				}
			}
			else
				Output.error("could not connect "+ta.getValue()+" and "+tb.getValue() + " ("+(a==null?"0":"1")+(b==null?"0":"1")+")");
		}*/

		ts_change_=Calendar.getInstance().getTimeInMillis();

		return true;
	}

	public void add(Attribute status) {
		list_.add(status);
	}

	public boolean remove(String s) {
		for(int i=0; i<list_.size(); i++)
			if(list_.get(i).getId().equals(s)) {
				list_.remove(i);
				return true;
			}
		return false;
	}

	protected void _setId(String id) {
		super._setId(id);
		for(int i=0; i<list_.size(); i++)
			list_.get(i)._setId(list_.get(i).getId());
	}

}
