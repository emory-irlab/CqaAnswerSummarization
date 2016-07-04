package evalPackage;

import java.util.ArrayList;
import java.util.Arrays;

public class support {
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
		
		double nol = supportMetric(scores, nuggets);
		//double nol = noveltyCost(scores, nuggets,0.2);
		System.out.println(nol);
	}
	public static double supportMetric(ArrayList<Double> scores, ArrayList<int[]> nuggets)
	{
		double result = 0;
		for(int i=1; i<=10; i++)
		{
			double xx = normSupportCost(scores, nuggets, (double)i/10.0);
			result += xx;
			//System.out.println(xx);
		}
		return result/10.0;
	}
	public static double normSupportCost(ArrayList<Double> scores, ArrayList<int[]> nuggets, double r)
	{
		double minCost = minSupportCost(scores, nuggets, r);
		double cost = supportCost(scores, nuggets, r);
		System.out.println(minCost+", "+cost);
		return minCost/cost;
	}
	public static double minSupportCost(ArrayList<Double> scores, ArrayList<int[]> nuggets, double r)
	{
		int numOfAns = scores.size();
		int numOfAsp = nuggets.get(0).length;
		
		ArrayList<Double> scores_t = new ArrayList<>(scores);
		ArrayList<int[]> nuggets_t = new ArrayList<>(nuggets);
		boolean[] exist = new boolean[numOfAsp];
		Arrays.fill(exist, false);		
		
		double totalProps = 0;
		for(int i=0; i<numOfAns; i++)//answer
		{
			int[] curNugget = nuggets.get(i);
			for(int j=0; j<numOfAsp; j++)//aspect
				totalProps += curNugget[j];
		}
		
		double sumProps = 0;
		double cost=0;
		
		for(int i=0; i<numOfAns;i++)
		{
			double max = 0;	
			int nxt = 0;
			for(int j=0; j<scores_t.size();j++)
			{
				int[] curNugget = nuggets_t.get(j);
				double newProps = 0;
				//double allAsp = scores_t.get(j);
				double allProps = 0;
				for(int k=0; k<numOfAsp; k++)
				{
					if(curNugget[k]!=0 && exist[k]==false) 
						newProps += curNugget[k];
					allProps += curNugget[k];
				}
				
				//if(newAsp/allAsp>max)
				if(newProps>max)
				{
					//max = newAsp/allAsp;
					max = newProps;
					nxt = j;
				}
				//System.out.println("r="+r+"; "+"j="+j+", "+newAsp+", "+allAsp);
			}
			double totalAsp = scores_t.remove(nxt);
			int[] nxtNugget = nuggets_t.remove(nxt);
			//System.out.println("choose: "+nxt);
			double allProps = 0;
			if(totalAsp!=0)
			{
				double newProps=0;
				for(int l=0;l<nxtNugget.length;l++)
				{
					if(nxtNugget[l]!=0 && exist[l]==false)
					{
						exist[l]=true;
						sumProps += nxtNugget[l];
						newProps += nxtNugget[l];
					}
					allProps += nxtNugget[l];
				}
				cost += 1+beta*(1-newProps/allProps);
			}			
			if(sumProps/totalProps>=r) break;
		}
		//System.out.println(r+", "+cost);

		return cost;
	}
	
	
	public static double supportCost(ArrayList<Double> scores, ArrayList<int[]> nuggets, double r)
	{
		int numOfAns = scores.size();
		int numOfAsp = nuggets.get(0).length;
		
		//calculate m
		//double aspSum = 0;
		double totalProps = 0;
		for(int i=0; i<numOfAns; i++)//answer
		{
			int[] curNugget = nuggets.get(i);
			for(int j=0; j<numOfAsp; j++)//aspect
				totalProps += curNugget[j];
		}
		double propsSum = 0;
		double cost = 0;
		boolean[] exist = new boolean[numOfAsp];
		Arrays.fill(exist, false);
		for(int i=0; i<numOfAns; i++)//answer
		{
			int[] curNugget = nuggets.get(i);
			//double newAsp = 0;
			double newProps = 0;
			double allAsp = scores.get(i);
			double allProps = 0;
			
			if(allAsp != 0)
			{
				for(int j=0; j<numOfAsp; j++)//aspect
				{
					if(curNugget[j]!=0 && exist[j]==false)
					{
						exist[j]=true;
						//newAsp++;
						newProps += curNugget[j];
						//aspSum++;
						propsSum += curNugget[j];
					}
					allProps += curNugget[j];
				}
				//cost += 1+beta*(1-newAsp/allAsp);
				cost += 1+beta*(1-newProps/allProps);
				//System.out.println(aspSum+", "+numOfAsp);
				//System.out.println(newAsp+", "+allAsp);
				if(propsSum/totalProps>=r) break;
			}

		}
		
		return cost;
	}
}
