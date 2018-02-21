(ns computerese.core
  (:require [clojure.string :as str])
  (:import [java.util Properties]
           [edu.stanford.nlp.pipeline StanfordCoreNLP]))

;; Adapted from https://stackoverflow.com/questions/21768802/how-can-i-get-the-nested-keys-of-a-map-in-clojure
(defn keys-in [m]
  "Get the nested keys in map m."
  (let [f (fn [[k v]]
            (let [nested-ks (filter (comp not empty?) (keys-in v))
                  append-ks (fn [path] (into [k] path))
                  kscoll    (map append-ks nested-ks)]
              (if (seq kscoll)
                kscoll
                [[k]])))]
    (if (map? m)
      (vec (mapcat f m))
      [])))

(defn path->str
  "Converts a key path (e.g. from keys-in) to a flattened CoreNLP key."
  [ks]
  (str/join "." (map name ks)))

(defn flatten-map
  "Flattens a map of nested keys."
  [m]
  (let [kscoll   (keys-in m)
        flat-k+v (fn [ks] [(path->str ks) (get-in m ks)])]
    (into {} (map flat-k+v kscoll))))

(defn- properties
  "Make a Properties object based on a map m."
  [m]
  (let [props (Properties.)]
    (.putAll props (flatten-map m))
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
