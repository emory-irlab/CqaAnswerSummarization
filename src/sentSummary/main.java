package sentSummary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.stanford.nlp.simple.*;
//import evalPackage.ranking;
import evalPackage.readJasonFile;

public class main {	
	static String rawDataSet = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\ydata-110_examples.text.json";
	static String clustersProp = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\ydata-110_examples.relevant_propositions.json";
	static String outFile = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\outfile\\sentSummary.txt";
	static int answerLength=1000;
	static int sentLength = 5;
	public static void main(String[] args) throws IOException{
		/**********************out put file******************************/           	  
	    FileWriter ps1 = write_out(outFile);	//rate and neggets (original order)
		//**************************used to store all q and a***************************************//
		ArrayList<String> questionCollection = new ArrayList<>();
		ArrayList<String[]> answersCollection = new ArrayList<>();
		ArrayList<ArrayList<String>> ansSentCollection = new ArrayList<>();
		ArrayList<ArrayList<ArrayList<String>>> clusterCollection = new ArrayList<>();		

		/***********************read file**********************************/	
		String rw = readJasonFile.readFile(rawDataSet);
		String prop = readJasonFile.readFile(clustersProp);	
		
		//************read necessary info from JSON file*********************//
		int collectionSize = getQueAnsClu(rw, prop, questionCollection, answersCollection, ansSentCollection, clusterCollection);
		
		
		/**************cal every answers' aspects**************/
		ArrayList<double[]> rateCollection = new ArrayList<>();
		ArrayList<ArrayList<int[]>> neggetsCollection = new ArrayList<>();

		getAspects(questionCollection, ansSentCollection, clusterCollection, rateCollection, neggetsCollection);
		/***********************************************
		 * qCollect     sentC       rateC   neggetsC
		 * question1----sentence1----rate----[n1, n2, ...]
		 *          ----sentence2----rate----[n1, n2, ...]
		 * question2----sentence1----rate----[n1, n2, ...]
		 *          ----sentence2----rate----[n1, n2, ...]
		 ***********************************************/
		//*******work
		ArrayList<Double> result = new ArrayList<>();
		for(int i=0; i<questionCollection.size();i++)
		{
			String curQuesiton = questionCollection.get(i);			
			ArrayList<String> ansSent = new ArrayList<>();
			ansSent.addAll(ansSentCollection.get(i));
			
			double[] rate = rateCollection.get(i);
			ArrayList<int[]> neggets = neggetsCollection.get(i);
			
			//***************ranking*******************//
			ranking rk = new ranking();
			//int[] sentRank = rk.bm25(curQuesiton, ansSent);
			int[] sentRank = rk.mmr(curQuesiton, ansSent, 0.6);
			//System.out.println("question "+i+". "+formedAnswer);
			StringBuilder formedAnswer = new StringBuilder();
			int n=0;
			while(formedAnswer.length()<answerLength)
			{				
				formedAnswer.append(ansSent.get(sentRank[n++]));
				if(n>=ansSent.size()) break;
			}			
			String finalAnswer =  formedAnswer.toString();
			//*******evaluation*******//
			evaluation eval = new evaluation();
			double score = eval.alpha_ndcg_length(sentRank, ansSent, rateCollection.get(i), neggetsCollection.get(i), answerLength);
			result.add(score);
			ps1.append("question "+(i+1)+".\t"+score+"\t"+stringProcess(finalAnswer)+"\n");
		}
	  System.out.println("average£º" + average_eval(result));
      ps1.close();
      System.out.println("finishied!");
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
			ArrayList<String[]> answersCollection,	ArrayList<ArrayList<String>> ansSentCollection, 
			ArrayList<ArrayList<ArrayList<String>>> clusterCollection )
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
	          	{
	          		JSONObject a = (JSONObject)answers.get(j);
	          		curAnswers[j] = a.get("answer_text").toString();
	          	} 
	          	answersCollection.add(curAnswers);
          		ArrayList<String> curSent = new ArrayList<>();
          		answersSplite(curAnswers, curSent, sentLength);
          		ansSentCollection.add(curSent);
	          	
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
	
	public static void getAspects(ArrayList<String> questionCollection , ArrayList<ArrayList<String>> ansSentCollection, ArrayList<ArrayList<ArrayList<String>>> clusterCollection, 
			ArrayList<double[]> rateCollection, ArrayList<ArrayList<int[]>> neggetsCollection)
	{
		for(int i=0; i<questionCollection.size(); i++)
		{
			String curQuesiton = questionCollection.get(i);
			ArrayList<String> curAnswers = ansSentCollection.get(i);
			ArrayList<ArrayList<String>> curCluster = clusterCollection.get(i);
			
			double[] rate = new double[curAnswers.size()];
			ArrayList<int[]> neggets = new ArrayList<>();
			
			curQuesiton = stringProcess(curQuesiton);			
			for(int j=0; j<curAnswers.size(); j++)//every answer
			{
				String ans = stringProcess(curAnswers.get(j));
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
	
	public static void answersSplite(String[] answers, ArrayList<String> ansSent, int minLength)
	{
		for(int i=0; i<answers.length; i++)
		{
			Document doc = new Document(answers[i]);
			String tmp = " ";
			for (Sentence sent : doc.sentences())
			{
				if(sent.length()>=minLength) 
				{
					if(tmp.equals(" "))	ansSent.add(sent.toString());
					else
					{
						ansSent.add(tmp+" "+sent.toString());
						tmp = " ";
					}
				} 
				else
					tmp = sent.toString();
			}
			if(!tmp.equals(" ")) ansSent.add(tmp);
		}
	}
}

