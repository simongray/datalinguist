(ns corenlp-clj.core
  (:import [java.util Properties]
           [edu.stanford.nlp.pipeline StanfordCoreNLP Annotation]
           [edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$TokensAnnotation]
           [edu.stanford.nlp.semgraph SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation]))

(def dependency-types
  {:basic SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
   :enhanced SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
   :enhanced++ SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation})

(defn properties
  [& args]
  (let [props (Properties.)]
    (.putAll props (apply hash-map args))
    props))

;; necessary properties for loading depparse
(def depparse
  (properties "annotators" "tokenize, ssplit, pos, depparse"))

(defn pipeline
  [^Properties props]
  (StanfordCoreNLP. props))

(defn process
  [^StanfordCoreNLP pipeline ^String s]
  (.process pipeline s))

(defn sentences
  [^Annotation annotation]
  (.get annotation CoreAnnotations$SentencesAnnotation))

(defn tokens
  [^Annotation annotation]
  (.get annotation CoreAnnotations$TokensAnnotation))

(defn dependencies
  ([^Annotation annotation type]
   (.get annotation (dependency-types type)))
  ([^Annotation annotation]
   (dependencies annotation :enhanced++)))

(defn dep
  "Shorthand version of the dependencies function; operates directly on a String s."
  ([^StanfordCoreNLP pipeline ^String s type]
   (map #(dependencies % type) (sentences (process pipeline s))))
  ([^StanfordCoreNLP pipeline ^String s]
   (dep pipeline s :enhanced++)))
