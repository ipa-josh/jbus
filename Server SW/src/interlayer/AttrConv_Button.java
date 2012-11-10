package interlayer;

import rights.User;

import common.Attribute;
import common.Output;

public class AttrConv_Button extends AttributeConversion {

	public AttrConv_Button(Attribute a, Attribute b, User usr) {
		super(a, b, usr);
	}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		if(attr_a_==null||attr_b_==null||attr==null) {
			Output.error("null pointer in AttributeConversion");
			return false;
		}
		try {
			if(attr==attr_b_) {
				if(!toBool(attr_b_.get(user_)))
					return attr_a_.set(user_, new Boolean(!toBool(attr_a_.get(user_))));
			}
		} catch (NotABool e) {
			Output.error(e);
		}
		return false;
	}

	private static class NotABool extends Exception {
		private static final long serialVersionUID = -6896567027778449444L;
	}

	public boolean toBool(Object v) throws NotABool {
		if(v==null)
			throw new NotABool();

		boolean t;
		if(Boolean.class.equals(v.getClass())) {
			t=(Boolean)v;
		}
		else
			throw new NotABool();

		return t;
	}

}
