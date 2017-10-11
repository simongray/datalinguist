(ns corenlp-clj.semgraph
  (:require [loom.graph :refer [Graph Digraph Edge]]
            [loom.attr :refer [AttrGraph]]
            [clojure.java.shell :refer [sh]])
  (:import [edu.stanford.nlp.semgraph SemanticGraph SemanticGraphEdge]
           [edu.stanford.nlp.trees TypedDependency GrammaticalRelation]
           [edu.stanford.nlp.ling IndexedWord]
           [java.util Collection]))

(defn root
  "The root node of a dependency graph (SemanticGraph)."
  [^SemanticGraph g]
  (.getFirstRoot g))

;; Unfortunately, loom.io/dot-str implicitly requires edges represented as vectors,
;; so it cannot be used to create dot-formats for a SemanticGraph object
(defn dot-format
  "The GraphViz dot-format of a dependency graph (SemanticGraph)."
  [^SemanticGraph g]
  (.toDotFormat g))

;; Re-implementation of loom.io/render-to-bytes using dot-format instead of loom.io/dot-str.
(defn render-to-bytes
  "Renders the graph g in the image format using GraphViz and returns data
  as a byte array.
  Requires GraphViz's 'dot' (or a specified algorithm) to be installed in
  the shell's path. Possible algorithms include :dot, :neato, :fdp, :sfdp,
  :twopi, and :circo. Possible formats include :png, :ps, :pdf, and :svg."
  [g & {:keys [alg fmt] :or {alg "dot" fmt :png} :as opts}]
  (let [dot-graph (dot-format g)
        cmd (sh (name alg) (str "-T" (name fmt)) :in dot-graph :out-enc :bytes)]
    (:out cmd)))

;; Re-implementation of loom.io/view using dot-format instead of loom.io/dot-strr.
(defn view
  "Converts graph g to a temporary image file using GraphViz and opens it
  in the current desktop environment's default viewer for said files.
  Requires GraphViz's 'dot' (or a specified algorithm) to be installed in
  the shell's path. Possible algorithms include :dot, :neato, :fdp, :sfdp,
  :twopi, and :circo. Possible formats include :png, :ps, :pdf, and :svg."
  [g & {:keys [fmt] :or {fmt :png} :as opts}]
  (#'loom.io/open-data (apply render-to-bytes g opts) fmt)) ; using #' since function is private

;; Note: src and dest are implemented further down as implementations of loom.graph/Edge.
(defn reln
  "The grammatical relation labeling an edge (SemanticGraphEdge) in a dependency graph."
  ([long-or-short ^SemanticGraphEdge edge]
   (cond
     (= :long long-or-short) (.getLongName (.getRelation edge))
     (= :short long-or-short) (.getShortName (.getRelation edge))
     :else (throw (IllegalArgumentException. "long-or-short must be :long or :short"))))
  ([^SemanticGraphEdge edge]
   (reln :long edge)))

(defn word
  "The word represented by a node (IndexedWord) in a dependency graph."
  [^IndexedWord iword]
  (.word iword))

(defn- flip
  "Returns a TypedDependency with governor and dependent flipped."
  [^TypedDependency td]
  ;; ROOT isn't a real node, it is just used to mark the root node
  (if (= (.reln td) (GrammaticalRelation/ROOT))
    (TypedDependency. (.reln td) (.gov td) (.dep td))
    (TypedDependency. (.reln td) (.dep td) (.gov td))))

;; SemanticGraph represents a dependency graph in Stanford CoreNLP
;; extended here to be compatible with the graph functions in Loom.
;; Note: unfortunately loom.attr/AttrGraph implicitly treats edges as [n1 n2] vectors,
;; and since loom.io/view depends on AttrGraph, graphs can't be displayed that way!
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
