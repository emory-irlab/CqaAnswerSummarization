package evalPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class bestPerf {
	String[] answers;
	int ansLength;
	ArrayList<ArrayList<int[]>> ngLocs;
	double alpha;
	int[] aspect;
	
	public bestPerf(ArrayList<ArrayList<int[]>> n, String[] a, int l, double al)
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
		ArrayList<Integer> remain = new ArrayList<>();
		for(int i=0; i<ngLocs.size(); i++)
			remain.add(i);
		int diff = ansLength;
		double max = 0;
		int nxt = 0 ;
		while(!remain.isEmpty())
		{			
			nxt = -1;
			max = -1;
			for(int j=0; j<remain.size(); j++)
			{
				int cur = remain.get(j);
				double curRate = rate(answers[cur], diff, ngLocs.get(cur));
				if(max < curRate)
				{
					nxt = cur;
					max = curRate;
				}
			}
			/*if(diff > answers[nxt].length())
			{
				result += answers[nxt] + " ";
				diff = diff - answers[nxt].length() - 1;
			}
			else
			{
				result += answers[nxt].substring(0, diff);
				break;
			}
			remain.remove(new Integer(nxt));*/
			if(diff > answers[nxt].length())
			{
				ArrayList<int[]> ngloc = ngLocs.get(nxt);//update aspect[]
				for(int k=0; k<ngloc.size(); k++)
				{
					int[] loc = ngloc.get(k);
					for(int t: loc)
						if(t>0 && t<=diff) aspect[k]++;
				}
				result += answers[nxt] + " ";
				diff = diff - answers[nxt].length() - 1;
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
				String sub = answers[nxt].substring(0, diff);
				
				result += sub;
				break;
			}
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
			for(int j=0; j<loc.length;j++)
			{
				if(loc[j]>0 && loc[j]<=length)
					score += Math.pow((1-alpha), aspect[i]);
			}
		}
		return score;
	}
}
