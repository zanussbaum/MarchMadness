import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class handleRank {
	public Map<String, Double> teamList = new HashMap<String, Double>();
	public double standardDev = 0;
	public handleRank() throws FileNotFoundException {
		Scanner scan = new Scanner(new File("2018ranking.txt"));

		do{
			double ranking = scan.nextDouble();
			String team = scan.next();
			
			while(!scan.hasNextDouble() && scan.hasNext()) {
				team+= scan.next();
			}
			teamList.put(team, ranking);
			
			
		}while(scan.hasNext());

		scan.close();
		standardDev = standardDeviation(teamList);
		
	}
	public static double CNDF(double x)
	{
		int neg = (x < 0d) ? 1 : 0;
		if ( neg == 1) 
			x *= -1d;

		double k = (1d / ( 1d + 0.2316419 * x));
		double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
				k - 0.356563782) * k + 0.319381530) * k;
		y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

		return (1d - neg) * y + neg * (1d - y);
	}
	public static double standardDeviation(Map<String, Double> teams) {
		double mean = 0;
		double standardDeviation = 0;

		for(double number: teams.values()) {
			mean+= number;
		}
		mean/=teams.size();

		for(double number: teams.values()) {
			standardDeviation += Math.pow((number-mean), 2);
		}

		return Math.sqrt(standardDeviation/teams.size());
	}
	
}

