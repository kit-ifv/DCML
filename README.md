# Discrete Choice Modeling

This project supplies a structure or framework for modeling of discrete choice situations.
It also provides some default implementations.
 
## Provided Interfaces and Structures


- __[ChoiceModel](src/main/kotlin/discreteChoice/models/ChoiceModel.kt)__ the most basic interface for selecting
anything out of a set of options.


- __[DiscreteChoiceModel](src/main/kotlin/discreteChoice/DiscreteChoiceModel.kt)__ a subset of ChoiceModels, based on a 
linear dependency on some parameters of an option (usually agents with parameters like age, position, number of 
siblings, bmi, time of day whatever).
  The parameters are mushed into a utility function, which spits out some utility
  value for that option.
  Out of those utility values probabilities for all options get calculated. These probabilities are then the basis of a
selection.


## Default Implementations

This project has default implementations for some of the defined structures:

### ChoiceModels

Simple selecting of options has some default implementations. I.e. picking an option equally distributed 
(`discreteChoice.models.RandomChoiceModel`).

DiscreteChoiceModel is also implemented. On creation a Distribution-, Utility-, SelectionFunction need to be provided. 

### Distribution functions
Functions that map each discrete option to a probability $p \in [0,1]$.

- The simplest case, transforming utility values directly to probabilities 
with [MultinomialLogit.kt](src/main/kotlin/discreteChoice/distribution/MultinomialLogit.kt)


- Probability distributions with nested structures of decisions can be calculated with
[CrossNestedLogit.kt](src/main/kotlin/discreteChoice/distribution/CrossNestedLogit.kt). Supports tree like structures. 
Motivated by the [Red-Bus/Blue-Bus Problem](https://legacy.sawtoothsoftware.com/help/lighthouse-studio/manual/hid_thered-bus.html).



- Probability distributions with nested structures, that aren't necessarily tree like can be modeled with 
[CrossNestedLogit.kt](src/main/kotlin/discreteChoice/distribution/CrossNestedLogit.kt).



## Common Lingo

Language you might stumble upon in this project.

- __Alternative__ - some choosable thing/object. Single entity.


- __utility__ - some value. Could be any. In contrast to probabilites it is not bound to the 
interval \[0,1]. Usually probabilities first need to be calculated out of all utility values.
Maybe with a 'softmax' function ($p_i = \frac{e^{u_i}}{\sum_j e^{u_j}}$, $u_j$ the utility
values, $p_i$ probability of i) or something else.