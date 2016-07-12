package evalPackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class testCV {
	//double alpha = 0;
	private double lamda;
	private int kfd;
	private double lamda_step, lamda_low, lamda_high;
	private ArrayList<String> allQuestions;
	private ArrayList<String[]> allAnswers;
	private ArrayList<ArrayList<Double>> allRate;
	private ArrayList<ArrayList<int[]>> allNuggets;
	public testCV(ArrayList<String> q, ArrayList<String[]> a, ArrayList<ArrayList<Double>> r, 
			ArrayList<ArrayList<int[]>> n, int k, double low, double high, double step)
	{
		this.kfd = k;
		this.allQuestions = new ArrayList<>(); this.allQuestions.addAll(q);
		this.allAnswers = new ArrayList<>(); this.allAnswers.addAll(a);
		this.allRate = new ArrayList<>(); this.allRate.addAll(r);
		this.allNuggets = new ArrayList<>(); this.allNuggets.addAll(n);
		this.lamda_step = step;
		this.lamda_low = low;
		this.lamda_high = high;
	}
	public int[][] split(int dataSize)
	{
		int groupSize = dataSize/kfd;//±ØÐëÕû³ý
		int[][] result = new int[kfd][groupSize];
		ArrayList<Integer> temp = new ArrayList<>();
		for(int i=0; i<dataSize; i++)  temp.add(i);
		
		Random rn = new Random();		
		int s = dataSize;
		for(int i=0; i<kfd; i++)
			for(int j=0; j<groupSize; j++)
				result[i][j] = temp.remove(rn.nextInt(s--));
		
		return result;
	}
	//tune lamda
	public void train(int[][] allData, int test) throws IOException//one partition for test, others for training
	{
		ArrayList<Double> para = new ArrayList<>();
		ArrayList<Double> avgScore = new ArrayList<>();
		lamda = lamda_low;
		while(lamda<=lamda_high){			
			ArrayList<Double> scores = new ArrayList<>();
			for(int i=0; i<allData.length; i++){
				if(i==test) continue;
				for(int j=0; j<allData[0].length; j++){
					String question = allQuestions.get(allData[i][j]);
					String[] answers = allAnswers.get(allData[i][j]);					
					ArrayList<Double> rate = allRate.get(allData[i][j]);
					ArrayList<int[]> nuggets = allNuggets.get(allData[i][j]);
					//System.out.println(allRate.get);
					int[] order = ranking.mmr(question, answers, lamda);
					
					ArrayList<Double> tmp_rate = new ArrayList<>();
					ArrayList<int[]> tmp_negguts = new ArrayList<>();
					for(int x=0; x<rate.size();x++){
		      			tmp_rate.add(rate.get(order[x]));
		      			tmp_negguts.add(nuggets.get(order[x]));
		      		}
					
					double score = eval.a_ndcg(tmp_rate, tmp_negguts, rate.size()-1);
					scores.add(score);
				}
			}
			double average = 0;
			for(int l=0; l<scores.size(); l++)
				average += scores.get(l);
			average = average/(double)scores.size();
			
			para.add(lamda);
			//System.out.println(lamda);
			avgScore.add(average);
			lamda += lamda_step;
		}
		double maxScore = 0;
		for(int l=0; l<para.size(); l++){
			if(maxScore<avgScore.get(l))
				lamda = para.get(l);
		}
	}
	//apply to test data
	public double test(int[][] allData, int test) throws IOException
	{
		ArrayList<Double> scores = new ArrayList<>();
		for(int i=0; i<allData[0].length; i++){
			String question = allQuestions.get(allData[test][i]);
			String[] answers = allAnswers.get(allData[test][i]);					
			ArrayList<Double> rate = allRate.get(allData[test][i]);
			ArrayList<int[]> nuggets = allNuggets.get(allData[test][i]);
			
			int[] order = ranking.mmr(question, answers, lamda);
			
			ArrayList<Double> tmp_rate = new ArrayList<>();
			ArrayList<int[]> tmp_negguts = new ArrayList<>();
			for(int x=0; x<rate.size();x++){
      			tmp_rate.add(rate.get(order[x]));
      			tmp_negguts.add(nuggets.get(order[x]));
      		}			
			double score = eval.a_ndcg(tmp_rate, tmp_negguts, rate.size()-1);
			scores.add(score);
		}
		double sum = 0;
		for(int l=0; l<scores.size(); l++)
			sum += scores.get(l);
		
		return sum/(double)scores.size();
	}
	public double[] work() throws IOException
	{
		double[] result = new double[kfd];
		//split dataset
		int[][] dataPartition = split(allAnswers.size());
/*		for(int i=0; i<dataPartition.length; i++)
			for(int j=0; j<dataPartition[0].length; j++)
				System.out.println(dataPartition[i][j]);*/
		//train and test
		for(int i=0; i<kfd; i++)
		{
			train(dataPartition, i);
			System.out.println("lamda: "+lamda);
			result[i] = test(dataPartition, i);
			System.out.println("test score: "+ result[i]);
		}
		return result;
	}
}
