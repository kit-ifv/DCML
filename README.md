# Discrete Choice Modeling
This project supplies a structure or framework for modeling of discrete choice situations.
It provides some default implementations but not 

## Default Implementations
This project has default implementations for some distribution functions.
- The simplest case, transforming utility values directly to probabilities with [MultinomialLogit.kt](src/main/kotlin/discreteChoice/distribution/MultinomialLogit.kt)
- Probabilities with nested structures of decisions can be calculated with [CrossNestedLogit.kt](src/main/kotlin/discreteChoice/distribution/CrossNestedLogit.kt)

### Common Lingo
Language you might stumble over in this project.
- __Alternative__ - some choosable thing/object. Single entity.
- __utility__ - some value. Could be any. In contrast to probabilites it is not bound to the interval \[0,1]. Usually probabilities first need to be calculated out of all utility values. Maybe with a 'softmax' function ($p_i = \frac{e^{u_i}}{\sum_j e^{u_j}}$, $u_j$ the utility values, $p_i$ probability of i) or something else.
- 
- 