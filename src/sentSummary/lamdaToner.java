package sentSummary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class lamdaToner {
	private double lamda;
	private int kfd;
	private int ansLength;
	private double lamda_step, lamda_low, lamda_high;
	private ArrayList<String> allQuestions;
	private ArrayList<ArrayList<String>> allAnswers;
	private ArrayList<ArrayList<ArrayList<String>>> clusterCollection;
	public lamdaToner(ArrayList<String> q, ArrayList<ArrayList<String>> a,  ArrayList<ArrayList<ArrayList<String>>> c,
			int k, double low, double high, double step, int l)
	{
		this.kfd = k;
		this.ansLength = l;
		this.allQuestions = q;
		this.allAnswers = a;
		this.clusterCollection = c;
		this.lamda_step = step;
		this.lamda_low = low;
		this.lamda_high = high;
		System.out.println(kfd+"-fold; from"+lamda_low+" to "+lamda_high+" with step: "+lamda_step);
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
					ArrayList<String> answerSent = allAnswers.get(allData[i][j]);					
					//System.out.println(allRate.get);
					ranking rk = new ranking();
					int[] order = rk.mmr(question, answerSent, lamda);
					
					String finalAnswer = mergingAnswer(answerSent, order, ansLength);
					
					evaluation eval = new evaluation();
					double score = eval.sumEval(finalAnswer, clusterCollection.get(allData[i][j]), 0.3);
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
			System.out.println("lamda: "+lamda+"; score: "+average);
			
			lamda += lamda_step;
		}
		double maxScore = 0;
		for(int l=0; l<para.size(); l++){
			if(maxScore<avgScore.get(l))
			{
				lamda = para.get(l);
				maxScore = avgScore.get(l);
			}
		}
	}
	//apply to test data
	public double test(int[][] allData, int test) throws IOException
	{
		ArrayList<Double> scores = new ArrayList<>();
		for(int i=0; i<allData[0].length; i++){
			String question = allQuestions.get(allData[test][i]);
			ArrayList<String> answerSent = allAnswers.get(allData[test][i]);					
			
			
			ranking rk = new ranking();
			int[] order = rk.mmr(question, answerSent, lamda);
			
			String finalAnswer = mergingAnswer(answerSent, order, ansLength);
			
			evaluation eval = new evaluation();
			double score = eval.sumEval(finalAnswer, clusterCollection.get(allData[test][i]), 0.6);
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
			System.out.print((i+1)+". ");
			train(dataPartition, i);
			System.out.println("lamda: "+lamda);
			result[i] = test(dataPartition, i);
			System.out.println("test score: "+ result[i]);
		}
		return result;
	}
	
	public String mergingAnswer(ArrayList<String> ansSent, int[] order, int length)
	{
		StringBuilder sb = new StringBuilder();
		int n=0;
		while(sb.length()<length)
		{				
			sb.append(ansSent.get(order[n++]));
			if(n>=ansSent.size()) break;
		}
		if(sb.length()>length) return sb.substring(0, length);
		else return sb.toString();
	}
}
