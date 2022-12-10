;; Exercise 1.13: Higher-derivative Lagrangians (code)
;; :PROPERTIES:
;; :header-args+: :tangle src/ch1/ex1_13.cljc :comments org
;; :END:

;; This exercise completes exercise 1.10 by asking for implementations of the
;; higher-order Lagrange equations that we derived. This was a nice Scheme
;; exercise; I would argue that this implementation should exist in the standard
;; library. But that would ruin the fun of the exercise...


(ns ch1.ex1-13
  (:refer-clojure :exclude [+ - * / compare zero? ref partial])
  (:require [sicmutils.env :as e #?@(:cljs [:include-macros true])]))

(e/bootstrap-repl!)
;; Part A: Acceleration-dependent Lagrangian Implementation

;; From the book:

;; #+begin_quote
;; Write a procedure to compute the Lagrange equations for Lagrangians that depend
;; upon acceleration, as in exercise 1.10. Note that Gamma can take an optional
;; argument giving the length of the initial segment of the local tuple needed. The
;; default length is 3, giving components of the local tuple up to and including
;; the velocities.
;; #+end_quote

;; Now that we know the math, the implementation is a straightforward extension of
;; the =Lagrange-equations= procedure presented in the book:


(defn Lagrange-equations3 [L]
  (fn [q]
    (let [state-path (Gamma q 4)]
      (+ ((square D) (compose ((partial 3) L) state-path))
         (- (D (compose ((partial 2) L) state-path)))
         (compose ((partial 1) L) state-path)))))
;; Part B: Applying HO-Lagrangians

;; Now it's time to use the new function. From the book:

;; #+begin_quote
;; Use your procedure to compute the Lagrange equations for the Lagrangian

;; \begin{equation}
;; L(t, x, v, a) = - {1 \over 2}mxa - {1 \over 2}kx^2
;; \end{equation}

;; Do you recognize the resulting equation of motion?
;; #+end_quote

;; Here is the Lagrangian described in the problem:


(defn L-1-13 [m k]
  (fn [[_ x _ a]]
    (- (* (/ -1 2) m x a)
       (* (/ 1 2) k (square x)))))


;; #+RESULTS:
;; : #'ch1.ex1-13/L-1-13

;; Use the new function to generate the Lagrange equations. This call includes a
;; factor of $-1$ to make the equation look nice:


(->tex-equation
 (- (((Lagrange-equations3 (L-1-13 'm 'k))
      (literal-function 'x)) 't)))
;; Part C: Generalized Lagrange Equations

;; Now, some more fun with Scheme. It just feels nice to implement Scheme
;; procedures. From the book:

;; #+begin_quote
;; For more fun, write the general Lagrange equation procedure that takes a
;; Lagrangian that depends on any number of derivatives, and the number of
;; derivatives, to produce the required equations of motion.
;; #+end_quote

;; As a reminder, this is the equation that we need to implement for each
;; coordinate:

;; \begin{equation}
;;   0 = \sum_{i = 0}^n(-1)^i D^{i}(\partial_{i+1}L \circ \Gamma[q])
;; \end{equation}

;; There are two ideas playing together here. Each term takes an element from:

;; - an alternating sequence of $1$ and $-1$
;; - a sequence of increasing-index $D^i(\partial_i L \circ \Gamma[q])$ terms

;; The alternating $1, -1$ sequence is similar to a more general idea: take any
;; ordered collection arranged in a cycle, start at some point and walk around the
;; cycle for $n$ steps. In Clojure this can be expressed as


(cycle [1 -1])


;; Now, the general =Lagrange-equations*= implementation:


(defn Lagrange-equations* [L n]
  (fn [q]
    (let [state-path (Gamma q (inc n))
          terms (map (fn [coef i]
                       (* coef
                          ((expt D i)
                           (compose ((partial (inc i)) L)
                                    state-path))))
                     (cycle [1 -1])
                     (range n))]
      (reduce + terms))))


;; #+RESULTS:
;; : #'ch1.ex1-13/Lagrange-equations*

;; Generate the Lagrange equations from part b once more to check that we get the
;; same result:


(->tex-equation
 (- (((Lagrange-equations* (L-1-13 'm 'k) 3)
      (literal-function 'x)) 't)))
