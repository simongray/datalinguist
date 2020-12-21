# CoreNLP packages
The main packages of CoreNLP along with a description of what they contain, links to official documentation, and - possibly - information about whether/how they have been wrapped in DataLinguist:

* [**edu.stanford.nlp.classify**](https://nlp.stanford.edu/software/classifier.shtml)
  - The classify package provides facilities for training classifiers.
* [**edu.stanford.nlp.coref**](https://stanfordnlp.github.io/CoreNLP/coref.html)
  - The CorefAnnotator finds mentions of the same entity in a text, such as when “Theresa May” and “she” refer to the same person.
* [**edu.stanford.nlp.dcoref**](https://nlp.stanford.edu/software/dcoref.shtml)
  - The deterministic coreference resolution system is still supported in StanfordCoreNLP by using the annotator dcoref, but is superseded by the _coref_ annotator.
* **edu.stanford.nlp.fsm**
  - Contains two implementations of finite state machines.
* **edu.stanford.nlp.graph**
  - Contsins the base graph implementation, plus a few graph algorithms. 
* [**edu.stanford.nlp.ie**](https://stanfordnlp.github.io/CoreNLP/openie.html)
  - This package implements various subpackages for information extraction.
  - The Open Information Extraction (OpenIE) annotator extracts open-domain relation triples, representing a subject, a relation, and the object of the relation.
* **edu.stanford.nlp.international**
  - Contains various language-specific classes.
* **edu.stanford.nlp.io**
  - Contains various IO-related utility classes.
* **edu.stanford.nlp.ling**
  - This package contains the different data structures used by JavaNLP throughout the years for dealing with linguistic objects in general, of which words are the most generally used. Most data structures in this package are deprecated.
  - [**edu.stanford.nlp.ling.tokensregex**](https://nlp.stanford.edu/software/tokensregex.html)
    * TokensRegex is a generic framework included in Stanford CoreNLP for defining patterns over text (sequences of tokens) and mapping it to semantic objects represented as Java objects.
    * See also: https://stanfordnlp.github.io/CoreNLP/tokensregex.html
* **edu.stanford.nlp.math**
  - Classes for Simple Math Functionality, such as Min, Max, WeightedAverage, Scientific Notation, etc.
* **edu.stanford.nlp.maxent**
  - This package deals with defining and solving maximum entropy problems.
* [**edu.stanford.nlp.naturalli**](https://stanfordnlp.github.io/CoreNLP/natlog.html)
  - This package defines operator and polarity marking according to natural logic, and defines the Stanford Open IE system.
  - See also: https://stanfordnlp.github.io/CoreNLP/openie.html
* **edu.stanford.nlp.net**
  - Contains a couple useful utility methods related to networks.
* **edu.stanford.nlp.neural**
  - Contains classes related to neural networks, in particular recursive neural networks (RNN).
* **edu.stanford.nlp.objectbank**
  - The ObjectBank class is designed to make it easy to change the format/source of data read in by other classes and to standardize how data is read in javaNLP classes. 
* **edu.stanford.nlp.optimization**
  - Numerical optimization, including a conjugate gradient implementation.
* **edu.stanford.nlp.paragraphs**
  - Contains the ParagraphAnnotator class.
* [**edu.stanford.nlp.parser**](https://nlp.stanford.edu/software/lex-parser.shtml)
  - Implementations of probabilistic natural language parsers in Java: PCFG and dependency parsers, a lexicalized PCFG parser, a super-fast neural-network dependency parser, and a deep learning reranker.
  - See also: https://nlp.stanford.edu/software/parser-faq.html
  - [**edu.stanford.nlp.parser.nndep**](https://nlp.stanford.edu/software/nndep.html)
  - [**edu.stanford.nlp.parser.shiftreduce**](https://nlp.stanford.edu/software/srparser.html)
* [**edu.stanford.nlp.patterns**](https://nlp.stanford.edu/software/patternslearning.html)
  - Pattern-based entity extraction and visualization.
  - See also: https://github.com/sonalgupta/SPIED-viz
* [**edu.stanford.nlp.pipeline**](https://stanfordnlp.github.io/CoreNLP/pipeline.html)
  - The centerpiece of CoreNLP is the pipeline. Pipelines take in text or xml and generate full annotation objects.
  - See also: https://stanfordnlp.github.io/CoreNLP/annotators.html
* **edu.stanford.nlp.process**
  - Contains classes for processing documents.
* [**edu.stanford.nlp.quoteattribution**](https://stanfordnlp.github.io/CoreNLP/quote.html)
  - Deterministically picks out quotes from a text.
* **edu.stanford.nlp.semgraph**
  - This package provides a representation of dependency graphs (normally the collapsed Stanford Dependencies representation) as a graph.
  - **edu.stanford.nlp.semgraph.semgrex**
    * A package for dependency graph (i.e. SemanticGraph) pattern expressions and matching these expressions to IndexedFeatureLabel instances.
* [**edu.stanford.nlp.sentiment**](https://stanfordnlp.github.io/CoreNLP/sentiment.html)
  - StanfordCoreNLP includes the sentiment tool and various programs which support it. SentimentAnnotator implements Socher et al’s sentiment model.
  - See also: https://nlp.stanford.edu/sentiment/
* **edu.stanford.nlp.sequences**
  - This package contains classes that support sequence modeling and inference (CMMs and CRFs).
* [**edu.stanford.nlp.simple**](https://stanfordnlp.github.io/CoreNLP/simple.html)
  - In addition to the fully-featured annotator pipeline interface to CoreNLP, Stanford provides a simple API for users who do not need a lot of customization.
* **edu.stanford.nlp.stats**
  - A set of tools for collecting, representing, and inferring with statistics.
* **edu.stanford.nlp.swing**
  - Collection of useful classes for building Swing GUIs.
* **edu.stanford.nlp.tagger**
  - This subpackage is a home for taggers.  They are defined in subpackages.
  - **edu.stanford.nlp.tagger.maxent**
    * A Maximum Entropy Part-of-Speech Tagger.
* [**edu.stanford.nlp.time**](https://stanfordnlp.github.io/CoreNLP/sutime.html)
  - StanfordCoreNLP includes SUTime, a library for processing temporal expressions such as February 4th, 2019. SUTime is built on top of TokensRegex.
* **edu.stanford.nlp.trees**
  - A package for (NLP) trees, sentences, and similar things.
  - **edu.stanford.nlp.trees.international**
    * This package and its subpackages define processes related to language-specific (or perhaps language- and corpus-specific) processing.
  - [**edu.stanford.nlp.trees.tregex**](https://nlp.stanford.edu/software/tregex.shtml)
    * Tregex is a utility for matching patterns in trees, based on tree relationships and regular expression matches on nodes (the name is short for "tree regular expressions"). Tregex comes with Tsurgeon, a tree transformation language.
* **edu.stanford.nlp.util**
  - A collection of useful general-purpose utility classes.
* **edu.stanford.nlp.wordseg**
  - A package for doing Chinese word segmentation.
