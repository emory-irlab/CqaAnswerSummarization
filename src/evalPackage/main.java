package evalPackage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/*import javax.json.*;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;*/

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class main {
	
	static String rawDataSet = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\ydata-110_examples.text.json";
	static String clustersProp = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\ydata-110_examples.relevant_propositions.json";
	static String outFile = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\outfile\\output.txt";
	static String qAnsFile = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\outfile\\qAnsFile.txt";
	
	static int finalLength = 200;
	public static void main(String[] args) throws IOException{
		/**********************out put file******************************/           	  
	    FileWriter ps1 = write_out(outFile);	//rate and neggets (original order)
		FileWriter ps2 = write_out(qAnsFile);	//rated question and answers (re-order)
		//**************************used to store all q and a***************************************//
		ArrayList<String> questionCollection = new ArrayList<>();
		ArrayList<String[]> answersCollection = new ArrayList<>();
		ArrayList<ArrayList<ArrayList<String>>> clusterCollection = new ArrayList<>();		

		/***********************read file**********************************/	
		String rw = readJasonFile.readFile(rawDataSet);
		String prop = readJasonFile.readFile(clustersProp);	
		
		//************read necessary info from JSON file*********************//
		int collectionSize = getQueAnsClu(rw, prop, questionCollection, answersCollection, clusterCollection);
		
		/**************cal every answers' aspects**************/
		//ArrayList<ArrayList<Double>> rateCollection = new ArrayList<>();
		ArrayList<double[]> rateCollection = new ArrayList<>();
		ArrayList<ArrayList<int[]>> neggetsCollection = new ArrayList<>();
		
		getAspects(questionCollection, answersCollection, clusterCollection, rateCollection, neggetsCollection);
		/***********************************************
		 * qCollect     ansC       rateC   neggetsC
		 * question1----answer1----rate----[n1, n2, ...]
		 *          ----answer2----rate----[n1, n2, ...]
		 * question2----answer1----rate----[n1, n2, ...]
		 *          ----answer2----rate----[n1, n2, ...]
		 ***********************************************/
		//*******work
		ArrayList<Double> result = new ArrayList<>();
		for(int i=0; i<questionCollection.size();i++)
		{
			String curQuesiton = questionCollection.get(i);
			String[] curAnswers = answersCollection.get(i);
			double[] rate = rateCollection.get(i);
			ArrayList<int[]> neggets = neggetsCollection.get(i);
			//***************ranking*******************//
			int[] order = ranking.random(curQuesiton, curAnswers, 1);//random--seed
          	//int[] order = ranking.bm25(curQuesiton, curAnswers);//bm25
            //int[] order = ranking.mmr(curQuesiton, curAnswers, 0.6);//mmr lamda	
			
			//******merge complete answer************//
			int next = 0;
			StringBuilder answerBuilder = new StringBuilder();
			while((answerBuilder.length()<finalLength) && (next<order.length))
				answerBuilder.append(curAnswers[order[next++]]);
			String answerString;
			if(answerBuilder.length()>finalLength) answerString=answerBuilder.substring(0, finalLength);
			else answerString = answerBuilder.toString();
			
			
/*			//how many apsect this merged answer contains
			ArrayList<ArrayList<String>> cluster = clusterCollection.get(i);
			int aspectsNum = stringAspects(answerString, cluster);
			double ttttt = (double)aspectsNum/cluster.size();
			System.out.println((i+1)+". aspects£º" + aspectsNum+"; total: "+cluster.size()+"; percentage: "+ ttttt);
			result.add(ttttt);*/
			ArrayList<ArrayList<String>> cluster = clusterCollection.get(i);
			double score = eval.testEval(answerString, cluster, 0.2);//answer, cluster, alpha
			System.out.println((i+1)+".  "+score);
			result.add(score);
/*			ArrayList<Double> newRate = new ArrayList<>();
			ArrayList<int[]> newNeggets = new ArrayList<>();			
			for(int x=0; x<rate.length;x++)
			{
				newRate.add(rate[order[x]]);
				newNeggets.add(neggets.get(order[x]));
      		}
			//***************evaluation*******************/
			/*
          	double score = eval.a_ndcg(newRate, newNeggets, rate.length-1);//alpha-ndcg
      		//double score = eval.nerr_ia(newRate, newNeggets, maxProp);//err-ia-normalized
          	//double score = eval.err_ia(newRate, newNeggets, maxProp);
      		//double score = eval.novelty_focused(newRate, newNeggets);//novelty-focused
      		//double score = eval.support_focused(newRate, newNeggets);//support-focused
      		//double score = testeval.nerr(newRate, newNeggets, maxProp);
      		//double score = testeval.alpha_dcg(newRate, newNeggets, rate.length-1);
          	result.add(score);*/
		}
		System.out.println("average£º" + average_eval(result));
      ps1.close();
      ps2.close();
      /*add
      testCV cv = new testCV(questionCollection, answersCollection, rateCollection, neggetsCollection, 5, 0.3, 0.7, 0.1);
      double[] rrrr = cv.work();
      double sum=0;
      for(int i=0; i<rrrr.length; i++)  sum += rrrr[i]; 
      System.out.println("average£º" + sum/rrrr.length);
      */
      System.out.println("finishied!");
	}

	public static int stringAspects(String s, ArrayList<ArrayList<String>> cluster)
	{
		int result = 0;
		s = stringProcess(s);
		for(int i=0; i<cluster.size(); i++)
		{
			ArrayList<String> aspect = cluster.get(i);
			for(int j=0; j<aspect.size(); j++)
			{
				if(s.contains(aspect.get(j)))
				{
					result++;
					break;
				}
			}
		}		
		return result;
	}

	public static FileWriter write_out(String Path) throws IOException
	{
		File outfile = new File(Path);
		if(outfile.exists())
		{
			System.out.println("deleting...");
			outfile.delete();
		}
		System.out.println("buiding new file");
	  	try{
	  		outfile.createNewFile();
	  	}catch (IOException e){e.printStackTrace();}            	  
	    FileWriter ps = new FileWriter(outfile, true);	
	    return ps;
	}
	
	public static double average_eval(ArrayList<Double> scores)
	{
		double sum = 0;
		for(double score: scores)	sum += score;
		return sum/(double)scores.size();
	}
	
	public static int getQueAnsClu(String rw, String prop, ArrayList<String> questionCollection ,	
			ArrayList<String[]> answersCollection,	ArrayList<ArrayList<ArrayList<String>>> clusterCollection )
	{
		int collectionSize = 0;
		JSONParser parser = new JSONParser();
        try 
        {
	      	JSONObject jsonRawdata = (JSONObject) parser.parse(rw);
	      	JSONObject jsonPropos = (JSONObject) parser.parse(prop);          
          
            JSONArray qArray = (JSONArray)jsonRawdata.get("questions");
            JSONArray cArray = (JSONArray)jsonPropos.get("questions"); 
                  
            //for each question -- identified by question id
            collectionSize = qArray.size();
            for(int i=0; i<qArray.size(); i++)
            {
            	JSONObject question = (JSONObject)qArray.get(i); 
	          	/* question:
	          	 * 0. answers	 * 1. question_timestamp   	 * 2. title	 * 3. body    	 * 4. question_id 
	          	 * */ 
	            JSONArray answers = (JSONArray)question.get("answers");
	          	Object question_id = question.get("question_id");
	          	//********form & store question string
	          	String title = question.containsKey("title")?question.get("title").toString():"";
	          	String body = question.containsKey("body")?question.get("body").toString():"";
	          	String questionString = title+" "+body;
	          	
	          	questionCollection.add(questionString);
	          	//*********store all the answers
	          	String[] curAnswers = new String[answers.size()];
	          	for(int j=0; j<answers.size(); j++)
	          		curAnswers[j] = answers.get(j).toString();
	          	answersCollection.add(curAnswers);
	          	
	          	//***********find & store its cluster
	          	int k;
	          	for(k=0; k<cArray.size(); k++)
	          	{
	          		JSONObject proposition = (JSONObject)cArray.get(k); 
	          		if(proposition.get("question_id").equals(question_id)) break;
	          	}
	          	JSONObject prop_group = (JSONObject)cArray.get(k);
	          	JSONArray cluster = (JSONArray)prop_group.get("proposition_clusters");
	            //curCluster <-- cluster
	          	ArrayList<ArrayList<String>> curCluster = new ArrayList<>();
	          	for(int m=0; m<cluster.size();m++)//every aspects
	          	{
	          		JSONArray aspect = (JSONArray)cluster.get(m);
	          		ArrayList<String> curAspect = new ArrayList<>();
	          		for(int n=0; n<aspect.size();n++)//every proposition
	          		{
	          			String s = aspect.get(n).toString();
	          			curAspect.add(s);
	          		}
	          		curCluster.add(curAspect);
	          	}
	          	clusterCollection.add(curCluster);
          }
      } catch (ParseException e) {e.printStackTrace();}
      return collectionSize;
	}
	
	public static void getAspects(ArrayList<String> questionCollection , ArrayList<String[]> answersCollection,	ArrayList<ArrayList<ArrayList<String>>> clusterCollection, 
			ArrayList<double[]> rateCollection, ArrayList<ArrayList<int[]>> neggetsCollection)
	{
		for(int i=0; i<questionCollection.size(); i++)
		{
			String curQuesiton = questionCollection.get(i);
			String[] curAnswers = answersCollection.get(i);
			ArrayList<ArrayList<String>> curCluster = clusterCollection.get(i);
			
			double[] rate = new double[curAnswers.length];
			ArrayList<int[]> neggets = new ArrayList<>();
			
			curQuesiton = stringProcess(curQuesiton);			
			for(int j=0; j<curAnswers.length; j++)//every answer
			{
				String ans = stringProcess(curAnswers[j]);
				double count = 0;
				int[] negget = new int[curCluster.size()];
				Arrays.fill(negget, 0);
				for(int k=0; k<curCluster.size(); k++)//exam every aspect
				{
					ArrayList<String> aspect = curCluster.get(k);
					for(int l=0; l<aspect.size(); l++)
					{
						String p = aspect.get(l).trim().toLowerCase();
						if(ans.contains(p)) negget[k]++;
					}
					count += negget[k]==0?0:1;
				}
				rate[j] = count;
				neggets.add(negget);
			}
			rateCollection.add(rate);
			neggetsCollection.add(neggets);
		}
	}
	
	public static String stringProcess(String s)
	{
		StringBuilder sb = new StringBuilder();		
		String text = s.trim().toLowerCase();
		char pre = 'a';
		char cur;
		for(int x=0; x<text.length();x++)
		{
			cur = text.charAt(x);
			if(cur=='\n') sb.append(" ");
			else if(cur==' ' && pre==' ') continue;
			else sb.append(cur);
			pre = cur;
		}
		return sb.toString();
	}
	
}

