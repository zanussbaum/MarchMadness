import java.io.*;
import java.net.URL;
import java.util.ArrayList;
public class Data {
	private static String dataWeb = "https://www.masseyratings.com/scores.php?s=292154&sub=11590&all=1&mode=2&format=1";
	private static String teamsWeb = "https://www.masseyratings.com/scores.php?s=292154&sub=11590&all=1&mode=2&format=2";
	public ArrayList<String> teamsSplit;

	private String copyData(String data) throws IOException{
		URL urlData = new URL(data);
		String arg = "";
		InputStream test = urlData.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(test));
		String temp;
		System.out.println("Reading in data from website\n\n");
		while((temp = reader.readLine()) != null) {
			arg += temp + "\n";
			teamsSplit.add(temp);
		}
		System.out.println("Finished copying data!\n");
		
		return arg;
	}
	
	private static void writeToFile(String data, String team) {
		String dataName = "2018games.txt";
		String teamsName = "2018teams.txt";

		System.out.println("Creating the game data file.\n");
		File dataFile = new File(dataName);
		System.out.println("Creating the team file.\n\n");
		File teamsFile =  new File(teamsName);

		FileWriter dataWrite;
		FileWriter teamsWrite;
		try {
			dataWrite = new FileWriter(dataFile);
			dataWrite.write(data);
			dataWrite.close();
			teamsWrite = new FileWriter(teamsFile);
			teamsWrite.write(team);
			teamsWrite.close();
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
			e.printStackTrace();
		};
	}


	public Data() {
		teamsSplit = new ArrayList<String>();
		try {
			String dataString = copyData(dataWeb);
			String teamString = copyData(teamsWeb);
			writeToFile(dataString,teamString);
		} catch (IOException e) {
			System.err.println("Caught IOException: " + e.getMessage());
			e.printStackTrace();
		}
	}
}