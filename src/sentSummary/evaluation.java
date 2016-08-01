package sentSummary;

import java.util.ArrayList;

import evalPackage.testeval;

public class evaluation {
	/*public double alpha_ndcg_length(int[] order, ArrayList<String> ansSents, 
			double[] scores, ArrayList<int[]> neggets, int length)
	{
		int[] sentLength = new int[ansSents.size()];
		ArrayList<Double> newScores = new ArrayList<>();
		ArrayList<int[]> newNeggets = new ArrayList<>();
		for(int i=0; i<order.length; i++)
		{
			int tmp = order[i];
			sentLength[i] = ansSents.get(tmp).length();
			newScores.add(scores[tmp]);
			newNeggets.add(neggets.get(tmp));
		}
		return alpha_ndcgAtLength.alphandcg(sentLength, newScores, newNeggets, length);
	}*/
	
	public double sumEval(String s, ArrayList<ArrayList<String>> cluster, double alpha)
	{
		sumEval_alpha neweval = new sumEval_alpha(alpha);
		return neweval.performance(s, cluster);
	}
}
