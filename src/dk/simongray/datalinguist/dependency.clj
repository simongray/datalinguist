(ns dk.simongray.datalinguist.dependency
  "Functions dealing with dependency grammar, i.e. the SemanticGraph class.

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
    - sorting methods; just use Clojure sort, e.g. (sort (nodes g))

  The methods in SemanticGraphUtils are mostly meant for internal consumption,
  though a few are useful enough to warrant wrapping here, e.g. subgraph.

  Functions dealing with semgrex in CoreNLP (dependency grammar patterns) have
  been wrapped so as to mimic the existing Clojure Core regex functions. The
  `sem-result` function also mimics re-groups and serves a similar purpose,
  although rather than returning groups it returns named nodes/relations defined
  in the pattern.

  Additionally, any mutating functions have deliberately not been wrapped!"
  (:require [clojure.string :as str]
            [loom.graph :refer [Graph Digraph Edge]]
            [loom.attr :refer [AttrGraph]]                  ; TODO: why is this here?)
            [dk.simongray.datalinguist.util :as util])
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
           [edu.stanford.nlp.semgraph.semgrex SemgrexPattern
                                              SemgrexMatcher]
           [edu.stanford.nlp.international Language]))

(defn source
  "The source (= governor) of the relation represented by `edge`."
  [^SemanticGraphEdge edge]
  (.getSource edge))

(defn target
  "The target (= dependent) of the relation represented by `edge`."
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

(defn nth-node
  "The node at index `n` in dependency graph `g`; `not-found` is optional.

  Note: indexes start at 1 in the SemanticGraph class, but this function
  respects the regular Clojure semantics and starts counting at 0."
  ([^SemanticGraph g n]
   (or (.getNodeByIndexSafe g (inc n))
       (throw (IndexOutOfBoundsException. (int n)))))
  ([^SemanticGraph g n not-found]
   (or (.getNodeByIndexSafe g (inc n))
       not-found)))

(defn nodes
  "The nodes of dependency graph `g`."
  [^SemanticGraph g]
  (.vertexSet g))

(defn edges
  "The edges of dependency graph `g`."
  [^SemanticGraph g]
  (seq (.edgeIterable g)))

(defn contains-node?
  "True if dependency graph `g` contains `node`."
  [^SemanticGraph g ^IndexedWord node]
  (.containsVertex g node))

(defn contains-edge?
  "True if dependency graph `g` contains `edge`, or `governor` + `dependent`."
  ([^SemanticGraph g ^IndexedWord governor ^IndexedWord dependent]
   (.containsEdge g governor dependent))
  ([^SemanticGraph g ^SemanticGraphEdge edge]
   (.containsEdge g edge)))

(defn parent
  "The syntactic parent of `node` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord node]
  (.getParent g node))

(defn parents
  "The parents of `node` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord node]
  (.getParents g node))

(defn children
  "The children of `node` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord node]
  (.getChildren g node))

;; TODO: any difference between yield, subgraph nodes and descendants??
(defn descendants
  "The descendants of the `node` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord node]
  (.descendants g node))

(defn siblings
  "The siblings of the `node` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord node]
  (.getSiblings g node))

(defn common-ancestor
  [^SemanticGraph g ^IndexedWord node1 ^IndexedWord node2]
  (.getCommonAncestor g node1 node2))

(defn ancestor?
  "True if `node` is the ancestor of `child` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord child ^IndexedWord node]
  (.isAncestor g child node))

(defn out-degree
  "The number of outgoing edges of `node` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord node]
  (.outDegree g node))

(defn in-degree
  "The number of incoming edges of `node` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord node]
  (.inDegree g node))

(defn outgoing-edges
  "The outgoing edges of `node` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord node]
  (.outgoingEdgeList g node))

(defn incoming-edges
  "The incoming edges of `node` in dependency graph `g`."
  [^SemanticGraph g ^IndexedWord node]
  (.incomingEdgeList g node))

(defn root
  "The root node of a dependency dependency graph `g`."
  [^SemanticGraph g]
  (.getFirstRoot g))

(defn acyclic?
  "True if the dependency graph `g` or subgraph at `node` contains no cycles."
  ([^SemanticGraph g]
   (.isDag g))
  ([^SemanticGraph g ^IndexedWord node]
   (.isDag g node)))

(defn topological-sort
  "The topologically sorted list of all nodes in dependency graph `g`."
  [^SemanticGraph g]
  (.topologicalSort g))

(defn span
  "The span of the subtree yield of this `node` in dependency graph `g`.
  Returns a zero-indexed pair of integers where end is exclusive."
  [^SemanticGraph g ^IndexedWord node]
  (let [^Pair pair (.yieldSpan g node)]
    [(.first pair) (.second pair)]))

(defn path
  "The shortest path `source-node` and `target-node` in dependency graph `g`;
  `style` can optionally be :directed (default) or :undirected."
  ([style ^SemanticGraph g ^IndexedWord source-node ^IndexedWord target-node]
   (case style
     :directed (.getShortestDirectedPathNodes g source-node target-node)
     :undirected (.getShortestUndirectedPathNodes g source-node target-node)))
  ([^SemanticGraph g ^IndexedWord from ^IndexedWord to]
   (path :directed g from to)))

(defn path-to-root
  "Find the path from the given `node` to the root of dependency graph `g`."
  [^SemanticGraph g ^IndexedWord node]
  (.getPathToRoot g node))

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
       :dot (.toDotFormat g graph-name (util/corelabel-formats label-format))
       :default (.toFormattedString g))
     (.toFormattedString g ^SemanticGraphFormatter style))))

;; TODO: investigate whether this is still the case
;; Necessary for certain loom functionality
(defn loom-digraph
  "Create a loom Digraph from dependency graph `g`."
  [^SemanticGraph g]
  (let [node-set      (nodes g)
        node+children #(list % (children g %))]
    (loom.graph/digraph (apply hash-map (mapcat node+children node-set)))))

(defn- flip
  "Flip the governor and dependent of a TypedDependency `td`."
  [^TypedDependency td]
  ;; ROOT isn't a real node, it is just used to mark the root node
  (if (= (.reln td) (GrammaticalRelation/ROOT))
    (TypedDependency. (.reln td) (.gov td) (.dep td))
    (TypedDependency. (.reln td) (.dep td) (.gov td))))

;; The following implementations of loom protocols are only strictly necessary
;; for supporting loom algorithms on SemanticGraphs. Unfortunately loom
;; sometimes implicitly treats edges as [n1 n2] vectors, so loom functionality
;; that depends on these constraints require conversion using loom-digraph.
(extend-type SemanticGraphEdge
  Edge
  (src [edge] (source edge))
  (dest [edge] (target edge)))

(extend-type SemanticGraph
  Graph
  (nodes [g] (nodes g))
  (edges [g] (edges g))
  (has-node? [g node] (contains-node? g node))
  (has-edge? [g n1 n2] (contains-edge? g n1 n2))
  (successors* [g node] (children g node))
  (out-degree [g node] (out-degree g node))
  (out-edges [g node] (outgoing-edges g node))

  Digraph
  (predecessors* [g node] (parents g node))
  (in-degree [g node] (in-degree g node))
  (in-edges [g node] (incoming-edges g node))
  (transpose [g] (SemanticGraph. ^Collection (map flip (.typedDependencies g)))))

(defn sem-pattern
  "Return an instance of SemgrexPattern, for use, e.g. in sem-matcher."
  [^String s]
  (SemgrexPattern/compile s))

(defn sem-matcher
  "Create a SemgrexMatcher from `s` and dependency graph `g`; use in sem-find."
  [^SemgrexPattern p ^SemanticGraph g]
  (.matcher p g))

(defn sem-result
  "Returns the named nodes and relations from the most recent match/find.
  If there are no named nodes/relations, returns the match itself.
  If there are named nodes/relations, returns a vector with the first element
  being the match itself and the second a map of names -> nodes/relations."
  [^SemgrexMatcher m]
  (let [match      (.getMatch m)
        node-names (not-empty (.getNodeNames m))
        reln-names (not-empty (.getRelationNames m))]
    (if (or node-names reln-names)
      [match
       (-> {}
           (into (map (juxt keyword #(.getNode m %)) node-names))
           (into (map (juxt keyword #(.getRelnString m %)) reln-names)))]
      match)))

(defn sem-find
  "Return the next semgrex match, if any, of string to pattern, using
  SemgrexMatcher.find()."
  ([^SemgrexMatcher m]
   (when (.find m)
     (sem-result m)))
  ([^SemgrexPattern p ^SemanticGraph g]
   (sem-find (sem-matcher p g))))

(defn sem-seq
  "Return a lazy list of matches of SemgrexPattern `p` in SemanticGraph `g`."
  [^SemgrexPattern p ^SemanticGraph g]
  (let [^SemgrexMatcher m (sem-matcher p g)]
    ((fn step []
       (when (.find m)
         (cons (sem-result m) (lazy-seq (step))))))))

(defn sem-matches
  "Returns the match, if any, of dependency graph `g` to pattern `p` using
  edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher.matches(). Uses sem-result to
  return any named nodes or relations.

  It's actually closer to java.util.regex's \"lookingAt\" in that the root of
  the graph has to match the root of the pattern but the whole tree does not
  have to be \"accounted for\"."
  [^SemgrexPattern p ^SemanticGraph g]
  (let [^SemgrexMatcher m (sem-matcher p g)]
    (when (.matches m)
      (sem-result m))))

(comment
  (def nlp
    (dk.simongray.datalinguist/->pipeline {:annotators ["depparse" "lemma"]}))

  (def example
    (->> (nlp "He sliced some slices of lemon into even smaller slices.")
         dk.simongray.datalinguist/sentences
         first
         dk.simongray.datalinguist/dependency-graph))

  ;; Find the first matching node
  (sem-find (sem-pattern "{lemma:slice}") example)
  (sem-find (sem-pattern "{lemma:lemon}") example)

  ;; Find every matching node
  (sem-seq (sem-pattern "{lemma:slice;tag:/NN.?/}") example)

  ;; Find pattern with named nodes and relations
  (sem-seq (sem-pattern "{lemma:slice} > {}=dependent") example)
  (sem-seq (sem-pattern "{lemma:slice} >=reln {}") example)
  (sem-seq (sem-pattern "{lemma:slice} >=reln {}=dependent") example)

  ;; See if the pattern "matches" (the root must match)
  (sem-matches (sem-pattern "{lemma:slice}") example)       ; matches
  (sem-matches (sem-pattern "{lemma:lemon}") example)       ; nil
  #_.)
