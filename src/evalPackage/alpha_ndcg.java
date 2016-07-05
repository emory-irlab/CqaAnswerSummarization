package evalPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class alpha_ndcg {
	static double alpha = 0;
	static double maxdcg = 0;
	/*public static void main(String[] args)//test case
	{
		ArrayList<Double> scores = new ArrayList<>();
		ArrayList<int[]> nuggets = new ArrayList<>();
		int[] t = new int[]{2,1,1,0,2,1,1,1,0,0};		
		for(int i=0; i<10;i++)
		{
			scores.add((double)t[i]);
		}
		nuggets.add(new int[]{0,1,0,1,0,0});//a
		nuggets.add(new int[]{0,1,0,0,0,0});//b
		nuggets.add(new int[]{0,1,0,0,0,0});//c
		nuggets.add(new int[]{0,0,0,0,0,0});//d
		nuggets.add(new int[]{1,0,0,0,0,1});//e
		nuggets.add(new int[]{1,0,0,0,0,0});//f
		nuggets.add(new int[]{0,0,1,0,0,0});//g
		nuggets.add(new int[]{1,0,0,0,0,0});//h
		nuggets.add(new int[]{0,0,0,0,0,0});//i
		nuggets.add(new int[]{0,0,0,0,0,0});//j
		double dcg = alphandcg(scores, nuggets, 9);
		System.out.println(dcg);
	}
	*/
	
	public static double alphandcg(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
	{
		double dcg = alpha_dcg(scores, nuggets, m);
		double idcg = alpha_idcg_greedy(scores, nuggets, m);
		//double idcg = alpha_idcg_permute(scores, nuggets, m);
		//System.out.println(idcg);
		return dcg/idcg;
	}
	public static double alpha_dcg(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
	{
		int len = scores.size();//#answers
		int num = nuggets.get(0).length;//#aspects for each answer
		//alpha gain vector gain[]
		double[] gain = new double[len];
		int[] appear = new int[num];
		for(int i=0; i<len; i++)//certain file
		{
			int[] judge = nuggets.get(i);
			for(int j=0; j<num; j++)
				if(judge[j]!=0)
				{
					gain[i] += judge[j]*Math.pow((1-alpha), appear[j]);
					appear[j] += judge[j];
				}			
		}
		//cumulative gain vector
		double[] cgain = new double[len];
		for(int i=0; i<len; i++)
			if(i==0) cgain[i] = gain[i];
			else cgain[i] = gain[i] + cgain[i-1];
		//discounted cumulative gain vector
		double[] dcgain = new double[len];
		for(int i=0; i<len; i++)
			if(i==0) dcgain[i] = gain[i]/(Math.log(i+2)/Math.log(2));
			else dcgain[i] = dcgain[i-1]+ gain[i]/(Math.log(i+2)/Math.log(2));
		return dcgain[m];
	}

/*	public static double alpha_idcg_permute(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
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
	}*/
	public static double alpha_idcg_greedy(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
	{
		//greedy
		int len = scores.size();
		int num = nuggets.get(0).length;
		int[] order = new int[len];
		double[] igain = new double[len];
		int[] appear = new int[num];
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
				{
					cur += (double)mx[j]*Math.pow((1-alpha), appear[j]);
				}
				if(cur>max) 
				{
					rec = remain.get(k);
					max = cur;
				}
			}
			order[i] = rec;
			igain[i] = max;	
			int[] mx = nuggets.get(rec);
			for(int j=0; j<num; j++)
				appear[j] += mx[j];
			boolean  xx = remain.remove((Integer)rec);
			//System.out.println(rec);
		}
		//icgain
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
		return idcgain[m];
	}
	/***************permutation******************/
/*	public static void permute(int[] nums, ArrayList<Double> scores, ArrayList<int[]> nuggets, int m, int x) {
	    if(nums == null || nums.length == 0) 
	    	{
	    	//System.out.println(nums.length);
	    	return;
	    	}
	    ArrayList<Integer> result = new ArrayList<>();
	    dfs(nums, result, scores, nuggets, m, x);
	}

	public static void dfs(int[] nums, ArrayList<Integer> result, ArrayList<Double> scores, ArrayList<int[]> nuggets, int m, int x){
	   // if(nums.length == result.size()){
		if(x == result.size()){
	    	ArrayList<Integer> temp = new ArrayList<>(result);
	    	//process the result--temp
	    	for(int i=0;i<nums.length; i++)
	    		System.out.println(temp.get(i));
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
	}*/
}
