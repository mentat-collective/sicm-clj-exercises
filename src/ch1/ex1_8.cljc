;; Exercise 1.8: Implementation of $\delta$
;; :PROPERTIES:
;; :header-args+: :tangle src/ch1/ex1_8.cljc :comments org
;; :END:


(ns ch1.ex1-8
  (:refer-clojure :exclude [+ - * / compare zero? ref partial])
  (:require [sicmutils.env :as e #?@(:cljs [:include-macros true])]))

(e/bootstrap-repl!)
;; Part A: Implement $\delta_\eta$

;; The goal here is to implement $\delta_\eta$ as a procedure. Explicitly:

;; #+begin_quote
;; Suppose we have a procedure =f= that implements a path-dependent function: for
;; path =q= and time =t= it has the value =((f q) t)=. The procedure delta computes
;; the variation $\delta_\eta f[q](t)$ as the value of the expression =((((delta
;; eta) f) q) t)=. Complete the definition of =delta=:
;; #+end_quote

;; After laboriously proving all of the properties above, the actual implementation
;; feels so simple.

;; The key is equation 1.22 in the book:

;; \begin{equation}
;; \label{eq:1-22}
;; \delta_\eta f[q] = \lim_{\epsilon \to 0} \left( {g(\epsilon) - g(0)} \over \epsilon \right) = Dg(0)
;; \end{equation}

;; Given $g(\epsilon) = f[q + \epsilon \eta]$. Through the magic of automatic
;; differentiation we can simply write:


(defn delta [eta]
  (fn [f]
    (fn [q]
      (letfn [(g [eps]
                (f (+ q (* eps eta))))]
        ((D g) 0)))))
;; Part B: Check $\delta_\eta$'s properties

;; Part B's problem description gave us a path-dependent function similar to this
;; one:


(defn litfn [sym]
  (fn [q]
    (let [Local '(UP Real (UP* Real 2) (UP* Real 2))
          F     (literal-function sym (list '-> Local 'Real))]
      (compose F (Gamma q)))))


;; #+RESULTS:
;; : #'ch1.ex1-8/litfn

;; I've modified it slightly to take in a symbol, since we'll need to generate
;; multiple functions for a few of the rules.

;; $litfn$ takes a symbol like $F$ and a path function -- a function from $t$ to any
;; number of coordinates (see the =UP*=?) -- and returns a generic expression for a
;; path dependent function $F$ that acts via $F \circ \Gamma[q]$. $F$ might be a
;; Lagrangian, for example.

;; The textbook also gives us this function from $t \to (x, y)$ to test out the
;; properties above. I've added an $\eta$ of the same type signature that we can
;; use to add variation to the path.


(def q (literal-function 'q (-> Real (UP Real Real))))
(def eta (literal-function 'eta (-> Real (UP Real Real))))
;; Variation Product Rule

;; Equation \eqref{eq:var-prod} states the product rule for variations. Here it is
;; in code. I've implemented the right and left sides and subtracted them. As
;; expected, the result is 0:


(let [f     (litfn 'f)
      g     (litfn 'g)
      de    (delta eta)
      left  ((de (* f g)) q)
      right (+ (* (g q) ((de f) q))
               (* (f q) ((de g) q)))]
  (->tex-equation
   ((- left right) 't)))
;; Variation Sum Rule

;; The sum rule is similar. Here's the Scheme implementation of equation
;; \eqref{eq:var-sum}:


(let [f     (litfn 'f)
      g     (litfn 'g)
      de    (delta eta)
      left  ((de (+ f g)) q)
      right (+ ((de f) q)
               ((de g) q))]
  (->tex-equation
   ((- left right) 't)))
;; Variation Scalar Multiplication

;; Here's equation \eqref{eq:var-scalar} in code. The sides are equal, so their
;; difference is 0:


(let [g     (litfn 'g)
      de    (delta eta)
      left  ((de (* 'c g)) q)
      right (* 'c ((de g) q))]
  (->tex-equation
   ((- left right) 't)))
;; Chain Rule for Variations

;; To compute the chain rule we'll need a version of =fn= that takes the derivative
;; of the inner function:


(defn Dfn [sym]
  (fn [q]
    (let [Local '(UP Real (UP* Real 2) (UP* Real 2))
          F     (literal-function sym ['-> Local 'Real])]
      (compose (D F) (Gamma q)))))


;; #+RESULTS:
;; : #'ch1.ex1-8/Dfn

;; For the Scheme implementation, remember that both =fn= and =Dfn= have $\Gamma$
;; baked in. The $g$ in equation \eqref{eq:var-chain} is hardcoded to $\Gamma$ in
;; the function below.

;; Here's a check that the two sides of equation \eqref{eq:var-chain} are equal:


(let [h     (litfn 'F)
      dh    (Dfn 'F)
      de    (delta eta)
      left  (de h)
      right (* dh (de Gamma))]
  (->tex-equation
   (((- left right) q) 't)))
;; $\delta_\eta$ commutes with $D$

;; Our final test. Here's equation \eqref{eq:var-commute} in code, showing that the
;; derivative commutes with the variation operator:


(let [f     (litfn 'f)
      g     (compose D f)
      de    (delta eta)
      left  (D ((de f) q))
      right ((de g) q)]
  (->tex-equation
   ((- left right) 't)))
