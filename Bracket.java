import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Bracket  {
	private static Node root;
	private ArrayList<Node> nodes = new ArrayList<Node>(); 
	private Map<String, Double> ratingLookup = new HashMap<String, Double>();
	public static Map<Integer, String> firstRound;
	private double standardDev;
	private static ArrayList<String> changeFrom;
	private static ArrayList<String> changeTo;

	public Bracket() throws FileNotFoundException {
		Data createFile = new Data();
		Ranking createRanking = new Ranking();
		Teams teamsObject = new Teams();

		changeTo = new ArrayList<String>();

		List<String> masseyTeamNames = List.of("Alabama", "Arizona", "Arkansas", "Auburn", "Bucknell", "Buffalo", 
				"Butler", "CS Fullerton", "Charleston", "Cincinnati", "Clemson", "Creighton", "Davidson", "Duke", 
				"Florida", "Florida St", "Georgia St", "Gonzaga", "Houston", "Iona", "Kansas", "Kansas St", 
				"Kentucky", "Lipscomb", "Loyola Chicago", "Marshall", "Miami FL", "Michigan St", "Michigan", 
				"Missouri", "Montana", "Murray St", "NC State", "Nevada", "New Mexico St", "North Carolina",
				"Ohio St", "Oklahoma", "Penn", "Providence", "Purdue", "Radford", "Rhode Island", "SF Austin",
				"San Diego St", "Seton Hall", "S Dakota St", "St Bonaventure", "Syracuse", "TCU", "Tennessee", 
				"Texas", "Texas A&M", "TX Southern", "Texas Tech", "UMBC", "UNC Greensboro", "Villanova", 
				"Virginia", "Virginia Tech", "West Virginia", "Wichita St", "Wright St", "Xavier");


		changeFrom = Teams.ncaaNames;
		changeTo.addAll(masseyTeamNames);


		root = null;

		handleRank rankings = new handleRank();
		ratingLookup = rankings.teamList;
		standardDev = rankings.standardDev;



		firstRound = teamsObject.westMatchups;


		seedPairing();

	}
	/**Method that creates a parent node for two child nodes
	 * 
	 * @param seedOne
	 * @param seedTwo
	 * @return parent Node that is put back into the arraylist that contains all the
	 * nodes
	 */
	private Node addParent(Node seedOne, Node seedTwo) {
		if(seedOne != null && seedTwo != null) {
			int nextRound = seedOne.round + 1;
			Node parent = new Node(0, nextRound);
			parent.left = seedOne;
			parent.right = seedTwo;
			seedOne.sibling = seedTwo;
			seedTwo.sibling = seedOne; 
			return parent;
		}
		return null;
	}

	/***
	 * This uses the rating lookup and the CNDF to create a probability of teamOne 
	 * beating teamTwo
	 * 
	 * @param thisTeam
	 * @param opponentTeam
	 * @return probability of teamOne beating teamTwo
	 */
	private double headToHead(String teamOne, String teamTwo) {
		double thisRating = ratingLookup.get(teamOne);
		double otherRating = ratingLookup.get(teamTwo);

		double temp = (thisRating-otherRating)/standardDev;

		return handleRank.CNDF(temp);
	}

	/***
	 * This method runs through every possibility in each next round and calculates
	 * the total probability, which is then stored in the parent node
	 * @param parent
	 * @param opponentTeam
	 * 
	 */
	private void updateProbability(Node parent) { 
		Node left = parent.left;
		Node right = parent.right;
		for(String team:left.probability.keySet()) {
			double lastRound;
			double thisRound;
			double newProb = 0;
			if(left.round == 1) {
				lastRound = 1;
			}
			else {
				lastRound = left.probability.get(team);
			}
			for(String otherTeam:right.probability.keySet()) {
				double win = headToHead(team, otherTeam);
				if(left.round == 1) {
					newProb = win;
				}
				else {
					thisRound = win*right.probability.get(otherTeam);
					newProb += thisRound;
				}
			}
			parent.probability.put(team, newProb*lastRound);
		}
		for(String team:right.probability.keySet()) {
			double lastRound;
			double thisRound;
			double newProb = 0;
			if(right.round == 1) {
				lastRound = 1;
			}
			else {
				lastRound = right.probability.get(team);
			}
			for(String otherTeam:left.probability.keySet()) {
				double win = headToHead(team, otherTeam);
				if(right.round == 1) {
					newProb = win;
				}
				else {
					thisRound = win*left.probability.get(otherTeam);
					newProb += thisRound;
				}
			}
			parent.probability.put(team, newProb*lastRound);
		}
	}
	public static String updateName(String name) {
		int index = changeFrom.indexOf(name);
		String newName = changeTo.get(index);
		return newName.replace(" ", "");
	}
	/**
	 * Takes in an array of doubles, sorted by seed, that contain the ratings of each team.
	 * This method then creates the bracket and updates the probability as well as 
	 * creating parent nodes and then setting the root equal to the top node.
	 * 
	 * @param seeds: contains the ratings of each seed. Each index+1 corresponds to the 
	 * the team's seed
	 */
	private void seedPairing() {
		ArrayList<Node> temp = new ArrayList<Node>();
		for(int i = 0; i<firstRound.size()/2; i++) {
			Node higher = new Node(i+1, 1);
			String higherSeedName = updateName(firstRound.get(i+1));
			higher.probability.put(higherSeedName, ratingLookup.get(higherSeedName));

			Node lower = new Node(firstRound.size()-i, 1);
			String lowerSeedName = updateName(firstRound.get(firstRound.size()-(i)));
			lower.probability.put(lowerSeedName,ratingLookup.get(lowerSeedName));
			temp.add(higher);
			temp.add(lower);
		}
		Collections.sort(temp);
		seed(temp);
	}

	/**private helper method that initializes the root as the top node of the bracket
	 * 
	 * @param temp: an arraylist of nodes
	 */
	private void seed(ArrayList<Node> temp) {
		if(temp.size() == 1) {
			root = temp.remove(0);
			return;
		}
		else {
			ArrayList<Node> practice = new ArrayList<Node>();
			while(temp.size()>1) {
				Node first = temp.remove(0);
				Node last = temp.remove(temp.size()-1);
				Node parent = addParent(first, last);
				updateProbability(parent);
				practice.add(parent);
			}
			nodes = practice;
			seed(nodes);
		}
	}

	/**Method that searches the tree and returns an array-list of doubles that 
	 * holds the teams probability of making it to that round. The first entry 
	 * in the array holds the probability of reaching the Final Four and the last 
	 * entry in the array holds the team's original rating
	 * 
	 * @param temp: an arraylist of nodes
	 */
	public static ArrayList<Double> printProbs(String search){
		ArrayList<Double> rounds = new ArrayList<Double>();
		searchTree(root, search, rounds);
		return rounds;
	}
	private static void searchTree(Node root, String search, ArrayList<Double>temp) {
		if(root.probability.containsKey(search)) {
			if(root.right == null && root.left == null) {
				temp.add((root.probability.get(search)));
				return;
			}
			else {
				temp.add((root.probability.get(search)));
				searchTree(root.right, search, temp);
				searchTree(root.left,search,temp);
			}
		}
		else {
			return;
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		Bracket bracket = new Bracket();
	}
}
