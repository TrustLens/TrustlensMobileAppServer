package org.uoa.trustlensmobileappserver.data;

public class QrcodeIotSystem extends IotSystem {

	private String qrcode;
	
	public QrcodeIotSystem(String id, String name, String description, String qrcode, String uri, String otherinfo) {
		super(id, name, description, uri, otherinfo);
		this.qrcode = qrcode;
		
		// TODO Auto-generated constructor stub
	}

	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}

	@Override
	public String toString() {
		return "QrcodeIotSystem [qrcode=" + qrcode + ", id=" + id + ", name=" + name + ", description=" + description
				+ ", otherinfo=" + otherinfo + "]";
	}
	

}
