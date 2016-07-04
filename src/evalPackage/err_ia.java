package evalPackage;

import java.util.ArrayList;

public class err_ia {
/*	public static void main(String[] args)//test case
	{
		ArrayList<Double> scores = new ArrayList<>();
		ArrayList<int[]> nuggets = new ArrayList<>();
		int[] maxProp = new int[]{3,3,1,1,0,1};
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
		double dcg = err_Intent(scores, nuggets, maxProp);
		double n = err_Opti(scores, nuggets, maxProp);
		System.out.println(dcg/n);
	}*/
	public static double nerr(ArrayList<Double> scores, ArrayList<int[]> nuggets, int[] maxProp)
	{
		double err_ia = err_Intent(scores, nuggets, maxProp);
		double err_op = err_Opti(scores, nuggets, maxProp);
		return err_ia/err_op;
	}
/*	public static double err_Intent(ArrayList<Double> scores, ArrayList<int[]> nuggets, int[] maxProp)
	{
		int len = scores.size();
		int num = nuggets.get(0).length;
		int sumProp = 0;
		for(int i=0; i<maxProp.length;i++)
			sumProp += maxProp[i];
		double err = 0;
		double[] p = new double[num];
		//for(int i=0; i<num;i++) p[i]=1;
		for(int i=0; i<num;i++) p[i]=(double)maxProp[i]/(double)sumProp;
		for(int i=0; i<len;i++)
		{
			int[] cur = nuggets.get(i);
			double sum = 0;
			for(int j=0; j<num; j++)
			{
				int c = (cur[j]==0?0:1);
				double r;
				if(maxProp[j]>30 && (maxProp[j]-cur[j])>30) r = 0;
				else if(maxProp[j]>30) r = (double) 1.0/(1<<(maxProp[j]-cur[j]));
				else r = (double)((1<<cur[j])-1)/(1<<maxProp[j]);
				//double r = (double)((1<<c)-1)/(1<<1);
				//sum += r*p[j]/(double)(j+1);//how to define this possibility?
				//sum += r*p[j]*((double)(cur[j]+1)/(double)(maxProp[j]+1));
				sum += r*p[j];
				//sum += r*p[j]*((double)maxProp[j]/(double)sumProp);
				//System.out.println(r+", "+sum);
				p[j] = p[j]*(1-r);
			}
			err += sum/(i+1);
		}
		return err;
	}*/
	public static double err_Intent(ArrayList<Double> scores, ArrayList<int[]> nuggets, int[] maxProp)
	{
		int len = scores.size();
		int num = nuggets.get(0).length;
		int sumProp = 0;
		for(int i=0; i<maxProp.length;i++)
			sumProp += maxProp[i];
		double err = 0;
		double[] p = new double[num];
		//for(int i=0; i<num;i++) p[i]=1;
		for(int i=0; i<num;i++) p[i]=(double)maxProp[i]/(double)sumProp;
		
		for(int i=0; i<len;i++)//every answer
		{
			int[] cur = nuggets.get(i);
			double sum = 0;
			for(int j=0; j<num; j++)//every topic
			{
				int c = (cur[j]==0?0:1);
				double r;
				if(maxProp[j]>30 && (maxProp[j]-cur[j])>30) r = 0;
				else if(maxProp[j]>30) r = (double) 1.0/(1<<(maxProp[j]-cur[j]));
				else r = (double)((1<<cur[j])-1)/(1<<maxProp[j]);
				//double r = (double)((1<<c)-1)/(1<<1);
				//sum += r*p[j]/(double)(j+1);//how to define this possibility?
				//sum += r*p[j]*((double)(cur[j]+1)/(double)(maxProp[j]+1));
				sum += r*p[j];
				//sum += r*p[j]*((double)maxProp[j]/(double)sumProp);
				//System.out.println(r+", "+sum);
				p[j] = p[j]*(1-r);
			}
			err += sum/(i+1);
		}
		return err;
	}
	public static double err_Opti(ArrayList<Double> scores, ArrayList<int[]> nuggets, int[] maxProp)
	{
		int len = scores.size();
		int num = nuggets.get(0).length;
		int sumProp = 0;
		for(int i=0; i<maxProp.length;i++)
			sumProp += maxProp[i];
		double err = 0;
		double[] p = new double[num];
		//for(int i=0; i<num;i++) p[i]=(double)maxProp[i]/(double)sumProp;
		
		ArrayList<Double> scores_t = new ArrayList<>();
		ArrayList<int[]> nuggets_t = new ArrayList<>();
		scores_t.addAll(scores);
		nuggets_t.addAll(nuggets);
		//System.out.println(len);
		for(int k=0; k<len;k++)
		{
			//System.out.println("k loop:"+ k+", "+len);
			double maxSum = 0;
			int indx = 0;
			for(int i=0; i<scores_t.size();i++)
			{
				int[] cur = nuggets_t.get(i);
				double sum = 0;
				for(int l=0; l<num;l++) p[l]=(double)maxProp[l]/(double)sumProp;
				//for(int l=0; l<num;l++) p[l]=1;
				for(int j=0; j<num; j++)
				{
					int c = (cur[j]==0?0:1);
					double r;
					if(maxProp[j]>30 && (maxProp[j]-cur[j])>30) r = 0;
					else if(maxProp[j]>30) r = (double) 1.0/(1<<(maxProp[j]-cur[j]));
					else r = (double)((1<<cur[j])-1)/(1<<maxProp[j]);
					//double r = (double)((1<<c)-1)/(1<<1);
					//sum += r*p[j]/(double)(j+1);//how to define this possibility?
					//sum += r*p[j]*((double)(cur[j]+1)/(double)(maxProp[j]+1));
					sum += r*p[j];
					//sum += r*p[j]*((double)maxProp[j]/(double)sumProp);
					//System.out.println(r+", "+sum);
					p[j] = p[j]*(1-r);
				}
				if(sum>=maxSum)
				{
					maxSum = sum;
					indx = i;
				}
			}
			//System.out.println(maxSum);
			err += maxSum/(k+1);
			scores_t.remove(indx);
			nuggets_t.remove(indx);		
		}
		return err;
	}
}
