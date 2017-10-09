(ns corenlp-clj.core
  (:import [java.util Properties]
           [edu.stanford.nlp.pipeline StanfordCoreNLP Annotation]
           [edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$TokensAnnotation]))

(defn properties
  "Convenience function for making a Properties object based on a Clojure map m."
  [m]
  (let [props (Properties.)]
    (.putAll props m)
    props))

(defn prerequisites
  "Find the prerequisities for the specified pipeline setup or a single annotator."
  ([xs m]
   (StanfordCoreNLP/ensurePrerequisiteAnnotators (into-array xs) (properties m)))
  ([x]
   (if (string? x)
     (prerequisites [x] {})
     (prerequisites x {}))))

(defn pipeline
  "Wraps a closure around a custom CoreNLP pipeline (as specified in m).
  The function returned is used to annotate a String of text."
  [m]
  (let [stanford-core-nlp (StanfordCoreNLP. ^Properties (properties m))]
    (fn [^String s] (.process stanford-core-nlp s))))

(defn annotation
  "Access the annotation of x as specified by class."
  [x ^Class class]
  (if (seqable? x)
    (map #(annotation % class) x) ; no tail recursion!
    (.get ^Annotation x class)))

;; convenience functions for accessing core annotations
;; these allow chaining using threading macros or function composition
(def sentences #(annotation % CoreAnnotations$SentencesAnnotation))
(def tokens #(annotation % CoreAnnotations$TokensAnnotation))
