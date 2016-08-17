package sentSummary;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class rankSent {
	
	int[] numOfSent;//how many sentences in each answer
	int totalSent;// total number of sentences
	int finalLength;
	
	String question;
	ArrayList<String> allSent;
	ArrayList<ArrayList<int[]>> allngloc;
	
	public rankSent(String q, ArrayList<ArrayList<String>> anwsers, ArrayList<ArrayList<ArrayList<int[]>>> nglocs, int length)
	{
		this.numOfSent = new int[anwsers.size()];
		this.totalSent = 0;
		this.question = q;
		this.allSent = new ArrayList<>();
		this.allngloc = new ArrayList<>();
		this.finalLength = length;
		for(int i=0; i<anwsers.size(); i++)
		{
			ArrayList<String> sents = anwsers.get(i);
			ArrayList<ArrayList<int[]>> ngloc = nglocs.get(i);
			allSent.addAll(sents);
			allngloc.addAll(ngloc);
			
			numOfSent[i] = sents.size();
			totalSent += numOfSent[i];
		}		
	}
	
	public String random()
	{	
		String result = "";
		List<Integer> temp = new ArrayList<>();
		for(int x=0;x<allSent.size();x++)  temp.add(x);
		Collections.shuffle(temp);
		
		/*Random rn = new Random(seed);
		int i=0;
		for(int x=anwsers.size(); x>0;x--)
			order[i++] = temp.remove(rn.nextInt(x));*/
		
		int diff = finalLength;
		for(int i=0; i<allSent.size(); i++)
		{
			int nxt = temp.get(i);
			result += allSent.get(nxt)+" ";
			diff -= allSent.get(nxt).length()+1;
			if(diff<=0) 
			{
				result = result.substring(0,finalLength);
				break;
			}
		}		
		return result;
	}
	
	public String bm25() throws IOException
	{
		SentBM25_loc rk = new SentBM25_loc(question, allSent, finalLength);
		return rk.aBM25();
	}
	
	public String mmr(double lamda) throws IOException
	{
		SentBM25_loc rk = new SentBM25_loc(question, allSent, finalLength);
		return rk.mmr_bm25based(lamda);
		//return rk.mmr_simbased(lamda);
		//return rk.mmr_mixsim(lamda);
		
	}
	
	public String best(double alpha)
	{
		bestPerfSent bf = new bestPerfSent(allngloc, allSent, finalLength, alpha);
		return bf.rank();
	}
}

