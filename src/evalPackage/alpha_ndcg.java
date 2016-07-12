package evalPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class alpha_ndcg {
	static double alpha = 0.5;
	static double maxdcg = 0;
	
	public static double alphandcg(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
	{
		double dcg = alpha_dcg(scores, nuggets, m);
		double idcg = alpha_idcg_greedy(scores, nuggets, m);
		//double idcg = alpha_idcg_permute(scores, nuggets, m);
		//System.out.println("dcg = "+dcg+ "; idcg = "+idcg);
		return dcg/idcg;
	}
	public static double alpha_dcg(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
	{
		int len = scores.size();//#answers
		int num = nuggets.get(0).length;//#aspects for each answer

		double[] gain = new double[len];
		int[] appear = new int[num];//r,s[i-1]
		Arrays.fill(appear, 0);
		for(int i=0; i<len; i++)//a file
		{
			int[] judge = nuggets.get(i);
			for(int j=0; j<num; j++)//an aspect
				if(judge[j]!=0)
				{
					gain[i] += Math.pow((1-alpha), appear[j]);
					appear[j]++;
				}
		}
		for(int i=0; i<len; i++)
		{
			if(i==0)
				gain[i] = gain[i]/(Math.log(i+2)/Math.log(2));
			else
				gain[i] = gain[i-1] + gain[i]/(Math.log(i+2)/Math.log(2));
		}
		return gain[m];
	}
	
	public static double alpha_idcg_greedy(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
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
			remain.remove((Integer)rec);
		}
		for(int i=0; i<len; i++)
		{
			if(i==0) igain[i] = igain[i]/(Math.log(i+2)/Math.log(2));
			else igain[i] = igain[i-1]+igain[i]/(Math.log(i+2)/Math.log(2));
		}
/*		//icgain
		double[] icgain = new double[len];
		for(int i=0; i<len; i++)
		{
			if(i==0) icgain[i] = igain[i];
			else icgain[i] = igain[i] + icgain[i-1];
		}
		//discounted cumulative gain vector
		double[] idcgain = new double[len];
		for(int i=0; i<len; i++)
		{
			if(i==0) idcgain[i] = igain[i]/(Math.log(i+2)/Math.log(2));
			else idcgain[i] = idcgain[i-1]+ igain[i]/(Math.log(i+2)/Math.log(2));
			//System.out.println(idcgain[i]);
		}
		return idcgain[m];*/
		return igain[m];
	}
	/***************permutation******************/
	public static double alpha_idcg_permute(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
	{
		maxdcg = 0;
		int len = scores.size();
		int[] nums = new int[len];
		int x=0, y=len-1;
		for(int i=0; i<len; i++) 
		{
			if(scores.get(i)!=0) nums[x++] = i;
			else nums[y--]=i;
		}
		//System.out.println(x);
		permute(nums, scores, nuggets, m, x);
		return maxdcg;
	}
	
	
	public static void permute(int[] nums, ArrayList<Double> scores, ArrayList<int[]> nuggets, int m, int x) {
	    if(nums == null || nums.length == 0) return;
	    ArrayList<Integer> result = new ArrayList<>();
	    dfs(nums, result, scores, nuggets, m, x);
	}

	public static void dfs(int[] nums, ArrayList<Integer> result, ArrayList<Double> scores, ArrayList<int[]> nuggets, int m, int x){
	   // if(nums.length == result.size()){
		if(x == result.size()){
	    	ArrayList<Integer> temp = new ArrayList<>();
	    	temp.addAll(result);
	    	//process the result--temp

	    	
	    	ArrayList<Double> scores_t = new ArrayList<>();
			ArrayList<int[]> nuggets_t = new ArrayList<>();
	    	for(int i=0;i<x; i++)
	    	{
	    		scores_t.add(scores.get(temp.get(i)));
	    		nuggets_t.add(nuggets.get(temp.get(i)));
	    	}
	    	for(int i=x;i<nums.length; i++)
	    	{
	    		scores_t.add(scores.get(nums[i]));
	    		nuggets_t.add(nuggets.get(nums[i]));
	    	}
	    	maxdcg = Math.max(maxdcg, alpha_dcg(scores_t,nuggets_t,m));
	    }        
	    for(int i=0; i<x; i++){
	        if(result.contains(nums[i])) continue; 
	        result.add(nums[i]);
	        dfs(nums, result, scores, nuggets, m, x);
	        result.remove(result.size()-1);
	    }
	}
}
