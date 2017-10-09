(ns corenlp-clj.annotations
  (:require [corenlp-clj.core :refer [annotation]])
  (:import [edu.stanford.nlp.ling CoreAnnotations$TextAnnotation
                                  CoreAnnotations$LemmaAnnotation
                                  CoreAnnotations$PartOfSpeechAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation
                                  CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$TokensAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation
                                  CoreAnnotations$BeforeAnnotation
                                  CoreAnnotations$AfterAnnotation]
           [edu.stanford.nlp.semgraph SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation]))

;; This namespace contains convenience functions for accessing the most common annotations of Stanford CoreNLP.
;; The functions are designed to be chained using the ->> macro or through function composition.
;; Please note that _any_ annotation can be accessed using the annotation function in corenlp-clj.core,
;; you are not just limited to using these convenience functions.

(def text (partial annotation CoreAnnotations$TextAnnotation))
(def lemma (partial annotation CoreAnnotations$LemmaAnnotation))
(def pos (partial annotation CoreAnnotations$PartOfSpeechAnnotation))
(def ner (partial annotation CoreAnnotations$NamedEntityTagAnnotation))
(def sentences (partial annotation CoreAnnotations$SentencesAnnotation))
(def tokens (partial annotation CoreAnnotations$TokensAnnotation))

(defn whitespace
  "Defaults to whitespace before."
  ([type x]
   (cond
     (= type :before) (annotation CoreAnnotations$BeforeAnnotation x)
     (= type :after) (annotation CoreAnnotations$AfterAnnotation x)
     :else (throw (IllegalArgumentException. "type must be :before or :after"))))
  ([x]
   (whitespace :before x)))

(defn dependencies
  "Defaults to enhanced++ dependencies."
  ([type x]
   (cond
     (= type :basic) (annotation SemanticGraphCoreAnnotations$BasicDependenciesAnnotation x)
     (= type :enhanced) (annotation SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation x)
     (= type :enhanced++) (annotation SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation x)
     :else (throw (IllegalArgumentException. "type must be :basic, :enhanced or :enhanced++"))))
  ([x]
   (dependencies :enhanced++ x)))
