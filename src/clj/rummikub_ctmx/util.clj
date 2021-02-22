(ns rummikub-ctmx.util
  (:require
    [clojure.string :as string]))

(defn fmt-style [style]
  (->> style
       (map (fn [[k v]] (str (name k) ": " v)))
       (string/join "; ")))
