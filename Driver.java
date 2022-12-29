package ozyegin;

import java.util.*;

public class Driver {
	static class Edge {
		int to, flow, capacity;
		ozyegin.Edge reverseEdge;

		public Edge(int to, int flow, int capacity) {
			this.to = to;
			this.flow = flow;
			this.capacity = capacity;
		}
	}

	static class Vertex {
		int id;
		LinkedList<Edge> edges;

		public Vertex(int id) {
			this.id = id;
			this.edges = new LinkedList<>();
		}
	}

	static class NetworkFlowGraph {
		ozyegin.Vertex[] vertices;

		public NetworkFlowGraph(int numVertices) {
			vertices = new ozyegin.Vertex[numVertices];
			for (int i = 0; i < numVertices; i++) {
				vertices[i] = new ozyegin.Vertex(i);
			}
		}

		public void addEdge(int from, int to, int capacity) {
			ozyegin.Edge forwardEdge = new ozyegin.Edge(to, 0, capacity);
			ozyegin.Edge reverseEdge = new ozyegin.Edge(from, 0, 0);
			forwardEdge.reverseEdge = reverseEdge;
			reverseEdge.reverseEdge = forwardEdge;
			vertices[from].edges.add(forwardEdge);
			vertices[to].edges.add(reverseEdge);
		}
	}
	static class MaximumFlow {
		NetworkFlowGraph graph;
		int source;
		int sink;

		public MaximumFlow(NetworkFlowGraph graph, int source, int sink) {
			this.graph = graph;
			this.source = source;
			this.sink = sink;
		}


		public void getMaximumFlow(int n,int[] outcomes, String[] projectNames, Map<String, List<String>> prerequisites) {
			int flow = 0;
			while (true) {

				// Find augmenting path using BFS
				int[] previous = new int[graph.vertices.length];
				LinkedList<Integer> queue = new LinkedList<>();
				queue.add(source);
				while (!queue.isEmpty()) {
					int u = queue.poll();
					for (ozyegin.Edge edge : graph.vertices[u].edges) {
						if (previous[edge.to] == 0 && edge.capacity > edge.flow) {
							queue.add(edge.to);
							previous[edge.to] = u;
						}
					}
				}

				// If no augmenting path found, return flow
				if (previous[sink] == 0) {
					//find the executable venture project subset that maximizes the profit
					List<String> executableProjects = new LinkedList<>();
					int maxProfit = 0;
					for (int i = 0; i < (1 << n); i++) {
						List<String> currentProjects = new LinkedList<>();
						int currentProfit = 0;
						for (int j = 0; j < n; j++) {
							if ((i & (1 << j)) != 0) {
								currentProjects.add(projectNames[j]);
								currentProfit += outcomes[j];
							}
						}
						if (isExecutable(currentProjects, prerequisites) && currentProfit > maxProfit) {
							executableProjects = currentProjects;
							maxProfit = currentProfit;
						}
					}

					//print the executable venture project subset and the maximum profit
					System.out.print("Venture projects: ");
					for (String project : executableProjects) {
						System.out.print(project + " ");
					}
					System.out.println();
					System.out.println("Maximum profit: " + maxProfit);
					return;
				}

				// Calculate minimum flow through augmenting path
				int minFlow = Integer.MAX_VALUE;
				for (int v = sink; v != source; v = previous[v]) {
					int u = previous[v];
					minFlow = Math.min(minFlow, graph.vertices[u].edges.get(v).capacity - graph.vertices[u].edges.get(v).flow);
				}

				// Update flow and residual capacities
				for (int v = sink; v != source; v = previous[v]) {
					int u = previous[v];
					graph.vertices[u].edges.get(v).flow += minFlow;
					graph.vertices[v].edges.get(u).flow -= minFlow;
				}

				// Add minimum flow to overall flow
				flow += minFlow;
			}
		}
		private boolean isExecutable(List<String> projects, Map<String, List<String>> prerequisites) {
			for (String project : projects) {
				if (!isDependenciesExecutable(project, projects, prerequisites)) {
					return false;
				}
			}
			return true;
		}

		private boolean isDependenciesExecutable(String project, List<String> projects, Map<String, List<String>> prerequisites) {
			List<String> dependencies = prerequisites.get(project);
			if (dependencies == null) {
				return true;
			}
			for (String dependency : dependencies) {
				if (!projects.contains(dependency) && !isDependenciesExecutable(dependency, projects, prerequisites)) {
					return false;
				}
			}
			return true;
		}
	}

	private static int getVentureProjectIndex(char[] ventureProjects, char ventureProject) {
		  for (int i = 0; i < ventureProjects.length; i++) {
		    if (ventureProjects[i] == ventureProject) {
		      return i;
		    }
		  }
		  return -1;
	}

	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter the value of n: ");
		int n = scanner.nextInt();
		char[][] prerequisites = new char[n][2];
		int row = 0;
		char[] ventureProjects = new char[n];
		int[] outcomes = new int[n];
		NetworkFlowGraph graph = new NetworkFlowGraph(n + 2);

		System.out.print("Enter the values for the ventureProjects array, separated by a space: ");
		for (int i = 0; i < n; i++) {
			ventureProjects[i] = scanner.next().charAt(0);
		}
		System.out.print("Enter the values for the outcomes array, separated by a space: ");
		for (int i = 0; i < n; i++) {
			outcomes[i] = scanner.nextInt();
		}
		System.out.print("Enter the values for the prerequisites array, separated by a space: ");

		while (scanner.hasNext()) {
			String prerequisite = scanner.next();
			if (prerequisite.equals("Decide")) {break;}

			// Split the string into two parts by the comma
			String[] parts = prerequisite.split(",");
			// Get the first and second letters between the parentheses
			char first = parts[0].charAt(1),second = parts[1].charAt(0);
			// Add the letters to the 2D array
			prerequisites[row][0] = first;
			prerequisites[row][1] = second;
			row++;
		}

		for (int i = 0; i < n; i++) {
			  if (outcomes[i] > 0) {
			    graph.addEdge(0, i + 1, Integer.MAX_VALUE);
			  }
		}

		// Add edges from venture projects to sink with negative outcomes
		for (int i = 0; i < n; i++) {
		  if (outcomes[i] < 0) {
			graph.addEdge(i + 1, n + 1, -Integer.MAX_VALUE);
		  }
		}

		for (char[] prerequisite : prerequisites) {
			  int from = getVentureProjectIndex(ventureProjects, prerequisite[0]);
			  int to = getVentureProjectIndex(ventureProjects, prerequisite[1]);
			  graph.addEdge(from + 1, to + 1, 1);
		}

		String[] projectNames = new String[n];
		for (int i = 0; i < n; i++) {
			projectNames[i] = String.valueOf(ventureProjects[i]);
		}

		Map<String, List<String>> prerequisitesList = new HashMap<>();
		for (int i = 0; i < n; i++) {
			String project = String.valueOf(prerequisites[i][0]);
			String dependency = String.valueOf(prerequisites[i][1]);
			if (!prerequisitesList.containsKey(project)) {
				prerequisitesList.put(project, new ArrayList<>());
			}
			prerequisitesList.get(project).add(dependency);
		}

		MaximumFlow maximumFlow = new MaximumFlow(graph, 0, n + 1);
		maximumFlow.getMaximumFlow(n,outcomes,projectNames,prerequisitesList);
	}
}