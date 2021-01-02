# Writing Semgrex using Datalog
DataLinguist supports writing Semgrex using Datomic-like Datalog triples that should be quite familiar to many Clojure programmers. This document shows how to this Datalog code translates into Semgrex. 

All cited sections are taken directly from the Javadoc at:

```clojure
edu.stanford.nlp.semgraph.semgrex.SemgrexPattern
```

Attributes
----------
### Semgrex
> A node is represented by a set of attributes and their values contained by curly braces: {attr1:value1;attr2:value2;...}. Therefore, {} represents any node in the graph. Attributes must be plain strings; values can be strings or regular expressions blocked off by "/". Regular expressions must match the whole attribute value, so that /NN/ matches "NN" only, while /NN.*/ matches "NN", "NNS", "NNP", etc.
>
> For example, {lemma:slice;tag:/VB.*&#47;} represents any verb nodes with "slice" as their lemma. Attributes are extracted using edu.stanford.nlp.ling.AnnotationLookup.

```semgrex
{lemma:slice;tag:/VB.*/}
```

### Clojure
```clojure
[[?e :lemma "slice"]
 [?e :tag #"VB.*"]]
```

Root node
---------
### Semgrex
> The root of the graph can be marked by the $ sign, that is {$} represents the root node.

```semgrex
{$}
```

### Clojure
```clojure
[_ :reln/ROOT ?e]
```

Negation
--------
### Semgrex
> A node description can be negated with '!'. !{lemma:boy} matches any token that isn't "boy".

```semgrex
!{lemma:boy}
```

> Another way to negate a node description is with a negative lookahead regex, although this starts to look a little ugly. For example, {lemma:/^(?!boy).*$/} will also match any token with a lemma that isn't "boy". Note, however, that if you use this style, there needs to be some lemma attached to the token.

```semgrex
{lemma:/^(?!boy).*$/}
```

### Clojure
```clojure
(not [?e :lemma "boy"])
```

```clojure
[?e :lemma #"^(?!boy).*$"]
```

Relations
---------
> Relations are defined by a symbol representing the type of relationship and a string or regular expression representing the value of the relationship. A relationship string of % means any relationship. It is also OK simply to omit the relationship symbol altogether.
> 
> Currently supported node relations and their symbols:

| Symbol | Meaning |
|--------|---------|
| A <reln B     | A is the dependent of a relation reln with B  |
| A >reln B     | A is the governor of a relation reln with B |
| A <<reln B    | A is the dependent of a relation reln in a chain to B following dep->gov paths |
| A >>reln B    | A is the governor of a relation reln in a chain to B following gov->dep paths |
| A x,y<<reln B | A is the dependent of a relation reln in a chain to B following dep->gov paths between distances of x and y |
| A x,y>>reln B | A is the governor of a relation reln in a chain to B following gov->dep paths between distances of x and y |
| A == B        | A and B are the same nodes in the same graph |
| A . B         | A immediately precedes B, i.e. A.index() == B.index() - 1 |
| A $+ B        | B is a right immediate sibling of A, i.e. A and B have the same parent and A.index() == B.index() - 1 |
| A $- B        | B is a left immediate sibling of A, i.e. A and B have the same parent and A.index() == B.index() + 1 |
| A $++ B       | B is a right sibling of A, i.e. A and B have the same parent and A.index() < B.index() |
| A $-- B       | B is a left sibling of A, i.e. A and B have the same parent and A.index() > B.index() |
| A @ B         | A is aligned to B (this is only used when you have two dependency graphs which are aligned) |

TODO: define Clojure code for all of the above (I imagine most of them would be something like `[?a :reln/reln ?b]`)

Naming nodes
------------
### Semgrex
> Nodes can be given names (a.k.a. handles) using '='. A named node will be stored in a map that maps names to nodes so that if a match is found, the node corresponding to the named node can be extracted from the map. For example ({tag:NN}=noun) will match a singular noun node and after a match is found, the map can be queried with the name to retrieved the matched node using SemgrexMatcher.getNode(String o) with (String) argument "noun" (not "=noun").
> 
```semgrex
{tag:NN}=noun
```

### Clojure
```clojure
[?noun :tag "NN"]
```

Naming relations
----------------
### Semgrex
> It is also possible to name relations. For example, you can write the pattern {idx:1} >=reln {idx:2} The name of the relation will then be stored in the matcher and can be extracted with getRelnName("reln") 
> 
```semgrex
{idx:1} >=reln {idx:2}
```

### Clojure
```clojure
[[?e1 :idx 1]
 [?e1 ?reln ?e2]
 [?e2 :idx 2]]
```
