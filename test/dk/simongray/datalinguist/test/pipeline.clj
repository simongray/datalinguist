(ns dk.simongray.datalinguist.test.pipeline
  (:require [clojure.test :refer :all]
            [dk.simongray.datalinguist :as dl]))

(def en
  (delay (dl/->pipeline {:annotators ["truecase"            ; TrueCaseAnnotation
                                      "quote"               ; QuotationsAnnotation
                                      "entitymentions"      ; MentionsAnnotation
                                      "depparse"
                                      "lemma"
                                      ;; TODO: issue #4 - kbp doesn't work
                                      ;"kbp"                 ; KBPTriplesAnnotation
                                      "ner"]
                         :quote      {:extractUnclosedQuotes "true"}})))
