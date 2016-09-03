package evalPackage;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ranking {

	public static String best(ArrayList<ArrayList<int[]>> ngLocs, String[] anwsers, int length, double alpha)
	{
		bestPerf bf = new bestPerf(ngLocs, anwsers, length, alpha);
		return bf.rank();
	}

	public static int[] bm25(String question, String[] anwsers) throws IOException
	{
		myBM25 rk = new myBM25(question, anwsers);
		return rk.aBM25();
	}
	
	public static String bm25_loc(String question, String[] anwsers, int length) throws IOException
	{
		BM25_loc rk = new BM25_loc(question, anwsers, length);
		return rk.aBM25();
	}

	public static int[] random(String question, String[] anwsers, int seed)
	{
		int[] order = new int[anwsers.length];
		
		ArrayList<Integer> temp = new ArrayList<>();
		for(int x=0;x<anwsers.length;x++)  temp.add(x);
		
		//Random rn = new Random(seed);
		Random rn = new Random(seed);
		int i=0;
		for(int x=anwsers.length; x>0;x--)
			order[i++] = temp.remove((int)rn.nextInt(x));
		return order;
	}
	
	public static String randString(String question, String[] anwsers, int seed, int finalLength)
	{
		String result = "";
		
		List<Integer> temp = new ArrayList<>();
		for(int x=0;x<anwsers.length;x++)  temp.add(x);
		Collections.shuffle(temp, new Random(seed));
		
		int diff = finalLength;
		for(int i=0; i<anwsers.length; i++)
		{
			int nxt = temp.get(i);
			result += anwsers[nxt]+" ";
			diff -= anwsers[nxt].length()+1;
			if(diff<=0) 
			{
				result = result.substring(0,finalLength);
				break;
			}
		}		
		return result;
	}
	
	public static int[] mmr(String question, String[] anwsers, double lamda) throws IOException
	{
		myBM25 rk = new myBM25(question, anwsers);
		return rk.mmr_bm25based(lamda);
		//return rk.mmr_simbased(lamda);
		//return rk.mmr_mixsim(lamda);
		
	}
	
	public static String mmr_loc(String question, String[] anwsers, double lamda, int length) throws IOException
	{
		BM25_loc rk = new BM25_loc(question, anwsers, length);		
		return rk.mmr_bm25based(lamda);		
	}

}
