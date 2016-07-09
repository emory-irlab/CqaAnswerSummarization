package evalPackage;

import java.util.ArrayList;

public class eval {
	public static double a_ndcg(ArrayList<Double> scores, ArrayList<int[]> nuggets, int k)
	{
		return alpha_ndcg.alphandcg(scores, nuggets, k);
	}
	public static double nerr_ia(ArrayList<Double> scores, ArrayList<int[]> nuggets, int[] maxProp)
	{
		return err_ia.nerr(scores, nuggets, maxProp);
	}
	public static double err_ia(ArrayList<Double> scores, ArrayList<int[]> nuggets, int[] maxProp)
	{
		return err_ia.err(scores, nuggets, maxProp);
	}
	public static double novelty_focused(ArrayList<Double> scores, ArrayList<int[]> nuggets)
	{
		return novelty.noveltyMetric(scores, nuggets);
	}
	public static double support_focused(ArrayList<Double> scores, ArrayList<int[]> nuggets)
	{
		return support.supportMetric(scores, nuggets);
	}
}
