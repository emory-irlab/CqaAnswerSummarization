package evalPackage;

import java.util.ArrayList;

public class err_ia {
	
	public static double err_Intent(ArrayList<Double> scores, ArrayList<int[]> nuggets)
	{
		int len = scores.size();
		int num = nuggets.get(0).length;
		double err = 0;
		double[] p = new double[num];
		for(int i=0; i<num;i++) p[i]=1;
		for(int i=0; i<len;i++)
		{
			int[] cur = nuggets.get(i);
			double sum = 0;
			for(int j=0; j<num; j++)
			{
				double r = (double)((1<<cur[j])-1)/(1<<1);				
				sum += r*p[j]/(j+1);//how to define this possibility?
				//System.out.println(r+", "+sum);
				p[j] = p[j]*(1-r);
			}
			err += sum/(i+1);
		}
		return err;
	}
}
