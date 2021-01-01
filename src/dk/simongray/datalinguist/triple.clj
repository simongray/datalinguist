(ns dk.simongray.datalinguist.triple
  "Functions dealing with (subject; relation; object) triples."
  (:require [dk.simongray.datalinguist.util :as util])
  (:import [edu.stanford.nlp.ie.util RelationTriple]
           [edu.stanford.nlp.util Pair]))

;; TODO: unit tests

(defn subject
  "The subject of the `triple`; `style` can be :span, :link, :head, :lemma or
  :text (default)."
  ([style ^RelationTriple triple]
   (case style
     :tokens (.-subject triple)
     :canonical (.-canonicalSubject triple)
     :text (.subjectGloss triple)
     :lemma (.subjectLemmaGloss triple)
     :head (.subjectHead triple)
     :link (.subjectLink triple)
     :span (let [^Pair pair (.subjectTokenSpan triple)]
             [(.first pair) (.second pair)])))
  ([^RelationTriple triple]
   (subject :tokens triple)))

(defn object
  "The object of the `triple`; `style` can be :span, :link, :head, :lemma or
  :text (default)."
  ([style ^RelationTriple triple]
   (case style
     :tokens (.-object triple)
     :canonical (.-canonicalObject triple)
     :text (.objectGloss triple)
     :lemma (.objectLemmaGloss triple)
     :head (.objectHead triple)
     :link (.objectLink triple)
     :span (let [^Pair pair (.objectTokenSpan triple)]
             [(.first pair) (.second pair)])))
  ([^RelationTriple triple]
   (object :tokens triple)))

(defn relation
  "The relation of the `triple`; `style` can be :span, :head, :lemma or
  :text (default)."
  ([style ^RelationTriple triple]
   (case style
     :tokens (.-relation triple)
     :text (.relationGloss triple)
     :lemma (.relationLemmaGloss triple)
     :head (.relationHead triple)
     :span (let [^Pair pair (.relationTokenSpan triple)]
             [(.first pair) (.second pair)])))
  ([^RelationTriple triple]
   (relation :tokens triple)))

(defn confidence
  "The confidence score of the `triple`."
  [^RelationTriple triple]
  (.-confidence triple))

(defn tokens
  "The tokens of the `triple`."
  [^RelationTriple triple]
  (.allTokens triple))

(defn prefix-be?
  "If true, this `triple` expresses a prefixed 'to be' relation. For example,
  'President Obama' expresses (Obama; be; President)."
  [^RelationTriple triple]
  (.isPrefixBe triple))

(defn suffix-be?
  "If true, this `triple` expresses a suffixed 'to be' relation. For example,
  'Tim's father Tom' expresses (Tim; 's father is; Tom)."
  [^RelationTriple triple]
  (.isSuffixBe triple))

(defn implied-be?
  "If true, this `triple` expresses an implied 'to be' relation."
  [^RelationTriple triple]
  (or (prefix-be? triple) (suffix-be? triple)))

(defn suffix-of?
  "If true, this `triple` has an ungrounded 'of' at the end of the relation.
  For example, 'United States president Barack Obama' expresses the relation
  (Obama; is president of; United States)."
  [^RelationTriple triple]
  (.isSuffixOf triple))

(defn tmod?
  "If true, this `triple` expresses a tmod (temporal modifier) relation that is
  not grounded in the sentence. For example, 'I went to the store Friday' would
  otherwise yield the strange triple (I; go to store; Friday)."
  [^RelationTriple triple]
  (.istmod triple))

(defn triple->dependency-graph
  "Convert the `triple` to a dependency graph."
  [^RelationTriple triple]
  (let [opt (.asDependencyTree triple)]
    (when (.isPresent opt)
      (.get opt))))

(defn triple->sentence
  "Convert the `triple` to a flat sentence."
  [^RelationTriple triple]
  (.asSentence triple))

(defn triple->datalog
  "Convert the `triple` to a Datomic-style EaV tuple."
  [^RelationTriple triple]
  (let [entity    (util/tokens->string (subject :canonical triple))
        value     (util/tokens->string (object :canonical triple))
        attribute (if (empty? (relation triple))
                    (if (implied-be? triple)
                      :is
                      :<unknown-relation>)
                    (util/tokens->keyword (relation :tokens triple)))]
    [entity attribute value]))
