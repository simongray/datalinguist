(ns dk.simongray.datalinguist
  "Functions for building a CoreNLP pipeline and extracting text annotations.

  The functions are designed to be chained using the threading macro or through
  function composition. Please note that *any* annotation can be accessed using
  the basic `annotation` function, you are not limited to using the convenience
  functions otherwise provided in this namespace.

  The functions here mirror the annotation system of Stanford CoreNLP: once the
  return value isn't an instance of TypesafeMap or a seq of TypesafeMap objects,
  the annotation functions cannot retrieve anything from it. One example of this
  might be `dependency-graph` which returns a SemanticGraph object.

  As a general rule, functions with names that are pluralised have a seqable
  output, e.g. `sentences` or `tokens`. This does not matter when chaining these
  functions, as all of the annotation functions will implicitly map to seqs."
  (:require [clojure.string :as str]
            [clojure.datafy :refer [datafy]]
            [clojure.core.protocols :as p]
            [camel-snake-kebab.core :as csk]
            [dk.simongray.datalinguist.dependency :as dependency]
            [dk.simongray.datalinguist.triple :as triple]
            [dk.simongray.datalinguist.util :as util])
  (:import [java.util Properties
                      Map]
           [edu.stanford.nlp.pipeline StanfordCoreNLP]
           [edu.stanford.nlp.util TypesafeMap]
           [edu.stanford.nlp.ling CoreAnnotations$TextAnnotation
                                  CoreAnnotations$LemmaAnnotation
                                  CoreAnnotations$PartOfSpeechAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation
                                  CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$TokensAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation
                                  CoreAnnotations$BeforeAnnotation
                                  CoreAnnotations$AfterAnnotation
                                  CoreAnnotations$IndexAnnotation
                                  CoreAnnotations$SentenceIndexAnnotation
                                  CoreAnnotations$CharacterOffsetBeginAnnotation
                                  CoreAnnotations$CharacterOffsetEndAnnotation
                                  CoreAnnotations$CoarseNamedEntityTagAnnotation
                                  CoreAnnotations$FineGrainedNamedEntityTagAnnotation
                                  CoreAnnotations$NamedEntityTagProbsAnnotation
                                  CoreAnnotations$TrueCaseAnnotation
                                  CoreAnnotations$TrueCaseTextAnnotation
                                  CoreAnnotations$QuotationsAnnotation
                                  CoreAnnotations$UnclosedQuotationsAnnotation
                                  CoreAnnotations$QuotationIndexAnnotation
                                  CoreAnnotations$NormalizedNamedEntityTagAnnotation
                                  CoreAnnotations$NumericTypeAnnotation
                                  CoreAnnotations$NumericValueAnnotation
                                  CoreAnnotations$NumericCompositeValueAnnotation
                                  CoreAnnotations$NumericCompositeTypeAnnotation
                                  CoreAnnotations$NumericCompositeObjectAnnotation
                                  CoreAnnotations$MentionsAnnotation
                                  CoreAnnotations$KBPTriplesAnnotation]
           [edu.stanford.nlp.semgraph SemanticGraph
                                      SemanticGraphEdge
                                      SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation]
           [edu.stanford.nlp.trees TreeCoreAnnotations$TreeAnnotation
                                   TreeCoreAnnotations$BinarizedTreeAnnotation
                                   TreeCoreAnnotations$KBestTreesAnnotation]
           [edu.stanford.nlp.naturalli NaturalLogicAnnotations$RelationTriplesAnnotation
                                       Polarity]
           [edu.stanford.nlp.ie.util RelationTriple]))

;;;;
;;;; ANNOTATION RETRIEVAL
;;;;

(defn- ok
  "Only return `coll` if it contains some information."
  [coll]
  (when (and (not-empty coll) (not-every? nil? coll))
    coll))

(defn annotation
  "Access the annotation of `x` as specified by class `c`.

  If `x` doesn't contain the annotation, tries to find the annotation inside any
  tokens or sentences within x, in that order. Generally, annotations will be
  located at either the document level, sentence level, or token level, so
  this behaviour allows skipping some steps in the REPL."
  [^Class c x]
  (cond
    (and (some? x) (seqable? x))
    (map (partial annotation c) x)

    (instance? TypesafeMap x)
    (let [tsm ^TypesafeMap x]
      (or (.get tsm c)
          (ok (annotation c (.get tsm CoreAnnotations$TokensAnnotation)))
          (ok (annotation c (.get tsm CoreAnnotations$SentencesAnnotation)))))))

(defn text
  "The text of `x`; `style` can be :true-case or :plain (default)."
  ([style x]
   (case style
     :plain (annotation CoreAnnotations$TextAnnotation x)
     :true-case (annotation CoreAnnotations$TrueCaseTextAnnotation x)))
  ([x]
   (text :plain x)))

(defn true-case
  "The true case of `x`."
  [x]
  (annotation CoreAnnotations$TrueCaseAnnotation x))

(defn quotations
  "The quotations of `x`; `style` can be :unclosed or :closed (default)."
  ([style x]
   (case style
     :closed (annotation CoreAnnotations$QuotationsAnnotation x)
     :unclosed (annotation CoreAnnotations$UnclosedQuotationsAnnotation x)))
  ([x]
   (quotations :closed x)))

(defn lemma
  "The lemma of `x`."
  [x]
  (annotation CoreAnnotations$LemmaAnnotation x))

;; TODO: coarse, other?
(defn pos
  "The part-of-speech of `x`."
  [x]
  (annotation CoreAnnotations$PartOfSpeechAnnotation x))

;; StackedNamedEntityTagAnnotation not included - seems to be internal use only
(defn named-entity
  "The named entity tag of `x`; `style` can be :probs, :coarse, :fine, or
  :tag (default)."
  ([style x]
   (case style
     :tag (annotation CoreAnnotations$NamedEntityTagAnnotation x)
     :fine (annotation CoreAnnotations$FineGrainedNamedEntityTagAnnotation x)
     :coarse (annotation CoreAnnotations$CoarseNamedEntityTagAnnotation x)
     :probs (map (fn [^Map m]
                   (let [[k v] (first m)]
                     [k v]))
                 (annotation CoreAnnotations$NamedEntityTagProbsAnnotation x))))
  ([x]
   (named-entity :tag x)))

;; TODO: what sets a CoreAnnotations$NumericCompositeObjectAnnotation??
(defn numeric
  "The numeric value or type of `x`; `style` can be :normalized, :composite,
  :composite-type, :composite-value, :type, or :value (default)."
  ([style x]
   (case style
     :value (annotation CoreAnnotations$NumericValueAnnotation x)
     :type (annotation CoreAnnotations$NumericTypeAnnotation x)
     :composite (annotation CoreAnnotations$NumericCompositeObjectAnnotation x)
     :composite-value (annotation CoreAnnotations$NumericCompositeValueAnnotation x)
     :composite-type (annotation CoreAnnotations$NumericCompositeTypeAnnotation x)
     :normalized (annotation CoreAnnotations$NormalizedNamedEntityTagAnnotation x)))
  ([x]
   (numeric :value x)))

(defn mentions
  "The named entity mentions of `x`."
  [x]
  (annotation CoreAnnotations$MentionsAnnotation x))

(defn sentences
  "The sentences of `x`."
  [x]
  (annotation CoreAnnotations$SentencesAnnotation x))

(defn tokens
  "The tokens of `x`."
  [x]
  (annotation CoreAnnotations$TokensAnnotation x))

;; TODO: issue #4 - kpb annotator doesn't work
;; TODO: tests
(defn triples
  "The triples of `x`; `style` can be :kbp or :openie (default)."
  ([style x]
   (case style
     :openie (annotation NaturalLogicAnnotations$RelationTriplesAnnotation x)
     :kbp (annotation CoreAnnotations$KBPTriplesAnnotation x)))
  ([x]
   (triples :openie x)))

(defn offset
  "The character offset of `x`; `style` can be :end or :begin (default)."
  ([style x]
   (case style
     :begin (annotation CoreAnnotations$CharacterOffsetBeginAnnotation x)
     :end (annotation CoreAnnotations$CharacterOffsetEndAnnotation x)))
  ([x]
   (offset :begin x)))

(defn index
  "The index of `x`; `style` can be :quote, :sentence, or :token (default)."
  ([style x]
   (case style
     :token (annotation CoreAnnotations$IndexAnnotation x)
     :sentence (annotation CoreAnnotations$SentenceIndexAnnotation x)
     :quote (annotation CoreAnnotations$QuotationIndexAnnotation x)))
  ([x]
   (index :token x)))

(defn whitespace
  "The whitespace around `x`; `style` can be :after or :before (default)."
  ([style x]
   (case style
     :before (annotation CoreAnnotations$BeforeAnnotation x)
     :after (annotation CoreAnnotations$AfterAnnotation x)))
  ([x]
   (whitespace :before x)))

;; TODO: add unit tests
;; TODO: wrap Tree class and other parts of trees package
;; TODO: datafy support
(defn constituency-tree
  "The constituency tree of `x`; `style` can be :kbest-trees, :binarized, or
  :standard (default)."
  ([style x]
   (case style
     :standard (annotation TreeCoreAnnotations$TreeAnnotation x)
     :binarized (annotation TreeCoreAnnotations$BinarizedTreeAnnotation x)
     :kbest-trees (annotation TreeCoreAnnotations$KBestTreesAnnotation x)))
  ([x]
   (constituency-tree :standard x)))

(defn dependency-graph
  "The dependency graph of `x`; `style` can be :basic, :enhanced, or :enhanced++
  (default)."
  ([style x]
   (case style
     :basic (annotation SemanticGraphCoreAnnotations$BasicDependenciesAnnotation x)
     :enhanced (annotation SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation x)
     :enhanced++ (annotation SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation x)))
  ([x]
   (dependency-graph :enhanced++ x)))


;;;;
;;;; DATAFICATION
;;;;

(defn- class->k
  "Convert a Java class `c` into an occasionally namespaced keyword."
  [^Class c]
  (-> (str c)
      (subs 6)                                              ; remove "class "
      (str/split #"\.|\$")
      (->> (filter (partial re-find #"Annotation"))
           (map #(str/replace % #"CoreAnnotation[s]?" ""))  ; removing only "Core" interferes with words like Coref
           (map #(str/replace % #"Annotation[s]?" ""))
           (remove empty?)
           (map csk/->kebab-case)
           ((fn [[c k :as parts]]
              (if (and k (str/starts-with? k c) (not= c k))
                [c (subs k (inc (count c)))]                ; remove repeated "prefix-"
                parts)))
           (interpose "/")
           (str/join))
      (str/replace #"^is-(.+)" #(str (second %) "?"))
      (keyword)))

(defn- datafy-tsm
  "Produce a map of the annotations of a TypesafeMap `tsm`."
  [^TypesafeMap tsm]
  (->> (.keySet ^TypesafeMap tsm)
       (map (juxt class->k #(annotation % tsm)))
       (reduce (fn [m [k v]]
                 (assoc m k (datafy v)))
               {})))

(extend-protocol p/Datafiable
  TypesafeMap
  (datafy [tsm]
    (datafy-tsm tsm))

  Polarity
  (datafy [polarity]
    (.toString polarity))

  RelationTriple
  (datafy [triple]
    (triple/triple->datalog triple))

  SemanticGraph
  (datafy [g]
    (into {} (for [vertex (dependency/vertices g)]
               [vertex (dependency/outgoing-edges g vertex)])))

  SemanticGraphEdge
  (datafy [edge]
    {:governor  (dependency/governor edge)
     :dependent (dependency/dependent edge)
     :relation  (dependency/relation edge)
     :weight    (dependency/weight edge)
     :extra?    (dependency/extra? edge)}))

(defn recur-datafy
  "Return a recursively datafied representation of `x`.
  Call at the end of an annotation chain to get plain Clojure data structures."
  [x]
  (let [x* (datafy x)]
    (cond
      (seq? x*)
      (mapv recur-datafy x)

      (set? x*)
      (set (map recur-datafy x*))

      (map? x*)
      (into {} (for [[k v] x*]
                 [(recur-datafy k) (recur-datafy v)]))

      ;; Catches nearly all Java collections, including custom CoreNLP ones.
      (instance? Iterable x*)
      (mapv recur-datafy x*)

      :else x*)))


;;;;
;;;; PIPELINE CONSTRUCTION
;;;;

(defn- attach-prerequisites!
  "Attach annotator prerequisites for pipeline defined in `props`."
  [^Properties props]
  (let [annotators (get props "annotators")]
    (doto props
      (.setProperty "annotators" (StanfordCoreNLP/ensurePrerequisiteAnnotators
                                   (into-array (if (string? annotators)
                                                 (str/split annotators #",")
                                                 annotators))
                                   props)))))

(defn ->pipeline
  "Wrap a closure around the CoreNLP pipeline specified in the `conf` map.

  The returned function will annotate input text with the annotators specified
  in addition to any unspecified dependency annotators."
  [conf]
  (let [props    (attach-prerequisites! (util/properties conf))
        core-nlp (StanfordCoreNLP. ^Properties props true)]
    (fn [^String s]
      (.process core-nlp s))))

(comment

  (def nlp
    (->pipeline {:annotators ["truecase"                    ; TrueCaseAnnotation
                              "quote"                       ; QuotationsAnnotation
                              "entitymentions"              ; MentionsAnnotation
                              "parse"                       ; TreeAnnotation
                              "depparse"
                              "lemma"
                              "coref"
                              "openie"
                              ;; TODO: issue #4 - kbp doesn't work
                              ;"kbp"                 ; KBPTriplesAnnotation
                              "ner"]
                 :quote      {:extractUnclosedQuotes "true"}}))

  (def example
    (nlp (str "Donald Trump was elected president in 2016. "
              "In 2021, Joe Biden will succeed Donald Trump as president.")))

  (->> (nlp "This here -- this is a dot.") tokens util/tokens->keyword)
  (->> example triples recur-datafy)
  (->> example sentences second triples first recur-datafy)
  (->> example sentences second triples second recur-datafy)
  (->> example sentences first triples first (triple/relation :head))
  (->> example sentences first triples first triple/confidence)
  (->> example sentences first triples (map triple/triple->datalog))
  (->> example sentences first triples first triple/triple->datalog)
  (->> example triples ffirst triple/->sentence)
  (->> example triples ffirst triple/triple->dependency-graph)

  #_.)
