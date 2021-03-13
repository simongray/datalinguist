(ns dk.simongray.test.ml-test
  (:require [clojure.test :refer :all]
            [tech.v3.dataset.modelling :as ds-mod]
            [tech.v3.dataset :as ds]
            [tech.v3.ml :as ml]
            [dk.simongray.datalinguist.ml.crf :refer :all])

  )


(deftest train-predict

  (let [train-ds
        (->
         (ds/->dataset "./test/data/jane-austen-emma-ch1.tsv" {:header-row? false :key-fn keyword})
         (ds-mod/set-inference-target :column-1))

        model
        (ml/train train-ds {:model-type :standford-nlp/crf})

        text-ds
        (ds/->dataset
         {:column-0 ["I like Mr. X and I like  Mr. Y" "Smith" "Mrs." "Weston" ]})


        prediction (ml/predict text-ds model)]

    (is (= [ ["PERS" 7 10]
             ["PERS" 25 30]]
           (first (:ner prediction))
           ))))
