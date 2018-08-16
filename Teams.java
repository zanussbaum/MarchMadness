import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Teams{
	public Map <Integer, String> eastMatchups, westMatchups, midwestMatchups, southMatchups;
	
	private String bracketSite = "https://www.ncaa.com/interactive-bracket/basketball-men/d1";
	
	public static ArrayList<String> ncaaNames;
	
	private static Map<Integer, String> getRegionMatchups(Elements gameArea){
		Map<Integer, String> teamList = new TreeMap<Integer, String>();

		for(int i = 0; i < 8; i ++) {
			Element game = gameArea.get(i);
			Elements topCell = game.select("div.team-info.info-top");
			Elements bottomCell = game.select("div.team-info.info-bottom");

			String topCellName = topCell.select("div.team-name").text();
			int topCellSeed = Integer.parseInt(topCell.select("div.team-seed").text());
			ncaaNames.add(topCellName);
			

			String bottomCellName = bottomCell.select("div.team-name").text();
			int bottomCellSeed = Integer.parseInt(bottomCell.select("div.team-seed").text());
			ncaaNames.add(bottomCellName);
			
			teamList.put(topCellSeed, topCellName);
			teamList.put(bottomCellSeed, bottomCellName);
		}
		return teamList;

	}
	public Teams() {
		ncaaNames = new ArrayList<String>();
		Document doc;
		try {
			doc = Jsoup.connect(bracketSite).get();
			Element eastRegion = doc.getElementById("bracket-top-right");
			Element southRegion = doc.getElementById("bracket-top");
			Element westRegion = doc.getElementById("bracket-bottom");
			Element midwestRegion = doc.getElementById("bracket-bottom-right");
			
			Elements eastGames = eastRegion.select("div.game-cell");
			Elements southGames = southRegion.select("div.game-cell");
			Elements westGames = westRegion.select("div.game-cell");
			Elements midwestGames = midwestRegion.select("div.game-cell");
			
			eastMatchups = getRegionMatchups(eastGames);
			southMatchups = getRegionMatchups(southGames);
			westMatchups = getRegionMatchups(westGames);
			midwestMatchups = getRegionMatchups(midwestGames);
			
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		Collections.sort(ncaaNames);
		
		
		System.out.println("Created round matchups for each region.");
	}
}

