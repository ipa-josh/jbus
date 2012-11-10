package HWDriver;

import common.Attribute;
import common.Device;

public interface DriverInterface {

	public String getName();
	public void getDevice(Device result);
	public void setDevice(Device result);
	public void setDeviceAttribute(Device result, Attribute attr);
}
