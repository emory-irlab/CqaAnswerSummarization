package sentSummary;

import java.util.ArrayList;
import java.util.Arrays;

public class bestPerfSent {
	ArrayList<String> answers;
	int ansLength;
	ArrayList<ArrayList<int[]>> ngLocs;
	double alpha;
	int[] aspect;
	
	
	public bestPerfSent(ArrayList<ArrayList<int[]>> n, ArrayList<String> a, int l, double al)
	{
		this.ansLength = l;
		this.ngLocs = n;
		this.answers = a;
		this.alpha = al;
		this.aspect = new int[ngLocs.get(0).size()];
	}
	
	public String rank()
	{
		String result = "";
		Arrays.fill(aspect, 0);
		ArrayList<Integer> remain = new ArrayList<>();
		for(int i=0; i<ngLocs.size(); i++)
			remain.add(i);
		int diff = ansLength;
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
				double curRate = rate(diff, ngLocs.get(cur));
				//System.out.println("tt--"+curRate);
				if(max < curRate)
				{					
					nxt = cur;
					max = curRate;
				}
			}
			if(diff > answers.get(nxt).length())
			{
				ArrayList<int[]> ngloc = ngLocs.get(nxt);//update aspect[]
				for(int k=0; k<ngloc.size(); k++)
				{
					int[] loc = ngloc.get(k);
					for(int t: loc)
						if(t>0 && t<=diff) aspect[k]++;
				}
				result += answers.get(nxt) + " ";
				diff = diff - answers.get(nxt).length() - 1;
			}
			else
			{
				ArrayList<int[]> ngloc = ngLocs.get(nxt);//update aspect[]
				for(int k=0; k<ngloc.size(); k++)
				{
					int[] loc = ngloc.get(k);
					for(int t: loc)
						if(t>0 && t<=diff) aspect[k]++;
				}
				String sub = answers.get(nxt).substring(0, diff);
				
				result += sub;
				break;
			}
			//System.out.println("bb----"+max);
			remain.remove(new Integer(nxt));
		}
		return result;
	}
	
	private double rate(int length, ArrayList<int[]> ngloc)
	{
		int score = 0;
		for(int i=0; i<ngloc.size(); i++)
		{
			int[] loc = ngloc.get(i);
			for(int j=0; j<loc.length;j++)
			{
				if(loc[j]>=0 && loc[j]<=length)
					score += Math.pow((1-alpha), aspect[i]);
					//score++;
			}
		}
		return score;
	}
}
