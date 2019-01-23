package executer;

import java.io.IOException;

import annotators.AIDAWrapper;

public class testAIDA {
	public static void main(String[] args) throws IOException{
	
		AIDAWrapper aida = new AIDAWrapper();
		aida.disambiguate_mentions("[[Dylan]] was born in [[Duluth]].","LOCAL");
		
	}

}
