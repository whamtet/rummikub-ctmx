(ns rummikub-ctmx.controller.board
  (:require
    [ctmx.core :refer [defn-parse]]
    [rummikub-ctmx.service.sse :as sse]
    [rummikub-ctmx.service.state :as state]))

(defn- parse-tile [s]
  (let [[a b c] (.split s "-")]
    [(keyword a) (Long/parseLong b) (Long/parseLong c)]))
(defn- parse-coord [s]
  (let [[a b] (.split s ", ")]
    [(Double/parseDouble a) (Double/parseDouble b)]))
(def ^:private parse-left #(-> % parse-coord first))

(defn update-table [tile x y user]
  (let [tile (parse-tile tile)]
    (state/put-down! tile [x y])
    (sse/update-tile tile [x y] user)))

(defn-parse drop-into-board [{{:keys [^:array position ^:array tile ^:int i]} :params} user]
  (let [lefts (map parse-left position)
        tiles (map parse-tile tile)
        row (->> (map list tiles lefts)
                 (sort-by second)
                 (map first))]
    (state/pick-up-used! row user i)
    (sse/update-tile (last tiles) [:hidden] user)))

(defn next-turn [id]
  (sse/pass-all id (state/next-turn!))
  nil)

(defn sort-tray [req user command]
  (case command
    "drop" (drop-into-board req user)
    "pick-up" (state/pick-up-one! user)
    "sort" (state/sort-player! user)
    "rummikub"
    (sse/rummikub! user)))
