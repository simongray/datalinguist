(ns corenlp-clj.semgraph
  (:require [corenlp-clj.core :refer [annotation]])
  (:import [edu.stanford.nlp.semgraph SemanticGraphCoreAnnotations$BasicDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation
                                      SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation]))

(def basic SemanticGraphCoreAnnotations$BasicDependenciesAnnotation)
(def enhanced SemanticGraphCoreAnnotations$EnhancedDependenciesAnnotation)
(def enchanced++ SemanticGraphCoreAnnotations$EnhancedPlusPlusDependenciesAnnotation)

(defn dependencies
  ([x class]
   (annotation x class))
  ([x]
   (dependencies x enchanced++)))
