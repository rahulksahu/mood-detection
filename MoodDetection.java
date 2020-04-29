import java.io.*;
import java.util.*;
import java.lang.*;

import libstemmer_java.java.org.tartarus.snowball.ext.englishStemmer;

import edu.smu.tspell.wordnet.*;

import com.google.common.*;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class Mood_Detection {

	/**
	 * @param args
	 */
	
	public static Map<String, String> acroDict = new HashMap<String, String>();
	public static Map<String, Integer> uniDict = new HashMap<String, Integer>();
	public static Map<String, Integer> awDict = new HashMap<String, Integer>();
	public static Map<String, Integer> swDict = new HashMap<String, Integer>();
	public static Map<String, int[]> emoDict = new HashMap<String, int[]>();
	public static Map<String, int[]> wlDict = new HashMap<String, int[]>();
	
	public static final String spl = "1234567890#@%^&()_=`{}:\"|[];\',./\n\t\r ";
	public static final CharMatcher matcher = CharMatcher.anyOf(spl);
	
	//public static final Map<String, Integer> emoval = new HashMap<String, Integer>();
	
	//Dictionary d = Dictionary.getDefaultResourceInstance();
	
	//public static final char anger=0, disgust=1, fear=2, joy=3, sad=4, surprise=5, other=6;
	public static final double[] amplifyFactor = {0.66, 1.5};
	public static final List<String> emotions = Arrays.asList("anger", "disgust", "fear", "joy", "sad", "surprise", "other");
	public static int[] emoset = {0,0,0,0,0,0,0};
	public static List<Integer> count_repeatition = new ArrayList<Integer>();
	public static final List<String> verbs1 = Arrays.asList("is", "am", "are");
	public static final List<String> verbs2 = Arrays.asList("was", "were");
	public static final List<String> verbs3 = Arrays.asList("will", "shall", "would");
	//public static List<Double> tagscore = new ArrayList<Double>();
	
	//public static englishStemmer stemmer = new englishStemmer();
	
	public static class tuple6 {
		private String word_stem, tag, word_orig;
		private double tag_score;
		private  int rcount;
		private Pair<Integer> pcount;
		public tuple6(){ word_stem=null; tag=null; word_orig=null; tag_score=0.0; rcount=0; pcount=null; }
		public tuple6(String a, String b, double c, String d, int e, Pair<Integer> f){
			word_stem=a; 
			tag=b; 
			word_orig=d; 
			tag_score=c; 
			rcount=e; 
			pcount=f;
		}
	}
	
	public static class Pair<T>{
		private T left, right;
		//public Pair<T>(){ left = 0; right = 0; }
		public Pair(T a, T b){
			left = a;
			right = b;
		}
	}
	
	public static String nonASCII (String text)
	{
		return text.replaceAll("[^\\x00-\\x7F]", " ");
	}
	
	//public String expandAcro ()
	
	public static String stringJoin(String[] str)
	{
		Joiner joiner = Joiner.on(" ").skipNulls();
		return joiner.join(str);
	}
	
	public static void AcroDictLoader() throws FileNotFoundException {
		
		//BufferedReader reader = new BufferedReader(new FileReader("E:\emo\data\acronym.txt"));
		Scanner sc = new Scanner(new File("E:\\emo\\data\\acronym.txt"));
		while(sc.hasNextLine())
		{
			//String line = sc.nextLine();
			String[] parts = sc.nextLine().split("\t");
			String part1 = parts[0].trim().toLowerCase();
			String[] part2 = parts[1].trim().toLowerCase().split(" ");
			for(int i=0; i<part2.length; i++)
			{
				//CharMatcher matcher = CharMatcher.anyOf(spl);
				part2[i] = matcher.trimFrom(part2[i]).toLowerCase();
				englishStemmer stemmer = new englishStemmer();
				stemmer.setCurrent(part2[i]);
				if(stemmer.stem())
					part2[i] = matcher.trimFrom(stemmer.getCurrent());
			}
			acroDict.put(part1, stringJoin(part2));
		}
		sc.close();
	}
	
	public static void SWDictLoader() throws FileNotFoundException {
		Scanner sc = new Scanner(new File("E:\\emo\\data\\stopWords.txt"));
		while(sc.hasNextLine())
		{
			String word = sc.nextLine().trim().toLowerCase();
			swDict.put(word, 0);
		}
		sc.close();
	}
	
	public static void EmoDictLoader() throws FileNotFoundException {
		Scanner sc = new Scanner(new File("E:\\emo\\data\\emoticonLexion.txt"));
		while(sc.hasNextLine())
		{
			String[] parts = sc.nextLine().split("\t");
			String part1 = parts[0].trim().toLowerCase();
			//CharMatcher matcher = CharMatcher.anyOf(spl);
			String part2 = matcher.trimFrom(parts[1]).toLowerCase();
			if(!emotions.contains(part2))
				part2 = "other";
			if(!emoDict.containsKey(part1))
				emoDict.put(part1, emoset);
			int[] temp = emoDict.get(part1);
			temp[emotions.indexOf(part2)] = 1;
			emoDict.put(part1, temp);
		}
		sc.close();
	}
		
	public static void AWDictLoader() throws FileNotFoundException {
		Scanner sc = new Scanner(new File("E:\\emo\\data\\amplifier.txt"));
		while(sc.hasNextLine())
		{
			String[] parts = sc.nextLine().split("\t");
			String word = new String();
			parts[0] = matcher.trimFrom(parts[0]).toLowerCase();
			englishStemmer stemmer = new englishStemmer();
			stemmer.setCurrent(parts[0]);
			if(stemmer.stem())
				word = stemmer.getCurrent();
			int label = Integer.parseInt(parts[1]);
			if(!awDict.containsKey(word))
				awDict.put(word, label);
		}
		sc.close();
	}
	
	public static void WLDictLoader() throws FileNotFoundException {
		Scanner sc = new Scanner(new File("E:\\emo\\data\\wordLexion.txt"));
		while(sc.hasNextLine())
		{
			String[] parts = sc.nextLine().split("\t");
			//System.out.println(parts[0] +" - "+ parts[1]);
			String part1 = matcher.trimFrom(parts[0]).toLowerCase();
			englishStemmer stemmer = new englishStemmer();
			stemmer.setCurrent(parts[0]);
			if(stemmer.stem())
				part1 = stemmer.getCurrent().trim();
			if(part1.length()<3)
				part1 = null;
			String part2 = matcher.trimFrom(parts[1]).toLowerCase();
			if(!emotions.contains(part2))
				part2 = "other";
			if(part1 != null){
				if(!wlDict.containsKey(part1))
					wlDict.put(part1, emoset);
				int[] temp = wlDict.get(part1);
				temp[emotions.indexOf(part2)] = 1;
				wlDict.put(part1, temp);
			}
		}
		sc.close();		
	}
	
	public static void UniDictLoader() throws FileNotFoundException {
		Scanner sc = new Scanner(new File("E:\\emo\\data\\unigram.txt"));
		while(sc.hasNextLine())
		{
			String word = matcher.trimFrom(sc.nextLine()).toLowerCase();
			if(!uniDict.containsKey(word))
				uniDict.put(word, 1);
		}
		sc.close();
	}
	
	public static int maxMatch(String str1, String str2){
		int count = 0, i = 0;
		while(i < Math.min(str1.length(), str2.length())){
			if(str1.charAt(i)!= str2.charAt(i))
				break;
			else 
				count++;
			i++;
		}
		return count;
	}
	
	public static String ExpandAcronym(String sentence) {
		//int count = 0;
		String text = new String();
		String[] str = sentence.split(" ");
		for(int i = 0; i<str.length; i++){
			//int flag = 0;;
			if(!emoDict.containsKey(str[i].trim())){
				String word = str[i].trim();
				if(word != null){
					//System.out.println(word);
					if(acroDict.containsKey(word.toLowerCase())){
						//System.out.println("mayank");
						if(word.toUpperCase().equals(word))
							text = text + " " + (acroDict.get(word.toLowerCase())).toUpperCase();
						else
							text = text + " " + (acroDict.get(word.toLowerCase())).toLowerCase();
					}
					else{
						word = matcher.trimFrom(word);
						if(word != null){
							if(acroDict.containsKey(word.toLowerCase())){
								if(word.toUpperCase().equals(word))
									text = text + " " + (acroDict.get(word.toLowerCase())).toUpperCase();
								else
									text = text + " " + (acroDict.get(word.toLowerCase())).toLowerCase();
							}
							else
							{
								//System.out.println(word);
								text = text + " " + word;
								//System.out.println(text);
							}
						}
					}
				}
			}
			else
				text = text + " " + str[i];
		}
		return text.trim();
	}
	
	public static String[] replaceRepetition(String sentence) throws Throwable {
		String[] str = sentence.split(" ");
		//String[] str2 = new String[]();
		for(int i = 0; i < str.length; i++){
			int count = 0;
			if(str[i].length()>3 && !emoDict.containsKey(str[i])){
				for(int j=3; j<str[i].length(); j++){
					if(Character.toLowerCase(str[i].charAt(j-3))==Character.toLowerCase(str[i].charAt(j-2)) &&
							Character.toLowerCase(str[i].charAt(j-2))==Character.toLowerCase(str[i].charAt(j-1)) && 
									Character.toLowerCase(str[i].charAt(j-1))==Character.toLowerCase(str[i].charAt(j))){
						str[i] = str[i].substring(0, j-3) + str[i].substring(j-2);
						j--;
						//System.out.println(str[i]);
						count++;
					}
				}
				str[i] = matcher.trimFrom(str[i]);
			}
			count_repeatition.add(count);
		}
		return str;
	}
	
	public static List<Pair<Integer>> CountPunctuation(String sentence) throws Throwable {
		String[] str = sentence.split(" ");
		List<Pair<Integer>> pairlist = new ArrayList<Pair<Integer>>();    //left is !, right is ?
		for(int i=0; i<str.length; i++){
			if(!emoDict.containsKey(str[i]))
				pairlist.add(new Pair<Integer>(CharMatcher.is('!').countIn(str[i]), CharMatcher.is('?').countIn(str[i])));
			else
				pairlist.add(new Pair<Integer>(0,0));
		}
		return pairlist;
	}
	
	public static String ExpandNegation(String sentence){
		String[] str = sentence.split(" ");
		List<String> text = new ArrayList<String>();
		for(int i=0; i<str.length; i++){
			String word = matcher.trimFrom(str[i].toLowerCase());
			if(word.length()>=3){
				//System.out.println(word);
				//System.out.println(word.substring(word.length()-3));
				
				if(word.substring(word.length()-3).equals("n't")){
					//System.out.println("mayank");
					if(word.substring(word.length()-5).equalsIgnoreCase("can't"))
						text.add("can");
					else
						text.add(word.substring(0, word.length()-3));
				text.add("not");
			}
			else
				text.add(str[i]);
		}
			else text.add(str[i]);
		}
		Joiner joiner = Joiner.on(" ").skipNulls();
		return joiner.join(text);
		//return stringJoin(text);
	}
	
	public static List<Pair<String>> POStags(String text) throws Throwable {
		MaxentTagger tagger = new MaxentTagger("tagger/english-left3words-distsim.tagger");
		//System.out.println(tagger.tagString(text));
		String[] temp = tagger.tagString(text).split(" ");
		List<Pair<String>> text_tag_pair = new ArrayList<Pair<String>>(); //left = word, right = tag
		for(int i=0; i<temp.length; i++){
			//System.out.println(temp[i]);
			String[] parts = temp[i].split("_");
			//System.out.println(parts[0] + "-" + parts[1]);
			text_tag_pair.add(new Pair<String>(parts[0], parts[1]));
		}
		//for(int k=0; k<text_tag_pair.size(); k++)
		//System.out.println(text_tag_pair.get(k).left + "-" + text_tag_pair.get(k).right);
		return text_tag_pair;
	}
	
	public static List<tuple6> DetectTense(List<Pair<String>> text_tag_pair) throws Throwable {
		Map<String, Double> tags = new HashMap<String, Double>();
		tags.put("VBP", 1.0);
		tags.put("VBZ", 1.0);
		tags.put("VBD", 0.5);
		tags.put("VBN", 0.5);
		List<String> keys = new ArrayList<String>();
		//keys.addAll(text_tag_pair.keySet());
		List<tuple6> text_tuple_list = new ArrayList<tuple6>(); 
		int count = 0;
		for(int i=0; i<keys.size(); i++){
			if(tags.containsKey(text_tag_pair.get(i).right))
				text_tuple_list.add(new tuple6(text_tag_pair.get(i).left,text_tag_pair.get(i).right, tags.get(text_tag_pair.get(i).right), null, 0, null));
				//tagscore.add(tags.get(text_tag_pair.get(keys.get(i))));
			else if(text_tag_pair.get(i).right.equals("VBG")){
				if(count>0){
					if(verbs1.contains(text_tag_pair.get(count-1).left.toLowerCase()))
						text_tuple_list.add(new tuple6(text_tag_pair.get(i).left, text_tag_pair.get(i).right, 1.0, null, 0, null));
							//tagscore.add(1.0);
					else if(text_tag_pair.get(count-1).left.toLowerCase().equals("be"))
						text_tuple_list.add(new tuple6(text_tag_pair.get(i).left, text_tag_pair.get(i).right, 0.75, null, 0, null));
							//tagscore.add(0.75);
					else if(verbs2.contains(text_tag_pair.get(count-1).left.toLowerCase()))
						text_tuple_list.add(new tuple6(text_tag_pair.get(i).left, text_tag_pair.get(i).right, 0.5, null, 0, null));	
						//tagscore.add(0.5);
					else
						text_tuple_list.add(new tuple6(text_tag_pair.get(i).left, text_tag_pair.get(i).right, 1.0, null, 0, null));
					}
				else
					text_tuple_list.add(new tuple6(text_tag_pair.get(i).left, text_tag_pair.get(i).right, 1.0, null, 0, null));
					}
			else if(text_tag_pair.get(i).right.equals("VB")){
				if(count>0){
					if(verbs3.contains(text_tag_pair.get(count-1).left.toLowerCase()))
						text_tuple_list.add(new tuple6(text_tag_pair.get(i).left, text_tag_pair.get(i).right, 0.75, null, 0, null));
					else
						text_tuple_list.add(new tuple6(text_tag_pair.get(i).left, text_tag_pair.get(i).right, 1.0, null, 0, null));
						}
				else
					text_tuple_list.add(new tuple6(text_tag_pair.get(i).left, text_tag_pair.get(i).right, 1.0, null, 0, null));
					}
			else
				text_tuple_list.add(new tuple6(text_tag_pair.get(i).left, text_tag_pair.get(i).right, 1.0, null, 0, null));
			count++;
			}
		return text_tuple_list;
	}
	
	public static double[] FeatureVector(List<tuple6> sentence_tuple, List<String> featurelist, double[] vector, int[] wordcount) throws Throwable {
		List<String> amp = new ArrayList<String>();
		amp.addAll(awDict.keySet());
		Collections.sort(amp, new Comparator<String>(){
			public int compare(String s1, String s2){
				if(s1.split(" ").length > s2.split(" ").length)
					return -1;
				else return 1;
			}});
		//Collections.reverse(amp);
		int i=0, j=0, cap=0;
		//String[] words = sentence.split(" ");
		while(i<sentence_tuple.size()){
			tuple6 word = sentence_tuple.get(i);
			if(word.word_stem.toUpperCase().equals(word.word_stem))
				cap++;
			List<String> phrase1 = new ArrayList<String>();
			for(int k=j; k<i; k++)
				phrase1.add(sentence_tuple.get(j).word_stem.toLowerCase());
				//System.arraycopy(words, j, phrase, 0, i-j);
			int factor1 = 1;
			double factor2 = 1.0;
			if(phrase1.contains("not"))
				factor1 = -1;
			Joiner joiner = Joiner.on(" ").skipNulls();
			String phrase2 = joiner.join(phrase1);
			//ListIterator amp_it = amp.listIterator();
			int itr = 0;
			while(itr<amp.size()){
				if(phrase2.contains(amp.get(itr))){
					if(amp.get(itr).contains("not"))
						factor1 = 1;
					for(int t=0; t<amp.get(itr).split(" ").length; t++)
						if(amp.get(itr).split(" ")[t].toUpperCase().equals(amp.get(itr).split(" ")[t]))
							cap++;
					factor2 = amplifyFactor[awDict.get(amp.get(itr))];
					break;
				}
				itr++;
			}
			List<String> temp = new ArrayList<String>();
			if(word.tag.length()>1){
				if((word.tag.startsWith("VB") || word.tag.startsWith("JJ") || word.tag.startsWith("NN")) && !swDict.containsKey(word.word_stem)){
					List<String> syn = Synonyms(word.word_orig);
					for(int t=0; t<syn.size(); t++){
						englishStemmer stemmer = new englishStemmer();
						stemmer.setCurrent(syn.get(t));
						if(stemmer.stem())
							temp.add(stemmer.getCurrent());
						}
					temp.add(word.word_stem);
					final String compword = word.word_stem;
					Collections.sort(temp, new Comparator<String>(){
						public int compare(String s1, String s2){
							if(maxMatch(s1, compword) > maxMatch(s2, compword))
								return -1;
							else return 1;
						}});
				}
			}
			else temp.add(word.word_stem);
			for(int t=0; t<temp.size(); t++){
				if(featurelist.contains(temp.get(t)) && !swDict.containsKey(temp.get(t))){
					int ind = featurelist.indexOf(temp.get(t));
					if(emoDict.containsKey(temp.get(t))){
						vector[ind] = vector[ind] + 2*factor1;
						wordcount[ind]++;
					}
					else if(wlDict.containsKey(temp.get(t))){
						vector[ind] = vector[ind] + (2+cap*0.5)*factor1*factor2*word.tag_score;
						wordcount[ind]++;
					}
					else if(uniDict.containsKey(temp.get(t))){
						vector[ind] = vector[ind] + (1+cap*0.5)*factor1*factor2*word.tag_score;
						wordcount[ind]++;
					}
					j++;
					break;
				}
			}
			i++;
		}
		return vector;
	}
	
	public static List<String> Synonyms(String word){
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(word);
		List<String> synlist = new ArrayList<String>();
		for(int i=0; i<synsets.length; i++){
			String[] syn = synsets[i].getWordForms();
			for(int j=0; j<syn.length; j++){
				if(!synlist.contains(syn[j]))
					synlist.add(syn[j]);
			}
		}
		final String compword = word;
		Collections.sort(synlist, new Comparator<String>(){
			public int compare(String s1, String s2){
				if(maxMatch(s1, compword) > maxMatch(s2,compword))
					return -1;
				else return 1;
			}});
		return synlist;
	}
	
	public static List<tuple6> PreProcessing(String sentence) throws Throwable{
		sentence = nonASCII(sentence);
		//System.out.println(sentence);
		sentence = ExpandNegation(sentence);
		//System.out.println(sentence);
		sentence = ExpandAcronym(sentence);
		System.out.println(sentence);
		List<Pair<Integer>> count_punct = CountPunctuation(sentence);
		String[] s1 = replaceRepetition(sentence);
		for(int i=0; i<s1.length; i++){
			if(!emoDict.containsKey(s1[i].trim()))
				s1[i] = matcher.trimFrom(s1[i]).toLowerCase();
			else
				s1[i] = s1[i].trim();
		}
		//Map<String, String> map = POStags(stringJoin(s1));
		//System.out.println(map);
		List<tuple6> temp = DetectTense(POStags(stringJoin(s1)));
		//System.out.println(temp.get(22).word_stem + "-" + temp.get(22).tag + "-" + temp.get(22).tag_score);
		for(int i=0; i<temp.size(); i++){
			if(!emoDict.containsKey(temp.get(i).word_stem.trim())){
				String s = new String();
				englishStemmer stemmer = new englishStemmer();
				stemmer.setCurrent(matcher.trimFrom((temp.get(i).word_stem)));
				if(stemmer.stem())
					s = matcher.trimFrom(stemmer.getCurrent());
				//System.out.println(s);
				temp.add(i, new tuple6(s, temp.get(i).tag, temp.get(i).tag_score, matcher.trimFrom(temp.get(i).word_stem), count_repeatition.get(i), count_punct.get(i)));
				}
			else
				temp.add(i, new tuple6(temp.get(i).word_stem.trim(), temp.get(i).tag, temp.get(i).tag_score, temp.get(i).word_stem.trim(), count_repeatition.get(i), count_punct.get(i)));
		if(temp.get(i).word_stem == null)
			temp.remove(i);
		}
		return temp;
	}
	
	public static void main(String[] args) throws Throwable {
		// TODO Auto-generated method stub
		
		//load unigram dictionary
		UniDictLoader();
		
		//load amplified word dict
		AWDictLoader();
		
		//load wordlexion dict
		WLDictLoader();
		
		//load emoticon lexion dict
		EmoDictLoader();
		
		//load acro dict
		AcroDictLoader();
		
		//load sw dict
		SWDictLoader();
		
		List<String> featurelist = new ArrayList<String>();
		featurelist.addAll(uniDict.keySet());
		featurelist.addAll(wlDict.keySet());
		featurelist.addAll(emoDict.keySet());
		
		//training starts..
		int[] wordcount = new int[featurelist.size()];
		for(int i=0; i<wordcount.length; i++)
			wordcount[i] = 0;
		
		Map<String, double[]> model = new HashMap<String, double[]>();
		for(int i=0; i<emotions.size(); i++){
			double[] temp = new double[featurelist.size()];
			for(int j=0; j<temp.length; j++)
				temp[j] = 0;
			model.put(emotions.get(i), temp);
		}
		//corpus1 training
		int sc_count = 0;
		//Scanner sc = new Scanner(new File("E:\\emo\\data\\corpus.txt"));
		Scanner sc = new Scanner(new File("E:\\emo\\data\\temp.txt"));
		while(sc.hasNextLine()){
			System.out.println(sc_count);
			sc_count++;
			String[] parts = sc.nextLine().split("\t");
			String label = parts[0];
			if(!emotions.contains(label))
				label = "other";
			double[] vector = new double[featurelist.size()];
			vector = FeatureVector(PreProcessing(parts[1]), featurelist, vector, wordcount);
			double[] temp = new double[featurelist.size()];
			temp = model.get(label);
			for(int i=0; i<featurelist.size(); i++)
				temp[i] = temp[i] + vector[i];
			//for(int j=0; j<featurelist.size(); j++)
				//System.out.println(temp[j]);
			model.put(label, temp);
		}
		sc.close();	
		
		//corpus2 training
		/*sc_count = 0;
		sc = new Scanner(new File("E:\\emo\\data\\corpus2.txt"));
		while(sc.hasNextLine()){
			System.out.println(sc_count);
			sc_count++;
			String[] parts = sc.nextLine().split("\t");
			//String label = parts[0];
			//if(!emotions.contains(label))
			//	label = "other";
			double[] vector = new double[featurelist.size()];
			vector = FeatureVector(PreProcessing(parts[0]), featurelist, vector, wordcount);
			for(int i=0; i<6; i++){
					double[] temp = new double[featurelist.size()];
					temp = model.get(emotions.get(i));
					for(int j=0; j<featurelist.size(); j++)
						temp[j] = temp[j] + vector[j]*Double.parseDouble(parts[i+1].trim());
			model.put(emotions.get(i), temp);
			}
		}
		sc.close();*/
		
		for(int j=0; j<featurelist.size(); j++)
			if(wordcount[j]!=0)
				System.out.println(wordcount[j]);
		
		for(int i=0; i<emotions.size(); i++){
			for(int j=0; j<featurelist.size(); j++)
				if(wordcount[j] != 0){
					double[] temp = new double[featurelist.size()];
					temp = model.get(emotions.get(i));
					temp[j] = (temp[j]*1.0)/wordcount[j];
					model.put(emotions.get(i), temp);
				}
			}
		
		List<String> wldlist = new ArrayList<String>(wlDict.keySet());
		for(int i=0; i<wldlist.size(); i++){
			for(int j=0; j<emotions.size(); j++){
				int ind = featurelist.indexOf(wldlist.get(i));
				double[] temp = new double[featurelist.size()];
				temp = model.get(emotions.get(j));
				temp[ind] = temp[ind] + wlDict.get(wldlist.get(i))[j];
				model.put(emotions.get(j), temp);
				//model.get(emotions.get(j))[ind] = model.get(emotions.get(j))[ind] + wlDict.get(wldlist.get(i))[j];
			}
		}
		
		List<String> emodlist = new ArrayList<String>(emoDict.keySet());
		for(int i=0; i<emodlist.size(); i++)
			for(int j=0; j<emotions.size(); j++){
				int ind = featurelist.indexOf(emodlist.get(i));
				double[] temp = new double[featurelist.size()];
				temp = model.get(emotions.get(j));
				temp[ind] = temp[ind] + emoDict.get(emodlist.get(i))[j];
				model.put(emotions.get(j), temp);
				//model.get(emotions.get(j))[ind] = model.get(emotions.get(j))[ind] + emoDict.get(emodlist.get(i))[j];
			}
		
		//for(int j=0; j<featurelist.size(); j++)
			//System.out.println(model.get(emotions.get(0))[j]);
		
		while(true){
			System.out.println("Input Sentence - \n");
			sc = new Scanner(System.in);
			double[] vector = new double[featurelist.size()];
			String sentence = sc.nextLine();
			vector = FeatureVector(PreProcessing(sentence), featurelist, vector, wordcount);
			//@SuppressWarnings("rawtypes")
			Vector<Double> ans = new Vector<Double>();
			for(int i=0; i<emotions.size(); i++){
				double m=0, q=0, count = 0;
				for(int j=0; j<featurelist.size(); j++){
					//System.out.println(model.get(emotions.get(i))[j]);
					m = m + Math.pow(model.get(emotions.get(i))[j], 2.0);
					q = q + Math.pow(vector[j], 2.0);
					count = count + model.get(emotions.get(i))[j] * vector[j];
				}
				//System.out.println(m + "---" + q);
				q = Math.sqrt(q);
				m = Math.sqrt(m);
				if(q==0)
					q = 1.0;
				if(m==0)
					m = 1.0;
				System.out.println(m + "---" + q);
				ans.add(count*100/(q*m));
			}
			System.out.println(sentence);
			System.out.println(ans);
		}
	}
}