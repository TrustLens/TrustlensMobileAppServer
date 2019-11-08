package org.uoa.trustlensmobileappserver.data;

public class Question extends QuestionButton {

	private String sparql_query;


	public Question(String id, String button_text, int trust_level, String sparql_query) {
		super(id, button_text, trust_level);
		this.sparql_query = sparql_query;     
		 
	 }
	
	public String getSparclQuery() {
		return this.sparql_query;
	}
	
	public QuestionButton toQuestionButton() {
		return new QuestionButton(id, button_text, trust_level);
	}
	
}