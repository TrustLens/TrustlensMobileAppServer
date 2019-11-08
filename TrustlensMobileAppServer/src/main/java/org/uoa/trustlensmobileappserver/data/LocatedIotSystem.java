package org.uoa.trustlensmobileappserver.data;

public class LocatedIotSystem extends IotSystem {
	
	String location;
	double latitude;
	double longitude;
	String postcode;
	String qrcode;
	
	public LocatedIotSystem(String id, String name, String description, String location, double latitude,
			double longitude, String postcode, String uri, String otherinfo) {
		super(id, name, description, uri, otherinfo);
		this.location = location;
		this.latitude = latitude;
		this.longitude = longitude;
		this.postcode = postcode;
	}
	
	public LocatedIotSystem(String id, String name, String description, String location, double latitude,
			double longitude, String postcode, String uri, String highlevelsystemid, String highlevelsystemname, String highlevelsystemdescription, String otherinfo) {
		super(id, name, description, uri, otherinfo);
		this.location = location;
		this.latitude = latitude;
		this.longitude = longitude;
		this.postcode = postcode;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	@Override
	public String toString() {
		return "LocatedIotSystem [location=" + location + ", latitude=" + latitude + ", longitude=" + longitude
				+ ", postcode=" + postcode + ", qrcode=" + qrcode + ", id=" + id + ", name=" + name + ", description="
				+ description + ", otherinfo=" + otherinfo + "]";
	}
	

}
