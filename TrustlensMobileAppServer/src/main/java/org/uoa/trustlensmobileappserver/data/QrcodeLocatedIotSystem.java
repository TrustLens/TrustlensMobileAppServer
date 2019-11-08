package org.uoa.trustlensmobileappserver.data;

public class QrcodeLocatedIotSystem extends LocatedIotSystem{
	
	public QrcodeLocatedIotSystem(String id, String name, String description, String location, double latitude,
			double longitude, String postcode, String qrcode, String uri, String otherinfo) {
		super(id, name, description, location, latitude, longitude, postcode, uri, otherinfo);
		this.qrcode = qrcode;
	}

	private String qrcode;
	
	
	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}
	
	public LocatedIotSystem toLocatedIotSystem() {
		return new LocatedIotSystem(id, name, description, location, latitude, longitude, postcode, uri, otherinfo);
	}

	public QrcodeIotSystem toQrcodeIotSystem() {
		return new QrcodeIotSystem(id, name, description, qrcode, uri, otherinfo);
	}

}
