(ns corenlp-clj.semgraph
  (:require [loom.graph :refer [Graph Digraph]])
  (:import [edu.stanford.nlp.semgraph SemanticGraph SemanticGraphEdge]
           [edu.stanford.nlp.trees TypedDependency GrammaticalRelation]
           [java.util Collection]))

(defn- flip
  "Returns a TypedDependency with governor and dependent flipped."
  [^TypedDependency td]
  ;; the ROOT node isn't a real node and should not be flipped
  (if (= (.reln td) (GrammaticalRelation/ROOT))
    (TypedDependency. (.reln td) (.gov td) (.dep td))
    (TypedDependency. (.reln td) (.dep td) (.gov td))))

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
