(ns dk.simongray.datalinguist.tree
  "Everything to do with trees, chiefly of the constituency grammar kind.

  Functions dealing with tregex in CoreNLP (constituency grammar patterns) have
  been wrapped so as to mimic the existing Clojure Core regex functions. The
  `tregex-result` function also mimics re-groups and serves a similar purpose,
  although rather than returning groups it returns named nodes defined  in the
  pattern."
  (:import [edu.stanford.nlp.trees.tregex TregexMatcher TregexPattern]
           [edu.stanford.nlp.trees Tree]))

;; TODO: implement more tree functionality (only tregex implemented as of now)

(defn tregex-pattern
  "Return an instance of TregexPattern, for use, e.g. in tregex-matcher."
  [^String s]
  (TregexPattern/compile s))

(defn tregex-matcher
  "Create a TregexMatcher from `p` and Tree `t`; use in tregex-find."
  [^TregexPattern p t]
  (.matcher p t))

(defn tregex-result
  "Returns the named nodes from the most recent match/find.
  If there are no named nodes/relations, returns the match itself.
  If there are named nodes/relations, returns a vector with the first element
  being the match itself and the second a map of names -> nodes."
  [^TregexMatcher m]
  (let [match      (.getMatch m)
        node-names (not-empty (.getNodeNames m))]
    (if node-names
      [match (into {} (map (juxt keyword #(.getNode m %)) node-names))]
      match)))

(defn tregex-find
  "Return the next tregex match, if any, of tokens to pattern, using
  TokenSequenceMatcher.find()."
  ([^TregexMatcher m]
   (when (.find m)
     (tregex-result m)))
  ([^TregexPattern p ^Tree t]
   (tregex-find (tregex-matcher p t))))

(defn tregex-seq
  "Return a lazy list of matches of TregexPattern `p` in Tree `t`."
  [^TregexPattern p ^Tree t]
  (let [^TregexMatcher m (tregex-matcher p t)]
    ((fn step []
       (when (.find m)
         (cons (tregex-result m) (lazy-seq (step))))))))

(defn tregex-matches
  "Returns the match, if any, of tokens to pattern, using
  edu.stanford.nlp.trees.tregex.TregexMatcher.matches().
  Uses tregex-result to return any named nodes."
  [^TregexPattern p ^Tree t]
  (let [^TregexMatcher m (tregex-matcher p t)]
    (when (.matches m)
      (tregex-result m))))
