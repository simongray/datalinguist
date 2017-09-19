(ns corenlp-clj.example
  (:require [corenlp-clj.core :refer :all]))

;; how to set up the dependency parse annotator
(def nlp (pipeline (properties "annotators" "tokenize, ssplit, pos, depparse")))
