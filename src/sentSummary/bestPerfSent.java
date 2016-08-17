package sentSummary;

import java.util.ArrayList;

public class bestPerfSent {
	ArrayList<String> answers;
	int ansLength;
	ArrayList<ArrayList<int[]>> ngLocs;
	double alpha;
	
	public bestPerfSent(ArrayList<ArrayList<int[]>> n, ArrayList<String> a, int l, double al)
	{
		this.ansLength = l;
		this.ngLocs = n;
		this.answers = a;
		this.alpha = al;
	}
	
	public String rank()
	{
		String result = "";
		ArrayList<Integer> remain = new ArrayList<>();
		for(int i=0; i<ngLocs.size(); i++)
			remain.add(i);
		int diff = ansLength;
		//int i=0;
		double max = 0;
		int nxt = 0 ;
		while(!remain.isEmpty())
		{			
			//nxt = remain.get(0);
			//max = rate(answers.get(nxt), diff, ngLocs.get(nxt));
			nxt = -1;
			max = -1;
			for(int j=0; j<remain.size(); j++)
			{
				int cur = remain.get(j);
				double curRate = rate(answers.get(cur), diff, ngLocs.get(cur));
				//System.out.println("tt--"+curRate);
				if(max < curRate)
				{					
					nxt = cur;
					max = curRate;
				}
			}
			if(diff > answers.get(nxt).length())
			{
				result += answers.get(nxt) + " ";
				diff = diff - answers.get(nxt).length() - 1;
			}
			else
			{
				result += answers.get(nxt).substring(0, diff);
				break;
			}
			//System.out.println("bb----"+max);
			remain.remove(new Integer(nxt));
		}
		return result;
	}
	
	private double rate(String s, int length, ArrayList<int[]> ngloc)
	{
		int score = 0;
		for(int i=0; i<ngloc.size(); i++)
		{
			int[] loc = ngloc.get(i);
			int num=0;
			for(int j=0; j<loc.length;j++)
			{
				if(loc[j]>=0 && loc[j]<=length)
					//score += Math.pow((1-alpha), num++);
					score++;
			}
		}
		return score;
	}
}
