(ns spec-race.dynaloadable)

(defmacro asdf []
  `(+ 1 2))

(defn asdf-consumer []
  (asdf))

