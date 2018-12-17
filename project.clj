(defproject computerese "0.1.0-SNAPSHOT"
  :description "Stanford CoreNLP in idiomatic Clojure."
  :url "https://github.com/simongray/computerese"
  :jvm-opts ["-Xmx2G"]                                      ; CoreNLP uses up memory fast
  :source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [aysylu/loom "1.0.2"]
                 [camel-snake-kebab "0.4.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.9.2"]]

  :profiles {:dev  {:dependencies [[edu.stanford.nlp/stanford-corenlp "3.9.2" :classifier "models"]
                                   [edu.stanford.nlp/stanford-corenlp "3.9.2" :classifier "models-chinese"]]
                    :source-paths ["dev/src"]
                    :repl-options {:init-ns user}}

             :rebl {:dependencies   [[org.openjfx/javafx-fxml "11"]
                                     [org.openjfx/javafx-swing "11"]
                                     [org.openjfx/javafx-web "11"]
                                     [org.clojure/core.async "0.4.490"]]
                    :resource-paths ["resources/REBL-0.9.109.jar"]
                    :repl-options {:init (do
                                           (require '[cognitect.rebl :as rebl])
                                           (rebl/ui))}}})
