# DataLinguist
DataLinguist<sup>[†](#name)</sup> is a Clojure wrapper for the Natural Language Processing behemoth, [Stanford CoreNLP](https://github.com/stanfordnlp/CoreNLP). The goal of the project is to support an NLP workflow in a data-oriented style, integrating relevant Clojure protocols and libraries.

Most Lisp dialects facilitate interactive development centred around a REPL. Clojure - being both a JVM language and a data-oriented one - is perfectly suited for wrapping Stanford CoreNLP's fairly opaque language processing tools. The dizzying heights of Java class hierarchies are brought down to eye level and repackaged into more accessible functions inside a few Clojure namespaces. At the same time, the API is streamlined with most of the obvious cruft left out.

* [Setup](#setup)
  - [Language models](#language-models)
* [How to use](#how-to-use)
  1. [Building an annotation pipeline](#1-building-an-annotation-pipeline)
  2. [Using the pipeline to annotate text](#2-using-the-pipeline-to-annotate-text)
  3. [Extracting annotations using the annotation functions](#3-extracting-annotations-using-the-annotation-functions)
* [Clojure integration](#clojure-integration)
  - [View in the REBL](#view-in-the-rebl)
  - [Loom integration](#loom-integration)
* [Wrapper state](#wrapper-state)

> _<a name="name"><sup>†</sup></a> The name is a play on "datalingvist" - the Danish word for "computational linguist" - and Clojure's love of all things data. As a sidenote, the Danish translation of "computer scientist" is actually "datalog"!_

## Setup
The library can be included as a `deps.edn` dependency by referencing a specific commit SHA in this repository:

```edn
simongray/datalinguist {:git/url "https://github.com/simongray/datalinguist"
                        :sha     "..."}}
```

TODO: put the library on clojars, add a short description

### Language models
DataLinguist requires a language model to access the full feature set. Several precompiled models are available through Maven Central<sup>[††](#maven)</sup>. These models follow the same versioning scheme as CoreNLP itself. See the official [CoreNLP documentation](https://stanfordnlp.github.io/CoreNLP/download.html) for more on language models.

In addition to adding DataLinguist itself as a project dependency, you should therefore _also_ make sure to add any language models you might need:

```edn
;; Currently available precompiled language models in deps.edn format
edu.stanford.nlp/stanford-corenlp$models             {:mvn/version "4.2.0"}
edu.stanford.nlp/stanford-corenlp$models-arabic      {:mvn/version "4.2.0"}
edu.stanford.nlp/stanford-corenlp$models-chinese     {:mvn/version "4.2.0"}
edu.stanford.nlp/stanford-corenlp$models-english     {:mvn/version "4.2.0"}
edu.stanford.nlp/stanford-corenlp$models-english-kbp {:mvn/version "4.2.0"}
edu.stanford.nlp/stanford-corenlp$models-french      {:mvn/version "4.2.0"}
edu.stanford.nlp/stanford-corenlp$models-german      {:mvn/version "4.2.0"}
edu.stanford.nlp/stanford-corenlp$models-spanish     {:mvn/version "4.2.0"}
```

_Note that several [unofficial language models](https://stanfordnlp.github.io/CoreNLP/human-languages.html#models-for-other-languages) also exist!_

You will likely also need to increase the amount of memory allotted to your JVM process, e.g. for Chinese I make sure to add `:jvm-opts ["-Xmx4G"]` to my _deps.edn_ file.

> _<a name="maven"><sup>††</sup></a> Stanford can sometimes be bit slow when it comes to uploading more recent versions of CoreNLP to Maven Central._

## How to use
DataLinguist has a very simple API for executing various NLP operations. To perform any language processing task you will generally need to **1.** build an annotation pipeline, **2.** annotate some text, and then **3.** extract some of these annotations for analysis.

### 1. Building an annotation pipeline
Before anything can happen, we need to construct an NLP pipeline. Pipelines are built using plain Clojure data structures which are converted into
[properties](https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/pipeline/StanfordCoreNLP.properties):

```Clojure
(require '[dk.simongray.datalinguist :refer :all]
         '[dk.simongray.datalinguist.annotations :refer :all])

;; Create a closure around a CoreNLP pipeline.
(def nlp
  (->pipeline {:annotators ["depparse" "lemma"]}))
```

This will create a pipeline with all of the [annotators](https://stanfordnlp.github.io/CoreNLP/annotators.html) required for dependency parsing and lemmatisation.

### 2. Using the pipeline to annotate text
If the resulting pipeline function is called on a piece of text it will output annotated data.

```Clojure
(def annotated-text
  (nlp "This is a piece of text. This is another one."))
```

This data consists of various Java objects and takes the shape of a tree. The data can then be queried using the annotation functions.

### 3. Extracting annotations using the annotation functions
In our example we built a pipeline that could do both dependency parsing and lemmatisation, so let's extract a dependency graph of the second sentence:

```Clojure
(->> annotated-text sentences second dependency-graph)
;=>
;#object[edu.stanford.nlp.semgraph.SemanticGraph
;        0x79d17876
;        "-> one/CD (root)
;           -> This/DT (nsubj)
;           -> is/VBZ (cop)
;           -> another/DT (det)
;           -> ./. (punct)
;         "]
```

We can also annotate another piece of text and return the lemmas:

```Clojure
(->> "She has beaten him before."
     nlp
     tokens
     lemma)
;=> ("she" "have" "beat" "he" "before" ".")
```

If at any point we grow tired of accessing Java objects using the annotation functions, we can call `recur-datafy` and get plain Clojure data structures:

```Clojure
(-> annotated-text tokens second recur-datafy)
;=>
;{:token-end 2,
; :original-text "is",
; :index 2,
; :part-of-speech "VBZ",
; :token-begin 1,
; :value "is",
; :sentence-index 0,
; :lemma "be",
; :newline? false,
; :character-offset-begin 5,
; :after " ",
; :character-offset-end 7,
; :before " ",
; :text "is"}
```

## Clojure integration
Apart from wrapping the features of Stanford CoreNLP, DataLinguist also integrates with common Clojure libraries and workflows. 

### View in the REBL
The pipeline's annotated output data extends the `Datafiable` protocol introduced in Clojure 1.10 and can therefore be navigated using the [REBL](https://github.com/cognitect-labs/REBL-distro).
 
![Navigating annotations in the REBL](https://raw.githubusercontent.com/simongray/corenlp-clj/master/doc/rebl_example.png)

Within the REBL, text annotations can be visualised and analysed interactively.

### Loom integration
The dependency graphs support the `Graph` and `DiGraph` protocols from [Loom](https://github.com/aysylu/loom) and associated algorithms.

Dependency graphs can also be visualised using a modified version of the view function from Loom (requires Graphiz installed).

```Clojure
(require '[dk.simongray.datalinguist.loom.io :refer [view]])

(view (->> "The dependencies of this sentence have been visualised using Graphviz."
           nlp
           sentences
           dependency-graph
           first))
```

![Dependency graph visualised using Graphviz](https://raw.githubusercontent.com/simongray/corenlp-clj/master/doc/graphviz_example.png)

## Wrapper state
You can already perform most common NLP tasks by following the 3-part process [described above](#how-to-use). However, CoreNLP is a huge undertaking that has amassed many NLP tools throughout the years, usually by integrating the products of various Stanford research projects. Some of these tools are well maintained, some not so much. Some play well with the other parts of CoreNLP, some do not. It is the goal of this project to wrap most of them, with a few exceptions:

1. Classes and methods that are merely implementation details are not wrapped.
2. Mutating code has generally been left out. This is not idiomatic in Clojure.
3. Easily replicated convenience methods have been left out. The Clojure standard library is better suited for this.
4. Code dealing with training new language models has been left out. For now, DataLinguist is focused on wrapping the language analysis functionality.

Please refer to [this overview](https://github.com/simongray/datalinguist/blob/master/doc/CoreNLP_packages.md) for more on the CoreNLP package structure and what's been wrapped in DataLinguist.
