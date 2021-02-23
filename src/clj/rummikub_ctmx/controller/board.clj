(ns rummikub-ctmx.controller.board
  (:require
    [rummikub-ctmx.service.sse :as sse]
    [rummikub-ctmx.service.state :as state]))

(defn- parse-tile [s]
  (let [[a b c] (.split s "-")
        a (keyword a)
        b (case b "joker" :joker (Long/parseLong b))
        c (some-> c Long/parseLong)]
    [a b c]))
(defn- parse-coord [s]
  (let [[a b] (.split s ", ")]
    [(Double/parseDouble a) (Double/parseDouble b)]))

(defn update-board [{:keys [session params]}]
  (let [{:keys [drop-tile drop-row]} params
        {:keys [user]} session
        tile (parse-tile drop-tile)
        [x y] (-> drop-tile keyword params parse-coord)
        drop-row (Long/parseLong drop-row)]
    (if (= 2 drop-row)
      (state/put-down! tile x y))))
