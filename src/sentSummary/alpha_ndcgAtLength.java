package sentSummary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class alpha_ndcgAtLength {
	static double alpha = 0.1;
	static double maxdcg = 0;
	
	public static double alphandcg(int[] sentLength, ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)//m--length of answer
	{
		double dcg = alpha_dcg(sentLength,scores, nuggets, m);
		double idcg = alpha_idcg_greedy(sentLength, scores, nuggets, m);
		//double idcg = alpha_idcg_permute(scores, nuggets, m);
		//System.out.println("dcg = "+dcg+ "; idcg = "+idcg);
		return dcg/idcg;
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
	
	public static double alpha_idcg_greedy(int[] sentLength, ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
	{
		//greedy
		int len = scores.size();
		int num = nuggets.get(0).length;
		int[] order = new int[len];
		double[] igain = new double[len];
		int[] appear = new int[num];
		Arrays.fill(appear, 0);
		LinkedList<Integer> remain = new LinkedList<>();
		for(int i=0; i<len; i++)
			remain.add(i);
		
		int curLength = 0;
		int numOfSent = 0;
		
		for(int i=0; i<len; i++)
		{
			double max = 0;
			int rec = 0;			
			for(int k=0; k<remain.size();k++)
			{
				double cur = 0;
				int[] mx = nuggets.get(remain.get(k));
				for(int j=0; j<num; j++)
					if(mx[j]!=0) cur += (double)Math.pow((1-alpha), appear[j]);
				if(cur>=max) 
				{
					rec = remain.get(k);
					max = cur;
				}
			}
			order[i] = rec;
			igain[i] = max;	
			int[] mx = nuggets.get(rec);
			for(int j=0; j<num; j++)
			{
				if(mx[j]!=0) appear[j]++;
			}
			numOfSent++;
			curLength += sentLength[rec];
			remain.remove((Integer)rec);
			if(curLength >= m) break;
		}
		for(int i=0; (i<=numOfSent)&&(i<len); i++)
		{
			if(i==0) igain[i] = igain[i]/(Math.log(i+2)/Math.log(2));
			else igain[i] = igain[i-1]+igain[i]/(Math.log(i+2)/Math.log(2));
		}
		return igain[numOfSent<(len-1)?numOfSent:(len-1)];
	}
}
