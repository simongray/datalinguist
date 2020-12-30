(ns dk.simongray.test.datalinguist.pipeline
  (:require [clojure.test :refer :all]
            [dk.simongray.datalinguist :as dl]))

(def en
  (delay (dl/->pipeline {:annotators ["truecase"            ; TrueCaseAnnotation
                                      "quote"               ; QuotationsAnnotation
                                      "entitymentions"      ; MentionsAnnotation
                                      "parse"               ; TreeAnnotation
                                      "depparse"
                                      "lemma"
                                      ;; TODO: issue #4 - kbp doesn't work
                                      ;"kbp"                 ; KBPTriplesAnnotation
                                      "ner"]
                         :quote      {:extractUnclosedQuotes "true"}})))
