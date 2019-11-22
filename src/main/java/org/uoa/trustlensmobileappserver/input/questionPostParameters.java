package org.uoa.trustlensmobileappserver.input;

public class questionPostParameters{
	private String system_id;
	private String question_id;
	public void setSystem_id(String system_id) {
		this.system_id = system_id;
	}
	public void setQuestion_id(String question_id) {
		this.question_id = question_id;
	}
	public String getSystem_id() {
		return system_id;
	}   
	public String getQuestion_id() {
		return question_id;
	}
	
	public String toString() {return system_id + " " + question_id;}
}
