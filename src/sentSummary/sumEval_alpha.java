package sentSummary;

import java.util.ArrayList;

public class sumEval_alpha {
	
	double alpha;
	ArrayList<ArrayList<String>> cluster;
	
	public  sumEval_alpha(double a, ArrayList<ArrayList<String>> c)
	{
		this.alpha = a;
		this.cluster = c;
	}
	
	public double performance(String s)
	{
		double result = 0;
		for(int i=0; i<cluster.size(); i++)
		{
			ArrayList<String> aspect = cluster.get(i);
			int num = 0;
			for(int j=0; j<aspect.size(); j++)
			{
				if(s.contains(aspect.get(j)))
				{
					//result += Math.pow((1-alpha), num++);
					result++;
				}
			}
		}		
		return result;
	}
}
