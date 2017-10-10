(ns corenlp-clj.example
  (:require [corenlp-clj.core :refer :all]
            [corenlp-clj.annotations :refer :all])
  (:import (edu.stanford.nlp.ling CoreAnnotations$PartOfSpeechAnnotation)))

;; create a custom Stanford CoreNLP pipeline
(def nlp (pipeline {"annotators" (prerequisites ["depparse" "lemma" "ner"])}))

;; using class names
(->> "Any annotation can be accessed using the proper Annotation class name."
     nlp
     tokens
     (annotation CoreAnnotations$PartOfSpeechAnnotation))
;=> ("DT" "NN" "MD" "VB" "VBN" "VBG" "DT" "JJ" "NNP" "NN" "NN" ".")

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

;; indexes (defaults to token)
(->> "Word indexes start from 1." nlp tokens index)
;=> (1 2 3 4 5 6)

(->> "Word indexes start from 1. Sentences start from 0."
     nlp tokens (index :sentence))
;=> (0 0 0 0 0 0 1 1 1 1 1)

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
