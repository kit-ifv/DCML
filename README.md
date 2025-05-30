This project supplies functionalities for modeling of discrete choice situations.

# Common Lingo
Language you might stumble over in this project.
- __Alternative__ - some choosable thing/object. Single entity.
- __utility__ - some value. Could be any. In contrast to probabilites it is not bound to the interval \[0,1]. Usually probabilities first need to be calculated out of all utility values. Maybe with a 'softmax' function ($p_i = \frac{e^{u_i}}{\sum_j e^{u_j}}$, $u_j$ the utility values, $p_i$ probability of i) or something else.
- 