package sentSummary;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ranking {

	public int[] bm25(String question, ArrayList<String> anwsers) throws IOException
	{
		sentBM25 rk = new sentBM25(question, anwsers);
		return rk.aBM25();
	}

	public int[] random(String question, String[] anwsers, int seed)
	{
		int[] order = new int[anwsers.length];
		
		ArrayList<Integer> temp = new ArrayList<>();
		for(int x=0;x<anwsers.length;x++)  temp.add(x);
		
		Random rn = new Random(seed);
		int i=0;
		for(int x=anwsers.length; x>0;x--)
			order[i++] = temp.remove(rn.nextInt(x));
		return order;
	}
	
	public int[] mmr(String question, ArrayList<String> anwsers, double lamda) throws IOException
	{
		sentBM25 rk = new sentBM25(question, anwsers);
		return rk.mmr_bm25based(lamda);
		//return rk.mmr_simbased(lamda);
		//return rk.mmr_mixsim(lamda);
		
	}
}

