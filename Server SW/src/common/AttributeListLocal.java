package common;

import HWDriver.AVRNETIO.AvrNetIoBoard;
import HWDriver.JBUS.JBusInterface;
import HWDriver.SUNSET.Sunset;
import common.attributes.Attr_Boolean;
import common.attributes.Attr_Integer;
import common.attributes.Attr_String;

public class AttributeListLocal implements AttributeList {

	@Override
	public Class[] getList() {
		return new Class [] {HAObject.class, Attr_Boolean.class, Attr_Integer.class, Attr_String.class, AvrNetIoBoard.class, Sunset.class, JBusInterface.class};
	}

}
