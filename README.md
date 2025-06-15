# Discrete Choice Modeling

This project supplies a structure or framework for modeling of discrete choice situations.
It provides some default implementations but not 

## Default Implementations

This project has default implementations for some of the defined structures:

### ChoiceModels

Simple selecting of options has some default implementations. I.e. picking an option equally distributed 
(`discreteChoice.models.RandomChoiceModel`).

### Distribution functions

- The simplest case, transforming utility values directly to probabilities 
with [MultinomialLogit.kt](src/main/kotlin/discreteChoice/distribution/MultinomialLogit.kt)


- Probabilities with nested structures of decisions can be calculated with
[CrossNestedLogit.kt](src/main/kotlin/discreteChoice/distribution/CrossNestedLogit.kt). Usefull for Red-Bus-Green-Bus
situations. (TODO: This probably needs more explanation)


- Probabilities with nested structures, that aren't necessarily tree like can be modeled with 
[CrossNestedLogit.kt](src/main/kotlin/discreteChoice/distribution/CrossNestedLogit.kt).



## Common Lingo

Language you might stumble upon in this project.

- __Alternative__ - some choosable thing/object. Single entity.


- __utility__ - some value. Could be any. In contrast to probabilites it is not bound to the 
interval \[0,1]. Usually probabilities first need to be calculated out of all utility values.
Maybe with a 'softmax' function ($p_i = \frac{e^{u_i}}{\sum_j e^{u_j}}$, $u_j$ the utility
values, $p_i$ probability of i) or something else.


- __DiscreteChoiceModel__ a subset of ChoiceModels(selecting something out of some options), based on a linear
dependency on some parameters of an agent (could be age, position, number of siblings, bmi, time of day whatever).
Usually the parameters of the agent get mushed into a utility function, for each option, which spits out some utility
value for that option.
Out of those utility values probabilities for all options get calculated. These probabilities are then the basis of a selection.