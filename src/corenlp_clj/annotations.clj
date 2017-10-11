(ns corenlp-clj.annotations
  (:import [edu.stanford.nlp.pipeline Annotation]
           [edu.stanford.nlp.ling CoreAnnotations$TextAnnotation
                                  CoreAnnotations$LemmaAnnotation
                                  CoreAnnotations$PartOfSpeechAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation
                                  CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$TokensAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation
                                  CoreAnnotations$BeforeAnnotation
                                  CoreAnnotations$AfterAnnotation
                                  CoreAnnotations$IndexAnnotation
                                  CoreAnnotations$SentenceIndexAnnotation]
           [edu.stanford.nlp.semgraph SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation]))

;; This namespace contains convenience functions for accessing the most common annotations of Stanford CoreNLP.
;; The functions are designed to be chained using the ->> macro or through function composition.
;; Please note that _any_ annotation can be accessed using the annotation function in corenlp-clj.core,
;; you are not just limited to using these convenience functions.

(defn annotation
  "Access the annotation of x as specified by class."
  [^Class class x]
  (if (seqable? x)
    (map #(annotation class %) x) ; no tail recursion!
    (.get ^Annotation x class)))

(def text (partial annotation CoreAnnotations$TextAnnotation))
(def lemma (partial annotation CoreAnnotations$LemmaAnnotation))
(def pos (partial annotation CoreAnnotations$PartOfSpeechAnnotation))
(def ner (partial annotation CoreAnnotations$NamedEntityTagAnnotation))
(def sentences (partial annotation CoreAnnotations$SentencesAnnotation))
(def tokens (partial annotation CoreAnnotations$TokensAnnotation))

(defn index
  "Defaults to token index."
  ([category x]
   (cond
     (= category :token) (annotation CoreAnnotations$IndexAnnotation x)
     (= category :sentence) (annotation CoreAnnotations$SentenceIndexAnnotation x)
     :else (throw (IllegalArgumentException. "category must be :token or :sentence"))))
  ([x]
   (index :token x)))

(defn whitespace
  "Defaults to whitespace before."
  ([category x]
   (cond
     (= category :before) (annotation CoreAnnotations$BeforeAnnotation x)
     (= category :after) (annotation CoreAnnotations$AfterAnnotation x)
     :else (throw (IllegalArgumentException. "category must be :before or :after"))))
  ([x]
   (whitespace :before x)))

(defn dependencies
  "Defaults to enhanced++ dependencies."
  ([category x]
   (cond
     (= category :basic) (annotation SemanticGraphCoreAnnotations$BasicDependenciesAnnotation x)
     (= category :enhanced) (annotation SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation x)
     (= category :enhanced++) (annotation SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation x)
     :else (throw (IllegalArgumentException. "category must be :basic, :enhanced or :enhanced++"))))
  ([x]
   (dependencies :enhanced++ x)))
