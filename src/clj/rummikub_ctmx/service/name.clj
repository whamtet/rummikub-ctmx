(ns rummikub-ctmx.service.name
  (:require
    [rummikub-ctmx.service.state :as state]))

(def first-names ["Obese" "Flying" "Naughty" "Sad"])
(def surnames ["Berty" "Fiona" "Nickel" "Sacks"])

(defn rand-name []
  (str (rand-nth first-names) " " (rand-nth surnames)))

(defn new-name []
  (loop [name (rand-name)]
    (if (some #(= name %) (state/users))
      (recur (rand-name))
      name)))
