(ns rummikub-ctmx.util
  (:require
    [clojure.data.json :as json]
    [clojure.string :as string]))

(defn fmt-style [style]
  (->> style
       (map (fn [[k v]] (str (name k) ": " v)))
       (string/join "; ")))

(def write-str json/write-str)

(defn filter-vals [f m]
  (into {}
        (for [[k v] m :when (f v)]
          [k v])))
