package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import com.google.common.graph.*;
import io.atlassian.fugue.Iterables;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.ui.ai.TestBase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import io.atlassian.fugue.Pair;
import org.w3c.dom.Node;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.readGraph;

public class MyAi implements Ai {

	// AI name
	@Nonnull @Override public String name() { return "Dijkstra's Detectives"; }
	
	// Implement Dijkstra's Algorithm
	/// We crete a version of Dijkstra's algorithm which calculates the shortest paths to all nodes from a given source node, then we find the furthest point away from the
	/// detectives for mrX to move to, it would be better if this point is closer to mrX
	/// The detectives will keep moving towards mrX according to whatever last revealed
	/// position mrX was at
	public final class Tuple<X,Y> {
		private X x;
		private Y y;
		private Tuple(X x, Y y) { this.x = x; this.y = y; }

		public X getDistance() { return x; }
		public Y getShortestPath() { return y; }
	}

	// find the node closest to an unsettled node
	private static Integer getLowestDistanceNode(Set<Integer> unsettled, Map<Integer,Tuple<Integer,LinkedList<Integer>>> nodeDistanceAndSP) {
		Integer lowestDistanceNode = null;
		int lowestDistance = Integer.MAX_VALUE;
		for (Integer node : unsettled) {
			int nodeDistance = nodeDistanceAndSP.get(node).getDistance();
			if (nodeDistance < lowestDistance) {
				lowestDistance = nodeDistance;
				lowestDistanceNode = node;
			}
		}
		return lowestDistanceNode;
	}

	// find the minimum distance so far
	private void findMinimumDistance(Integer evalNode, Integer source, Integer edgeWeight, Map<Integer,Tuple<Integer,LinkedList<Integer>>> nodeDistanceAndSP) {
		Integer sourceDistance = nodeDistanceAndSP.get(source).getDistance();
		if (sourceDistance + edgeWeight < nodeDistanceAndSP.get(evalNode).getDistance()) {
			LinkedList<Integer> shortestPath = nodeDistanceAndSP.get(evalNode).getShortestPath();
			shortestPath.add(source);
			nodeDistanceAndSP.put(evalNode, new Tuple<Integer,LinkedList<Integer>>(sourceDistance + edgeWeight,shortestPath));
		}
	}

	// actual implementation
	// note that this finds the shortest path to every node from the source node
	private LinkedList<Integer> shortestPathFinder(ImmutableValueGraph<Integer, ImmutableSet<Transport>> graph, Integer source, Integer destination) {
		// build adjacency matrix
		Map<Integer,Tuple<Integer,LinkedList<Integer>>> nodeDistanceAndSP = new HashMap<>();
		for (Integer node : graph.nodes()) {
			Tuple distanceAndSP = new Tuple(Integer.MAX_VALUE,new LinkedList<Integer>());
			nodeDistanceAndSP.put(node,distanceAndSP);
		}

		// sets of settled and unsettled nodes
		Set<Integer> unsettled = new HashSet<>();
		Set<Integer> settled = new HashSet<>();

		unsettled.add(source);

		// work through unsettled nodes
		while (!unsettled.isEmpty()) {
			// set a new current node as the adjacent node with the lowest distance
			Integer currentNode = getLowestDistanceNode(unsettled,nodeDistanceAndSP);
			if (currentNode != null) {
				// remove current node form unsettled as it is now "settled"
				if (unsettled.contains(currentNode)) unsettled.remove(currentNode);

				// add any adjacent nodes to unsettled if not in unsettled already
				for (Integer adjacent : graph.adjacentNodes(currentNode)) {
					Integer adjacentNode = adjacent;
					if (!settled.contains(adjacentNode)) {
						findMinimumDistance(adjacentNode,currentNode,1,nodeDistanceAndSP);
						unsettled.add(adjacentNode);
					}
				}
			}
			settled.add(currentNode);
		}

		// now that we have all the shortest paths from the source to any other node
		// we choose our destination
		LinkedList<Integer> shortestPath = nodeDistanceAndSP.get(destination).getShortestPath();
		return shortestPath;
	}

	/// Test 1
//	public void dijkstraTest() {
//		// create nodes
//		Node node1 = new Node(1,ImmutableMap.of());
//		Node node2 = new Node(2, ImmutableMap.of());
//		Node node3 = new Node(3, ImmutableMap.of());
//		Node node4 = new Node(4, ImmutableMap.of());
//		Node node5 = new Node(5, ImmutableMap.of());
//		Node node6 = new Node(6, ImmutableMap.of());
//
//		// declare connections between nodes
//		// since transport irrelevant for now, we put all as TAXI
//		node1.addNeighbour(node2,TAXI); // 1 -> 2
//		node1.addNeighbour(node3,TAXI); // 1 -> 3
//		node1.addNeighbour(node4,TAXI); // 1 -> 4
//		node2.addNeighbour(node3,TAXI); // 2 -> 3
//		node3.addNeighbour(node5,TAXI); // 3 -> 5
//		node3.addNeighbour(node6,TAXI); // 3 -> 6
//		node4.addNeighbour(node5,TAXI); // 4 -> 5
//		node5.addNeighbour(node6,TAXI); // 5 -> 6
//
//		// set of nodes
//		HashSet<Node> nodes = new HashSet<>();
//		nodes.add(node1);
//		nodes.add(node2);
//		nodes.add(node3);
//		nodes.add(node4);
//		nodes.add(node5);
//
//		// create graph
//		Graph graph = new Graph(nodes);
//
//		// shortest path according to our implementation
//		LinkedList<Node> foundShortestPath = node1.shortestPath;
//
//		// actual shortest path
//		LinkedList<Node> actualShortestPath = new LinkedList<>();
//		actualShortestPath.add(node1);
//		actualShortestPath.add(node3);
//		actualShortestPath.add(node6);
//
//		// test
//		if (actualShortestPath.size() != foundShortestPath.size()) throw new RuntimeException("Shortest path doesn't match");
//		for (int i = 0; i < actualShortestPath.size(); i++) {
//			if (!actualShortestPath.get(i).equals(foundShortestPath.get(i))) throw new RuntimeException("Wrong node");
//		}
//	}

	// find our players in play
	private static ImmutableSet<Player> findPlayers(Board board) {
		// we build our players based on the available pieces
		Set<Piece> pieces = board.getPlayers();
		ImmutableSet<Move> moves = board.getAvailableMoves();

		// tickets
		Map<Ticket,Integer> blackTickets = new HashMap<>();
		Map<Ticket,Integer> redTickets = new HashMap<>();
		Map<Ticket,Integer> greenTickets = new HashMap<>();
		Map<Ticket,Integer> blueTickets = new HashMap<>();
		Map<Ticket,Integer> yellowTickets = new HashMap<>();
		Map<Ticket,Integer> whiteTickets = new HashMap<>();

		// locations
		int mrXLoc = 0;
		int redLoc = 0;
		int greenLoc = 0;
		int blueLoc = 0;
		int yellowLoc = 0;
		int whiteLoc = 0;

		// extract location and tickets from getAvailableMoves
		// seperate case for mrX
		if ((moves.stream().anyMatch(m -> m instanceof Move.DoubleMove)) || moves.stream().anyMatch(m -> m.tickets().iterator().next().equals(Ticket.SECRET))) {
			for (Move move : moves) { move.tickets().forEach(ticket -> blackTickets.put(ticket, 1)); }
		}

		// for detectives
		for (Move move : moves) {
			switch (move.commencedBy().webColour()) {
				case "#f00":
					move.tickets().forEach(ticket -> redTickets.put(ticket, 1));
					redLoc = move.source();
					break;

				case "#0f0":
					move.tickets().forEach(ticket -> greenTickets.put(ticket, 1));
					greenLoc = move.source();
					break;

				case "#00f":
					move.tickets().forEach(ticket -> blueTickets.put(ticket, 1));
					blueLoc = move.source();
					break;

				case "#ff0":
					move.tickets().forEach(ticket -> yellowTickets.put(ticket, 1));
					yellowLoc = move.source();
					break;

				case "#fff":
					move.tickets().forEach(ticket -> whiteTickets.put(ticket, 1));
					whiteLoc = move.source();
					break;
			}
		}


		// add in players
		Set<Player> players = new HashSet<>();
		for (Piece piece : pieces) {
			switch (piece.webColour()) {
				case "#000": players.add(new Player(piece,ImmutableMap.copyOf(blackTickets),mrXLoc)); break;
				case "#f00": players.add(new Player(piece,ImmutableMap.copyOf(redTickets),redLoc)); break;
				case "#0f0": players.add(new Player(piece,ImmutableMap.copyOf(greenTickets),greenLoc)); break;
				case "#00f": players.add(new Player(piece,ImmutableMap.copyOf(blueTickets),blueLoc)); break;
				case "#ff0": players.add(new Player(piece,ImmutableMap.copyOf(yellowTickets),yellowLoc)); break;
				case "#fff": players.add(new Player(piece,ImmutableMap.copyOf(whiteTickets),whiteLoc)); break;
				default: break;
			}
		}

		// remove any players not in play
		Set<Player> playersInPlay = new HashSet<>();
		for (Player player : players) { if (!player.tickets().isEmpty()) playersInPlay.add(player); }

		return ImmutableSet.copyOf(playersInPlay);
	} // test this

	// find mrX's last revealed location
	private static Integer lastRevealed(Board board, GameSetup setup) {
		// get location of last revealed move of mrX
		int lastRevealedIdx = 0;
		int logEntriesSize = board.getMrXTravelLog().size() - 1;

		// slice setup.moves to the size of the current travel log
		ImmutableList<Boolean> revealHiddenMoves = setup.moves.subList(0, logEntriesSize);
		lastRevealedIdx = revealHiddenMoves.lastIndexOf(true);

		// in the case mrX hasn't been revealed yet
		if (lastRevealedIdx == -1) return -1;

		// in the case mrX has been revealed
		LogEntry lastRevealedLogEntry = board.getMrXTravelLog().get(lastRevealedIdx);
		return lastRevealedLogEntry.location().get();
	}

	// build graph of available moves
	private static ImmutableValueGraph<Integer, ImmutableSet<Transport>> buildGraphOfAvailableMoves(ImmutableSet<Move> moves, GameSetup setup) {
		MutableValueGraph<Integer, ImmutableSet<Transport>> newGraph = ValueGraphBuilder.undirected().build();
		for (Move move : moves) {
			int node = move.source();
			if (setup.graph.nodes().contains(node)) {
				newGraph.addNode(node);
				for (Integer adjacentNode : setup.graph.adjacentNodes(node)) {
					newGraph.putEdgeValue(move.source(),adjacentNode,ImmutableSet.of());
				}
			}
		}
		return ImmutableValueGraph.copyOf(newGraph);
	}

	private ImmutableMap<Player,LinkedList<Integer>> shortestPathsFromDetectiveToMrX(Board board, ImmutableValueGraph<Integer, ImmutableSet<Transport>> graph, ImmutableSet<Player> players, Integer mrXLocation) {
		// a set of shortest paths for each detective
		Map<Player,LinkedList<Integer>> playerShortestPaths = new HashMap<>();

		for (Player player : players) {
			if (player.isDetective()) {
				LinkedList<Integer> path = shortestPathFinder(graph,player.location(),mrXLocation);
				playerShortestPaths.put(player,path);
			}
		}

		return ImmutableMap.copyOf(playerShortestPaths);
	}

	// scoring system to choose moves
	// idea: for the detectives, the closer to mrX (last revealed) location, the better
	// 		 for mrX, the farther away from all the detectives, the better
	//		 scores based on available moves i.e. we remove the highest scoring move from
	//		 the shortest path to get second highest scoring move
	public ImmutableSet<Move> implementation(Board board, GameSetup setup) {
		ImmutableSet<Move> moves = board.getAvailableMoves();
		ImmutableValueGraph<Integer,ImmutableSet<Transport>> graph = buildGraphOfAvailableMoves(moves,setup);
		ImmutableSet<Player> players = findPlayers(board);

		// if mrX has not been revealed yet
		Integer lastReveledLocation = lastRevealed(board,setup);
		if (lastReveledLocation == -1) return ImmutableSet.of();

//		if (players.size() == 1) score = scoreMrX(board, graph, moves);
//		else score = scoreDetectives(board, setup, graph, moves);

		Set<Move.SingleMove> singleMoves = new HashSet<>();
		for (Move move : moves) { singleMoves.add((Move.SingleMove) move); }


		// detective implementation
		ImmutableMap<Player,LinkedList<Integer>> shortestPathsForDetectives = shortestPathsFromDetectiveToMrX(board, graph, players, lastReveledLocation);

		Set<Move> movesForDetectives = new HashSet<>();

		// add moves that are on the shortest path
		for (Integer nodeOnShortestPath : shortestPathsForDetectives.get(0)) {
			for (Move.SingleMove move : singleMoves) {
				if (nodeOnShortestPath == move.source()) movesForDetectives.add(move);
			}
		}

		return ImmutableSet.copyOf(movesForDetectives);
	}

	// when mrX hasn't been revealed yet, let detectives run randomly
	private static Move runRandom(Board board) {
		var moves = board.getAvailableMoves().asList();
		return moves.get(new Random().nextInt(moves.size()));
	}

	@Override public void onStart() {
//		dijkstraTest();

	}

	@Override public void onTerminate() {

	}

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {

		var moves = implementation(board,board.getSetup()).asList();

		// in the case where mrX hasn't been revealed yet,
		// the detectives run randomly
		if (moves.isEmpty()) return runRandom(board);

		return moves.get(0);
	}

}
