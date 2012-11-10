package interlayer;

import common.Attribute;
import common.Output;

public class AttrConvListLocal implements AttrConvList {

	@Override
	public Class[] getList() {
		return new Class [] {AttributeConversion.class, AttrConv_Button.class};
	}

}
