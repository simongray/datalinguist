(ns tech.ml.ner
  (:require [tech.v3.dataset :as dataset]
            [tech.v3.dataset.modelling :as ds-mod]
            [tech.v3.dataset.column-filters :as cf]
            [tech.ml.standfort-nlp :as nlp]
            [tech.v3.ml :as ml]
            )
  )


; https://towardsdatascience.com/a-review-of-named-entity-recognition-ner-using-automatic-summarization-of-resumes-5248a75de175
(def standard-props
  {

   ;; location where you would like to save (serialize) your
   ;; classifier             ; adding .gz at the end automatically gzips the file,
   ;; making it smaller , and faster to load
   "serializeTo"   "ner-model.ser.gz"
   ;; structure of your training file     ; this tells the classifier that
   ;; the word is in column 0 and the correct answer is in column 1
   ;; This specifies the order of the CRF: order 1 means that features
   ;; apply at most to a class pair of previous class and current class
   ;; or current class and next class.
   "maxLeft" "1"
   ;; these are the features we'd like to train with
   ;; some are discussed below , the rest can be
   ;; understood by looking at NERFeatureFactory
   "useClassFeature" "true"
   "useWord" "true"
   ;; word character ngrams will be included up to length 6 as prefixes
   ;; and suffixes only
   "useNGrams" "true"
   "noMidNGrams" "true"
   "maxNGramLeng" "6"
   "usePrev" "true"
   "useNext" "true"
   "useDisjunctive" "true"
   "useSequences" "true"
   "usePrevSequences" "true"
   ;; the last 4 properties deal with word shape features
   "useTypeSeqs" "true"
   "useTypeSeqs2" "true"
   "useTypeySequences" "true"
   ;;wordShape chris2useLC
   "wordShape" "none"
  ;;useBoundarySequences true
  ;;useNeighborNGrams true
  ;;useTaggySequences true
  ;;printFeatures true
  ;;saveFeatureIndexToDisk   true
  ;;useObservedSequencesOnly   true
  ;;useWordPairs   true
   }
  )


(defn train [feature-ds target-ds options]
  (nlp/train
   (dataset/concat feature-ds target-ds)
   (first (dataset/column-names feature-ds))
   (first (dataset/column-names target-ds))
   (merge standard-props (:cfr-options options))))


(defn predict [feature-ds thawed-model model]
  (def model model)
  (def thawed-model thawed-model)
  (nlp/predict feature-ds (first  (dataset/column-names feature-ds)) thawed-model))

(ml/define-model! :standford-nlp/cfr
  train
  predict
  {})


(comment
  (def train-ds
    (->
     (dataset/->dataset "./data/jane-austen-emma-ch1.tsv" {:header-row? false :key-fn keyword})
     (ds-mod/set-inference-target :column-1)))

  (def model
    (ml/train train-ds {:model-type :standford-nlp/cfr}))

  (def text-ds
    (dataset/->dataset
     {:column-0 ["Mr." "Smith" "Mrs." "Weston" ]})
    )

  (ml/predict text-ds model)
  )
