package annotators;


import java.io.IOException;
import java.util.HashMap;

import annotations.Annotation.ANNOTATION;
import annotations.Class_;
import resources.Resources;
import data.Document_;
import data.Table_;
import executer.RunTimeAnalysis;

public class Annotator {

	public static enum TYPE {
		simple, plus
	};

	private TYPE annotator_type;

	public Annotator(Resources resources) {
		if (resources.isNER_switch())
			this.annotator_type = TYPE.plus;
		else
			this.annotator_type = TYPE.simple;
	}

	public void annotate(Document_ document) throws ClassCastException, ClassNotFoundException, IOException {
		// stanford_ner_results = resources.loadStanfordNerResult();
		AnnotatorWrapper stanford_ner = StanfordWrapper.getStanfordWrapper();
		QuantityAnnotatorWrapper quantityAnnotator = null;
		AnnotationEnrichment ann_enricher = new AnnotationEnrichment();
		switch (annotator_type) {
		case simple:
			
			stanford_ner.process(document);// including tables
			break;
		case plus:
			quantityAnnotator = new QuantityAnnotatorWrapper();
			// gazettersAnnotator = new GazetteersAnnotator();
			stanford_ner.process(document); // including tables
			quantityAnnotator.process(document); // for tables
			RunTimeAnalysis.mile_stone_end = System.nanoTime();
			RunTimeAnalysis.annotation_time = RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
			RunTimeAnalysis.mile_stone_start = System.nanoTime();
			ann_enricher.process(document);
//			RunTimeAnalysis.mile_stone_end = System.nanoTime();
//			RunTimeAnalysis.annotatiion_enrichment_time = RunTimeAnalysis.mile_stone_end
//					- RunTimeAnalysis.mile_stone_start;

			break;
		}
		// document.saveNerResult(resources.getStanford_ner_result());
	}

	public void printAnnotations(Document_ document, String string) {
		// TODO Auto-generated method stub

	}

	public void annotate(HashMap<String, Document_> documents)
			throws ClassCastException, ClassNotFoundException, IOException {
		for (Document_ document : documents.values()) {
			annotate(document);
		}

	}

	public void annotateTableHeader(Table_ table) {
		// this method is created specially for the case of Sarwagi and Downey
		// evaluation
		// we only need to find calsses in the  header
		String[] row = table.getHeader();
	
		for (int j = 0; j < row.length; j++) {
			if (table.isColumnHasEntityAnnotations(j)) {
				table.addAnnotation(row[j], 0, j, new Class_(0, j, 0, row[j].length() - 1, ANNOTATION.CLASS));
			}

		}

	}

}
