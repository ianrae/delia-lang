package org.delia.sort.topo;

/******************************************************************************
* File: TopologicalSort.java
* Author: Keith Schwarz (htiek@cs.stanford.edu)
*
* A linear-time algorithm for computing a topological sort of a directed
* acyclic graph.  A topological sort is an ordering of the nodes in a graph
* such that for each node v, all of the ancestors of v appear in the ordering
* before v itself.  Topological sorting is useful, for example, when computing
* some function on a DAG where each node's value depends on its ancestors.
* Running a topological sort and then visiting the nodes in the order
* specified by this sorted order ensures that the necessary values for each
* node are available before the node is visited.
*
* There are several algorithms for computing topological sorts.  The one used
* here was first described in "Edge-Disjoint Spanning Trees and Depth-First
* Search" by Robert Tarjan.  The algorithm is reminiscent of Kosaraju's SCC
* algorithm.  We begin by constructing the reverse graph G^{rev} from the
* source graph, then running a depth-first search from each node in the graph.
* Whenever we finish expanding a node, we add it to a list of visited nodes.
* The intution behind this algorithm is that a DFS in the reverse graph will
* visit every node that is an ancestor of the given node before it finishes
* expanding out any node.  Since those nodes will be added to the sorted order
* before the expanded node, we have the desired property of the topological
* sort.
*
* This process can be augmented to detect a cycle in the original graph.  As
* we do the search, we'll maintain a set of nodes that we have visited and a
* set of nodes that we have expanded.  If when doing the DFS we find a node
* that has been visited but not expanded, it means that we have encountered a
* cycle in the graph.  Moreover, if a cycle exists, we know that this will
* occur, since the first time any node in the cycle is visited the DFS will
* expand out the cycle.
*/

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopologicalSort {

    /**
    * Given a directed acyclic graph, returns a topological sorting of the
    * nodes in the graph.  If the input graph is not a DAG, throws an
    * IllegalArgumentException.
    *
    * @param g A directed acyclic graph.
    * @return A topological sort of that graph.
    * @throws IllegalArgumentException If the graph is not a DAG.
    */
   public static <T> List<T> sort(DirectedGraph<T> g) {
       /* Construct the reverse graph from the input graph. */
       DirectedGraph<T> gRev = reverseGraph(g);

       /* Maintain two structures - a set of visited nodes (so that once we've
        * added a node to the list, we don't label it again), and a list of
        * nodes that actually holds the topological ordering.
        */
       List<T> result = new ArrayList<T>();
       Set<T> visited = new HashSet<T>();

       /* We'll also maintain a third set consisting of all nodes that have
        * been fully expanded.  If the graph contains a cycle, then we can
        * detect this by noting that a node has been explored but not fully
        * expanded.
        */
       Set<T> expanded = new HashSet<T>();

       /* Fire off a DFS from each node in the graph. */
       for (T node: gRev)
           explore(node, gRev, result, visited, expanded);

       /* Hand back the resulting ordering. */
       return result;
   }


   /**
    * Recursively performs a DFS from the specified node, marking all nodes
    * encountered by the search.
    *
    * @param node The node to begin the search from.
    * @param g The graph in which to perform the search.
    * @param ordering A list holding the topological sort of the graph.
    * @param visited A set of nodes that have already been visited.
    * @param expanded A set of nodes that have been fully expanded.
    */
   private static <T> void explore(T node, DirectedGraph<T> g,
                                   List<T> ordering, Set<T> visited,
                                   Set<T> expanded) {
       /* Check whether we've been here before.  If so, we should stop the
        * search.
        */
       if (visited.contains(node)) {
           /* There are two cases to consider.  First, if this node has
            * already been expanded, then it's already been assigned a
            * position in the final topological sort and we don't need to
            * explore it again.  However, if it hasn't been expanded, it means
            * that we've just found a node that is currently being explored,
            * and therefore is part of a cycle.  In that case, we should 
            * report an error.
            */
           if (expanded.contains(node)) return;
           throw new IllegalArgumentException("Graph contains a cycle.");
       }
       
       /* Mark that we've been here */
       visited.add(node);

       /* Recursively explore all of the node's predecessors. */
       for (T predecessor: g.edgesFrom(node))
           explore(predecessor, g, ordering, visited, expanded);

       /* Having explored all of the node's predecessors, we can now add this
        * node to the sorted ordering.
        */
       ordering.add(node);

       /* Similarly, mark that this node is done being expanded. */
       expanded.add(node);
   }

   /**
    * Returns the reverse of the input graph.
    *
    * @param g A graph to reverse.
    * @return The reverse of that graph.
    */
   private static <T> DirectedGraph<T> reverseGraph(DirectedGraph<T> g) {
       DirectedGraph<T> result = new DirectedGraph<T>();

       /* Add all the nodes from the original graph. */
       for (T node: g)
           result.addNode(node);

       /* Scan over all the edges in the graph, adding their reverse to the
        * reverse graph.
        */
       for (T node: g)
           for (T endpoint: g.edgesFrom(node))
               result.addEdge(endpoint, node);

       return result;
   }
}

