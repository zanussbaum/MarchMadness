// This software (or code, etc.) by Tim Chartier is licensed under a 
// Creative Commons Attribution-NonCommerical 4.0 International License.  
// Permissions beyond the scope of this license may be negotiated by contacting 
// Tim Chartier at tichartier@davidson.edu.

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.text.*; 

public class Ranking {

	private static final DecimalFormat fmt = new DecimalFormat("0.000000");

	public Ranking() {

		boolean keepLooping = true;

		while (keepLooping){		

			JRadioButton b1 = new JRadioButton("Every game counts as 1 game");
			JRadioButton b2 = new JRadioButton("Weight games with a line");
			JRadioButton b3 = new JRadioButton("Break season into intervals");
			b1.setSelected(true);	

			ButtonGroup group = new ButtonGroup();
			group.add(b1);
			group.add(b2);
			group.add(b3);

			Object[] array = {
					new JLabel("Select how you would like to weight the games:"),
					b1,
					b2,
					b3,
			};

			int res = JOptionPane.showConfirmDialog(null, array, "Sports Ranking", 
					JOptionPane.OK_CANCEL_OPTION);

			// User hit OK
			//		if (res == JOptionPane.OK_OPTION) { System.out.println( "OK_OPTION" ); }

			// User hit CANCEL or closed the window without hitting any button
			if ((res == JOptionPane.CANCEL_OPTION) | (res == JOptionPane.CLOSED_OPTION))
				System.exit(0);

			int weightMethod = 0;
			// Determine which weighting was selected
			if (b1.isSelected()) 
				weightMethod = 0; // Uniform weighting
			else if (b2.isSelected()) 
				weightMethod = 1; // Linear weighting
			else 
				weightMethod = 2; // step function

			JRadioButton m1 = new JRadioButton("Winning percentage");
			JRadioButton m2 = new JRadioButton("Colley method");
			JRadioButton m3 = new JRadioButton("Massey method");
			m1.setSelected(true);	

			ButtonGroup methodGroup = new ButtonGroup();
			methodGroup.add(m1);
			methodGroup.add(m2);
			methodGroup.add(m3);

			Object[] methodArray = {
					new JLabel("Select your ranking method:"),
					m1,
					m2,
					m3,
			};

			res = JOptionPane.showConfirmDialog(null, methodArray, "Ranking Method", 
					JOptionPane.OK_CANCEL_OPTION);

			// User hit CANCEL or closed the window without hitting any button
			if ((res == JOptionPane.CANCEL_OPTION) | (res == JOptionPane.CLOSED_OPTION))
				System.exit(0);

			int rankingMethod = 0;
			// Determine which weighting was selected
			if (m1.isSelected()) 
				rankingMethod = 0; // Winning percentage
			else if (m2.isSelected()) 
				rankingMethod = 1; // Colley
			else 
				rankingMethod = 2; // Massey

			int teamsLowerBound = 350; 
			String[][] games = null;
			String[] teams = null; 
			ArrayList teamArray = new ArrayList(teamsLowerBound);
			ArrayList gameArray = new ArrayList(teamsLowerBound);

			String gamesFile = "2018games.txt";
			String teamsFile = "2018teams.txt";

			// Read the team names could deliminate by ", "
			BufferedReader inputStream = null;
			String inputLine = null; 
			try {
				inputStream = new BufferedReader(new FileReader(teamsFile));
				while ((inputLine = inputStream.readLine()) != null) {
					teamArray.add(inputLine);
				}
				inputStream.close(); 

				inputStream = new BufferedReader(new FileReader(gamesFile));
				while ((inputLine = inputStream.readLine()) != null) {
					gameArray.add(inputLine);
				}
				inputStream.close(); 

			} catch (FileNotFoundException e) {
				System.err.println("File not found: " + e.getMessage());
			} catch (IOException e) {
				System.err.println("I/O Exception: " + e.getMessage());
			} 

			// Load the team data into an array
			StringTokenizer tokenizer = null; 

			teams = new String[teamArray.size()];

			for (int i=0; i < teamArray.size(); i++){
				tokenizer = new StringTokenizer((String) teamArray.get(i), " ,"); 			
				tokenizer.nextToken();
				teams[i] = tokenizer.nextToken(); 
			}

			// Load the game data into a 2D array
			tokenizer = new StringTokenizer((String) gameArray.get(0),",");
			int numberOfColumns = tokenizer.countTokens();
			int numberOfGames = gameArray.size(); 
			games = new String[numberOfGames][numberOfColumns];

			for (int i=0; i < gameArray.size(); i++){
				tokenizer = new StringTokenizer((String) gameArray.get(i), ","); 			
				int j = 0; 
				while (tokenizer.hasMoreTokens()){
					games[i][j] = tokenizer.nextToken().trim();
					j++;
				}
			}

			// Return input arrays back to memory
			teamArray = null; 
			gameArray = null; 

			double[] weights = new double[games.length];
			int[] days = new int[games.length];
			for (int i = 0; i < days.length; i++) {
				days[i] = Integer.parseInt(games[i][0]);
			}

			// Weightings
			if (weightMethod == 0) {
				// No weighting
				for (int i = 0; i < weights.length; i++) {
					weights[i] = 1;
				}
			} else if (weightMethod == 1) {
				// Linear weighting

				String inputString=
						JOptionPane.showInputDialog("We will weight the games with a line.\n" +
								"What slope would you like for your line?");
				double slope = Double.parseDouble(inputString);

				for (int i = 0; i < weights.length; i++) {
					weights[i] = slope*((double)days[i]-days[0])/(days[games.length-1]-days[0]);
				}
			} else {
				// Using step function weighting
				String inputString=
						JOptionPane.showInputDialog("We will break the season into intervals.\n" +
								"How many intervals would you like?");
				int numberOfIntervals = Integer.parseInt(inputString);

				double[] intervalWeight = new double[numberOfIntervals];

				for (int i=0; i < numberOfIntervals; i++){
					inputString=
							JOptionPane.showInputDialog("How much will a game count in interval #" + (i+1));
					intervalWeight[i] = Double.parseDouble(inputString);
				}

				int totalDaysInSeason = days[games.length-1] - days[0]; 
				int currentDay; 
				for (int i = 0; i < weights.length; i++) {
					currentDay = days[i] - days[0]; 
					weights[i] = 1; 
					for (int j=0; j < numberOfIntervals; j++) {
						if ((double)currentDay/totalDaysInSeason < intervalWeight[j]){
							weights[i] = intervalWeight[j];
							break; 
						}
					}
				}

			}

			// find out who beat who
			int team1ID;
			int team1Score;
			int team2ID;
			int team2Score;
			double[][] teamTeam = new double[teams.length][teams.length];
			double[]  ratingRHS = new double[teams.length];
			for (int i = 0; i < games.length; i++) {
				team1ID = Integer.parseInt(games[i][2]) - 1;
				team1Score = Integer.parseInt(games[i][4]);
				team2ID = Integer.parseInt(games[i][5]) - 1;
				team2Score = Integer.parseInt(games[i][7]);

				ratingRHS[team2ID] += weights[i]*(team2Score - team1Score);
				ratingRHS[team1ID] += weights[i]*(team1Score - team2Score);

				if (team1Score > team2Score) {
					teamTeam[team2ID][team1ID] = teamTeam[team2ID][team1ID] + weights[i];
				} else {
					teamTeam[team1ID][team2ID] = teamTeam[team1ID][team2ID] + weights[i];
				}
			}

			// Calculate linear system
			double[][] ratingMatrix   = new double[teams.length][teams.length];

			double[]   weightedLosses = new double[teams.length];
			double[]   weightedWins   = new double[teams.length];

			double[]   ratings = new double[teams.length];

			// Find the number of weighted losses and wins for each team
			for (int i=0; i < teams.length; i++) {
				weightedLosses[i] = 0; 
				weightedWins[i] = 0;
				for (int j=0; j < teams.length; j++){
					weightedLosses[i] += teamTeam[i][j]; // sum ith row 
					weightedWins[i]   += teamTeam[j][i]; // sum ith column 
				}
			}

			if (rankingMethod == 0) {
				// Calculate weighted winning percentage 
				double weightedTotalGames; 
				for (int i=0; i < teams.length; i++) {
					weightedTotalGames = weightedWins[i] + weightedLosses[i]; 
					if (weightedTotalGames == 0)
						ratings[i] = -1; 
					else 
						ratings[i] += weightedWins[i]/weightedTotalGames ;
				}
			} else {  // Massey or Colley
				// Form the off diagonal elements of the Colley or Massey matrix 
				for (int i=0; i < teams.length; i++) {
					for (int j=0; j < teams.length; j++) {
						ratingMatrix[i][j] = -teamTeam[i][j] - teamTeam[j][i]; 
					}
				}
			}
			
			if (rankingMethod == 1){				
				// Form the diagonal entries of the Colley matrix 
				for (int i=0; i < teams.length; i++) {
					ratingMatrix[i][i] = 2 + weightedWins[i] + weightedLosses[i]; 
				}

				// Form the RHS of the Colley linear system 
				for (int i=0; i < teams.length; i++) {
					ratingRHS[i] = 0.5*(weightedWins[i] - weightedLosses[i]) + 1;
				}
			} else if (rankingMethod == 2){  // Massey 
				// Form the diagonal entries of the Colley matrix 
				for (int i=0; i < teams.length; i++) {
					ratingMatrix[i][i] = weightedWins[i] + weightedLosses[i]; 
				}
				// Rest of RHS formed earlier for Massey 
				// Replace last row of Massey matrix with 1's and RHS with 0. 
				for (int i=0; i < teams.length; i++) {
					ratingMatrix[teams.length-1][i] = 1; 
				}
				ratingRHS[teams.length-1] = 0; 
			}

			if (rankingMethod > 0) {						
				// Find the rating vector
				double tolerance = 1E-10;  // acceptable difference in GS method

				gaussSeidelMethod(ratingMatrix,ratings,ratingRHS, tolerance);
			}

			bubbleSort(ratings, teams);

			// Write the sorted list to an output file to be imported by Excel
			PrintWriter outputStream = null;
			String outputFile = "2018ranking.txt";

			try {
				outputStream = new PrintWriter(new FileWriter(outputFile));
				// Print ratings 
				//outputStream.println("Here are the teams ranked with their associated rating vector.");
				for (int i=0; i < teams.length; i++){
					outputStream.println(fmt.format(ratings[i]) + "\t" + teams[i].replaceAll("_", " ")); 
				}
				outputStream.close();

			} // end try
			catch (IOException e) {
				System.out.println("Error writing file");
			} // end catch
			finally {
				if (outputStream != null) {
					outputStream.close();
				} // end if
			} // end finally

			int numberOfTopTeamsToList = 10;
			String outputText = "Here are the top " + numberOfTopTeamsToList + " teams and their ratings:\n\n";
			for (int i=0; i < numberOfTopTeamsToList; i++){
				outputText += (fmt.format(ratings[i]) + "  " + teams[i].replaceAll("_", " ") + "\n"); 
			}
			
			JOptionPane.showMessageDialog(null,outputText);
			/*
			outputText = "Full results written to file \"" + outputFile + "\".";
			JOptionPane.showMessageDialog(null,outputText);

			// Ask the user whether to continue
			int answer= JOptionPane.showConfirmDialog(null,"Would you like to repeat the program?", "Continue?",JOptionPane.YES_NO_OPTION);
			keepLooping = answer == JOptionPane.YES_OPTION;
			*/
			break;
		}

	}

	public static void bubbleSort(double[] a, String[] label) {
		int pass = 0;
		boolean sorted = false;
		while (!sorted) {
			sorted = true;
			for (int i=0; i<a.length-1-pass; i++) {
				if (a[i] < (a[i+1])) {
					// Swap the keys
					double t = a[i];
					a[i] = a[i+1];
					a[i+1] = t;
					// Swap the labels
					String s = label[i];
					label[i] = label[i+1];
					label[i+1] = s;
					sorted = false;
				}
			}
			pass++;
		}
	}

	public static void gaussSeidelMethod(double[][]A,double[] x,double [] b, double tolerance){

		double infinityNorm = 0; 
		double xNew; 

		for (int i=0; i < x.length; i++)
			x[i] = 0; 

		double difference; 
		do {
			infinityNorm = 0; 
			for (int i=0; i < A.length; i++) {
				xNew = b[i]; 
				for (int j=0; j < A[i].length; j++){
					if (j != i) {
						xNew -= A[i][j]*x[j];
					}
				}
				xNew = 1/A[i][i]*xNew;
				difference = Math.abs(xNew - x[i]);  
				if (difference > infinityNorm)
					infinityNorm = difference; 
				x[i] = xNew; 
			} 
		} while (infinityNorm > tolerance);
	}

}