package evalPackage;

import java.util.ArrayList;

public class permute {
	public void per(int[] nums) {
	   // ArrayList<ArrayList<Integer>> results = new ArrayList<>();
	    if(nums == null || nums.length == 0) return;
	    ArrayList<Integer> result = new ArrayList<>();
	    //dfs(nums, results, result);
	    dfs(nums, result);
	    //return results;
	}

	//public void dfs(int[] nums, ArrayList<ArrayList<Integer>> results, ArrayList<Integer> result){
	public void dfs(int[] nums, ArrayList<Integer> result){
	    if(nums.length == result.size()){
	    	ArrayList<Integer> temp = new ArrayList<>(result);
	        //results.add(temp);
	    	//process the result
	    	System.out.println(temp);
	    }        
	    for(int i=0; i<nums.length; i++){
	        if(result.contains(nums[i])) continue; 
	        result.add(nums[i]);
	        //dfs(nums, results, result);
	        dfs(nums, result);
	        result.remove(result.size()-1);
	    }
	}
}
