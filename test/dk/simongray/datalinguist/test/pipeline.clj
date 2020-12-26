(ns dk.simongray.datalinguist.test.pipeline
  (:require [clojure.test :refer :all]
            [dk.simongray.datalinguist :as dl]))

(def en
  (delay (dl/->pipeline {:annotators ["truecase"            ; TrueCaseAnnotation
                                      "quote"               ; QuotationsAnnotation
                                      "depparse"
                                      "lemma"
                                      "ner"]
                         :quote      {:extractUnclosedQuotes "true"}})))
