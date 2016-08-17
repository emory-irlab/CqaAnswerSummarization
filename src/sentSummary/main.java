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
import evalPackage.ranking;
//import evalPackage.ranking;
import evalPackage.readJasonFile;

public class main {	
	static String rawDataSet = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\ydata-110_examples.text.json";
	static String clustersProp = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\ydata-110_examples.relevant_propositions.json";
	static String outFile = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\outfile\\sentSummary.txt";
	static int answerLength=100;
	static int sentLength = 5;
	static double lamda = 0;
	static double alpha = 0.5;
	public static void main(String[] args) throws IOException{
		/**********************out put file******************************/           	  
	   FileWriter ps1 = write_out(outFile);	//rate and neggets (original order)
		//**************************used to store all q and a***************************************//
		ArrayList<String> questionCollection = new ArrayList<>();
		ArrayList<String[]> answersCollection = new ArrayList<>();
		ArrayList<ArrayList<ArrayList<String>>> ansSentCollection = new ArrayList<>();
		ArrayList<ArrayList<ArrayList<String>>> clusterCollection = new ArrayList<>();		

		/***********************read file**********************************/	
		String rw = readJasonFile.readFile(rawDataSet);
		String prop = readJasonFile.readFile(clustersProp);	
		
		//************read necessary info from JSON file*********************//
		int collectionSize = getQueAnsClu(rw, prop, questionCollection, answersCollection, ansSentCollection, clusterCollection);
		/**************cal every answers' aspects**************/
		ArrayList<ArrayList<double[]>> rateCollection = new ArrayList<>();
		ArrayList<ArrayList<ArrayList<int[]>>> neggetsCollection = new ArrayList<>();
		ArrayList<ArrayList<ArrayList<ArrayList<int[]>>>> nglocCollection = new ArrayList<>();

		getAspects(questionCollection, ansSentCollection, clusterCollection, rateCollection, neggetsCollection, nglocCollection);
		//----------------------------------------------------------
		// qCollect     ansC       sentC    rateC     neggetsC
		//  question1----answer1----sent1----rate1----[n1, n2, ...]
		//                      ----sent2----rate2----[n1, n2, ...]
		//           ----answer2----sent1----rate1----[n1, n2, ...]
		//                      ----sent2----rate2----[n1, n2, ...]
		//  question2----answer1----sent1----rate1----[n1, n2, ...]
		//                      ----sent2----rate2----[n1, n2, ...]
		//           ----answer2----sent1----rate1----[n1, n2, ...]
		//                      ----sent2----rate2----[n1, n2, ...]
		//----------------------------------------------------------
/*		
		//cv
		lamdaToner lt = new lamdaToner(questionCollection, ansSentCollection, clusterCollection, 5, 0, 1, 0.1, answerLength);
		double[] r = lt.work();
		double print = 0;
		for(double tmp: r)
		{
			print += tmp;
		}
		System.out.println(print/r.length);*/
		//*******work
		ArrayList<Double> result = new ArrayList<>();
		for(int i=0; i<questionCollection.size();i++)//every single question in the dataset
		{
			String curQuesiton = questionCollection.get(i);			
			ArrayList<ArrayList<String>> ansSent = ansSentCollection.get(i);
			ArrayList<double[]> rate = rateCollection.get(i);
			ArrayList<ArrayList<int[]>> neggets = neggetsCollection.get(i);
			ArrayList<ArrayList<String>> cluster = clusterCollection.get(i);
			ArrayList<ArrayList<ArrayList<int[]>>> nglocs = nglocCollection.get(i); 
			//--------------------ranking--------------------
			rankSent rk = new rankSent(curQuesiton, ansSent, nglocs, answerLength);
			//String sentAns = rk.random();
			String sentAns = rk.bm25();
			//String sentAns = rk.mmr(lamda);
			
			//************best possible answer--greedy*********//
			String bestAns = rk.best(alpha);
			
			//*******evaluation*******			
			evaluation eval = new evaluation(cluster, alpha);
			double score = eval.sumEval(sentAns);
			double bestScore = eval.sumEval(bestAns);
			double ratio = score/bestScore;
			System.out.println((i+1)+". score: "+score+"; best: "+bestScore+"; ratio: "+ratio);
			result.add(ratio);
			ps1.append("Question "+(i+1)+". "+stringProcess(curQuesiton)+"\tscore: "+ratio+"\n");
			ps1.append("----Answer: "+stringProcess(sentAns)+"\n");
			stringAspects(sentAns, cluster, ps1);
			ps1.append("----Best  : "+stringProcess(bestAns)+"\n");
			stringAspects(bestAns, cluster, ps1);
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
			ArrayList<String[]> answersCollection,	ArrayList<ArrayList<ArrayList<String>>> ansSentCollection, 
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
          		//ArrayList<String> curSent = new ArrayList<>();
          		ArrayList<ArrayList<String>> curSent = new ArrayList<>();
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
	
	public static void getAspects(ArrayList<String> questionCollection , ArrayList<ArrayList<ArrayList<String>>> ansSentCollection, ArrayList<ArrayList<ArrayList<String>>> clusterCollection, 
			ArrayList<ArrayList<double[]>> rateCollection, ArrayList<ArrayList<ArrayList<int[]>>> neggetsCollection,
			ArrayList<ArrayList<ArrayList<ArrayList<int[]>>>> nglocCollection)
	{
		for(int i=0; i<questionCollection.size(); i++)//every question
		{
			String curQuesiton = questionCollection.get(i);
			ArrayList<ArrayList<String>> curAnswers = ansSentCollection.get(i);//
			ArrayList<ArrayList<String>> curCluster = clusterCollection.get(i);
			
			ArrayList<double[]> curRate = new ArrayList<>();
			ArrayList<ArrayList<int[]>> curNeggets = new ArrayList<>();
			ArrayList<ArrayList<ArrayList<int[]>>> nglocs = new ArrayList<>();
			
			for(int j=0; j<curAnswers.size(); j++)//every answer
			{
				ArrayList<String> ans = curAnswers.get(j);
				double[] rate = new double[curAnswers.size()];
				ArrayList<int[]> neggets = new ArrayList<>();
				ArrayList<ArrayList<int[]>> ngloc = new ArrayList<>();
				for(int x=0; x<ans.size(); x++)//every sentence
				{
					String curSentence = ans.get(x);
					double count = 0;
					int[] negget = new int[curCluster.size()];
					Arrays.fill(negget, 0);
					ArrayList<int[]> locs = new ArrayList<>();
					for(int k=0; k<curCluster.size(); k++)//exam every aspect
					{
						ArrayList<String> aspect = curCluster.get(k);
						int[] loc = new int[aspect.size()];
						for(int l=0; l<aspect.size(); l++)
						{
							String p = aspect.get(l);
							if(curSentence.contains(p)) negget[k]++;
							int start = curSentence.indexOf(p);
							int addLength = start==-1?0:p.length();
							loc[l] = curSentence.indexOf(p) + addLength;//end
						}
						if(negget[k]!=0) count++;
						locs.add(loc);
					}
					rate[j] = count;
					neggets.add(negget);
					ngloc.add(locs);
				}
				curRate.add(rate);
				curNeggets.add(neggets);
				nglocs.add(ngloc);
			}
			rateCollection.add(curRate);
			neggetsCollection.add(curNeggets);
			nglocCollection.add(nglocs);
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
	
	public static void answersSplite(String[] answers, ArrayList<ArrayList<String>> ansSent, int minLength)
	{
		for(int i=0; i<answers.length; i++)
		{
			ArrayList<String> curAnsSent = new ArrayList<>();//
			Document doc = new Document(answers[i]);
			String tmp = " ";
			for (Sentence sent : doc.sentences())
			{
		/*		if(sent.length()>=minLength) 
				{
					if(tmp.equals(" "))	curAnsSent.add(sent.toString());
					else
					{
						curAnsSent.add(tmp+" "+sent.toString());
						tmp = " ";
					}
				} 
				else
					tmp = sent.toString();*/
				curAnsSent.add(sent.toString());
			}
			//if(!tmp.equals(" ")) curAnsSent.add(tmp);
			ansSent.add(curAnsSent);
		}
	}
	
	public static int stringAspects(String s, ArrayList<ArrayList<String>> cluster, FileWriter ps) throws IOException
	{
		int result = 0;
		//s = stringProcess(s);//////////////////////////////
		for(int i=0; i<cluster.size(); i++)
		{
			ArrayList<String> aspect = cluster.get(i);
			for(int j=0; j<aspect.size(); j++)
			{
				if(s.contains(aspect.get(j)))
				{
					ps.append("  >>> " + aspect.get(j) + "\n");
					result++;
					break;
				}
			}
		}		
		return result;
	}
}

