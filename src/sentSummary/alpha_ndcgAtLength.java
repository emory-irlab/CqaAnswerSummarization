package sentSummary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class alpha_ndcgAtLength {
	static double alpha = 0.6;
	static double maxdcg = 0;
	
	public static double alphandcg(int[] sentLength, ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)//m--length of answer
	{
		double dcg = alpha_dcg(sentLength,scores, nuggets, m);
		//double idcg = alpha_idcg_permute(scores, nuggets, m);
		//System.out.println("dcg = "+dcg+ "; idcg = "+idcg);
		return dcg;
	}
	public static double alpha_dcg(int[] sentLength, ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
	{
		int len = scores.size();//#answers
		int num = nuggets.get(0).length;//#aspects for each answer
		
		int curLength = 0;		
		int numOfSent=0;
		
		double[] gain = new double[len];
		int[] appear = new int[num];//r,s[i-1]
		Arrays.fill(appear, 0);
		//for(int i=0; i<len; i++)		
		for(int i=0; (curLength<m)&&(i<len); i++)//a file
		{
			numOfSent++;
			int[] judge = nuggets.get(i);
			for(int j=0; j<num; j++)//an aspect
				if(judge[j]!=0)
				{
					gain[i] += Math.pow((1-alpha), appear[j]);
					appear[j]++;
				}
			curLength += sentLength[i];
		}
		for(int i=0; (i<=numOfSent) &&(i<len); i++)
		{
			if(i==0)
				gain[i] = gain[i]/(Math.log(i+2)/Math.log(2));
			else
				gain[i] = gain[i-1] + gain[i]/(Math.log(i+2)/Math.log(2));
		}
		return gain[numOfSent<(len-1)?numOfSent:(len-1)];
	}
}
