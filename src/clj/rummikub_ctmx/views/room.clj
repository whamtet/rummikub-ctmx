(ns rummikub-ctmx.views.room
  (:require
    [ctmx.core :as ctmx]
    [rummikub-ctmx.service.state :as state]
    [rummikub-ctmx.util :as util]
    [rummikub-ctmx.views.control-panel :as control-panel]))

(def ^:private base-style {:width "2em"
                           :height "3em"
                           :border "1px solid grey"
                           :margin "5px"
                           :display "inline-block"})
(defn tile [[color number]]
  (let [color (case color :yellow "gold" (name color))]
    [:div.text-center.tile {:style (-> base-style (assoc :color color) util/fmt-style)}
     (case number :joker ":)" number)]))

(defn board [players]
  (let [tiles-for-row
        (fn [row]
          (for [[tile [_ i]] players :when (= row i)] tile))]
    [:div#board
     [:div {:style "border: 1px solid black"}
      (->> 0 tiles-for-row (map tile))]
     [:div {:style "border: 1px solid black"}
      (->> 1 tiles-for-row (map tile))]]))

(defn table-div [table]
  [:div#table {:style "height: 450px"}])

(ctmx/defcomponent room [req]
  (let [{:keys [user]} (:session req)
        {:keys [table players]} (state/player-state user)]
    [:div {:hx-sse (str "connect:/api/sse?user=" user)}
     [:div {:hx-sse "swap:script" :hx-swap "innerHTML"}]
     (control-panel/control-panel req user)
     [:h2 "Welcome " user]
     (table-div table)
     (board players)]))
