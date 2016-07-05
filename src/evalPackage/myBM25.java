package evalPackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class myBM25 {
	static String question;
	static String[] answers;
	
	static int qlength;
	static int[] alength;
	
	static HashMap<String, Integer> qMap;
	static ArrayList<HashMap<String, Integer>> answerList;
	
	public myBM25(String q, String[] as)
	{
		this.question = q;
		this.answers = new String[as.length];
		for(int i=0;i<as.length;i++)
			answers[i] = as[i];
		alength = new int[as.length];
		answerList = new ArrayList<>();
		qMap = new HashMap<>();
	}
	
	public static int[] aBM25() throws IOException
	{
		int len = answers.length;
		parseAll();
		int[] order = new int[len];
		double[] scores = new double[len];
		double avgdl = avgdl();
		for(int i=0;i<len;i++)
			scores[i] = bm25Similarity(qMap, answerList.get(i), alength[i], avgdl);
		
		return getOrder(scores);
	}
	public static void parseAll() throws IOException
	{
		qlength = parse(question, qMap);
		for(int i=0;i<answers.length;i++)
		{
			HashMap<String, Integer> aMap = new HashMap<>();
			alength[i] = parse(answers[i], aMap);
			answerList.add(aMap);
		}
	}
/*	public static void main() throws IOException
	{
		String q = "I would like to lose 50 lbs in 3 months? how can I do this can someone tell me things to do at the gym and of any diets that work or the foods to eat. I would like to look good by my birthday. I'm 6' and weigh 230 50 lbs less is average I think";
		HashMap<String, Integer> hm = new HashMap<>();
		int t = parse(q, hm);
		System.out.println(t);
	}*/
	public static int parse(String line, HashMap<String, Integer> an) throws IOException//return #words of string
	{
		StandardAnalyzer analyzer = new StandardAnalyzer();
		TokenStream stream = analyzer.tokenStream(null, line);
		PorterStemFilter stemmer1 = new PorterStemFilter(stream);
		CharTermAttribute cattr = stemmer1.addAttribute(CharTermAttribute.class);
		stemmer1.reset();
		int length = 0;
		while (stemmer1.incrementToken()) {
			length++;
			String curS = cattr.toString();
			if(an.containsKey(curS)) an.put(curS, an.get(curS)+1);
			else an.put(curS, 1);			
		}		
		stemmer1.end();
		stemmer1.close();
		return length;
	}
	public static double bm25Similarity(HashMap<String, Integer> qMap, HashMap<String, Integer> aMap, int alen, double avgdl)
	{
		double score = 0;
		for(String word: qMap.keySet())
			score += idf(word)*freq(word, aMap, avgdl, alen);
		return score;
	}
	public static double idf(String query)//query is a single word
	{
		double score = 0;
		double fileCount = 0;

		for(int i=0; i<answerList.size(); i++)
		{
			if(answerList.get(i).containsKey(query))
				fileCount++;
		}		
		score = (double)Math.log(1+(0.5+(double)answerList.size()-fileCount)/(0.5+fileCount));
		return score;
	}
	public static double freq(String query, HashMap<String, Integer> an1, double avgdl, double len)
	{
		double fq = 0;
		double k = 1.2;
		double b = 0.75;
		if(an1.containsKey(query)) fq = (double)an1.get(query);
		double result = fq*(1+k)/(fq+k*(1-b+b*(len/avgdl)));
		return result;
	}

	public static double avgdl()
	{
		int total = 0;
		for(int i=0;i<alength.length;i++)
			total += alength[i];
		return (double)total/alength.length;
	}
	public static int[] getOrder(double[] scores)
	{
		int[] result = new int[scores.length];
		ArrayList<double[]> pair = new ArrayList<>();
		for(int i=0; i<scores.length; i++)
		{
			double[] tmp = new double[]{scores[i], (double)i};
			pair.add(tmp);
		}
		
		Collections.sort(pair, new Comparator<double[]>(){
			@Override
			public int compare(double[] a, double[] b) {
				return (b[0]-a[0])>0?1:-1;
			}
		});
		
		for(int i=0; i<scores.length; i++)
			result[i] = (int) pair.get(i)[1];
		return result;
	}
}
