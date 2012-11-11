package interlayer;

public class AttrConvListLocal implements AttrConvList {

	@Override
	public Class[] getList() {
		return new Class [] {AttributeConversion.class, AttrConv_Button.class};
	}

}
