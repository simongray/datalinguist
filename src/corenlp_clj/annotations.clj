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
;;;; Please note that *any* annotation can be accessed using corenlp-clj.annotations/annotation,
;;;; you are not just limited to using the convenience functions provided in this namespace.
;;;;
;;;; The functions here mirror the annotation system of Stanford CoreNLP: once the returned object is not a TypesafeMap
;;;; or a seq of TypesafeMap objects, annotation functions cannot retrieve anything from it.
;;;; An example of this might be the dependency-graph annotation which returns a SemanticGraph object.
;;;; However, using a function such as corenlp-clj.semgraph/nodes on a SemanticGraph object returns IndexedWord objects
;;;; which *are* implementations of TypesafeMap. Consequently, the annotation functions can take them as params.
;;;;
;;;; As a general rule, functions with names that are pluralised have a seqable output, e.g. sentences or tokens.
;;;; This does not matter when chaining these functions, as all annotation functions will also implicitly map to seqs.

(defn annotation
  "Access the annotation of x as specified by class."
  [^Class class x]
  (if (seqable? x)
    (map #(annotation class %) x) ; no tail recursion!
    (.get ^TypesafeMap x class)))

(def text(partial annotation CoreAnnotations$TextAnnotation))
(def lemma (partial annotation CoreAnnotations$LemmaAnnotation))
(def pos (partial annotation CoreAnnotations$PartOfSpeechAnnotation))
(def ner (partial annotation CoreAnnotations$NamedEntityTagAnnotation))
(def sentences (partial annotation CoreAnnotations$SentencesAnnotation))
(def tokens (partial annotation CoreAnnotations$TokensAnnotation))

(defn offset
  "The character offset of x. Style can be :begin (default) or :end."
  ([style x]
   (case style
     :begin (annotation CoreAnnotations$CharacterOffsetBeginAnnotation x)
     :end (annotation CoreAnnotations$CharacterOffsetEndAnnotation x)))
  ([x]
   (offset :begin x)))

(defn index
  "The index of x. Style can be :token (default) or :sentence."
  ([style x]
   (case style
     :token (annotation CoreAnnotations$IndexAnnotation x)
     :sentence (annotation CoreAnnotations$SentenceIndexAnnotation x)))
  ([x]
   (index :token x)))

(defn whitespace
  "The whitespace around x. Style can be :before (default) or :after."
  ([style x]
   (case style
     :before (annotation CoreAnnotations$BeforeAnnotation x)
     :after (annotation CoreAnnotations$AfterAnnotation x)))
  ([x]
   (whitespace :before x)))

(defn dependency-graph
  "The dependency graph of x. Style can be :basic, :enhanced or :enhanced++ (default)."
  ([style x]
   (case style
     :basic (annotation SemanticGraphCoreAnnotations$BasicDependenciesAnnotation x)
     :enhanced (annotation SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation x)
     :enhanced++ (annotation SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation x)))
  ([x]
   (dependency-graph :enhanced++ x)))
