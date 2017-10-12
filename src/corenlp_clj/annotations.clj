(ns corenlp-clj.annotations
  (:import [edu.stanford.nlp.pipeline]
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
                                  CoreAnnotations$SentenceIndexAnnotation
                                  CoreAnnotations$CharacterOffsetBeginAnnotation
                                  CoreAnnotations$CharacterOffsetEndAnnotation]
           [edu.stanford.nlp.util TypesafeMap]
           [edu.stanford.nlp.semgraph SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation]))

;;;; This namespace contains convenience functions for accessing the most common annotations of Stanford CoreNLP.
;;;; The functions are designed to be chained using the ->> macro or through function composition.
;;;; Please note that _any_ annotation can be accessed using corenlp-clj.core/annotation,
;;;; you are not just limited to using the annotations of the convenience functions provided in this namespace.

;;;; This namespace mirrors the annotation system of Stanford CoreNLP: once your returned object is not a TypesafeMap
;;;; or a seq of TypesafeMap objects, then that is a cue that you will need functions from another namespace.
;;;; An example of this might be the _dependencies_ annotation which returns a SemanticGraph object.
;;;; However, using a function such as corenlp-clj.semgraph/nodes on a SemanticGraph object returns IndexedWord objects
;;;; which *are* implementations of TypesafeMap. Consequently, the annotation functions can take them as params.

(defn annotation
  "Access the annotation of x as specified by class."
  [^Class class x]
  (if (seqable? x)
    (map #(annotation class %) x) ; no tail recursion!
    (.get ^TypesafeMap x class)))

(def text (partial annotation CoreAnnotations$TextAnnotation))
(def lemma (partial annotation CoreAnnotations$LemmaAnnotation))
(def pos (partial annotation CoreAnnotations$PartOfSpeechAnnotation))
(def ner (partial annotation CoreAnnotations$NamedEntityTagAnnotation))
(def sentences (partial annotation CoreAnnotations$SentencesAnnotation))
(def tokens (partial annotation CoreAnnotations$TokensAnnotation))

(defn offset
  "Defaults to beginning character offset."
  ([category x]
   (cond
     (= category :begin) (annotation CoreAnnotations$CharacterOffsetBeginAnnotation x)
     (= category :end) (annotation CoreAnnotations$CharacterOffsetEndAnnotation x)
     :else (throw (IllegalArgumentException. "category must be :begin or :end"))))
  ([x]
   (offset :begin x)))

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
