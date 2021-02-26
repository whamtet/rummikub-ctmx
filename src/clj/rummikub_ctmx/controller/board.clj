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

(defn update-table [{:keys [session params]}]
  (let [{:keys [tile position]} params
        {:keys [user]} session]
    (state/put-down! (parse-tile tile) (parse-coord position))
    (sse/play-all user)
    nil))

(defn-parse drop-into-board [{{:keys [^:array position ^:array tile]} :params
                              {:keys [user]} :session}
                             i]
  (let [lefts (map parse-left position)
        tiles (map parse-tile tile)
        row (->> (map list tiles lefts)
                 (sort-by second)
                 (map first))]
    (state/pick-up-used! row user i)
    (sse/play-all user)
    row))

(defn sort-tray [user command]
  (case command
    "pick-up" (state/pick-up-one! user)
    "sort" (state/sort-player! user)
    "next"
    (do
      (state/next-turn!)
      (sse/play-all user)
      (sse/pass-all user)
      nil)))
