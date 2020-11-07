(ns ^{:doc "Fns dealing with semgrex in CoreNLP"} dk.simongray.datalinguist.semgraph.semgrex
  (:import [edu.stanford.nlp.semgraph SemanticGraph]
           [edu.stanford.nlp.semgraph.semgrex SemgrexPattern
                                              SemgrexMatcher]))

;; Se-pattern, se-matcher, se-find, and se-seq mimic Clojure regex functions.
;; The other functions prepended with "se-" deal with extracting named nodes and
;; relations from matches. Matches are of type IndexedWord. Underlying data can
;; be obtained using the functions in `computere.annotations`.

(defn se-pattern
  "Return an instance of SemgrexPattern, for use, e.g. in se-matcher."
  [^String s]
  (SemgrexPattern/compile s))

(defn se-matcher
  "Return an instance of SemgrexMatcher, for use, e.g. in se-find."
  [^SemgrexPattern p ^SemanticGraph g]
  (.matcher p g))

(defn se-find
  "Return the next semgrex match, if any, of string to pattern, using
  SemgrexMatcher.findNextMatchingNode()."
  [^SemgrexMatcher m]
  (when (.findNextMatchingNode m) (.getMatch m)))

(defn se-seq
  "Return a list of all unique matches of SemgrexPattern in SemanticGraph
  (note: not lazy)."
  [^SemgrexPattern p ^SemanticGraph g]
  (let [^SemgrexMatcher m (se-matcher p g)]
    (loop [matches ()]
      (if (.findNextMatchingNode m)
        (recur (conj matches (.getMatch m)))
        matches))))

(defn se-node-names
  "Return list of matching labeled node names after latest call to se-find."
  [^SemgrexMatcher m]
  (.getNodeNames m))

(defn se-relation-names
  "Return list of matching labeled relation names after latest call to se-find."
  [^SemgrexMatcher m]
  (.getRelationNames m))

(defn se-nodes
  "Return a map of matching node names to nodes after latest call to se-find."
  [^SemgrexMatcher m]
  (into {} (map #(vector (keyword %) (.getNode m %))
                (se-node-names m))))

(defn se-relations
  "Return a map of matching relation names to relations after latest call to
  se-find."
  [^SemgrexMatcher m]
  (into {} (map #(vector (keyword %) (.getRelnString m %))
                (se-relation-names m))))

(defn se-find-full
  "Return a vector of the next semgrex match, matching named nodes, and matching
  named relations, if any."
  [^SemgrexMatcher m]
  (when-let [match (se-find m)]
    [match (se-nodes m) (se-relations m)]))

(defn se-seq-full
  "Return vectors of semgrex match, matching named nodes, and matching named
  relations for all matches."
  [^SemgrexPattern p ^SemanticGraph g]
  (let [^SemgrexMatcher m (se-matcher p g)]
    (loop [matches ()]
      (if (.findNextMatchingNode m)
        (recur (conj matches [(.getMatch m) (se-nodes m) (se-relations m)]))
        matches))))
