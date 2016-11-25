package org.xmeta.codes;

import org.xmeta.ThingCoder;

public class DmwThingCoder extends DmlThingCoder{
	private static String TYPE = "dmw";
	private static String TYPE_XML = "dmw_xml";
	private static String TYPE_PROPERTY = "dmw_property";

	public DmwThingCoder(ThingCoder xmlThingCoder, ThingCoder txtThingCoder,
			ThingCoder propertyThingCoder) {
		super(xmlThingCoder, txtThingCoder, propertyThingCoder);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean acceptType(String type) {
		return TYPE_PROPERTY.equals(type) || TYPE_XML.equals(type) || TYPE.equals(type);
	}

	@Override
	public String[] getCodeTypes() {
		return new String[]{TYPE_XML, TYPE_PROPERTY};
	}

}
