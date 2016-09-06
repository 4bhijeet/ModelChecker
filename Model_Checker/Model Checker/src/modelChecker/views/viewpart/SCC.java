package modelChecker.views.viewpart;
/**
 ** Java Program to Implement Kosaraju Algorithm
 *http://www.sanfoundry.com/java-program-kosaraju-algorithm/
 **/
 
import java.util.*;
 
/** class Kosaraju **/
public class SCC
{
    /** DFS function **/
    public static void DFS(List<Integer>[] graph, int v, boolean[] visited, List<Integer> comp) 
    {
        visited[v] = true;
        for (int i = 0; i < graph[v].size(); i++)
            if (!visited[graph[v].get(i)])
                DFS(graph, graph[v].get(i), visited, comp);
        comp.add(v);
    }
    /** function fill order **/
    public static List<Integer> fillOrder(List<Integer>[] graph, boolean[] visited) 
    {        
        int V = graph.length;
        List<Integer> order = new ArrayList<Integer>();
 
        for (int i = 0; i < V; i++)
            if (!visited[i])
                DFS(graph, i, visited, order);
        return order;
    }
    /** function to get transpose of graph **/
    public static List<Integer>[] getTranspose(List<Integer>[] graph)
    {
        int V = graph.length;
        List<Integer>[] g = new List[V];
        for (int i = 0; i < V; i++)
            g[i] = new ArrayList<Integer>();
        for (int v = 0; v < V; v++)
            for (int i = 0; i < graph[v].size(); i++)
                g[graph[v].get(i)].add(v);
        return g;
    }
    /** function to get all SCC **/
    public static ArrayList<Integer> getSCComponents(List<Integer>[] graph)
    {
        int V = graph.length;
        boolean[] visited = new boolean[V];
        List<Integer> order = fillOrder(graph, visited);       
        /** get transpose of the graph **/
        List<Integer>[] reverseGraph = getTranspose(graph);        
        /** clear visited array **/
        visited = new boolean[V];
        /** reverse order or alternatively use a stack for order **/
        Collections.reverse(order);
 
        /** get all scc **/
        ArrayList<Integer> SCComp = new ArrayList();
        for (int i = 0; i < order.size(); i++)
        {
            /** if stack is used for order pop out elements **/
            int v = order.get(i);
            if (!visited[v]) 
            {
                List<Integer> comp = new ArrayList<>();
                DFS(reverseGraph, v, visited, comp);
                if(comp.size()>1) SCComp.addAll(comp);
                else if(graph[i].contains(new Integer(i))) SCComp.addAll(comp);
            }
        }
        return SCComp;
    }
        
}