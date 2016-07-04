package evalPackage;

import java.util.ArrayList;
import java.util.Arrays;

public class novelty {
	static double beta = 0.5;
	public static void main(String[] args)
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
		
		double nol = noveltyMetric(scores, nuggets);
		//double nol = noveltyCost(scores, nuggets,0.5);
		System.out.println(nol);
	}
	public static double noveltyMetric(ArrayList<Double> scores, ArrayList<int[]> nuggets)
	{
		double result = 0;
		for(int i=1; i<=10; i++)
			result += normNoveltyCost(scores, nuggets, (double)i/10.0);
		return result/10.0;
	}
	public static double normNoveltyCost(ArrayList<Double> scores, ArrayList<int[]> nuggets, double r)
	{
		double minCost = minNoveltyCost(scores, nuggets, r);
		double cost = noveltyCost(scores, nuggets, r);
		return minCost/cost;
	}
	public static double minNoveltyCost(ArrayList<Double> scores, ArrayList<int[]> nuggets, double r)
	{
		int numOfAns = scores.size();
		int numOfAsp = nuggets.get(0).length;
		double cost=0;
		ArrayList<Double> scores_t = new ArrayList<>(scores);
		ArrayList<int[]> nuggets_t = new ArrayList<>(nuggets);
		boolean[] exist = new boolean[numOfAsp];
		Arrays.fill(exist, false);
		double sumAsp = 0;
		for(int i=0; i<numOfAns;i++)
		{
			double max = 0;	
			int nxt = 0;
			for(int j=0; j<scores_t.size();j++)
			{
				int[] curNugget = nuggets_t.get(j);
				double newAsp = 0;
				double allAsp = scores_t.get(j);
				//if(allAsp == 0) 
				for(int k=0; k<numOfAsp; k++)
					if(curNugget[k]!=0 && exist[k]==false) 
						newAsp++;
				if(newAsp/allAsp>max)
				{
					max = newAsp/allAsp;
					nxt = j;
				}
				//System.out.println("r="+r+"; "+"j="+j+", "+newAsp+", "+allAsp);
			}
			double totalAsp = scores_t.remove(nxt);
			int[] nxtNugget = nuggets_t.remove(nxt);
			//System.out.println("choose: "+nxt);
			if(totalAsp!=0)
			{
				double newAsp=0;
				for(int l=0;l<nxtNugget.length;l++)
				{
					if(nxtNugget[l]!=0 && exist[l]==false)
					{
						exist[l]=true;
						sumAsp++;
						newAsp++;
					}
				}
				cost += 1+beta*(1-newAsp/totalAsp);
			}			
			if(sumAsp/(double)numOfAsp>=r) break;
		}
		//System.out.println(r+", "+cost);

		return cost;
	}
	public static double noveltyCost(ArrayList<Double> scores, ArrayList<int[]> nuggets, double r)
	{
		int numOfAns = scores.size();
		int numOfAsp = nuggets.get(0).length;
		
		//calculate m
		int m = 0;
		int aspSum = 0;
		double cost = 0;
		boolean[] exist = new boolean[numOfAsp];
		Arrays.fill(exist, false);
		for(int i=0; i<numOfAns; i++)
		{
			int[] curNugget = nuggets.get(i);
			double newAsp = 0;
			double allAsp = scores.get(i);
			if(allAsp != 0)
			{
				for(int j=0; j<numOfAsp; j++)
				{
					if(curNugget[j]!=0 && exist[j]==false)
					{
						exist[j]=true;
						newAsp++;
						aspSum++;
					}
				}
				cost += 1+beta*(1-newAsp/allAsp);
				//System.out.println(aspSum+", "+numOfAsp);
				//System.out.println(newAsp+", "+allAsp);
				if((double)aspSum/(double)numOfAsp>=r) 
				{
					//System.out.println("here");
					m = i;
					break;
				}
			}

		}
		
		return cost;
	}
	
	public static ArrayList<ArrayList<Integer>> permute(int[] num) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
	 
		//start from an empty list
		result.add(new ArrayList<Integer>());
	 
		for (int i = 0; i < num.length; i++) {
			//list of list in current iteration of the array num
			ArrayList<ArrayList<Integer>> current = new ArrayList<ArrayList<Integer>>();
	 
			for (ArrayList<Integer> l : result) {
				// # of locations to insert is largest index + 1
				for (int j = 0; j < l.size()+1; j++) {
					// + add num[i] to different locations
					l.add(j, num[i]);
	 
					ArrayList<Integer> temp = new ArrayList<Integer>(l);
					current.add(temp);
	 
					//System.out.println(temp);
	 
					// - remove num[i] add
					l.remove(j);
				}
			}
	 
			result = new ArrayList<ArrayList<Integer>>(current);
		}
	 
		return result;
	}
	/*
	public static void permute(int n) {
        List<List<Integer>> result = new ArrayList<>();
        ArrayList<Integer> elements = new ArrayList<>();
        
        for(int i=0; i<n; i++) elements.add(i);
        for(int i=0; i<n; i++)
        {
            ArrayList<Integer> prefix = new ArrayList<>();
            ArrayList<Integer> remain = new ArrayList<>(elements);
            prefix.add(i);
            remain.remove(i);
            permute_b(prefix,remain);
        }
    }
   // private static void permute_b(ArrayList<Integer> prefix, ArrayList<Integer> remain, List<List<Integer>> result)
    private static void permute_b(ArrayList<Integer> prefix, ArrayList<Integer> remain)
    {
        if(remain.size() == 0)
        {
            //result.add(prefix);
            return;
        }
        else
        {
            for(int i=0; i<remain.size(); i++)
            {
                ArrayList<Integer> pri = new ArrayList<>(prefix);
                ArrayList<Integer> rem = new ArrayList<>(remain);
                pri.add(remain.get(i));
                rem.remove(i);
                permute_b(pri,rem);
            }
        }
    }*/
}
