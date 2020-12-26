(ns dk.simongray.datalinguist.annotation
  "Functions for accessing CoreNLP annotations on a processed piece of text.

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
            [dk.simongray.datalinguist.semgraph :as semgraph])
  (:import [java.util ArrayList Map]
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
                                  CoreAnnotations$NumericCompositeObjectAnnotation]
           [edu.stanford.nlp.semgraph SemanticGraph
                                      SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation SemanticGraphEdge]))

(defn annotation
  "Access the annotation of `x` as specified by class `c`.
  If `x` is seqable, return the annotation of each item in the seq."
  [^Class c x]
  (if (seqable? x)
    (map (partial annotation c) x)
    (.get ^TypesafeMap x c)))

(defn text
  "The text of `x`; `style` can be :true-case or :plain (default)."
  {:annotations #{CoreAnnotations$TextAnnotation
                  CoreAnnotations$TrueCaseTextAnnotation}}
  ([style x]
   (case style
     :plain (annotation CoreAnnotations$TextAnnotation x)
     :true-case (annotation CoreAnnotations$TrueCaseTextAnnotation x)))
  ([x]
   (text :plain x)))

(defn true-case
  "The true case of `x`."
  {:annotations #{CoreAnnotations$TrueCaseAnnotation}}
  [x]
  (annotation CoreAnnotations$TrueCaseAnnotation x))

(defn quotations
  "The quotations of `x`; `style` can be :unclosed or :closed (default)."
  {:annotations #{CoreAnnotations$QuotationsAnnotation
                  CoreAnnotations$UnclosedQuotationsAnnotation}}
  ([style x]
   (case style
     :closed (annotation CoreAnnotations$QuotationsAnnotation x)
     :unclosed (annotation CoreAnnotations$UnclosedQuotationsAnnotation x)))
  ([x]
   (quotations :closed x)))

(defn lemma
  "The lemma of `x`."
  {:annotations #{CoreAnnotations$LemmaAnnotation}}
  [x]
  (annotation CoreAnnotations$LemmaAnnotation x))

;; TODO: coarse, other?
(defn pos
  "The part-of-speech of `x`."
  {:annotations #{CoreAnnotations$PartOfSpeechAnnotation}}
  [x]
  (annotation CoreAnnotations$PartOfSpeechAnnotation x))

;; StackedNamedEntityTagAnnotation not included - seems to be internal use only
(defn ner
  "The named entity tag of `x`; `style` can be :probs, :coarse, :fine, or
  :tag (default)."
  {:annotations #{CoreAnnotations$NamedEntityTagAnnotation
                  CoreAnnotations$FineGrainedNamedEntityTagAnnotation
                  CoreAnnotations$CoarseNamedEntityTagAnnotation
                  CoreAnnotations$NamedEntityTagProbsAnnotation}}
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
   (ner :tag x)))

;; TODO: what sets a CoreAnnotations$NumericCompositeObjectAnnotation??
(defn numeric
  "The numeric value or type of `x`; `style` can be :normalized, :composite,
  :composite-type, :composite-value, :type, or :value (default)."
  {:annotations #{CoreAnnotations$NumericValueAnnotation
                  CoreAnnotations$NumericTypeAnnotation
                  CoreAnnotations$NumericCompositeObjectAnnotation
                  CoreAnnotations$NumericCompositeValueAnnotation
                  CoreAnnotations$NumericCompositeTypeAnnotation
                  CoreAnnotations$NormalizedNamedEntityTagAnnotation}}
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

(defn sentences
  "The sentences of `x`."
  {:annotations #{CoreAnnotations$SentencesAnnotation}}
  [x]
  (annotation CoreAnnotations$SentencesAnnotation x))

(defn tokens
  "The tokens of `x`."
  {:annotations #{CoreAnnotations$TokensAnnotation}}
  [x]
  (annotation CoreAnnotations$TokensAnnotation x))

(defn offset
  "The character offset of `x`; `style` can be :end or :begin (default)."
  {:annotations #{CoreAnnotations$CharacterOffsetBeginAnnotation
                  CoreAnnotations$CharacterOffsetEndAnnotation}}
  ([style x]
   (case style
     :begin (annotation CoreAnnotations$CharacterOffsetBeginAnnotation x)
     :end (annotation CoreAnnotations$CharacterOffsetEndAnnotation x)))
  ([x]
   (offset :begin x)))

(defn index
  "The index of `x`; `style` can be :quote, :sentence, or :token (default)."
  {:annotations #{CoreAnnotations$IndexAnnotation
                  CoreAnnotations$SentenceIndexAnnotation
                  CoreAnnotations$QuotationIndexAnnotation}}
  ([style x]
   (case style
     :token (annotation CoreAnnotations$IndexAnnotation x)
     :sentence (annotation CoreAnnotations$SentenceIndexAnnotation x)
     :quote (annotation CoreAnnotations$QuotationIndexAnnotation x)))
  ([x]
   (index :token x)))

(defn whitespace
  "The whitespace around `x`; `style` can be :after or :before (default)."
  {:annotations #{CoreAnnotations$BeforeAnnotation
                  CoreAnnotations$AfterAnnotation}}
  ([style x]
   (case style
     :before (annotation CoreAnnotations$BeforeAnnotation x)
     :after (annotation CoreAnnotations$AfterAnnotation x)))
  ([x]
   (whitespace :before x)))

(defn dependency-graph
  "The dependency graph of `x`; `style` can be :basic, :enhanced or :enhanced++
  (default)."
  {:annotations #{SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                  SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                  SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation}}
  ([style x]
   (case style
     :basic (annotation SemanticGraphCoreAnnotations$BasicDependenciesAnnotation x)
     :enhanced (annotation SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation x)
     :enhanced++ (annotation SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation x)))
  ([x]
   (dependency-graph :enhanced++ x)))

(defn- class->k
  "Convert a Java class `c` into an (occasionally namespaced) keyword.

   (helper fn for datafy-tsm)"
  [^Class c]
  (-> (str c)
      (subs 6)                                              ; remove "class "
      (str/split #"\.|\$")
      (->> (filter (partial re-find #"Annotation"))
           (map #(str/replace % #"Annotation[s]?" ""))
           (map #(str/replace % #"Core" ""))
           (remove empty?)
           (map csk/->kebab-case)
           (interpose "/")
           (str/join))
      (str/replace #"^is-(.+)" #(str (second %) "?"))
      (keyword)))

(defn- datafy-tsm
  "Produce a map of the annotations of a TypesafeMap `tsm`."
  [^TypesafeMap tsm]
  (->> (.keySet ^TypesafeMap tsm)
       (map #(vector (class->k %) (annotation % tsm)))
       (reduce (fn [m [k v]]
                 (assoc m k (datafy v)))
               {})))

(extend-protocol p/Datafiable
  TypesafeMap
  (datafy [tsm]
    (datafy-tsm tsm))

  SemanticGraph
  (datafy [g]
    (into {} (for [vertex (semgraph/vertices g)]
               [vertex (semgraph/outgoing-edges g vertex)])))

  SemanticGraphEdge
  (datafy [edge]
    {:governor  (semgraph/governor edge)
     :dependent (semgraph/dependent edge)
     :relation  (semgraph/relation edge)
     :weight    (semgraph/weight edge)
     :extra?    (semgraph/extra? edge)}))

(defn recur-datafy
  "Return a recursively datafied representation of `x`.
  Call at the end of an annotation chain to get plain Clojure data structures."
  [x]
  (let [x* (datafy x)]
    (cond
      (instance? ArrayList x*)
      (mapv recur-datafy x*)

      (seq? x*)
      (mapv recur-datafy x)

      (set? x*)
      (set (map recur-datafy x*))

      (map? x*)
      (into {} (for [[k v] x*]
                 [(recur-datafy k) (recur-datafy v)]))

      :else x*)))
