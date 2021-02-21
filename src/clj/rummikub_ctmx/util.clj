(ns rummikub-ctmx.util
  (:require
    clojure.pprint))

(defn ppr-str [arg]
  (with-out-str
    (clojure.pprint/pprint arg)))
