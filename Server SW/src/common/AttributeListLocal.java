package common;

import HWDriver.AVRNETIO.AvrNetIoBoard;
import HWDriver.COMMAND.Command;
import HWDriver.COMMAND.Editor;
import HWDriver.CONVERTER.Temperature;
import HWDriver.JBUS.JBusInterface;
import HWDriver.LOGIC.AND;
import HWDriver.LOGIC.Delay;
import HWDriver.LOGIC.OR;
import HWDriver.SUNSET.Sunset;
import HWDriver.SUNSET.Time;
import common.attributes.Attr_Boolean;
import common.attributes.Attr_Double;
import common.attributes.Attr_Integer;
import common.attributes.Attr_String;

public class AttributeListLocal implements AttributeList {

	@Override
	public Class[] getList() {
		return new Class [] {HAObject.class, Attr_Boolean.class, Attr_Integer.class, Attr_Double.class, Attr_String.class, AvrNetIoBoard.class, Sunset.class, Time.class, AND.class, OR.class, JBusInterface.class, Temperature.class, Command.class, Editor.class, Delay.class};
	}

}
