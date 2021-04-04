(ns dk.simongray.datalinguist.ml.crf
  (:require [tech.v3.dataset :as ds]
            [dk.simongray.datalinguist.util :as util]
            [scicloj.metamorph.ml :as ml])
  (:import [edu.stanford.nlp.ie.crf CRFClassifier]
           [java.io File]))


(def default-opts
  {;; location where you would like to save (serialize) your classifier
   ;; adding .gz at the end automatically gzips the file, making it smaller, and
   ;; faster to load
   :serializeTo       "ner-model.ser.gz"
   ;; structure of your training file     ; this tells the classifier that
   ;; the word is in column 0 and the correct answer is in column 1
   ;; This specifies the order of the CRF: order 1 means that features
   ;; apply at most to a class pair of previous class and current class
   ;; or current class and next class.
   :maxLeft           "1"
   ;; these are the features we'd like to train with
   ;; some are discussed below , the rest can be
   ;; understood by looking at NERFeatureFactory
   :useClassFeature   "true"
   :useWord           "true"
   ;; word character ngrams will be included up to length 6 as prefixes
   ;; and suffixes only
   :useNGrams         "true"
   :noMidNGrams       "true"
   :maxNGramLeng      "6"
   :usePrev           "true"
   :useNext           "true"
   :useDisjunctive    "true"
   :useSequences      "true"
   :usePrevSequences  "true"
   ;; the last 4 properties deal with word shape features
   :useTypeSeqs       "true"
   :useTypeSeqs2      "true"
   :useTypeySequences "true"
   ;;wordShape chris2useLC
   :wordShape         "none"
   #_#_:useBoundarySequences "true"
   #_#_:useNeighborNGrams "true"
   #_#_:useTaggySequences "true"
   #_#_:printFeatures "true"
   #_#_:saveFeatureIndexToDisk "true"
   #_#_:useObservedSequencesOnly "true"
   #_#_:useWordPairs "true"})

(def file-opts
  {:map "word=0,answer=1"})

(defn- train-crf
  [dataset word-col tag-col opts]
  (let [training-file (File/createTempFile "ner-data" ".tsv")
        props         (-> opts
                          (merge file-opts)
                          (assoc
                           "trainFile" (.getAbsolutePath training-file))
                          (util/properties))]
    (-> dataset
        (ds/select-columns [word-col tag-col])
        (ds/write! (.getAbsolutePath training-file) {:skip-headers true}))
    (doto (CRFClassifier. props)
      (.train))))

(defn- triple->seq
  [triples]
  (map #(.asList %) triples))

(defn- predict-crf
  [dataset word-col model]
  (->> (get dataset word-col)
       (map #(triple->seq (.classifyToCharacterOffsets model %)))
       (ds/new-column :ner)
       (ds/add-column dataset)))

(defn train
  "Train an edu.stanford.nlp.ie.crf.CRFClassifier.

  The training data needs to be given as tokens in the feature column of the
  dataset and the gold class needs to be given in the inference-target column of
  the dataset. The options maps can contain any option specified in the javadoc
  of CRFClassifier.

  This methods gets called indirectly by `tech.ml`."
  [feature-dataset target-dataset opts]
  (train-crf (ds/concat feature-dataset target-dataset)
             (first (ds/column-names feature-dataset))
             (first (ds/column-names target-dataset))
             (merge default-opts opts)))

;; TODO: what's with the unused model param?
(defn predict
  "Predict on new data using the CRF classifier.

  The data need to be given a texts in the feature column of the dataset.

  This method gets called indirectly by `tech.ml`."
  [feature-dataset thawed-model model]
  (let [word-col (first (ds/column-names feature-dataset))]
    (predict-crf feature-dataset word-col thawed-model)))



(def metamorph-ml-options
  (map
   (fn [[k v]]
     (hash-map :name k :type :string :default v))
   default-opts
   ))

(ml/define-model! :corenlp/crf train predict
  {:documentation {:javadoc "https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/ie/crf/CRFClassifier.html"
                   :user-guide "https://nlp.stanford.edu/software/CRF-NER.html"}
   :options metamorph-ml-options
   })


