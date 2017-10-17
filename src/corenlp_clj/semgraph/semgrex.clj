(ns corenlp-clj.semgraph.semgrex
  (:import [edu.stanford.nlp.semgraph.semgrex SemgrexPattern SemgrexMatcher]
           [edu.stanford.nlp.semgraph SemanticGraph]))

;;;; This namespace contains implementations of functions relating to semgrex in Stanford CoreNLP.
;;;; sgx-pattern, sgx-matcher, sgx-find, and sgx-seq mimic standard Clojure regex functions.
;;;; Matches are of type IndexedWord. Underlying data can be obtained using the functions in corenlp-clj.annotations.

(defn sgx-pattern
  "Returns an instance of SemgrexPattern, for use, e.g. in sg-matcher."
  [^String s]
  (SemgrexPattern/compile s))

(defn sgx-matcher
  "Returns an instance of SemgrexMatcher, for use, e.g. in sg-find."
  [^SemgrexPattern p ^SemanticGraph g]
  (.matcher p g))

(defn sgx-find
  "Returns the next semgrex match, if any, of string to pattern, using SemgrexMatcher.findNextMatchingNode()."
  [^SemgrexMatcher m]
  (when (.findNextMatchingNode m) (.getMatch m)))

(defn sgx-seq
  "Returns a list of all unique matches of SemgrexPattern in SemanticGraph (note: not lazy)."
  [^SemgrexPattern sgx ^SemanticGraph g]
  (let [m (sgx-matcher sgx g)]
    (loop [matches ()]
      (if (.findNextMatchingNode m)
        (recur (conj matches (.getMatch m)))
        matches))))

(defn sgx-node-names
  "Returns list of matching labeled node names after latest call to sgx-find."
  [^SemgrexMatcher m]
  (.getNodeNames m))

(defn sgx-relation-names
  "Returns list of matching labeled relation names after latest call to sgx-find."
  [^SemgrexMatcher m]
  (.getRelationNames m))

(defn sgx-nodes
  "Returns a map of matching node names to nodes after latest call to sgx-find."
  [^SemgrexMatcher m]
  (into {} (map
             #(vector (keyword %) (.getNode m %))
             (sgx-node-names m))))

(defn sgx-relations
  "Returns a map of matching relation names to relations after latest call to sgx-find."
  [^SemgrexMatcher m]
  (into {} (map
             #(vector (keyword %) (.getRelnString m %))
             (sgx-relation-names m))))
