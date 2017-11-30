(ns computerese.loom.io
  (:require [corenlp-clj.semgraph.core :as semgraph]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :refer [file]]))

;; Re-implementation of loom.io/view using dot-format instead of loom.io/dot-str.
;; The functions os, open, open-data, and view itself are all copy-past from loom.io,
;; the only real change is in render-to-bytes, which now uses corenlp-clj.semgraph/dot-format

(defn- os
  "Returns :win, :mac, :unix, or nil"
  []
  (condp
    #(<= 0 (.indexOf ^String %2 ^String %1))
    (.toLowerCase (System/getProperty "os.name"))
    "win" :win
    "mac" :mac
    "nix" :unix
    "nux" :unix
    nil))

(defn- open
  "Opens the given file (a string, File, or file URI) in the default
  application for the current desktop environment. Returns nil"
  [f]
  (let [f (file f)]
    ;; There's an 'open' method in java.awt.Desktop but it hangs on Windows
    ;; using Clojure Box and turns the process into a GUI process on Max OS X.
    ;; Maybe it's ok for Linux?
    (condp = (os)
      :mac (sh "open" (str f))
      :win (sh "cmd" (str "/c start " (-> f .toURI .toURL str)))
      :unix (sh "xdg-open" (str f)))
    nil))

(defn- open-data
  "Writes the given data (string or bytes) to a temporary file with the
  given extension (string or keyword, with or without the dot) and then open
  it in the default application for that extension in the current desktop
  environment. Returns nil"
  [data ext]
  (let [ext (name ext)
        ext (if (= \. (first ext)) ext (str \. ext))
        tmp (java.io.File/createTempFile (subs ext 1) ext)]
    (if (string? data)
      (with-open [w (java.io.FileWriter. tmp)]
        (.write w ^String data))
      (with-open [w (java.io.FileOutputStream. tmp)]
        (.write w ^bytes data)))
    (.deleteOnExit tmp)
    (open tmp)))

(defn render-to-bytes
  "Renders the graph g in the image format using GraphViz and returns data
  as a byte array.
  Requires GraphViz's 'dot' (or a specified algorithm) to be installed in
  the shell's path. Possible algorithms include :dot, :neato, :fdp, :sfdp,
  :twopi, and :circo. Possible formats include :png, :ps, :pdf, and :svg."
  [g & {:keys [alg fmt] :or {alg "dot" fmt :png} :as opts}]
  (let [dot-graph (semgraph/formatted-string :dot g)
        cmd (sh (name alg) (str "-T" (name fmt)) :in dot-graph :out-enc :bytes)]
    (:out cmd)))

(defn view
  "Converts graph g to a temporary image file using GraphViz and opens it
  in the current desktop environment's default viewer for said files.
  Requires GraphViz's 'dot' (or a specified algorithm) to be installed in
  the shell's path. Possible algorithms include :dot, :neato, :fdp, :sfdp,
  :twopi, and :circo. Possible formats include :png, :ps, :pdf, and :svg."
  [g & {:keys [fmt] :or {fmt :png} :as opts}]
  (open-data (apply render-to-bytes g opts) fmt))
