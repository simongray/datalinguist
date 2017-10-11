(defproject corenlp-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :jvm-opts ["-Xmx2G"] ; CoreNLP uses up memory fast
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [aysylu/loom "1.0.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.8.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.8.0" :classifier "models"]
                 [edu.stanford.nlp/stanford-corenlp "3.8.0" :classifier "models-chinese"]])
