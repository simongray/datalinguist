(ns dk.simongray.datalinguist.configs
  "Pipeline configurations for various languages. Need when deviating from the
  default configuration for English or when annotating with another language.")

;; Adapted from configuration previously found on this page (now missing):
;;   https://stanfordnlp.github.io/CoreNLP/human-languages.html#chinese
(def chinese
  {:annotators "tokenize,ssplit,pos,depparse",
   :depparse   {:model "edu/stanford/nlp/models/parser/nndep/UD_Chinese.gz"},
   :ndepparse  {:language "chinese"},
   :tokenize   {:language "zh"},
   :segment    {:model                "edu/stanford/nlp/models/segmenter/chinese/ctb.gz",
                :sighanCorporaDict    "edu/stanford/nlp/models/segmenter/chinese",
                :serDictionary        "edu/stanford/nlp/models/segmenter/chinese/dict-chris6.ser.gz",
                :sighanPostProcessing "true"},
   :ssplit     {:boundaryTokenRegex "[.。]|[!?！？]+"},
   :pos        {:model "edu/stanford/nlp/models/pos-tagger/chinese-distsim.tagger"}})
