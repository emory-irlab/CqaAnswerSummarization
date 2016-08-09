package sentSummary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class alpha_ndcgAtLength {
	double alpha;
	public alpha_ndcgAtLength(double a)
	{
		this.alpha = a;
	}
	public double performance(String s, ArrayList<ArrayList<String>> cluster)
	{
		double result = 0;
		for(int i=0; i<cluster.size(); i++)
		{
			ArrayList<String> aspect = cluster.get(i);
			boolean first = true;
			int num = 0;
			for(int j=0; j<aspect.size(); j++)
			{
				if(s.contains(aspect.get(j)))
				{
					if(first) 
					{
						num=0;
						first = false;
					}
					result += Math.pow((1-alpha), num++);
				}
			}
		}		
		return result;
	}
	
}
