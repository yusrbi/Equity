package knowledgebase;

import knowledgebase.Candidate.TYPE;
import resources.Resources;

public class CandidateQuantity extends Candidate {

	// "https://en.wikipedia.org/wiki/Percentage"
	// "https://en.wikipedia.org/wiki/Calendar_year"
	// "https://en.wikipedia.org/wiki/Calendar_date"
	// "https://en.wikipedia.org/wiki/Time"
	// "https://en.wikipedia.org/wiki/Ordinal_number" Rank/Order
	public CandidateQuantity(String semanticTargetId, TYPE type, double score, String magnitude, String calass_name,
			String dimension_name, String unit_name) {
		super(semanticTargetId, type, score, "");
		String url = Resources.getResources().getQkb_url()+
				"?class=" + calass_name;
		if (dimension_name != "NULL"){
			url += "&measure=" + dimension_name;
			if (unit_name != "NULL") {
				url += "&unit=" + unit_name;
			}
		}
		url +="&value="+magnitude;
		super.setUrl(url);
	}

}