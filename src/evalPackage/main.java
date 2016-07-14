package evalPackage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
	public static void main(String[] args) throws IOException{
		/**********************out put file******************************/           	  
	    FileWriter ps = write_out(outFile);	
		FileWriter ps2 = write_out(qAnsFile);		
		//**************************used to store all q and a***************************************//
		ArrayList<String> questionCollection = new ArrayList<>();
		ArrayList<String[]> answersCollection = new ArrayList<>();
		ArrayList<ArrayList<Double>> rateCollection = new ArrayList<>();
		ArrayList<ArrayList<int[]>> neggetsCollection = new ArrayList<>();
		/***********************read file**********************************/	
		String rw = readJasonFile.readFile(rawDataSet);
		String prop = readJasonFile.readFile(clustersProp);		
		/**********************process************************************/	
		JSONParser parser = new JSONParser();
        try {
	      	JSONObject jsonRawdata = (JSONObject) parser.parse(rw);
	      	JSONObject jsonPropos = (JSONObject) parser.parse(prop);          
          
            JSONArray qArray = (JSONArray)jsonRawdata.get("questions");
            JSONArray cArray = (JSONArray)jsonPropos.get("questions"); 
          
            ArrayList<Double> scores = new ArrayList<>();          
            //for each question -- identified by question id
            for(int i=0; i<qArray.size(); i++){
            	JSONObject question = (JSONObject)qArray.get(i); 
          	/* question:
          	 * 0. answers	 * 1. question_timestamp   	 * 2. title	 * 3. body    	 * 4. question_id 
          	 * */ 
            JSONArray answers = (JSONArray)question.get("answers");
          	Object question_id = question.get("question_id");
          	//forming query
          	String title = question.containsKey("title")?question.get("title").toString():"";
          	String body = question.containsKey("body")?question.get("body").toString():"";
          	String questionString = title+" "+body;
          	
          	ps2.append("Question: "+'\t'+title+'\t'+body+"\n");
          	
          	//find its cluster
          	int k;
          	for(k=0; k<cArray.size(); k++)
          	{
          		JSONObject proposition = (JSONObject)cArray.get(k); 
          		if(proposition.get("question_id").equals(question_id)) break;
          	}
          	JSONObject prop_group = (JSONObject)cArray.get(k);
          	JSONArray cluster = (JSONArray)prop_group.get("proposition_clusters");
          	/* answers: 
          	 * 0. answer_text  * 1. is_best_answer  * 2. thumbs_down   
          	 * 3. answer_sources(optional)  * 4. answer_timestamp  * 5. thumbs_up
          	 * */          	
          	ArrayList<Double> rate = new ArrayList<>();
          	ArrayList<int[]> nuggets = new ArrayList<>();
          	int[] maxProp = new int[cluster.size()];//used to store #propositions in each aspect
          	String[] allAnswers = new String[answers.size()];
          	
          	//ArrayList<String> answersContent = new ArrayList<>(); 
          	for(int j=0; j<answers.size();j++)//every answer for each question
          	{
      			JSONObject answer = (JSONObject)answers.get(j);   
      			String atxt = answer.get("answer_text").toString();
      			//answersContent.add(atxt);
      			allAnswers[j] = atxt;
      			int count = 0;
      			int[] nvlty = new int[cluster.size()];
      			
      			for(int m=0; m<cluster.size();m++)//for every aspect in the cluster
      			{
      				JSONArray aspect = (JSONArray)cluster.get(m);
      				int flag = 0;
      				maxProp[m] = aspect.size();
      				for(int n=0; n<aspect.size();n++)//for every prop in an aspect     				
      				{
      					String text = atxt.trim();
      					/*******need some process********/
      					StringBuilder tmps = new StringBuilder();
      					char pre = 'a';
      					char cur;
      					for(int x=0; x<text.length();x++)
      					{
      						cur = text.charAt(x);
      						if(cur=='\n') tmps.append(" ");
      						else if(cur==' ' && pre==' ') continue;
      						else tmps.append(cur);
      						pre = cur;
      					}
      					text = tmps.toString().toLowerCase();
      					/*******need some process********/
      					Object p = aspect.get(n);
      					if(text.contains(p.toString().trim().toLowerCase()))//TBD
      					{
      						if(flag==0)	
      						{
      							count++;
      							flag=1;
      						}
      						nvlty[m] += 1;
      					}
      				}
      			}
      			rate.add((double)count);
      			nuggets.add(nvlty);
          	}
          	
          	ArrayList<Double> random_rate = new ArrayList<>();
          	ArrayList<int[]> random_negguts = new ArrayList<>();
          	//********************collect all q and ans first**********************************//
          	questionCollection.add(questionString);
          	answersCollection.add(allAnswers);
      		rateCollection.add(rate);
      		neggetsCollection.add(nuggets);
          	//***************ranking*******************//*
/*          	//int[] order = ranking.random(questionString, allAnswers, 1);//random
          	//int[] order = ranking.bm25(questionString, allAnswers);//bm25
            //int[] order = ranking.mmr(questionString, allAnswers, 0.9);//bm25
      		for(int x=0; x<rate.size();x++){
      			random_rate.add(rate.get(order[x]));
      			random_negguts.add(nuggets.get(order[x]));
      			ps2.append(x+". "+'\t'+allAnswers[order[x]]+"\n");
      		}

      		//***************evaluation*******************//*
          	double score = eval.a_ndcg(random_rate, random_negguts, rate.size()-1);//alpha-ndcg
      		//double score = eval.nerr_ia(random_rate, random_negguts, maxProp);//err-ia-normalized
          	//double score = eval.err_ia(random_rate, random_negguts, maxProp);
      		//double score = eval.novelty_focused(random_rate, random_negguts);//novelty-focused
      		//double score = eval.support_focused(random_rate, random_negguts);//support-focused
      		//double score = testeval.nerr(random_rate, random_negguts, maxProp);
      		//double score = testeval.alpha_dcg(random_rate, random_negguts, rate.size()-1);
          	scores.add(score);
          	System.out.println(question_id.toString()+ score);
  			ps.append(question_id.toString()+'\t'+ score+"\n");*/
          }
/*          double aveg = average_eval(scores);
          ps.append("Average: "+'\t'+ aveg+"\n");
          System.out.println("average: "+ aveg);*/

      } catch (ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
      ps.close();
      ps2.close();
      //add
      testCV cv = new testCV(questionCollection, answersCollection, rateCollection, neggetsCollection, 5, 0.7, 1.0, 0.04);
      double[] rrrr = cv.work();
      double sum=0;
      for(int i=0; i<rrrr.length; i++)  sum += rrrr[i]; 
      System.out.println("average£º" + sum/rrrr.length);
      //end
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
}

