(ns corenlp-clj.core
  (:import [java.util Properties]
           [edu.stanford.nlp.pipeline StanfordCoreNLP Annotation]
           [edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$TokensAnnotation]
           [edu.stanford.nlp.semgraph SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation]))

(def dep-types {:basic SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                :enhanced SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                :enhanced++ SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation})

(defn properties
  [& args]
  (let [props (Properties.)]
    (.putAll props (apply hash-map args))
    props))

(defn pipeline
  [^Properties props]
  (StanfordCoreNLP. props))

(defn annotation
  [^StanfordCoreNLP pipeline ^String s]
  (let [annotation (Annotation. s)]
    (.annotate pipeline annotation) ; mutating!
    annotation))

(defn sentences
  [^Annotation annotation]
  (.get annotation CoreAnnotations$SentencesAnnotation))

(defn tokens
  [^Annotation annotation]
  (.get annotation CoreAnnotations$TokensAnnotation))

(defn dependencies
  ([^Annotation annotation type]
   (.get annotation (dep-types type)))
  ([^Annotation annotation]
   (dependencies annotation :enhanced++)))

(defn dep
  "Shorthand version of the dependencies function; operates directly on a String s."
  ([^StanfordCoreNLP pipeline ^String s type]
   (map #(dependencies % type) (sentences (annotation pipeline s))))
  ([^StanfordCoreNLP pipeline ^String s]
   (dep pipeline s :enhanced++)))
