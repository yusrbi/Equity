package annotators;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import loader.DocumentLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import data.Document_;
import data.Table_;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Triple;


public class StanfordWrapper implements AnnotatorWrapper {

	private static StanfordWrapper stanford_wrapper;
	private static StanfordCoreNLP pipeline = null;
	private final static String serializedClassifier = "annotators/classifiers/english.muc.7class.distsim.crf.ser.gz";
	private CRFClassifier<?> classifier;
	private static Logger slogger_ = LoggerFactory
			.getLogger(DocumentLoader.class);

	private StanfordWrapper() throws ClassCastException,
			ClassNotFoundException, IOException {
		 Properties props = new Properties();
		 props.setProperty("annotators", "tokenize,ssplit,pos, parse");
		 props.setProperty("ner.model", serializedClassifier);
		pipeline = new StanfordCoreNLP(props);
//		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//		InputStream in_stream = classLoader.getResourceAsStream(serializedClassifier);
//		ObjectInputStream obj_input_stream = new ObjectInputStream(in_stream);
		classifier = CRFClassifier.getClassifier(serializedClassifier);
	}

	public static StanfordWrapper getStanfordWrapper()
			throws ClassCastException, ClassNotFoundException, IOException {
		if (stanford_wrapper == null)
			stanford_wrapper = new StanfordWrapper();
		return stanford_wrapper;
	}

//	/**
//	 * process the document and its tables as a single document
//	 * @param document
//	 */
//	public void processAllOnce(Document_ document) {// using the simple is faster, but
//		
//		slogger_.info("processing document: " + document.getId());
//		
//		List<Triple<String, Integer, Integer>> triples = classifier
//				.classifyToCharacterOffsets(document.getContentAndTables());
//		document.processNEAnnotations(triples);
//		
//		Annotation doc_annotations = new Annotation(document.getContentAndTables());
//		pipeline.annotate(doc_annotations);		
//		List<CoreMap> sentences = doc_annotations.get(SentencesAnnotation.class);
//		document.processPOSAnnotations(sentences);
//	}

	public List<String> getNounPhrases(String text){
		Annotation document = new Annotation(text);
		// run all Annotators on this text
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		List<String> np = new LinkedList<String>();
		for (CoreMap sentence : sentences) {
			 Tree tree = sentence.get(TreeAnnotation.class);
			  dfs(tree.firstChild(),np);
		}
		return np;
	}
	private void dfs(Tree tree, List<String> np) {
		if(tree == null)
			return;
		if(tree.label().value().equals("NP")){
			StringBuilder text = new StringBuilder();
			for(Tree leave: tree.getLeaves()){
				text.append(leave.value()+" ");
			}
			np.add(text.toString().trim().replace("-LRB-", "(").replace("-RRB-", ")").replace("''", ""));			
		}
		for(Tree child : tree.children()){
			dfs(child,np);
		}
	}

	public void process(String text) {
		// Properties props = new Properties();
		// props.setProperty("annotators", "tokenize, ssplit, pos, ner");
		// StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);
		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				
				token.endPosition();
				token.beginPosition();
			}
		}

	}

	public void process(Document_ document) {// using the simple is faster, but
												// if we will load the core NLP
												// each time then it easier to
												// load once.
		
		slogger_.info("processing document: " + document.getId());
		
		if(document.getContent() != null){
			List<Triple<String, Integer, Integer>> triples = classifier
					.classifyToCharacterOffsets(document.getContent());
			document.processAnnotations(triples);
		}
		
		
		for (Table_ table : document.getTables()) {

			//triples = classifier
				//	.classifyToCharacterOffsets(table.getAsString());
			String result = classifier.classifyToString(table.getAsString(),
					"tsv", false);
			table.processAnnotaions(result);
			//table.processAnnotaions(triples);
		}
		// this is not needed any more as we taking all noun phrases in the text
		//document.propagateAnnotationsToTables(); // stop the propagation 
		
		
				
		// StringBuilder annotations = new StringBuilder();
		//
		// for (Triple<String, Integer, Integer> trip : triples) {
		// annotations.append(String.format("%s[%d, %d)\n",trip.first(),
		// trip.second(), trip.third()));
		// }
		// document.setAnnotations(annotations.toString());

		// for(Sentence sent : page_doc.sentences() ){
		// sent.nerTags();
		// }
		// edu.stanford.nlp.simple.Document table_doc;
		// for(Table_ table : document.getTables()){
		// table_doc = new edu.stanford.nlp.simple.Document(table.getContent());
		// //String xml_table =
		// table_doc.xml(edu.stanford.nlp.simple.Document::ner); does not work
		// ner method is not impelmented
		// for (Sentence sent : table_doc.sentences()){
		// sent.nerTags();
		// }
		//
		// }

	}

	
}

//"annotators"    <== "tokenize, ssplit, pos, lemma, ner, parse, dcoref"
//"pos.model"     <== ! @"pos-tagger\english-bidirectional\english-bidirectional-distsim.tagger"
//"ner.model"     <== ! @"ner\english.all.3class.distsim.crf.ser.gz"
//"parse.model"   <== ! @"lexparser\englishPCFG.ser.gz"
// 
//"dcoref.demonym"            <== ! @"dcoref\demonyms.txt"
//"dcoref.states"             <== ! @"dcoref\state-abbreviations.txt"
//"dcoref.animate"            <== ! @"dcoref\animate.unigrams.txt"
//"dcoref.inanimate"          <== ! @"dcoref\inanimate.unigrams.txt"
//"dcoref.male"               <== ! @"dcoref\male.unigrams.txt"
//"dcoref.neutral"            <== ! @"dcoref\neutral.unigrams.txt"
//"dcoref.female"             <== ! @"dcoref\female.unigrams.txt"
//"dcoref.plural"             <== ! @"dcoref\plural.unigrams.txt"
//"dcoref.singular"           <== ! @"dcoref\singular.unigrams.txt"
//"dcoref.countries"          <== ! @"dcoref\countries"
//"dcoref.extra.gender"       <== ! @"dcoref\namegender.combine.txt"
//"dcoref.states.provinces"   <== ! @"dcoref\statesandprovinces"
//"dcoref.singleton.predictor"<== ! @"dcoref\singleton.predictor.ser
