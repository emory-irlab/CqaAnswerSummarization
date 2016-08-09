package evalPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class bestPerf {
	String question;
	String[] answers;
	ArrayList<ArrayList<String>> cluster;
	double[] rate;
	public bestPerf(String q, String[] a, ArrayList<ArrayList<String>> c)
	{
		this.question = q;
		this.answers = a;
		this.cluster = c;
	}
	public bestPerf(double[] r, String[] a)
	{
		this.rate = r;
		this.answers = a;
	}
	public int[] rank()
	{
		int[] order = new int[answers.length];
		int[] aspects = new int[answers.length];//record how many aspects the corresponding answer contains
		Arrays.fill(aspects, 0);
		for(int i=0; i<answers.length; i++)//every answer
		{
			String aString = answers[i];
			for(int j=0; j<cluster.size(); j++)
			{
				ArrayList<String> c = cluster.get(j);
				for(String s: c)
				{
					if(aString.contains(s))
					{
						aspects[i]++;
						break;
					}
				}
			}
		}
		//rank
		ArrayList<int[]> pair = new ArrayList<>();
		for(int i=0; i<answers.length; i++)
		{
			int[] cur = new int[2];
			cur[0] = i;
			cur[1] = aspects[i];
			pair.add(cur);
		}
		Collections.sort(pair, new Comparator<int[]>(){
			public int compare(int a[], int b[]){ return (b[1]-a[1])<0?1:-1;}
		});
		//fetch the order
		for(int i=0; i<answers.length; i++)
		{
			for(int[] a: pair)
				order[i] = a[0];
		}
		return order;
	}
	
	public int[] rank2()
	{
		int[] order = new int[rate.length];
		ArrayList<Integer> remain = new ArrayList<>();
		for(int i=0; i<rate.length; i++)
			remain.add(i);
		int i=0;
		double max = 0;
		int nxt = 0 ;
		int nxtLength = 0;
		while(!remain.isEmpty())
		{			
			nxt = remain.get(0);
			max = rate[nxt];
			nxtLength = answers[nxt].length();
			for(int j=0; j<remain.size(); j++)
			{
				int cur = remain.get(j);
				if(max <= rate[cur])
				{
					if(max == rate[cur] && nxtLength < answers[cur].length())
					{
						continue;
					}
					nxt = cur;
					max = rate[cur];
					nxtLength = answers[cur].length();
				}
			}
			order[i++]=nxt;
			remain.remove(new Integer(nxt));
		}
		return order;
	}
	
}
