package drew.corenlp;

import com.google.common.io.Files;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** A simple corenlp example ripped directly from the Stanford CoreNLP website using text from wikinews. */
public class TruecaseExample {

  public static void main(String[] args) throws IOException {
    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, truecase");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    // read some text from the file..
    File inputFile = new File("src/test/resources/sample-content.txt");
    String input = Files.toString(inputFile, Charset.forName("UTF-8"));
    String lcInput = input.toLowerCase(); // downcase everything.

    // create an empty Annotation with just the downcased text.
    Annotation document = new Annotation(lcInput);

    // run all Annotators on this text
    pipeline.annotate(document);

    // these are all the sentences in this document
    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);

    // capture the true cased tokens for evaluation.
    List<String> tcTokens = new ArrayList<String>();

    System.out.println("------ begin truecase output -----");
    for (CoreMap sentence : sentences) {
      // traversing the words in the current sentence
      // a CoreLabel is a CoreMap with additional token-specific methods
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        // this is the text of the token
        String text = token.get(TextAnnotation.class);
        String trueCase = token.get(TrueCaseAnnotation.class);
        String trueCaseText = token.get(TrueCaseTextAnnotation.class);
        System.out.printf("input:%s state:%s output:%s\n", text, trueCase, trueCaseText);
        tcTokens.add(trueCaseText);
      }
    }
    System.out.println("------ end truecase otuput -----");


    // create an empty Annotation with just the standard text.
    document = new Annotation(input);

    // run all Annotators on this text
    pipeline.annotate(document);
    sentences = document.get(SentencesAnnotation.class);

    // capture the standard tokens for evaluation - note this assumes that
    // the pipeline won't generate additional tokens for the same input.
    List<String> stdTokens = new ArrayList<String>();

    for (CoreMap sentence : sentences) {
      // traversing the words in the current sentence
      // a CoreLabel is a CoreMap with additional token-specific methods
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        // this is the text of the token
        String word = token.get(TextAnnotation.class);
        stdTokens.add(word);
      }
    }

    // compare the output of the tc and the original to see how well we've done
    int match = 0;
    int sz = tcTokens.size();

    System.out.println("------ begin evaluation output -----");

    for (int i=0; i < sz; i++) {
      String tcToken = tcTokens.get(i);
      String stdToken = stdTokens.get(i);
      if (tcToken.equals(stdToken)) {
        match++;
      }
      else {
        System.out.printf("Truecase mismatch: input:'%s' output:'%s' @ %d\n", stdToken, tcToken, i);
      }
    }

    float errorRate = ((float) sz - match) / sz;
    System.out.println("Error Rate: " + errorRate);

    System.out.println("------ end evaluation output -----");
  }
}
