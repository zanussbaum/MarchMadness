
import java.util.HashMap;
import java.util.Map;

public class Node implements Comparable<Node> {
	public Map<String, Double> probability; //integer is what team, double is prob for that round 
	public int round;
	public Node left;
	public Node right;
	public Node sibling; 
	public int seed;

	public Node(int seed, int round) {
		this.round = round;
		this.seed = seed; 
		probability = new HashMap<String, Double>();
		left = null;
		right = null;

	}
	
	public int compareTo(Node other) {
		if(seed == other.seed) {
			return left.compareTo(other.left);
		}
		else {
			return seed - other.seed;
		}
	}
	
	public String toString() {
		return "[" + seed + "]";
	}
}