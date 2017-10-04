(ns corenlp-clj.example
  (:require [corenlp-clj.core :refer :all]))

;; the long way of setting up the dependency parse annotator
;(def nlp (pipeline (properties "annotators" "tokenize, ssplit, pos, depparse")))

;; the short way of setting up the dependency parse annotator
(def nlp (pipeline depparse))

(def example "This is just an example sentence in English. This is another.")

;; the long way of getting a dependency graph
(def deps-long (map dependencies (sentences (annotation nlp example))))

;; the short way of getting a dependency graph
(def deps (dep nlp example))
