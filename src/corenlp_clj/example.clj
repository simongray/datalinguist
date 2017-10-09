(ns corenlp-clj.example
  (:require [corenlp-clj.core :refer :all]
            [corenlp-clj.semgraph :refer [dependencies]]))

;; loading a dependency parsing pipeline
(def nlp (pipeline {"annotators" (prerequisites "depparse")}))

(def example "This is an example sentence. That is another.")

;; accessing various annotations
(def example-tokens (-> example
                        nlp
                        sentences
                        tokens))

(def example-dependencies (-> example
                              nlp
                              sentences
                              dependencies))
