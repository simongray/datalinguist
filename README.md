# Computerese
````
COMPUTERESE |kəmˌpjuːtəˈriːz|

noun (informal)
  the technical language and jargon used in computing and computer science.
````

Computerese is a Clojure wrapper for the Natural Language Processing behemoth,
Stanford CoreNLP.

## How to use
Computerese has a very simple API for executing various NLP operations.

### 1. Building an annotation pipeline
Before anything can happen, you need to build a pipeline. 
Pipelines are built using plain Clojure data structures which are converted into
[properties](https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/pipeline/StanfordCoreNLP.properties):

```Clojure
(require '[computerese.core :refer :all]
         '[computerese.annotations :refer :all])

;; Create a closure around a CoreNLP pipeline.
(def nlp (pipeline {:annotators (prerequisites ["depparse" "lemma"])}))
```

This will create a pipeline with all of the 
[annotators](https://stanfordnlp.github.io/CoreNLP/annotators.html) 
required for dependency parsing and lemmatisation.

### 2. Using the pipeline to annotate text
If the resulting pipeline function is called on a piece of text it will output 
annotated data.

```Clojure
(def annotated-text (nlp "This is a piece of text. This is another one."))
```

The data consists of Java objects and takes the shape of a tree.
This data can then be queried using the annotation functions.

### 3. Extracting annotations using the annotation functions
In our example we built a pipeline that could do both dependency parsing and
lemmatisation, so let's extract some of these annotations from the text:

```Clojure
;; Get a dependency graph of the second sentence using the annotation functions.
(->> annotated-text sentences second dependency-graph)
;#object[edu.stanford.nlp.semgraph.SemanticGraph
;        0x79d17876
;        "-> one/CD (root)
;           -> This/DT (nsubj)
;           -> is/VBZ (cop)
;           -> another/DT (det)
;           -> ./. (punct)
;         "]

;; Extract lemmas from the text using the annotation functions.
(->> "She has beaten him before."
     nlp
     tokens
     lemma)
;=> ("she" "have" "beat" "he" "before" ".")

```

## Loom integration
The dependency graphs support the `Graph` protocol from 
[Loom](https://github.com/aysylu/loom) and associated algorithms.

Dependency graphs can also be visualised using a modified version of the view 
function from Loom (requires Graphiz installed).

```Clojure
(require '[computerese.loom.io :refer [view]])

(view (->> "The dependencies of this sentence have been visualised using Graphviz."
           nlp
           sentences
           dependency-graph
           first))
```

![Dependency graph visualised using Graphviz](https://raw.githubusercontent.com/simongray/corenlp-clj/master/doc/graphviz_example.png)

## View in the REBL
The pipeline's output data extends the `Datafiable` protocol 
introduced in Clojure 1.10 and can therefore be navigated using the 
[REBL](https://github.com/cognitect-labs/REBL-distro).
 
![Navigating annotations in the REBL](https://raw.githubusercontent.com/simongray/corenlp-clj/master/doc/rebl_example.png)
