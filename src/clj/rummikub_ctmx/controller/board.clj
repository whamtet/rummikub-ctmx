(ns rummikub-ctmx.controller.board
  (:require
    [rummikub-ctmx.service.sse :as sse]
    [rummikub-ctmx.service.state :as state]))

(defn- parse-tile [s]
  (let [[_ a b c] (.split s "-")
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
        [x y] (-> drop-tile params parse-coord)
        valid-key? #(or (= % drop-tile) (and (string? %) (.startsWith % drop-row)))
        row (->> params
                 (filter #(-> % first valid-key?))
                 (sort-by #(-> % second parse-coord first))
                 (map #(-> % first parse-tile)))
        i (Long/parseLong drop-row)]
    (if (= 2 i)
      (state/put-down! tile x y)
      (state/pick-up-used! row user i))
    (sse/play-all user)))

(defn sort-tray [user command]
  (case command
    "pick-up" (state/pick-up-one! user)
    "sort" (state/sort-player! user)
    "next"
    (do
      (state/next-turn!)
      (sse/play-all user))))
