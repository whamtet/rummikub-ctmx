(ns rummikub-ctmx.util
  (:require
    [clojure.data.json :as json]
    [clojure.string :as string]))

(defn fmt-style [style]
  (string/join "; "
               (for [[k v] style :when v]
                 (str (name k) ": " v))))

(def write-str json/write-str)

(defn filter-vals [f m]
  (into {}
        (for [[k v] m :when (f v)]
          [k v])))

(defn map-vals [f m]
  (zipmap (keys m) (map f (vals m))))
