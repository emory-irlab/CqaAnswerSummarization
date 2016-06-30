package evalPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import javax.json.*;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class main {
	
	static String rawDataSet = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\ydata-110_examples.text.json";
	static String clustersProp = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\ydata-110_examples.relevant_propositions.json";
	static String outFile = "E:\\CScourse\\summer_project\\dataset\\Webscope_L29\\outfile\\output.txt";
	
	public static void main(String[] args) throws IOException {
		//out put file
		File outfile = new File(outFile);
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
			
		//read file	
		String rw = readJsonFile(rawDataSet);
		String prop = readJsonFile(clustersProp);
		//process	
		JSONParser parser = new JSONParser();
        try {
	      	Object obj = parser.parse(rw);
	      	Object pro = parser.parse(prop);
	          
	      	JSONObject jsonObject = (JSONObject) obj;
	      	JSONObject jsonPropos = (JSONObject) pro;
	          
	      	Object questions_obj = jsonObject.get("questions");
	      	Object questions_pro = jsonPropos.get("questions");            
          
            JSONArray qArray = (JSONArray)questions_obj;
            JSONArray cArray = (JSONArray)questions_pro;
          
            ArrayList<Double> scores = new ArrayList<>();
          
          //for each question -- identified by question id
            for(int i=0; i<qArray.size(); i++){
            	JSONObject question = (JSONObject)qArray.get(i); 
          	/* question:
          	 * 0. answers	 * 1. question_timestamp   	 * 2. title	 * 3. body    	 * 4. question_id 
          	 * */ 
          	Object answers = question.get("answers");
//          Object title = question.get("title");
//          Object body = question.get("body");
          	Object question_id = question.get("question_id");
          	
          	//find its cluster
          	int k;
          	for(k=0; k<cArray.size(); k++)
          	{
          		JSONObject proposition = (JSONObject)cArray.get(k); 
          		//System.out.println(proposition.get("question_id"));
          		if(proposition.get("question_id").equals(question_id)) break;
          	}
          	JSONObject prop_group = (JSONObject)cArray.get(k);
          	JSONArray cluster = (JSONArray)prop_group.get("proposition_clusters");
          	/* answers: 
          	 * 0. answer_text  * 1. is_best_answer  * 2. thumbs_down   
          	 * 3. answer_sources(optional)  * 4. answer_timestamp  * 5. thumbs_up
          	 * */
          	JSONArray ans = (JSONArray)answers;
          	ArrayList<Double> rate = new ArrayList<>();
          	ArrayList<int[]> nuggets = new ArrayList<>();
          	for(int j=0; j<ans.size();j++)//every answer for each question
          	{
      			JSONObject answer = (JSONObject)ans.get(j); 
      			//System.out.println(answer.get("answer_text"));   
      			Object atxt = answer.get("answer_text");
      			int count = 0;
      			int[] nvlty = new int[cluster.size()];
      			for(int m=0; m<cluster.size();m++)//for every aspect in the cluster
      			{
      				JSONArray aspect = (JSONArray)cluster.get(m);
      				for(int n=0; n<aspect.size();n++)//for every prop in an aspect     				
      				{
      					String text = atxt.toString();
      					Object p = aspect.get(n);
      					if(text.contains(p.toString()))
      					{
      						count++;
      						nvlty[m] = 1;
      						break;
      					}
      				}
      			}
      			rate.add((double)count);
      			nuggets.add(nvlty);
      			/*
      			ps.append(question_id.toString()+'\t'+ count+": ");
      			for(int x: nvlty)
      			{
      				ps.append(x+", ");
      			}
      			ps.append("\n");
      			*/
      			
          	}
          	//double score = eval_ndcg_random(rate);
  			//scores.add(score);
          	/***************random*******************/
          	ArrayList<Integer> temp = new ArrayList<>();
          	ArrayList<Double> random_rate = new ArrayList<>();
          	ArrayList<int[]> random_negguts = new ArrayList<>();
          	for(int x=0;x<rate.size();x++)  temp.add(x);
          	
      		Random rn = new Random();
      		for(int x=rate.size(); x>0;x--)
      		{			
      			int r = rn.nextInt(x);
      			int nxt = temp.remove(r);
      			random_rate.add(rate.get(nxt));
      			random_negguts.add(nuggets.get(nxt));
      			//System.out.print(nxt+", ");
      		}
      		//System.out.println();
      		/***************random*******************/
      		
      		/****alpha-ndcg******/
          	double score = alpha_ndcg.alphandcg(random_rate, random_negguts, rate.size()-1);
          	
          	/****err-ia*******/
      		//double score = err_ia.err_Intent(random_rate, random_negguts);
          	
          	/***novelty-focused***/
      		//double score = novelty.noveltyMetric(random_rate, random_negguts);
          	
          	scores.add(score);
          	//System.out.println(question_id.toString()+ score);
  			ps.append(question_id.toString()+'\t'+ score+"\n");
          }
          double aveg = average_eval(scores);
          ps.append("Average: "+'\t'+ aveg+"\n");
          System.out.println("average: "+ aveg);

      } catch (ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
      ps.close();
      System.out.println("finishied!");
	}
	public static String readJsonFile(String Path){
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try{
			FileInputStream file = new FileInputStream(Path);
			InputStreamReader sr = new InputStreamReader(file, "UTF-8");
			br = new BufferedReader(sr);
			String line = null;
			while((line = br.readLine())!= null){
				sb.append(line);
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(br != null){
				try{
					br.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}		
		return sb.toString();
	}
	
	public static double average_eval(ArrayList<Double> scores)
	{
		double sum = 0;
		for(double score: scores)	sum += score;
		return sum/(double)scores.size();
	}
}

