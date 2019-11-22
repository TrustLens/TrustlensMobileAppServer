package org.uoa.trustlensmobileappserver.data;

public class QuestionButton {

	protected String id;
	protected String button_text;
	protected int trust_level;


	public QuestionButton(String id, String button_text, int trust_level) {
	    this.id = id;
	    this.button_text = button_text;
	    this.trust_level = trust_level;
	 }
	
	public String getId() {
		return this.id;
	}
		
	public String getButton_text() {
		return this.button_text;
	}
		
	public int getTrust_level() {
		return this.trust_level;
	}
		
}