(ns corenlp-clj.core
  (:import [java.util Properties]
           [edu.stanford.nlp.pipeline StanfordCoreNLP]))

(defn- properties
  "Make a Properties object based on a map m."
  [m]
  (let [props (Properties.)]
    (.putAll props m)
    props))

(defn prerequisites
  "Find the prerequisities for the specified pipeline setup or a single annotator."
  ([xs opts]
   (StanfordCoreNLP/ensurePrerequisiteAnnotators (into-array xs) (properties opts)))
  ([x]
   (if (string? x)
     (prerequisites [x] {})
     (prerequisites x {}))))

(defn pipeline
  "Wrap a closure around a custom CoreNLP pipeline as specified in opts.
  The returned function will annotate text as per the specifications."
  [opts]
  (let [stanford-core-nlp (StanfordCoreNLP. ^Properties (properties opts))]
    (fn [^String s] (.process stanford-core-nlp s))))
