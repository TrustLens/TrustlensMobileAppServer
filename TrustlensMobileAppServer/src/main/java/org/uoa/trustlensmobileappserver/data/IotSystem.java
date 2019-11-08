package org.uoa.trustlensmobileappserver.data;

public class IotSystem {

	String id;
	String name;
	String description;
	String otherinfo;
	String uri;
	String highlevelsystemid;
	String highlevelsystemname;
	String highlevelsystemdescription;
	
	public IotSystem(String id, String name, String description, String uri, String highlevelsystemid, String highlevelsystemname, String highlevelsystemdescription, String otherinfo) {
		this(id, name, description, uri, otherinfo);
		this.highlevelsystemid = highlevelsystemid;
		this.highlevelsystemname = highlevelsystemname;
		this.highlevelsystemdescription = highlevelsystemdescription;
	}
	
	public IotSystem(String id, String name, String description, String uri, String otherinfo) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.otherinfo = otherinfo;
		this.uri = uri;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUri() {
		return "<" + id + ">";
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public boolean equals (Object obj){
		if(obj instanceof IotSystem) {
		   IotSystem otherIot = (IotSystem) obj;
		   if ( otherIot.getId() == this.getId())
			   return true;
		   else 
			   return false;
		}
		else{
			return false;
		}
	} 
}
