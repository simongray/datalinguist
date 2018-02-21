(defproject computerese "0.1.0-SNAPSHOT"
  :description "Stanford CoreNLP in idiomatic Clojure."
  :url "https://github.com/simongray/computerese"
  :jvm-opts ["-Xmx2G"]                                      ; CoreNLP uses up memory fast
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [aysylu/loom "1.0.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.8.0"]]

  :profiles {:dev {:dependencies [[edu.stanford.nlp/stanford-corenlp "3.8.0" :classifier "models"]
                                  [edu.stanford.nlp/stanford-corenlp "3.8.0" :classifier "models-chinese"]]}})
