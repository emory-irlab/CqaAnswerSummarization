package evalPackage;

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

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class BM25_loc {
	static String question;
	static String[] answers;
	
	static int qlength;
	static int[] alength;
	
	static HashMap<String, ArrayList<Integer>> qMap;
	static ArrayList<HashMap<String, ArrayList<Integer>>> answerList;
	
	static int answerLength;
	
	public BM25_loc(String q, String[] as, int l)
	{
		this.question = q;
		this.answers = as;
		this.answerLength = l;

		alength = new int[as.length];
		answerList = new ArrayList<>();
		qMap = new HashMap<>();
	}
	
	public String aBM25() throws IOException
	{
		String finalAnswer = "";
		int diff = answerLength;
		parseAll();
		ArrayList<Integer> remain = new ArrayList<>();
		for(int i=0; i<answers.length; i++)
			remain.add(i);
		double avgdl = avgdl();
		while(!remain.isEmpty())
		{
			double maxScore = -1;
			int nxt=0;
			for(int i=0;i<remain.size();i++)
			{
				double curScore = bm25Similarity(qMap, answerList.get(i), alength[i], avgdl, diff);
				if(curScore > maxScore)
				{
					maxScore = curScore;
					nxt = i;
				}
			}
			String curAns = answers[remain.get(nxt)];
			remain.remove(nxt);
			if(diff >= curAns.length()) finalAnswer += curAns+" ";
			else finalAnswer += curAns.substring(0, diff);
			diff -= curAns.length()+1;
			if(diff <= 0) break;
		}
		return finalAnswer;
	}
	/*
	public static int[] mmr_simbased(double lamda) throws IOException
	{
		int len = answers.length;
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
*/
	public static String mmr_bm25based(double lamda) throws IOException
	{
		String finalAnswer = "";
		int diff = answerLength;
		parseAll();
		LinkedList<Integer> selected = new LinkedList<>();
		LinkedList<Integer> unselect = new LinkedList<>();
		for(int i=0; i<answers.length; i++) unselect.add(i);//add index to this list
		double avgdl = avgdl();
		
		while(!unselect.isEmpty())
		{
			double maxDiScore = Double.NEGATIVE_INFINITY;
			int maxDi = 0;
			for(int i=0; i<unselect.size();i++)//choose one from the unselected list
			{
				int curInd = unselect.get(i);
				double qaSim = bm25Similarity(qMap, answerList.get(curInd), alength[curInd], avgdl, diff);
				
				double max = -1;//shouldn't be a problem
				for(int j=0; j<selected.size();j++)
				{
					int tmpInd = selected.get(j);
					max = Math.max(max, bm25Similarity(answerList.get(curInd),answerList.get(tmpInd), alength[tmpInd], avgdl, diff));
				}
				double mSim = lamda*qaSim-(1-lamda)*max;
				if(mSim >= maxDiScore)
				{
					maxDiScore = mSim;
					maxDi = i;//index
				}			
			}
			String curAns = answers[unselect.get(maxDi)];
			int tmp = unselect.remove(maxDi);
			selected.add(tmp);
			if(diff >= curAns.length()) finalAnswer += curAns+" ";
			else finalAnswer += curAns.substring(0, diff);
			diff -= curAns.length()+1;
			if(diff <= 0) break;
		}
		return finalAnswer;
	}
	
	public static double simpleSim(HashMap<String, Integer> a1Map, HashMap<String, Integer> a2Map, int a1len, int a2len)
	{
		int cover = 0;
		for(String word: a1Map.keySet())
			if(a2Map.containsKey(word)) cover += Math.min(a1Map.get(word), a2Map.get(word));
		return (double)cover/(a1len+a2len);
	}
	
	public static void parseAll() throws IOException
	{
		qlength = parse(question, qMap);
		for(int i=0;i<answers.length;i++)
		{
			HashMap<String, ArrayList<Integer>> aMap = new HashMap<>();
			alength[i] = parse(answers[i], aMap);
			answerList.add(aMap);
		}
	}

	public static int parse(String line, HashMap<String, ArrayList<Integer>> an) throws IOException//return #words of string
	{
		int length = 0;
		Document doc = new Document(line);
		int curPos = 0;
		for(Sentence sent: doc.sentences())
		{
			for(String word: sent.words())
			{
				length++;
				curPos = line.indexOf(word, curPos)+word.length();
				if(an.containsKey(word)) an.get(word).add(curPos);
				else
				{
					ArrayList<Integer> loc = new ArrayList<>();
					loc.add(curPos);
					an.put(word, loc);
				}
			}
		}
		return length;
	}
	public static double bm25Similarity(HashMap<String, ArrayList<Integer>> qMap, HashMap<String, ArrayList<Integer>> aMap, 
			int alen, double avgdl, int diff)
	{
		double score = 0;
		for(String word: qMap.keySet())
			if (aMap.containsKey(word))
			   score += idf(word)*freq(word, aMap, avgdl, alen, diff);
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
	public static double freq(String query, HashMap<String,  ArrayList<Integer>> an1, double avgdl, double len, int diff)
	{
		double fq = 0;
		double k = 1.2;
		double b = 0.75;
		if(an1.containsKey(query))
		{
			ArrayList<Integer> loc = an1.get(query);
			for(Integer i: loc)
			{
				if(i<=diff) fq++;
			}
			
		}
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
				return Double.compare(b[0], a[0]);
			}
		});
		
		for(int i=0; i<scores.length; i++)
			result[i] = (int) pair.get(i)[1];
		return result;
	}
}

