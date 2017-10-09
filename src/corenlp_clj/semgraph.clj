(ns corenlp-clj.semgraph
  (:require [corenlp-clj.core :refer [annotation]])
  (:import [edu.stanford.nlp.semgraph SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation]))

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
