(ns ^{:doc "Fns dealing with dependency grammar"} computerese.semgraph.core
  (:require [loom.graph :refer [Graph Digraph Edge]]
            [loom.attr :refer [AttrGraph]])
  (:import [java.util Collection]
           [edu.stanford.nlp.ling IndexedWord
                                  CoreLabel$OutputFormat]
           [edu.stanford.nlp.util Pair]
           [edu.stanford.nlp.trees TypedDependency
                                   GrammaticalRelation]
           [edu.stanford.nlp.semgraph SemanticGraph
                                      SemanticGraphEdge
                                      SemanticGraph$OutputFormat
                                      SemanticGraphFormatter]))

;; TODO: implement useful parts of SemanticGraphUtils

;; This namespace contains functions relevant for dependency grammar.
;; Mutating functions have deliberately not been re-implemented.
;;
;; Properties of SemanticGraphEdge that were left unimplemented:
;;     * weight: seems like it isn't used at all
;;     * extra: it's only used internally in some Util function
;;     * duplicated functions, e.g. only governor implemented, not source
;;
;; Properties of SemanticGraph that were left unimplemented:
;;     * obvious cruft:
;;         - matchPatternToVertex
;;         - variations on basic graph functionality, e.g. getChildList
;;         - isNegatedVerb, isNegatedVertex, isInConditionalContext, etc
;;         - getSubgraphVertices is equal in functionality to descendants
;;     * useless utility functions, easily replicated:
;;         - toRecoveredSentenceString and the like
;;         - empty, size
;;         - sorting methods; just use Clojure sort, e.g. (sort (vertices g))
;;         - descendants; use loom, e.g. (loom.alg/pre-traverse g vertex)
;;
;; Properties of Pair, IndexedWord, TypedDependency and GrammaticalRelation
;; left unimplemented:
;;     * everything! they seem mostly just for internal use
;;     * IndexedWord is simply a wrapper class for CoreLabel

(defn governor
  "The governor (= source) of the relation represented by edge."
  [^SemanticGraphEdge edge]
  (.getSource edge))

(defn dependent
  "The dependent (= target) of the relation represented by edge."
  [^SemanticGraphEdge edge]
  (.getTarget edge))

(defn relation
  "The grammatical relation labeling an edge in a dependency graph.
  Style can be :long or :short (default)."
  ([style ^SemanticGraphEdge edge]
   (case style
     :long (.getLongName (.getRelation edge))
     :short (.getShortName (.getRelation edge))))
  ([^SemanticGraphEdge edge]
   (relation :short edge)))

(defn vertices
  "The vertices of dependency graph g."
  [^SemanticGraph g]
  (.vertexSet g))

(defn edges
  "The edges of dependency graph g."
  [^SemanticGraph g]
  (seq (.edgeIterable g)))

(defn contains-vertex?
  "True if dependency graph g contains vertex."
  [^SemanticGraph g ^IndexedWord vertex]
  (.containsVertex g vertex))

(defn contains-edge?
  "True if dependency graph g contains edge."
  ([^SemanticGraph g ^IndexedWord governor ^IndexedWord dependent]
   (.containsEdge g governor dependent))
  ([^SemanticGraph g ^SemanticGraphEdge edge]
   (.containsEdge g edge)))

(defn children
  "The children of vertex in dependency graph g."
  [^SemanticGraph g ^IndexedWord vertex]
  (.getChildren g vertex))

(defn parents
  "The parents of vertex in dependency graph g."
  [^SemanticGraph g ^IndexedWord vertex]
  (.getParents g vertex))

(defn siblings
  "The siblings of the vertex in dependency graph g."
  [^SemanticGraph g ^IndexedWord vertex]
  (.getSiblings g vertex))

(defn out-degree
  "The number of outgoing edges of vertex in dependency graph g."
  [^SemanticGraph g ^IndexedWord vertex]
  (.outDegree g vertex))

(defn in-degree
  "The number of incoming edges of vertex in dependency graph g."
  [^SemanticGraph g ^IndexedWord vertex]
  (.inDegree g vertex))

(defn outgoing-edges
  "The outgoing edges of vertex in dependency graph g."
  [^SemanticGraph g ^IndexedWord vertex]
  (.outgoingEdgeList g vertex))

(defn incoming-edges
  "The incoming edges of vertex in dependency graph g."
  [^SemanticGraph g ^IndexedWord vertex]
  (.incomingEdgeList g vertex))

(defn root
  "The root vertex of a dependency dependency graph."
  [^SemanticGraph g]
  (.getFirstRoot g))

(defn acyclic?
  "True if the dependency graph (or subgraph at vertex) contains no cycles."
  ([^SemanticGraph g]
   (.isDag g))
  ([^SemanticGraph g ^IndexedWord vertex]
   (.isDag g vertex)))

(defn span
  "The span of the subtree yield of this vertex in dependency graph g.
  Returns a zero-indexed pair of integers where end is exclusive."
  [^SemanticGraph g ^IndexedWord vertex]
  (let [^Pair pair (.yieldSpan g vertex)]
    [(.first pair) (.second pair)]))

(defn path-to-root
  "Find the path from the given vertex to the root of dependency graph g."
  [^SemanticGraph g ^IndexedWord vertex]
  (.getPathToRoot g vertex))

(defn parse
  "Create a dependency graph from a string using the compact string format.
   Example: [ate subj>Bill dobj>[muffins compound>blueberry]]"
  [^String s]
  (SemanticGraph/valueOf s))

;; From the CoreLabel class - may move somewhere else in the future.
;; As per the messy conventions of Stanford CoreNLP, word = value.
(def corelabel-formats
  {:all             CoreLabel$OutputFormat/ALL
   :lemma-index     CoreLabel$OutputFormat/LEMMA_INDEX
   :map             CoreLabel$OutputFormat/MAP
   :value           CoreLabel$OutputFormat/VALUE
   :value-index     CoreLabel$OutputFormat/VALUE_INDEX
   :value-index-map CoreLabel$OutputFormat/VALUE_INDEX_MAP
   :value-map       CoreLabel$OutputFormat/VALUE_MAP
   :value-tag       CoreLabel$OutputFormat/VALUE_TAG
   :value-tag-index CoreLabel$OutputFormat/VALUE_TAG_INDEX
   :value-tag-ner   CoreLabel$OutputFormat/VALUE_TAG_NER
   :word            CoreLabel$OutputFormat/WORD
   :word-index      CoreLabel$OutputFormat/WORD_INDEX})

(defn formatted-string
  "Format dependency graph g according to style; otherwise use default format.
  Style can be a SemanticGraphFormatter or one of: :xml, :list, :readable,
  :recursive, :pos, :compact, :compact-pos, or :dot."
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
       :dot (.toDotFormat g graph-name (corelabel-formats label-format))
       :default (.toFormattedString g))
     (.toFormattedString g ^SemanticGraphFormatter style))))

;; Necessary for certain loom functionality
(defn loom-digraph
  "Create a loom Digraph from dependency graph g."
  [^SemanticGraph g]
  (let [vertex-set      (vertices g)
        vertex+children #(list % (children g %))]
    (loom.graph/digraph (apply hash-map (mapcat vertex+children vertex-set)))))

(defn- flip
  "Flip the governor and dependent of a TypedDependency td."
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
