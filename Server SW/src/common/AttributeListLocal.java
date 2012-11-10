package common;

import HWDriver.AVRNETIO.AvrNetIoBoard;
import common.attributes.Attr_Boolean;
import common.attributes.Attr_Integer;

public class AttributeListLocal implements AttributeList {

	@Override
	public Class[] getList() {
		return new Class [] {HAObject.class, Attr_Boolean.class, Attr_Integer.class, AvrNetIoBoard.class};
	}

}
