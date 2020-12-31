(ns dk.simongray.datalinguist.static
  "Collections of more or less static data to be used from other namespaces."
  (:import [edu.stanford.nlp.ling CoreLabel$OutputFormat]))


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

(def configs
  "Example pipeline configurations for various languages or special setups."

  ;; Adapted from configuration previously found on this page (now missing):
  ;;   https://stanfordnlp.github.io/CoreNLP/human-languages.html#chinese
  {:chinese {:annotators "tokenize,ssplit,pos,depparse",
             :depparse   {:model "edu/stanford/nlp/models/parser/nndep/UD_Chinese.gz"},
             :ndepparse  {:language "chinese"},
             :tokenize   {:language "zh"},
             :segment    {:model                "edu/stanford/nlp/models/segmenter/chinese/ctb.gz",
                          :sighanCorporaDict    "edu/stanford/nlp/models/segmenter/chinese",
                          :serDictionary        "edu/stanford/nlp/models/segmenter/chinese/dict-chris6.ser.gz",
                          :sighanPostProcessing "true"},
             :ssplit     {:boundaryTokenRegex "[.。]|[!?！？]+"},
             :pos        {:model "edu/stanford/nlp/models/pos-tagger/chinese-distsim.tagger"}}})
