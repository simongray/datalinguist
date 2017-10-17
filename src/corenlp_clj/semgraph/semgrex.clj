(ns corenlp-clj.semgraph.semgrex
  (:import [edu.stanford.nlp.semgraph.semgrex SemgrexPattern SemgrexMatcher]
           [edu.stanford.nlp.semgraph SemanticGraph]))

;;;; Matches are of type IndexedWord whose information can be obtained via the basic annotation functions.

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
