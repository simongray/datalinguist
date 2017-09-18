(ns corenlp-clj.core
  (:import [java.util Properties]
           [edu.stanford.nlp.pipeline StanfordCoreNLP]))


(defn properties [& args]
  (let [props (Properties.)]
    (.putAll props (apply hash-map args))
    props))

(defn pipeline [props]
  (StanfordCoreNLP. ^Properties props))
