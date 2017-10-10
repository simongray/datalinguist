(ns corenlp-clj.core
  (:import [java.util Properties]
           [edu.stanford.nlp.pipeline StanfordCoreNLP]))

(defn- properties
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
  "Wraps a closure around a custom CoreNLP pipeline as specified in m."
  [m]
  (let [stanford-core-nlp (StanfordCoreNLP. ^Properties (properties m))]
    (fn [^String s] (.process stanford-core-nlp s))))
