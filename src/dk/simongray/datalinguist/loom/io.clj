(ns dk.simongray.datalinguist.loom.io
  "This namespace contains an implementation of `loom.io/view` that uses
  `dk.simongray.datalinguist.dependency/formatted-string` instead of
  `loom.io/dot-str`.

  The main reason for this is that `loom.io/render-to-bytes` explicitly requires
  graphs made of pure data, so the function won't work with e.g. SemanticGraph
  even though the class satifies loom's Graph protocol.

  See `loom.io` for documentation of the functions."
  (:require [clojure.java.shell :refer [sh]]
            [clojure.java.io :refer [file]]
            [dk.simongray.datalinguist.dependency :as dependency]
            [loom.io :as lio]))

(defn render-to-bytes
  [g & {:keys [alg fmt] :or {alg "dot" fmt :png} :as opts}]
  (let [dot (dependency/formatted-string :dot g)
        cmd (sh (name alg) (str "-T" (name fmt)) :in dot :out-enc :bytes)]
    (:out cmd)))

(defn view
  [g & {:keys [fmt] :or {fmt :png} :as opts}]
  (with-redefs [lio/render-to-bytes render-to-bytes]
    (if opts
      (lio/view g opts)
      (lio/view g))))
