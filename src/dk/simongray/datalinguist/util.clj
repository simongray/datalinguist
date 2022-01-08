(ns dk.simongray.datalinguist.util
  "Various utility functions used from the other namespaces, along with
  collections of more or less static data."
  (:require [clojure.string :as str])
  (:import [java.util Properties]
           [edu.stanford.nlp.ling CoreLabel
                                  CoreLabel$OutputFormat
                                  CoreAnnotations$TrueCaseTextAnnotation
                                  CoreAnnotations$PartOfSpeechAnnotation]))

;; TODO: document annotator support for the official language models
;;       https://stanfordnlp.github.io/CoreNLP/human-languages.html
;; TODO: implement a spellcheck for annotators during pipeline creation
;;       https://en.wikibooks.org/wiki/Clojure_Programming/Examples/Norvig_Spelling_Corrector
(def annotators
  "The list of annotators included with CoreNLP."
  #{"cdc"
    "cleanxml"
    "coref"
    "coref.mention"
    "dcoref"
    "depparse"
    "docdate"
    "entitylink"
    "entitymentions"
    "gender"
    "kbp"
    "lemma"
    "mwt"
    "natlog"
    "ner"
    "openie"
    "parse"
    "pos"
    "quote"
    "quote.attribution"
    "regexner"
    "relation"
    "sentiment"
    "ssplit"
    "tokenize"
    "tokensregex"
    "truecase"
    "udfeats"})

(def corelabel-formats
  "Ways to format CoreLabels. Per the convention of CoreNLP, word = value."
  {:all             CoreLabel$OutputFormat/ALL
   :lemma-index     CoreLabel$OutputFormat/LEMMA_INDEX
   :map             CoreLabel$OutputFormat/MAP
   :value           CoreLabel$OutputFormat/VALUE
   :value-index     CoreLabel$OutputFormat/VALUE_INDEX
   :value-index-map CoreLabel$OutputFormat/VALUE_INDEX_MAP
   :value-map       CoreLabel$OutputFormat/VALUE_MAP
   :value-tag       CoreLabel$OutputFormat/VALUE_TAG
   :value-tag-index CoreLabel$OutputFormat/VALUE_TAG_INDEX
   :value-tag-ner   CoreLabel$OutputFormat/VALUE_TAG_NER
   :word            CoreLabel$OutputFormat/WORD
   :word-index      CoreLabel$OutputFormat/WORD_INDEX})

(def punctuation-tags
  "Part-of-speech tags for punctuation, copied from getPunctuationTags() in the
  `edu.stanford.nlp.parser.nndep.ParsingSystem` abstract class."
  #{"''"
    ","
    "."
    ":"
    "``"
    "-LRB-"
    "-RRB-"})

(def configs
  "Example pipeline configurations for various languages or special setups."

  ;; Adapted from configuration previously found on this page (now missing):
  ;;   https://stanfordnlp.github.io/CoreNLP/human-languages.html#chinese
  {:chinese {:annotators "tokenize,ssplit,pos,parse,depparse",
             :parse      {:model "edu/stanford/nlp/models/srparser/chineseSR.ser.gz"}
             :depparse   {:model "edu/stanford/nlp/models/parser/nndep/UD_Chinese.gz"},
             :ndepparse  {:language "chinese"},
             :tokenize   {:language "zh"},
             :segment    {:model                "edu/stanford/nlp/models/segmenter/chinese/ctb.gz",
                          :sighanCorporaDict    "edu/stanford/nlp/models/segmenter/chinese",
                          :serDictionary        "edu/stanford/nlp/models/segmenter/chinese/dict-chris6.ser.gz",
                          :sighanPostProcessing "true"},
             :ssplit     {:boundaryTokenRegex "[.。]|[!?！？]+"},
             :pos        {:model "edu/stanford/nlp/models/pos-tagger/chinese-distsim.tagger"}}})

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

(defn properties
  "Make a Properties object based on a map `m`."
  [m]
  (doto (Properties.)
    (.putAll (flatten-map m))))

(defn tokens->string
  "Get a normalised string representation of the given `tokens`."
  [tokens]
  (let [token->text (fn [^CoreLabel token]
                      (str
                        (or (.before token) " ")
                        (or (.get token CoreAnnotations$TrueCaseTextAnnotation)
                            (.word token))))]
    (str/triml (str/join (map token->text tokens)))))

(defn tokens->keyword
  "Get a normalised keyword representation of the given `tokens`."
  [tokens]
  (let [non-word? (fn [^CoreLabel token]
                    (let [tag (.get token CoreAnnotations$PartOfSpeechAnnotation)]
                      (or (get punctuation-tags tag)
                          (re-matches #"\W" (.word token)))))]
    (->> (remove non-word? tokens)
         (map (comp str/lower-case #(.word ^CoreLabel %)))
         (str/join "-")
         keyword)))

