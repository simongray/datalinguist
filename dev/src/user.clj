(ns user
  (:require [clojure.reflect :refer [reflect]]
            [clojure.datafy :refer [datafy nav]]
            [dk.simongray.datalinguist :refer :all]
            [dk.simongray.datalinguist.static :as static]
            [dk.simongray.datalinguist.dependency :as dependency]
            [dk.simongray.datalinguist.loom.io :refer [view]])
  (:import [edu.stanford.nlp.ling CoreAnnotations$PartOfSpeechAnnotation]))

(set! *warn-on-reflection* true)

(defn en-pipeline
  []
  (->pipeline {:annotators ["depparse" "lemma" "ner"]}))

;; https://stanfordnlp.github.io/CoreNLP/human-languages.html#chinese
(defn zh-pipeline
  []
  (->pipeline (:chinese static/configs)))

(comment
  ;;; ENGLISH
  ;; create a custom Stanford CoreNLP pipeline
  ;; English is the default language and a pipeline setup rarely requires many params
  (def nlp (en-pipeline))

  ;; using class names
  (->> "Any annotation can be accessed using the proper Annotation class name."
       nlp
       tokens
       (annotation CoreAnnotations$PartOfSpeechAnnotation))
  ;=> ("DT" "NN" "MD" "VB" "VBN" "VBG" "DT" "JJ" "NNP" "NN" "NN" ".")

  ;; parts of speech
  (->> "This is an example sentence. That is another."
       nlp
       sentences
       tokens
       pos)
  ;=> (("DT" "VBZ" "DT" "NN" "NN" ".") ("DT" "VBZ" "DT" "."))

  ;; named entity tags
  (->> "Anna went travelling in China."
       nlp
       tokens
       named-entity)
  ;=> ("PERSON" "O" "O" "O" "LOCATION" "O")

  ;; lemmatisation
  (->> "She has beaten him before."
       nlp
       tokens
       lemma)
  ;=> ("she" "have" "beat" "he" "before" ".")

  ;; words
  (->> "You can also just get the words."
       nlp
       tokens
       text)
  ;=> ("You" "can" "also" "just" "get" "the" "words" ".")

  ;; indexes (defaults to token)
  (->> "Word indexes start from 1."
       nlp
       tokens
       index)
  ;=> (1 2 3 4 5 6)

  (->> "Word indexes start from 1. Sentences start from 0."
       nlp
       tokens
       (index :sentence))
  ;=> (0 0 0 0 0 0 1 1 1 1 1)

  ;; whitespace (defaults to before)
  (->> "   A sentence   with whitespace.   "
       nlp
       tokens
       whitespace)
  ;=> ("   " " " "   " " " "")

  ;; dependencies (defaults to enhanced++)
  (->> "A sentence has dependencies."
       nlp
       sentences
       dependency-graph)
  ;=> (#object[edu.stanford.nlp.semgraph.SemanticGraph])
  ;            0x3ed8576b
  ;            "-> has/VBZ (root)
  ;               -> sentence/NN (nsubj)
  ;                 -> A/DT (det)
  ;               -> dependencies/NNS (dobj)
  ;               -> ./. (punct)
  ;             "])

  ;; viewing a visualisation of sentence dependencies (requires Graphviz)
  (view (->> "The dependencies of this sentence have been visualised using Graphviz."
             nlp
             sentences
             dependency-graph
             first))
  ;=> https://raw.githubusercontent.com/simongray/corenlp-clj/master/doc/graphviz_example.png


  ;;; CHINESE
  ;; create a custom Chinese Stanford CoreNLP pipeline
  ;; straying from the default English parameters requires some additional setup
  (def nlp (zh-pipeline))

  ;; words are segmented as part of the annotation process
  (->> "妈妈骂马吗？我不清楚，你问问她。"
       nlp
       tokens
       text)
  ;=> ("妈妈" "骂" "马" "吗" "？" "我" "不" "清楚" "，" "你" "问问" "她" "。")

  ;; Chinese is also a supported language for grammatical dependencies
  (->> "有一次他大胆提出了自己的看法。"
       nlp
       sentences
       first
       dependency-graph))
