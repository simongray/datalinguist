(ns dk.simongray.datalinguist.test.annotation
  (:require [clojure.test :refer :all]
            [dk.simongray.datalinguist.test.pipeline :as pipeline]
            [dk.simongray.datalinguist.annotation :refer :all]))

(def example
  (@pipeline/en "I flew to Sweden with Mary to attend a conference in Ystad."))

(def example-lower
  (@pipeline/en "i flew to sweden with mary to attend a conference in ystad."))

(deftest test-text
  (testing :plain
    (let [actual1  (-> example tokens text)
          actual2  (->> example tokens (text :plain))
          expected ["I" "flew" "to" "Sweden" "with" "Mary" "to" "attend" "a" "conference" "in" "Ystad" "."]]
      (is (= actual1 actual2 expected))))

  (testing :true-case
    (let [actual   (->> example-lower tokens (text :true-case))
          expected ["I" "flew" "to" "Sweden" "with" "Mary" "to" "attend" "a" "conference" "in" "Ystad" "."]]
      (is (= actual expected)))))

(deftest test-true-case
  (let [actual   (-> example tokens true-case)
        expected ["UPPER" "LOWER" "LOWER" "INIT_UPPER" "LOWER" "INIT_UPPER" "LOWER" "LOWER" "LOWER" "LOWER" "LOWER" "INIT_UPPER" "O"]]
    (is (= actual expected))))

(deftest test-quotations
  (testing :closed
    (let [quote-example (@pipeline/en "He said: \"never\".")
          actual1       (-> quote-example quotations text)
          actual2       (->> quote-example (quotations :closed) text)
          expected      ["\"never\""]]
      (is (= actual1 actual2 expected))))

  (testing :unclosed
    (let [actual   (->> (@pipeline/en "He said: \"never.") (quotations :unclosed) text)
          expected ["\"never."]]
      (is (= actual expected)))))

;; TODO: :token, :index styles
(deftest test-index
  ;; The :quote index just IDs the quotations in order (from 0).
  (testing :quote
    (let [quote-example (@pipeline/en "He said \"marry me\"; she said: \"ok.\"")
          actual        (->> quote-example quotations (index :quote))
          expected      [0 1]]
      (is (= actual expected)))))

(deftest test-pos
  (let [actual   (-> example tokens pos)
        expected ["PRP" "VBD" "IN" "NNP" "IN" "NNP" "TO" "VB" "DT" "NN" "IN" "NNP" "."]]
    (is (= actual expected))))

(deftest test-named-entity-recognition
  (testing :tag
    (let [actual1  (-> example tokens named-entity)
          actual2  (->> example tokens (named-entity :tag))
          expected ["O" "O" "O" "COUNTRY" "O" "PERSON" "O" "O" "O" "O" "O" "CITY" "O"]]
      (is (= actual1 actual2 expected))))

  (testing :fine
    (let [actual   (->> example tokens (named-entity :fine))
          expected ["O" "O" "O" "COUNTRY" "O" "PERSON" "O" "O" "O" "O" "O" "CITY" "O"]]
      (is (= actual expected))))

  (testing :coarse
    (let [actual   (->> example tokens (named-entity :coarse))
          expected ["O" "O" "O" "LOCATION" "O" "PERSON" "O" "O" "O" "O" "O" "LOCATION" "O"]]
      (is (= actual expected))))

  (testing :probs
    (let [actual   (->> example tokens (named-entity :probs))
          expected ["O" "O" "O" "LOCATION" "O" "PERSON" "O" "O" "O" "O" "O" "LOCATION" "O"]]
      (is (= (map first actual) expected)))))

(deftest test-numeric
  (let [words (tokens (@pipeline/en "It was his twenty first time."))]
    (testing :value
      (let [actual1  (numeric words)
            actual2  (numeric :value words)
            expected [nil nil nil 20 1 nil nil]]
        (is (= actual1 actual2 expected))))

    (testing :type
      (let [actual   (numeric :type words)
            expected [nil nil nil "NUMBER" "ORDINAL" nil nil]]
        (is (= actual expected))))

    ;; TODO: nothing here - how to get :composite annotations?
    (testing :composite)

    (testing :composite-value
      (let [actual   (numeric :composite-value words)
            expected [nil nil nil 21.0 21.0 nil nil]]
        (is (= actual expected))))

    (testing :composite-type
      (let [actual   (numeric :composite-type words)
            expected [nil nil nil "ORDINAL" "ORDINAL" nil nil]]
        (is (= actual expected))))

    (testing :normalized
      (let [actual   (->> (@pipeline/en "He went to Sweden for a week the first time.")
                          tokens
                          (numeric :normalized))
            expected [nil nil nil nil nil "P1W" "P1W" nil "1.0" nil nil]]
        (is (= actual expected))))))

(deftest test-mentions
  (let [actual   (-> example mentions text)
        expected ["Sweden" "Mary" "Ystad"]]
    (is (= actual expected))))
