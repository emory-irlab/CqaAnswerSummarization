package evalPackage;

import java.util.ArrayList;

public class testeval {
	double alpha;
	public  testeval(double a)
	{
		this.alpha = a;
	}
	public double performance(String s, ArrayList<ArrayList<String>> cluster)
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
					result += Math.pow((1-alpha), num++);
				}
			}
		}		
		return result;
	}
}
