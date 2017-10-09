(ns corenlp-clj.example
  (:require [corenlp-clj.core :refer [pipeline prerequisites sentences tokens pos ner lemma text whitespace]]
            [corenlp-clj.semgraph :refer [dependencies]]))

;; pipeline for dependency parsing, lemmatisation and named entity recognition
(def nlp (pipeline {"annotators" (prerequisites ["depparse" "lemma" "ner"])}))

;; parts of speech
(->> "This is an example sentence. That is another." nlp sentences tokens pos)
;=> (("DT" "VBZ" "DT" "NN" "NN" ".") ("DT" "VBZ" "DT" "."))

;; named entity tags
(->> "Anna went travelling in China." nlp tokens ner)
;=> ("PERSON" "O" "O" "O" "LOCATION" "O")

;; lemmatisation
(->> "She has beaten him before." nlp tokens lemma)
;=> ("she" "have" "beat" "he" "before" ".")

;; words
(->> "You can also just get the words." nlp tokens text)
;=> ("You" "can" "also" "just" "get" "the" "words" ".")

;; whitespace (defaults to before)
(->> "   A sentence   with whitespace.   " nlp tokens whitespace)
;=> ("   " " " "   " " " "")

;; dependencies (defaults to enhanced++)
(->> "A sentence has dependencies." nlp sentences dependencies)
;=> (#object[edu.stanford.nlp.semgraph.SemanticGraph])
;            0x3ed8576b
;            "-> has/VBZ (root)
;               -> sentence/NN (nsubj)
;                 -> A/DT (det)
;               -> dependencies/NNS (dobj)
;               -> ./. (punct)
;             "])
