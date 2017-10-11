(ns corenlp-clj.semgraph
  (:require [loom.graph :refer [Graph Digraph Edge]])
  (:import [edu.stanford.nlp.semgraph SemanticGraph SemanticGraphEdge]
           [edu.stanford.nlp.trees TypedDependency GrammaticalRelation]
           [java.util Collection]))

(defn- flip
  "Returns a TypedDependency with governor and dependent flipped."
  [^TypedDependency td]
  ;; note: ROOT isn't a real node, it is just used to mark the root node
  (if (= (.reln td) (GrammaticalRelation/ROOT))
    (TypedDependency. (.reln td) (.gov td) (.dep td))
    (TypedDependency. (.reln td) (.dep td) (.gov td))))

;; SemanticGraph represents a dependency graph in Stanford CoreNLP
;; extended here to be compatible with the graph functions in Loom
(extend-type SemanticGraph
  Graph
    (nodes [g] (.vertexSet g))
    (edges [g] (seq (.edgeIterable g)))
    (has-node? [g node] (contains? (.vertexSet g) node))
    (has-edge? [g n1 n2] (or (.containsEdge g n1 n2) (.containsEdge g n2 n1)))
    (successors* [g node] (.getChildren g node))
    (out-degree [g node] (.outDegree g node))
    (out-edges [g node] (.outgoingEdgeList g node))
  Digraph
    (predecessors* [g node] (.getParents g node))
    (in-degree [g node] (.inDegree g node))
    (in-edges [g node] (.incomingEdgeList g node))
    (transpose [g] (SemanticGraph. ^Collection (map flip (.typedDependencies g)))))

(extend-type SemanticGraphEdge
  Edge
    (src [edge] (.getSource edge))
    (dest [edge] (.getTarget edge)))

(defn root
  "The root node of a dependency graph."
  [^SemanticGraph g]
  (.getFirstRoot g))

(defn relation
  "The grammatical relation represented by an edge in a dependency graph."
  [^SemanticGraphEdge edge]
  (.getRelation edge))
