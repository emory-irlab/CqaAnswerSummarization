package evalPackage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class readJasonFile {
	public static String readFile(String Path){
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
}
