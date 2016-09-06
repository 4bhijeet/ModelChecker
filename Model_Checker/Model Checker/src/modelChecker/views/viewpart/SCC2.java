package modelChecker.views.viewpart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
//https://sites.google.com/site/indy256/algo/strongly_connected_components
public class SCC2 {
	public static ArrayList<Integer> scc(List<Integer>[] graph) {
	    int n = graph.length;
	    boolean[] used = new boolean[n];
	    List<Integer> order = new ArrayList<>();
	    for (int i = 0; i < n; i++)
	      if (!used[i])
	        dfs(graph, used, order, i);

	    List<Integer>[] reverseGraph = new List[n];
	    for (int i = 0; i < n; i++)
	      reverseGraph[i] = new ArrayList<>();
	    for (int i = 0; i < n; i++)
	      for (int j : graph[i])
	        reverseGraph[j].add(i);

	    //List<List<Integer>> components = new ArrayList<>();
	    HashSet<Integer> components = new HashSet<>();
	    Arrays.fill(used, false);
	    Collections.reverse(order);

	    for (int u : order)
	      if (!used[u]) {
	        List<Integer> component = new ArrayList<>();
	        dfs(reverseGraph, used, component, u);
	        if(component.size()>1) components.addAll(component);
            else if(graph[u].contains(new Integer(u))) components.addAll(component);;
	      }
	    ArrayList<Integer> result = new ArrayList(components);
	    return result;
	  }

	  static void dfs(List<Integer>[] graph, boolean[] used, List<Integer> res, int u) {
	    used[u] = true;
	    for (int v : graph[u])
	      if (!used[v])
	        dfs(graph, used, res, v);
	    res.add(u);
	  }
}
