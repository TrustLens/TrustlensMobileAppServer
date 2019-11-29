package org.uoa.trustlensmobileappserver.input;

public class locatesystemsPostParameters {

	private int mode=0;
	private double latitude;
	private double longitude;
	private String postcode;
	private int radius;
	
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
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
		return postcode.toUpperCase()
				;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public int getRadius() {
		return radius;
	}
	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	@Override
	public String toString() {
		return "locatesystemsPostParameters [mode=" + mode + ", latitude=" + latitude + ", longitude=" + longitude
				+ ", postcode=" + postcode + ", radius=" + radius + "]";
	}

	
}
