package evalPackage;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ranking {

	public static int[] bm25(String question, String[] anwsers) throws IOException
	{
		myBM25 rk = new myBM25(question, anwsers);
		return rk.aBM25();
	}

	public static int[] random(String question, String[] anwsers, int seed)
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
	public static int[] mmr(String question, String[] anwsers, double lamda) throws IOException
	{
		myBM25 rk = new myBM25(question, anwsers);
		//return rk.mmr_bm25based(lamda);
		//return rk.mmr_simbased(lamda);
		return rk.mmr_mixsim(lamda);
		
	}
}
