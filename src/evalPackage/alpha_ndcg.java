package evalPackage;

import java.util.ArrayList;
import java.util.LinkedList;

public class alpha_ndcg {
	static double alpha = 0;
	
	public static double alphandcg(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
	{
		double dcg = alpha_dcg(scores, nuggets, m);
		double idcg = alpha_idcg(scores, nuggets, m);
		return dcg/idcg;
	}
	public static double alpha_dcg(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
	{
		int len = scores.size();
		int num = nuggets.get(0).length;
		//alpha gain vector gain[]
		double[] gain = new double[len];
		int[] appear = new int[num];
		for(int i=0; i<len; i++)//certain file
		{
			int[] judge = nuggets.get(i);
			for(int j=0; j<num; j++)
			{
				gain[i] += judge[j]*Math.pow((1-alpha), appear[j]);
				appear[j] += judge[j];
				
			}
			//System.out.println(gain[i]);
		}
		//cumulative gain vector
		double[] cgain = new double[len];
		for(int i=0; i<len; i++)
		{
			if(i==0) cgain[i] = gain[i];
			else cgain[i] = gain[i] + cgain[i-1];
			//System.out.println(cgain[i]);
		}
		//discounted cumulative gain vector
		double[] dcgain = new double[len];
		for(int i=0; i<len; i++)
		{
			if(i==0) dcgain[i] = gain[i]/(Math.log(i+2)/Math.log(2));
			else dcgain[i] = dcgain[i-1]+ gain[i]/(Math.log(i+2)/Math.log(2));
			//System.out.println(dcgain[i]);
		}
		return dcgain[m];
	}
	
	public static double alpha_idcg(ArrayList<Double> scores, ArrayList<int[]> nuggets, int m)
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
	
}
