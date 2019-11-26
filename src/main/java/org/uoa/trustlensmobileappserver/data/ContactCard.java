package org.uoa.trustlensmobileappserver.data;

import java.util.ArrayList;

public class ContactCard {

	private String html =""; 
	
	public ContactCard (String name, String organisation, ArrayList data) {
		
		String contact_info = "";
		
		for (int i =0; i < data.size();i++)
			contact_info = contact_info + "<br>"+data.get(i);
		
		html ="<style>" + 
				".card {" + 
				"  box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);" + 
				"  transition: 0.3s;" + 
				"  width: 90%;" + 
				"}" + 
				"" + 
				".card:hover {" + 
				"  box-shadow: 0 8px 16px 0 rgba(0,0,0,0.2);" + 
				"}" + 
				"" + 
				".cardcontainer {" + 
				"  padding: 2px 16px;" + 
				"}" + 
				"</style>"
				+ "<div class=\"card\">" + 
				"  <img src=\"https://www.w3schools.com/howto/img_avatar.png\" alt=\"Avatar\" style=\"width:100%\">" + 
				"  <div class=\"container\">" +
				"<strong>Position:</strong>"+name+"<br>"+
				"<strong>Organisation:</strong>"+organisation+"<br>"+
				"<strong>Contact options:</strong>"+
				contact_info + 
				"  </div>" + 
				"</div>"; 
	}
	
	//Return the html that should be displayed by the app
		public String getHTML () {		
			return html;
		}
	
}
