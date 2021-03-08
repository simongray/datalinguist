(ns dk.simongray.datalinguist.ml.crf
  (:require [clojurewerkz.propertied.properties :as p]
            [tech.v3.dataset :as ds]
            [tech.v3.dataset.modelling :as ds-mod]
            [tech.v3.dataset.column-filters :as cf]
            [tech.v3.ml :as ml]            )
  (:import [edu.stanford.nlp.sequences SeqClassifierFlags]
           [edu.stanford.nlp.ie.crf CRFClassifier]

           [java.io File])
  )



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

(def file-options
  {
   "map" "word=0,answer=1"}
  )

(defn- train-crf [dataset word-col tag-col options]
  (let [training-file (File/createTempFile "ner-data" ".tsv")
        props
        (-> options
            (merge file-options)
            (assoc
             "trainFile" (.getAbsolutePath training-file))
            (p/map->properties)
            )

        _ (-> dataset
              (ds/select-columns [word-col tag-col])
              (ds/write! (.getAbsolutePath training-file)
                         ;; {:skip-headers true}
                         )
              )
        crf (CRFClassifier. props)
        _ (.train crf)]
    crf))



(defn- predict-crf [dataset text-col crf]
  (-> dataset
      (ds/add-column
       (ds/new-column :ner
                      (map
                       #(.classifyToCharacterOffsets crf %)
                       (get dataset text-col))))))



(defn train [feature-ds target-ds options]
  (train-crf
   (ds/concat feature-ds target-ds)
   (first (ds/column-names feature-ds))
   (first (ds/column-names target-ds))
   (merge standard-props (:cfr-options options))))


(defn predict [feature-ds thawed-model model]
  (predict-crf feature-ds (first  (ds/column-names feature-ds)) thawed-model))

(ml/define-model! :standford-nlp/cfr
  train
  predict
  {})


(comment
  (def train-ds
    (->
     (ds/->dataset "./test/data/jane-austen-emma-ch1.tsv" {:header-row? false :key-fn keyword})
     (ds-mod/set-inference-target :column-1)))

  (def model
    (ml/train train-ds {:model-type :standford-nlp/cfr}))

  (def text-ds
    (ds/->dataset
     {:column-0 ["Mr." "Smith" "Mrs." "Weston" ]})
    )

   (ml/predict text-ds model)
  )
