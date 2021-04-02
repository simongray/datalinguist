(ns dk.simongray.datalinguist.dependency.semgrex
  "Functions dealing with semgrex in CoreNLP (dependency grammar patterns).

  The `sem-pattern`, `sem-matcher`, `sem-find`, and `sem-seq` functions mimic
  existing Clojure regex functions. The `sem-named` function also mimics
  the re-groups function and serves a similar purpose, although rather than
  returning regex groups it returns named nodes in the semgrex patterns.

  Matches are of type IndexedWord. Underlying data can be obtained using the
  core annotation functions in `dk.simongray.datalinguist`."
  (:import [edu.stanford.nlp.semgraph SemanticGraph]
           [edu.stanford.nlp.semgraph.semgrex SemgrexPattern
                                              SemgrexMatcher]))

(defn sem-pattern
  "Return an instance of SemgrexPattern, for use, e.g. in se-matcher."
  [^String s]
  (SemgrexPattern/compile s))

(defn sem-matcher
  "Create a SemgrexMatcher from `s` and dependency graph `g`; use in se-find."
  [^SemgrexPattern p ^SemanticGraph g]
  (.matcher p g))

(defn sem-named
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
     (sem-named m)))
  ([^SemgrexPattern p ^SemanticGraph g]
   (sem-find (sem-matcher p g))))

(defn sem-seq
  "Return a lazy list of matches of SemgrexPattern `p` in SemanticGraph `g`."
  [^SemgrexPattern p ^SemanticGraph g]
  (let [^SemgrexMatcher m (sem-matcher p g)]
    ((fn step []
       (when (.find m)
         (cons (sem-named m) (lazy-seq (step))))))))

(defn sem-matches
  "Returns the match, if any, of dependency graph `g` to pattern `p` using
  edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher.matches(). Uses sem-named to
  return the groups.

  It's actually closer to java.util.regex's \"lookingAt\" in that the root of
  the graph has to match the root of the pattern but the whole tree does not
  have to be \"accounted for\"."
  [^SemgrexPattern p ^SemanticGraph g]
  (let [m (sem-matcher p g)]
    (when (.matches m)
      (sem-named m))))

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
