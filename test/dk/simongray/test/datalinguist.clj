(ns dk.simongray.test.datalinguist
  (:require [clojure.test :refer :all]
            [dk.simongray.datalinguist :refer :all]
            [dk.simongray.datalinguist.dependency :refer :all])
  (:import [edu.stanford.nlp.ling IndexedWord]))

(def en-nlp
  (delay (->pipeline {:annotators ["truecase"               ; TrueCaseAnnotation
                                   "quote"                  ; QuotationsAnnotation
                                   "entitymentions"         ; MentionsAnnotation
                                   "parse"                  ; TreeAnnotation
                                   "depparse"
                                   "lemma"
                                   "coref"                  ; required by "quote"
                                   "openie"                 ; RelationTriplesAnnotation
                                   ;; TODO: issue #4 - kbp doesn't work
                                   ;"kbp"                 ; KBPTriplesAnnotation
                                   "ner"]
                      :quote      {:extractUnclosedQuotes "true"}})))

(def sweden-example
  (@en-nlp "I flew to Sweden with Mary to attend a conference in Ystad."))

(def sweden-example-lc
  (@en-nlp "i flew to sweden with mary to attend a conference in ystad."))

(deftest test-text
  (testing :plain
    (let [actual1  (-> sweden-example tokens text)
          actual2  (->> sweden-example tokens (text :plain))
          expected ["I" "flew" "to" "Sweden" "with" "Mary" "to" "attend" "a" "conference" "in" "Ystad" "."]]
      (is (= actual1 actual2 expected))

      ;; Fetches the text annotation as a full string at the document level.
      (is (string? (text sweden-example)))))

  (testing :true-case
    (let [actual1  (->> sweden-example-lc tokens (text :true-case))
          actual2  (text :true-case sweden-example-lc)
          expected ["I" "flew" "to" "Sweden" "with" "Mary" "to" "attend" "a" "conference" "in" "Ystad" "."]]
      (is (= actual1 actual2 expected)))))

(deftest test-true-case
  (let [actual1  (-> sweden-example tokens true-case)
        actual2  (true-case sweden-example)
        expected ["UPPER" "LOWER" "LOWER" "INIT_UPPER" "LOWER" "INIT_UPPER" "LOWER" "LOWER" "LOWER" "LOWER" "LOWER" "INIT_UPPER" "O"]]
    (is (= actual1 actual2 expected))))

(deftest test-quotations
  (testing :closed
    (let [example  (@en-nlp "He said: \"never\".")
          actual1  (-> example quotations text)
          actual2  (->> example (quotations :closed) text)
          expected ["\"never\""]]
      (is (= actual1 actual2 expected))))

  (testing :unclosed
    (let [example  (@en-nlp "He said: \"never.")
          actual   (->> example (quotations :unclosed) text)
          expected ["\"never."]]
      (is (= actual expected)))))

;; TODO: :token, :index styles
(deftest test-index
  ;; The :quote index just IDs the quotations in order (from 0).
  (testing :quote
    (let [example   (@en-nlp "He said \"marry me\"; she said: \"ok.\"")
          actual1   (->> example quotations (index :quote))
          actual2   (index :quote example)
          expected1 [0 1]
          expected2 [nil nil 0 0 0 0 nil nil nil nil 1 1 1 1]]
      (is (= actual1 expected1))

      ;; Fetches the quote index at the token level instead.
      (is (= actual2 expected2)))))

(deftest test-pos
  (let [actual1  (-> sweden-example tokens pos)
        actual2  (pos sweden-example)
        expected ["PRP" "VBD" "IN" "NNP" "IN" "NNP" "TO" "VB" "DT" "NN" "IN" "NNP" "."]]
    (is (= actual1 actual2 expected))))

(deftest test-named-entity-recognition
  (testing :tag
    (let [actual1  (-> sweden-example tokens named-entity)
          actual2  (->> sweden-example tokens (named-entity :tag))
          actual3  (named-entity :tag sweden-example)
          expected ["O" "O" "O" "COUNTRY" "O" "PERSON" "O" "O" "O" "O" "O" "CITY" "O"]]
      (is (= actual1 actual2 actual3 expected))))

  (testing :fine
    (let [actual1  (->> sweden-example tokens (named-entity :fine))
          actual2  (named-entity :fine sweden-example)
          expected ["O" "O" "O" "COUNTRY" "O" "PERSON" "O" "O" "O" "O" "O" "CITY" "O"]]
      (is (= actual1 actual2 expected))))

  (testing :coarse
    (let [actual1  (->> sweden-example tokens (named-entity :coarse))
          actual2  (named-entity :coarse sweden-example)
          expected ["O" "O" "O" "LOCATION" "O" "PERSON" "O" "O" "O" "O" "O" "LOCATION" "O"]]
      (is (= actual1 actual2 expected))))

  (testing :probs
    (let [actual1  (->> sweden-example tokens (named-entity :probs))
          actual2  (named-entity :probs sweden-example)
          expected ["O" "O" "O" "LOCATION" "O" "PERSON" "O" "O" "O" "O" "O" "LOCATION" "O"]]
      (is (= (map first actual1) (map first actual2) expected)))))

(deftest test-numeric
  (let [example (@en-nlp "It was his twenty first time.")]
    (testing :value
      (let [actual1  (-> example tokens numeric)
            actual2  (->> example tokens (numeric :value))
            actual3  (numeric :value example)
            expected [nil nil nil 20 1 nil nil]]
        (is (= actual1 actual2 actual3 expected))))

    (testing :type
      (let [actual1  (->> example tokens (numeric :type))
            actual2  (numeric :type example)
            expected [nil nil nil "NUMBER" "ORDINAL" nil nil]]
        (is (= actual1 actual2 expected))))

    ;; TODO: nothing here - how to get :composite annotations?
    (testing :composite)

    (testing :composite-value
      (let [actual1  (->> example tokens (numeric :composite-value))
            actual2  (numeric :composite-value example)
            expected [nil nil nil 21.0 21.0 nil nil]]
        (is (= actual1 actual2 expected))))

    (testing :composite-type
      (let [actual1  (->> example tokens (numeric :composite-type))
            actual2  (numeric :composite-type example)
            expected [nil nil nil "ORDINAL" "ORDINAL" nil nil]]
        (is (= actual1 actual2 expected))))

    (testing :normalized
      (let [example  (@en-nlp "He went to Sweden for a week the first time.")
            actual1  (->> example tokens (numeric :normalized))
            actual2  (numeric :normalized example)
            expected [nil nil nil nil nil "P1W" "P1W" nil "1.0" nil nil]]
        (is (= actual1 actual2 expected))))))

(deftest test-mentions
  (let [actual1  (-> sweden-example mentions text)
        expected ["Sweden" "Mary" "Ystad"]]
    (is (= actual1 expected))))

(deftest test-semgrex
  (let [example (->> (@en-nlp "He sliced some slices of lemon into even smaller slices.")
                     dk.simongray.datalinguist/sentences
                     first
                     dk.simongray.datalinguist/dependency-graph)]

    ;; Find the first matching node
    (is (= "slice" (.lemma (sem-find (sem-pattern "{lemma:slice}") example))))
    (is (= "lemon" (.lemma (sem-find (sem-pattern "{lemma:lemon}") example))))

    ;; Find every matching node
    (is (= 2 (count (sem-seq (sem-pattern "{lemma:slice;tag:/NN.?/}") example))))

    ;; Find pattern with named nodes and relations
    (let [deps  (sem-seq (sem-pattern "{lemma:slice} > {}=dependent") example)
          relns (sem-seq (sem-pattern "{lemma:slice} >=reln {}") example)
          both  (sem-seq (sem-pattern "{lemma:slice} >=reln {}=dependent") example)
          all   (concat deps relns both)]
      (is (= 8 (count deps) (count relns) (count both)))
      (is (every? vector all))
      (is (every? #(instance? IndexedWord %) (map first all)))
      (is (every? map? (map second all)))
      (is (= #{:dependent} (set (keys (second (first deps))))))
      (is (= #{:reln} (set (keys (second (first relns))))))
      (is (= #{:reln :dependent} (set (keys (second (first both)))))))

    ;; See if the pattern "matches" (the root must match)
    (is (some? (sem-matches (sem-pattern "{lemma:slice}") example)))
    (is (nil? (sem-matches (sem-pattern "{lemma:lemon}") example)))))