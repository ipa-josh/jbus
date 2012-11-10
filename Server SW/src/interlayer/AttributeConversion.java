package interlayer;

import rights.User;
import common.Attribute;
import common.Callback;
import common.Output;

public class AttributeConversion implements Callback {
	
	protected User user_=null;
	protected Attribute attr_a_ = null;
	protected Attribute attr_b_ = null;

	public AttributeConversion(Attribute a, Attribute b, User usr) {
		user_=usr;
		attr_a_=a;
		attr_b_=b;
		if(attr_a_==null||attr_b_==null) {
			Output.error("null pointer in AttributeConversion()");
		}
		else {
			attr_a_.getNotifier().addCallback(this);
			attr_b_.getNotifier().addCallback(this);
		}
	}

	@Override
	public boolean onAttributeChanged(Attribute attr) {
		if(attr_a_==null||attr_b_==null||attr==null) {
			Output.error("null pointer in AttributeConversion");
			return false;
		}
		if(attr==attr_a_) {
			return attr_b_.set(user_, attr_a_.get(user_));
		}
		else if(attr==attr_b_) {
			return attr_a_.set(user_, attr_b_.get(user_));
		}
		return false;
	}

	@Override
	public boolean required() {
		return true;
	}

}
