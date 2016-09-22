(ns spec-race.core-test
  (:require [clojure.test :refer :all]))

;;;; locking the dynaload require so the same file isn't concurrently loaded
;;;; makes both map & pmap work
;;  (locking require-lock
;;    (require (symbol ns)))
;;;; vs.
;;  (require (symbol ns))
(def require-lock (Object.))

(defn- dynaload
  [s]
  (let [ns (namespace s)]
    (assert ns)
    (require (symbol ns))
    (let [v (resolve s)]
      (if v
        @v
        (throw (RuntimeException. (str "Var " s " is not on the classpath")))))))

(defn call-asdf-consumer [x]
  (let [f (dynaload 'spec-race.dynaloadable/asdf-consumer)]
    (f)))

;;; uncommenting this makes both map & pmap work (eager-loading the ns before adding parallelism)
;; (require 'clojure-spec.dynaloadable)

(defn mapped-values [n]
  (map call-asdf-consumer (range n)))

(defn pmapped-values [n]
  (pmap call-asdf-consumer (range n)))

(def iterations 1000)

;; pmap fails
(deftest racy-test
  (testing "race repro"
    (is (= (repeat iterations 3)
           (pmapped-values iterations)))))

;;;; map works fine
;;(deftest working-test
;;  (testing "race repro"
;;    (is (= (repeat iterations 3)
;;           (mapped-values iterations)))))

