package org.uoa.trustlensmobileappserver.data;

import java.util.HashMap;
import java.util.Iterator;

public class SimpleCollapseCard {

	String html = "";
	
	public SimpleCollapseCard (String idBase, HashMap <String, String> items, String color) {
		
		String itemSections = "";
		Iterator<String>  it = items.keySet().iterator();
		int i = 0; 
		
		while (it.hasNext()) {
			i++;
			String key = it.next();
			itemSections = itemSections + "<div class=\"card col-sm-10\">" + 
					"    <div class=\"card-header\" id=\"heading"+idBase+i+"\" style=\"background-color:"+color+"\">" + 
					"      <h2 class=\"mb-0\">" + 
					"        <button class=\"btn btn-link\" style=\" white-space: normal;\" type=\"button\" data-toggle=\"collapse\" data-target=\"#collapse"+idBase+i+"\" aria-expanded=\"true\" aria-controls=\"collapse"+idBase+i+"\">" + 
					key+ 
					" <span class=\"glyphicon glyphicon-info-sign\" aria-hidden=\"true\"></span>"+
					"        </button>" + 
					"      </h2>" + 
					"    </div>" + 
					"" + 
					"    <div id=\"collapse"+idBase+i+"\" class=\"collapse\" aria-labelledby=\"heading"+idBase+i+"\" data-parent=\"#accordionExample\">" + 
					"      <div class=\"card-body\">" + 
					items.get(key)+
					"      </div>" + 
					"    </div>" + 
					"  </div>";
		}
		
		html = "<div class=\"accordion\" id=\"accordionExample\">" + 
				itemSections +
				"</div>";
		
	}

	public String getHTML() {
		// TODO Auto-generated method stub
		return html;
	}
	
}
