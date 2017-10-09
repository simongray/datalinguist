(ns corenlp-clj.example
  (:require [corenlp-clj.core :refer :all]
            [corenlp-clj.semgraph :refer [dependencies]]))

;; pipeline for dependency parsing, lemmatisation and named entity recognition
(def nlp (pipeline {"annotators" (prerequisites ["depparse" "lemma" "ner"])}))

;; parts of speech
(-> "This is an example sentence. That is another."
    nlp
    sentences
    tokens
    pos)
;=> (("DT" "VBZ" "DT" "NN" "NN" ".") ("DT" "VBZ" "DT" "."))

;; named entity tags
(-> "Anna went travelling in China."
    nlp
    sentences
    tokens
    ner)
;=> (("PERSON" "O" "O" "O" "LOCATION" "O"))

;; lemmatisation
(-> "She has beaten him before."
    nlp
    sentences
    tokens
    lemma)
;=> (("she" "have" "beat" "he" "before" "."))

;; dependencies (according to dependency grammar)
(-> "A sentence has dependencies."
    nlp
    sentences
    dependencies)
;=>
;(#object[edu.stanford.nlp.semgraph.SemanticGraph
;         0x3ed8576b
;         "-> has/VBZ (root)
;            -> sentence/NN (nsubj)
;              -> A/DT (det)
;            -> dependencies/NNS (dobj)
;            -> ./. (punct)
;          "])
