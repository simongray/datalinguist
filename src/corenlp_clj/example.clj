(ns corenlp-clj.example
  (:require [corenlp-clj.core :refer :all]))

;; how to set up the dependency parse annotator
(def nlp (pipeline (properties "annotators" "tokenize, ssplit, pos, depparse")))

(def example "This is just an example sentence in English. This is another.")

;; the long way of getting a dependency graph
(def deps-long (map dependencies (sentences (annotation nlp example))))

;; the short way of getting a dependency graph
(def deps-short (dep nlp example))
