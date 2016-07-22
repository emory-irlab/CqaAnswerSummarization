package sentSummary;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class sentBM25 {
	static String question;
	static ArrayList<String> answers;
	
	static int qlength;
	static int[] alength;
	
	static HashMap<String, Integer> qMap;
	static ArrayList<HashMap<String, Integer>> answerList;
	
	public sentBM25(String q, ArrayList<String> as)
	{
		this.question = q;
		this.answers = new ArrayList<>();
		for(int i=0;i<as.size();i++)
			answers.add(as.get(i));
		alength = new int[as.size()];
		answerList = new ArrayList<>();
		qMap = new HashMap<>();
	}
	
	public int[] aBM25() throws IOException
	{
		int len = answers.size();
		parseAll();
		double[] scores = new double[len];
		double avgdl = avgdl();
		for(int i=0;i<len;i++)
			scores[i] = bm25Similarity(qMap, answerList.get(i), alength[i], avgdl);
		return getOrder(scores);
		
	}
	public int[] mmr_simbased(double lamda) throws IOException
	{
		int len = answers.size();
		parseAll();
		LinkedList<Integer> selected = new LinkedList<>();
		LinkedList<Integer> unselect = new LinkedList<>();
		for(int i=0; i<len;i++) unselect.add(i);//add index to this list
		double avgdl = avgdl();
		
		while(!unselect.isEmpty())
		{
			double maxDiScore = Double.NEGATIVE_INFINITY;
			int maxDi = 0;
			for(int i=0; i<unselect.size();i++)//choose one from the unselected list
			{
				int curInd = unselect.get(i);
				double qaSim = similarity(qMap, answerList.get(curInd), qlength , alength[curInd]);
				
				double max = -1;//shouldn't be a problem
				for(int j=0; j<selected.size();j++)
				{
					int tmpInd = selected.get(j);
					//max = Math.max(max, bm25Similarity(answerList.get(tmpInd),answerList.get(curInd), alength[curInd], avgdl));
					max = Math.max(max, similarity(answerList.get(curInd),answerList.get(tmpInd), alength[curInd], alength[tmpInd]));
				}
//				double mSim = lamda*(qaSim-(1-lamda)*max);
				double mSim = lamda*qaSim-(1-lamda)*max;
				if(mSim >= maxDiScore)
				{
					maxDiScore = mSim;
					maxDi = i;//index
				}			
			}
			int tmp = unselect.remove(maxDi);
			selected.add(tmp);		
		}
		int[] order = new int[len];
		for(int i=0; i<len; i++)
			order[i] = selected.get(i);

		return order;
		
	}
	public int[] mmr_mixsim(double lamda) throws IOException
	{
		int len = answers.size();
		parseAll();
		LinkedList<Integer> selected = new LinkedList<>();
		LinkedList<Integer> unselect = new LinkedList<>();
		for(int i=0; i<len;i++) unselect.add(i);//add index to this list
		double avgdl = avgdl();
		
		while(!unselect.isEmpty())
		{
			double maxDiScore = Double.NEGATIVE_INFINITY;
			int maxDi = 0;
			for(int i=0; i<unselect.size();i++)//choose one from the unselected list
			{
				int curInd = unselect.get(i);
				double qaSim = bm25Similarity(qMap, answerList.get(curInd), qlength , alength[curInd]);
				
				double max = -1;//shouldn't be a problem
				for(int j=0; j<selected.size();j++)
				{
					int tmpInd = selected.get(j);
					//max = Math.max(max, bm25Similarity(answerList.get(tmpInd),answerList.get(curInd), alength[curInd], avgdl));
					max = Math.max(max, similarity(answerList.get(curInd),answerList.get(tmpInd), alength[curInd], alength[tmpInd]));
				}
				double mSim = lamda*(qaSim-(1-lamda)*max);
				//double mSim = lamda*qaSim-(1-lamda)*max;
				if(mSim >= maxDiScore)
				{
					maxDiScore = mSim;
					maxDi = i;//index
				}			
			}
			int tmp = unselect.remove(maxDi);
			selected.add(tmp);		
		}
		int[] order = new int[len];
		for(int i=0; i<len; i++)
			order[i] = selected.get(i);
				
		return order;
		
	}
	public static double similarity(HashMap<String, Integer> a1Map, HashMap<String, Integer> a2Map, int a1len, int a2len)
	{
		int cover = 0;
		for(String word: a1Map.keySet())
			if(a2Map.containsKey(word)) cover += Math.min(a1Map.get(word), a2Map.get(word));
		return (double)cover/(a1len+a2len);
	}
	public static double cosineSim(HashMap<String, Integer> a1Map, HashMap<String, Integer> a2Map)
	{
		double a1Length=0, a2Length=0;
		double dotProduct = 0;
		for(String word: a1Map.keySet())
			a1Length += Math.pow(a1Map.get(word), 2);
		a1Length = Math.sqrt(a1Length);
		for(String word: a2Map.keySet())
			a2Length += Math.pow(a2Map.get(word), 2);
		a2Length = Math.sqrt(a2Length);
		
		for(String word: a1Map.keySet())
			if(a2Map.containsKey(word)) 
				dotProduct += (double)a1Map.get(word)* a2Map.get(word);
		
		return dotProduct/(a1Length*a2Length);
	}
	public int[] mmr_bm25based(double lamda) throws IOException
	{
		int len = answers.size();
		parseAll();
		LinkedList<Integer> selected = new LinkedList<>();
		LinkedList<Integer> unselect = new LinkedList<>();
		for(int i=0; i<len;i++) unselect.add(i);//add index to this list
		double avgdl = avgdl();
		
		while(!unselect.isEmpty())
		{
			double maxDiScore = Double.NEGATIVE_INFINITY;
			int maxDi = 0;
			for(int i=0; i<unselect.size();i++)//choose one from the unselected list
			{
				int curInd = unselect.get(i);
				double qaSim = bm25Similarity(qMap, answerList.get(curInd), alength[curInd], avgdl);
				
				double max = -1;//shouldn't be a problem
				for(int j=0; j<selected.size();j++)
				{
					int tmpInd = selected.get(j);
					//max = Math.max(max, bm25Similarity(answerList.get(tmpInd),answerList.get(curInd), alength[curInd], avgdl));
					max = Math.max(max, bm25Similarity(answerList.get(curInd),answerList.get(tmpInd), alength[tmpInd], avgdl));
				}
//				double mSim = lamda*(qaSim-(1-lamda)*max);
				double mSim = lamda*qaSim-(1-lamda)*max;
				if(mSim >= maxDiScore)
				{
					maxDiScore = mSim;
					maxDi = i;//index
				}			
			}
			int tmp = unselect.remove(maxDi);
			selected.add(tmp);		
		}
		int[] order = new int[len];
		for(int i=0; i<len; i++)
			order[i] = selected.get(i);
		
		return order;
	}
	
	public static void parseAll() throws IOException
	{
		qlength = parse(question, qMap);
		for(int i=0;i<answers.size();i++)
		{
			HashMap<String, Integer> aMap = new HashMap<>();
			alength[i] = parse(answers.get(i), aMap);
			answerList.add(aMap);
		}
	}

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
		double docFreq = 0;
		double numDoc = (double)answerList.size();

		for(int i=0; i<answerList.size(); i++)
		{
			if(answerList.get(i).containsKey(query))
				docFreq++;
		}		
		score = (double)Math.log(1+(0.5+numDoc-docFreq)/(0.5+docFreq));
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

