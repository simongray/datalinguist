(ns dk.simongray.datalinguist.semgraph
  "Functions dealing with dependency grammar graphs, AKA Semantic Graphs.

  CoreNLP contains some duplicate field and method names, e.g. governor is
  the same as source. This namespace only retains a single name for these terms.

  Some easily replicated convenience function cruft has also not been retained:
    - matchPatternToVertex
    - variations on basic graph functionality, e.g. getChildList
    - isNegatedVerb, isNegatedVertex, isInConditionalContext, etc.
    - getSubgraphVertices, yield seem equal in functionality to descendants

  Nor have any useless utility functions that are easily replicated:
    - toRecoveredSentenceString and the like
    - empty, size
    - sorting methods; just use Clojure sort, e.g. (sort (vertices g))

  The methods in SemanticGraphUtils are mostly meant for internal consumption,
  though a few are useful enough to warrant wrapping here, e.g. subgraph.

  Additionally, any mutating functions have deliberately not been wrapped!"
  (:require [clojure.string :as str]
            [loom.graph :refer [Graph Digraph Edge]]
            [loom.attr :refer [AttrGraph]]                  ; TODO: why is this here?)
            [dk.simongray.datalinguist.static :as static])
  (:refer-clojure :exclude [parents descendants])
  (:import [java.util Collection]
           [edu.stanford.nlp.ling IndexedWord]
           [edu.stanford.nlp.util Pair]
           [edu.stanford.nlp.trees TypedDependency
                                   GrammaticalRelation]
           [edu.stanford.nlp.semgraph SemanticGraph
                                      SemanticGraph$OutputFormat
                                      SemanticGraphEdge
                                      SemanticGraphFormatter
                                      SemanticGraphUtils]
           [edu.stanford.nlp.international Language]))

(defn governor
  "The governor (= source) of the relation represented by `edge`."
  [^SemanticGraphEdge edge]
  (.getSource edge))

(defn dependent
  "The dependent (= target) of the relation represented by `edge`."
  [^SemanticGraphEdge edge]
  (.getTarget edge))

(defn relation
  "The grammatical relation labeling an `edge` in a dependency graph.
  The optional `style` can be either :long or :short (default)."
  ([style ^SemanticGraphEdge edge]
   (case style
     :long (.getLongName (.getRelation edge))
     :short (.getShortName (.getRelation edge))))
  ([^SemanticGraphEdge edge]
   (relation :short edge)))

(defn weight
  "A score or weight attached to the `edge` (not often used)."
  [^SemanticGraphEdge edge]
  (.getWeight edge))

;; TODO: reconsider inclusion of this meaningless function
(defn extra?
  "Whether or not the dependency that this `edge` represents was 'extra'."
  [^SemanticGraphEdge edge]
  (.isExtra edge))

(defn nth-vertex
  "The vertex at index `n` in dependency graph `g`; `not-found` is optional.

  Note: indexes start at 1 in the SemanticGraph class, but this function
  respects the regular Clojure semantics and starts counting at 0."
  ([^SemanticGraph g n]
   (or (.getNodeByIndexSafe g (inc n))
       (throw (IndexOutOfBoundsException. (int n)))))
  ([^SemanticGraph g n not-found]
   (or (.getNodeByIndexSafe g (inc n))
       not-found)))

(defn vertices
  "The vertices of dependency graph `g`."
  [^SemanticGraph g]
  (.vertexSet g))

(defn edges
  "The edges of dependency graph `g`."
  [^SemanticGraph g]
  (seq (.edgeIterable g)))

(defn contains-vertex?
  "True if dependency graph `g` contains `vertex`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.containsVertex g vertex))

(defn contains-edge?
  "True if dependency graph `g` contains `edge`, or `governor` + `dependent`."
  ([^SemanticGraph g ^IndexedWord governor ^IndexedWord dependent]
   (.containsEdge g governor dependent))
  ([^SemanticGraph g ^SemanticGraphEdge edge]
   (.containsEdge g edge)))

(defn parent
  "The syntactic parent of `vertex` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.getParent g vertex))

(defn parents
  "The parents of `vertex` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.getParents g vertex))

(defn children
  "The children of `vertex` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.getChildren g vertex))

;; TODO: any difference between yield, subgraph vertices and descendants??
(defn descendants
  "The descendants of the `vertex` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.descendants g vertex))

(defn siblings
  "The siblings of the `vertex` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.getSiblings g vertex))

(defn common-ancestor
  [^SemanticGraph g ^IndexedWord vertex1 ^IndexedWord vertex2]
  (.getCommonAncestor g vertex1 vertex2))

(defn ancestor?
  "True if `vertex` is the ancestor of `child` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord child ^IndexedWord vertex]
  (.isAncestor g child vertex))

(defn out-degree
  "The number of outgoing edges of `vertex` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.outDegree g vertex))

(defn in-degree
  "The number of incoming edges of `vertex` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.inDegree g vertex))

(defn outgoing-edges
  "The outgoing edges of `vertex` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.outgoingEdgeList g vertex))

(defn incoming-edges
  "The incoming edges of `vertex` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.incomingEdgeList g vertex))

(defn root
  "The root vertex of a dependency dependency graph `g`."
  [^SemanticGraph g]
  (.getFirstRoot g))

(defn acyclic?
  "True if the dependency graph `g` or subgraph at `vertex` contains no cycles."
  ([^SemanticGraph g]
   (.isDag g))
  ([^SemanticGraph g ^IndexedWord vertex]
   (.isDag g vertex)))

(defn topological-sort
  "The topologically sorted list of all vertices in dependency graph `g`."
  [^SemanticGraph g]
  (.topologicalSort g))

(defn span
  "The span of the subtree yield of this `vertex` in dependency graph `g`.
  Returns a zero-indexed pair of integers where end is exclusive."
  [^SemanticGraph g ^IndexedWord vertex]
  (let [^Pair pair (.yieldSpan g vertex)]
    [(.first pair) (.second pair)]))

(defn path
  "The shortest path `from` vertex and `to` vertex in dependency graph `g`;
  `style` can optionally be :directed (default) or :undirected."
  ([style ^SemanticGraph g ^IndexedWord from ^IndexedWord to]
   (case style
     :directed (.getShortestDirectedPathNodes g from to)
     :undirected (.getShortestUndirectedPathNodes g from to)))
  ([^SemanticGraph g ^IndexedWord from ^IndexedWord to]
   (path :directed g from to)))

(defn path-to-root
  "Find the path from the given `vertex` to the root of dependency graph `g`."
  [^SemanticGraph g ^IndexedWord vertex]
  (.getPathToRoot g vertex))

(defn subgraph
  "Create a subgraph of dependency graph `g` from the chosen `root`."
  [^SemanticGraph g ^IndexedWord root]
  (SemanticGraphUtils/makeGraphFromNodes (descendants g root) g))

(defn parse
  "Create a dependency graph from a string `s` using the compact string format.
  Assumes English by default, but another supported `language` may be specified
  as either a keyword/string or using the CoreNLP Language enum.

  Example string: [ate subj>Bill dobj>[muffins compound>blueberry]]"
  ([^String s]
   (SemanticGraph/valueOf s))
  ([^String s language]
   (let [language* (if (instance? Language language)
                     language
                     (Language/valueOf (str/capitalize (name language))))]
     (SemanticGraph/valueOf s language*))))

(defn tree?
  "True if the dependency graph `g` is a tree."
  [^SemanticGraph g]
  (SemanticGraphUtils/isTree g))

(defn formatted-string
  "Format dependency graph `g`, optionally according to a specified `style`.

  The style can be a SemanticGraphFormatter or one of:
    :xml, :list, :readable, :recursive, :pos, :compact, :compact-pos, or :dot."
  ([^SemanticGraph g]
   (.toFormattedString g))
  ([style ^SemanticGraph g & {:keys [graph-name label-format]
                              :or   {graph-name   ""
                                     label-format :value-tag-index}}]
   (if (keyword? style)
     (case style
       :xml (.toString g SemanticGraph$OutputFormat/XML)
       :list (.toString g SemanticGraph$OutputFormat/LIST)
       :readable (.toString g SemanticGraph$OutputFormat/READABLE)
       :recursive (.toString g SemanticGraph$OutputFormat/RECURSIVE)
       :pos (.toPOSList g)
       :compact (.toCompactString g)
       :compact-pos (.toCompactString g true)
       :dot (.toDotFormat g graph-name (static/corelabel-formats label-format))
       :default (.toFormattedString g))
     (.toFormattedString g ^SemanticGraphFormatter style))))

;; TODO: investigate whether this is still the case
;; Necessary for certain loom functionality
(defn loom-digraph
  "Create a loom Digraph from dependency graph `g`."
  [^SemanticGraph g]
  (let [vertex-set      (vertices g)
        vertex+children #(list % (children g %))]
    (loom.graph/digraph (apply hash-map (mapcat vertex+children vertex-set)))))

(defn- flip
  "Flip the governor and dependent of a TypedDependency `td`."
  [^TypedDependency td]
  ;; ROOT isn't a real vertex, it is just used to mark the root vertex
  (if (= (.reln td) (GrammaticalRelation/ROOT))
    (TypedDependency. (.reln td) (.gov td) (.dep td))
    (TypedDependency. (.reln td) (.dep td) (.gov td))))

;; The following implementations of loom protocols are only strictly necessary
;; for supporting loom algorithms on SemanticGraphs. Unfortunately loom
;; sometimes implicitly treats edges as [n1 n2] vectors, so loom functionality
;; that depends on these constraints require conversion using loom-digraph.
(extend-type SemanticGraphEdge
  Edge
  (src [edge] (governor edge))
  (dest [edge] (dependent edge)))

(extend-type SemanticGraph
  Graph
  (nodes [g] (vertices g))
  (edges [g] (edges g))
  (has-node? [g node] (contains-vertex? g node))
  (has-edge? [g n1 n2] (contains-edge? g n1 n2))
  (successors* [g node] (children g node))
  (out-degree [g node] (out-degree g node))
  (out-edges [g node] (outgoing-edges g node))

  Digraph
  (predecessors* [g node] (parents g node))
  (in-degree [g node] (in-degree g node))
  (in-edges [g node] (incoming-edges g node))
  (transpose [g] (SemanticGraph. ^Collection (map flip (.typedDependencies g)))))
