(ns dk.simongray.datalinguist
  (:require [clojure.string :as str])
  (:import [java.util Properties]
           [edu.stanford.nlp.pipeline StanfordCoreNLP]))

(defn- keys-in
  "Get the nested keys in map `m`."
  [m]
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

(defn- ks->str
  "Convert `ks` (e.g. from keys-in) to a flattened CoreNLP key."
  [ks]
  (str/join "." (map name ks)))

(defn- flatten-map
  "Flatten a map `m` of nested keys."
  [m]
  (let [kscoll   (keys-in m)
        flat-k+v (fn [ks] [(ks->str ks) (get-in m ks)])]
    (into {} (map flat-k+v kscoll))))

(defn- properties
  "Make a Properties object based on a map `m`."
  [m]
  (doto (Properties.)
    (.putAll (flatten-map m))))

(defn- attach-prerequisites!
  "Attach prerequisites of `annotators` for pipeline defined in `props`."
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
  (let [props    (attach-prerequisites! (properties conf))
        core-nlp (StanfordCoreNLP. ^Properties props true)]
    (fn [^String s]
      (.process core-nlp s))))
