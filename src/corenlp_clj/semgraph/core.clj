(ns corenlp-clj.semgraph.core
  (:require [loom.graph :refer [Graph Digraph Edge]]
            [loom.attr :refer [AttrGraph]])
  (:import [edu.stanford.nlp.semgraph SemanticGraph SemanticGraphEdge SemanticGraph$OutputFormat SemanticGraphFormatter]
           [edu.stanford.nlp.trees TypedDependency GrammaticalRelation]
           [edu.stanford.nlp.ling IndexedWord CoreLabel$OutputFormat]
           [edu.stanford.nlp.util Pair]
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
;;;;     * obvious cruft:
;;;;         - matchPatternToVertex
;;;;         - variations on basic graph functionality, e.g. getChildList
;;;;         - isNegatedVerb, isNegatedVertex, isInConditionalContext, etc
;;;;         - getSubgraphVertices is equal in functionality to descendants
;;;;     * useless utility functions, easily replicated:
;;;;         - toRecoveredSentenceString and the like
;;;;         - empty, size
;;;;         - sorting methods; just use Clojure sort, e.g. (sort (nodes g))
;;;;         - descendants; use loom, e.g. (loom.alg/pre-traverse g node)
;;;;
;;;; Properties of Pair, IndexedWord, TypedDependency and GrammaticalRelation left unimplemented:
;;;;     * everything! they seem mostly just for internal use
;;;;     * IndexedWord is simply a wrapper class for CoreLabel
;;;;
;;;; TODO: implement useful parts of SemanticGraphUtils

(extend-type SemanticGraphEdge
  Edge
  (src [edge] (.getSource edge))
  (dest [edge] (.getTarget edge)))

(defn reln
  "The grammatical relation labeling an edge in a dependency graph.
  Style can be :long or :short; defaults to :short."
  ([style ^SemanticGraphEdge edge]
   (case style
     :long (.getLongName (.getRelation edge))
     :short (.getShortName (.getRelation edge))))
  ([^SemanticGraphEdge edge]
   (reln :short edge)))

(defn- flip
  "Returns a TypedDependency with governor and dependent flipped."
  [^TypedDependency td]
  ;; ROOT isn't a real node, it is just used to mark the root node
  (if (= (.reln td) (GrammaticalRelation/ROOT))
    (TypedDependency. (.reln td) (.gov td) (.dep td))
    (TypedDependency. (.reln td) (.dep td) (.gov td))))

;; Note: unfortunately loom sometimes implicitly treats edges as [n1 n2] vectors.
;; Loom functionality that depends on these implicit constraints require conversion using loom-digraph.
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

(defn acyclic?
  "True if the graph (or subgraph at node) contains no cycles."
  ([^SemanticGraph g]
   (.isDag g))
  ([^SemanticGraph g ^IndexedWord node]
   (.isDag g node)))

(defn span
  "The span of the subtree yield of this node represented as a pair of integers.
  The span is zero indexed. The begin is inclusive and the end is exclusive."
  [^SemanticGraph g ^IndexedWord node]
  (let [^Pair pair (.yieldSpan g node)]
    [(.first pair) (.second pair)]))

(defn siblings
  "Returns the other children of the node's parent."
  [^SemanticGraph g ^IndexedWord node]
  (.getSiblings g node))

(defn path-to-root
  "Find the path from the given node to the root."
  [^SemanticGraph g ^IndexedWord node]
  (.getPathToRoot g node))

(defn parse
  "Create a SemanticGraph from a string using the compact string format.
   Example: [ate subj>Bill dobj>[muffins compound>blueberry]]"
  [^String s]
  (SemanticGraph/valueOf s))

;; From the CoreLabel class - may move somewhere else in the future.
;; As per the messy conventions of Stanford CoreNLP, word = value.
(def corelabel-formats
  {:all CoreLabel$OutputFormat/ALL
   :lemma-index CoreLabel$OutputFormat/LEMMA_INDEX
   :map CoreLabel$OutputFormat/MAP
   :value CoreLabel$OutputFormat/VALUE
   :value-index CoreLabel$OutputFormat/VALUE_INDEX
   :value-index-map CoreLabel$OutputFormat/VALUE_INDEX_MAP
   :value-map CoreLabel$OutputFormat/VALUE_MAP
   :value-tag CoreLabel$OutputFormat/VALUE_TAG
   :value-tag-index CoreLabel$OutputFormat/VALUE_TAG_INDEX
   :value-tag-ner CoreLabel$OutputFormat/VALUE_TAG_NER
   :word CoreLabel$OutputFormat/WORD
   :word-index CoreLabel$OutputFormat/WORD_INDEX})

(defn formatted-string
  "Format according to style or SemanticGraphFormatter; otherwise uses default formatting."
  ([^SemanticGraph g]
   (.toFormattedString g))
  ([style ^SemanticGraph g & {:keys [graph-name label-format]
                              :or {graph-name ""
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

(defn loom-digraph
  "Converts a SemanticGraph into a loom Digraph; necessary for certain loom functionality."
  [^SemanticGraph g]
  (let [node-set (loom.graph/nodes g)
        get-children #(list % (loom.graph/successors* g %))]
    (loom.graph/digraph (apply hash-map (mapcat get-children node-set)))))
