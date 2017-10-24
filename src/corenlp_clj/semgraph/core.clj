(ns corenlp-clj.semgraph.core
  (:require [loom.graph :refer [Graph Digraph Edge]]
            [loom.attr :refer [AttrGraph]])
  (:import [edu.stanford.nlp.semgraph SemanticGraph SemanticGraphEdge]
           [edu.stanford.nlp.trees TypedDependency GrammaticalRelation]
           [edu.stanford.nlp.ling IndexedWord]
           [java.util Collection]))

;;;; This namespace contains functions relevant for dependency grammar:
;;;;     * graphs (SemanticGraph)
;;;;     * nodes (IndexedWord)
;;;;     * edges (SemanticGraphEdge)
;;;;
;;;; Mutating functions have deliberately not been implemented.
;;;;
;;;; Properties of SemanticGraphEdge that were left unimplemented:
;;;;     * weight: seems like it isn't used at all
;;;;     * extra: it's only used internally in some Util function
;;;;
;;;; Properties of SemanticGraph that were left unimplemented:
;;;;     * toDotFormat: could make better use of formatting functionality
;;;;     * a lot! some of it is cruft, but will need to determine on a case by case basis
;;;;
;;;; Properties of IndexedWord, TypedDependency and GrammaticalRelation left unimplemented:
;;;;     * everything! they seem mostly just for internal use
;;;;     * IndexedWord is simply a wrapper class for CoreLabel

(extend-type SemanticGraphEdge
  Edge
  (src [edge] (.getSource edge))
  (dest [edge] (.getTarget edge)))

(defn reln
  "The grammatical relation labeling an edge in a dependency graph."
  ([long-or-short ^SemanticGraphEdge edge]
   (cond
     (= :long long-or-short) (.getLongName (.getRelation edge))
     (= :short long-or-short) (.getShortName (.getRelation edge))
     :else (throw (IllegalArgumentException. "long-or-short must be :long or :short"))))
  ([^SemanticGraphEdge edge]
   (reln :long edge)))

(defn- flip
  "Returns a TypedDependency with governor and dependent flipped."
  [^TypedDependency td]
  ;; ROOT isn't a real node, it is just used to mark the root node
  (if (= (.reln td) (GrammaticalRelation/ROOT))
    (TypedDependency. (.reln td) (.gov td) (.dep td))
    (TypedDependency. (.reln td) (.dep td) (.gov td))))

;; Note: unfortunately loom sometimes implicitly treats edges as [n1 n2] vectors.
;; Loom functionality that depends on these implicit constraints is still unavailable.
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

(defn root
  "The root node of a dependency graph (SemanticGraph)."
  [^SemanticGraph g]
  (.getFirstRoot g))

(defn dot-format
  "The GraphViz dot-format of a dependency graph (SemanticGraph)."
  [^SemanticGraph g]
  (.toDotFormat g))

(defn dag?
  "True if the graph (or subgraph at w) contains no cycles."
  ([^SemanticGraph g]
   (.isDag g))
  ([^SemanticGraph g ^IndexedWord w
    (.isDag g w)]))
