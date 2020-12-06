(ns ^{:doc "Fns for accessing CoreNLP annotations."} dk.simongray.datalinguist.annotations
  (:require [clojure.string :as str]
            [clojure.datafy :refer [datafy]]
            [clojure.core.protocols :as p]
            [camel-snake-kebab.core :as csk]
            [dk.simongray.datalinguist.semgraph :as semgraph])
  (:import [java.util ArrayList]
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
                                  CoreAnnotations$CharacterOffsetEndAnnotation]
           [edu.stanford.nlp.semgraph SemanticGraph
                                      SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation SemanticGraphEdge]))

;; This namespace contains convenience functions for accessing the most common
;; annotations of Stanford CoreNLP. The functions are designed to be chained
;; using the ->> macro or through function composition.

;; Please note that *any* annotation can be accessed using
;; corenlp-clj.annotations/annotation, you are not just limited to using the
;; convenience functions provided in this namespace.
;;
;; The functions here mirror the annotation system of Stanford CoreNLP:
;; once the returned object isn't a TypesafeMap or a seq of TypesafeMap objects,
;; annotation functions cannot retrieve anything from it. An example of this
;; might be `dependency-graph` which returns a SemanticGraph object. However,
;; using a function such as corenlp-clj.semgraph/nodes on a SemanticGraph object
;; returns IndexedWord objects which *are* implementations of TypesafeMap.
;; Consequently, the annotation functions can take them as params.
;;
;; As a general rule, functions with names that are pluralised have a seqable
;; output, e.g. sentences or tokens. This does not matter when chaining these
;; functions, as all annotation functions will also implicitly map to seqs.

(defn annotation
  "Access the annotation of x as specified by class.
  x may also be a seq of objects carrying annotations."
  [^Class class x]
  (if (seqable? x)
    (map (partial annotation class) x)
    (.get ^TypesafeMap x class)))

(defn text
  "The text of x (TextAnnotation)."
  [x]
  (annotation CoreAnnotations$TextAnnotation x))

(defn lemma
  "The lemma of x (LemmaAnnotation)."
  [x]
  (annotation CoreAnnotations$LemmaAnnotation x))

(defn pos
  "The part-of-speech of x (PartOfSpeechAnnotation)."
  [x]
  (annotation CoreAnnotations$PartOfSpeechAnnotation x))

(defn ner
  "The named entity tag of x (NamedEntityTagAnnotation)."
  [x]
  (annotation CoreAnnotations$NamedEntityTagAnnotation x))

(defn sentences
  "The sentences of x (SentencesAnnotation)."
  [x]
  (annotation CoreAnnotations$SentencesAnnotation x))

(defn tokens
  "The tokens of x (TokensAnnotation)."
  [x]
  (annotation CoreAnnotations$TokensAnnotation x))

(defn offset
  "The character offset of x (CharacterOffsetBeginAnnotation -or-
  CharacterOffsetEndAnnotation). Style can be :begin (default) or :end."
  ([style x]
   (case style
     :begin (annotation CoreAnnotations$CharacterOffsetBeginAnnotation x)
     :end (annotation CoreAnnotations$CharacterOffsetEndAnnotation x)))
  ([x]
   (offset :begin x)))

(defn index
  "The index of x (IndexAnnotation -or- SentenceIndexAnnotation).
  Style can be :token (default) or :sentence."
  ([style x]
   (case style
     :token (annotation CoreAnnotations$IndexAnnotation x)
     :sentence (annotation CoreAnnotations$SentenceIndexAnnotation x)))
  ([x]
   (index :token x)))

(defn whitespace
  "The whitespace around x (BeforeAnnotation -or- AfterAnnotation).
  Style can be :before (default) or :after."
  ([style x]
   (case style
     :before (annotation CoreAnnotations$BeforeAnnotation x)
     :after (annotation CoreAnnotations$AfterAnnotation x)))
  ([x]
   (whitespace :before x)))

(defn dependency-graph
  "The dependency graph of x (BasicDependenciesAnnotation -or-
  EnhancedDependenciesAnnotation -or- EnhancedPlusPlusDependenciesAnnotation).
  Style can be :basic, :enhanced or :enhanced++ (default)."
  ([style x]
   (case style
     :basic (annotation SemanticGraphCoreAnnotations$BasicDependenciesAnnotation x)
     :enhanced (annotation SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation x)
     :enhanced++ (annotation SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation x)))
  ([x]
   (dependency-graph :enhanced++ x)))

(defn- class->k
  "Convert a Java class name into an (occasionally namespaced) keyword.

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
  "Produce a map of the annotations of a TypesafeMap."
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
  "Return a recursively datafied representation of x.
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
