(ns rummikub-ctmx.views.room
  (:require
    [rummikub-ctmx.util :as util]))

(def ^:private base-style {:width "2em" :height "3em" :border "1px solid grey" :margin "5px"})
(defn tile [[number color]]
  [:div {:style (-> base-style (assoc :color color) util/fmt-style)}
   (case number :joker ":)" number)])

(def ^:private board-style {:border "1ps solid black"})
(defn board [tiles]
  [:div {:style (util/fmt-style board-style)}
   (map tile tiles)])

(defn room [req]
  (let [{:keys [user]} (:session req)]
  [:div
   [:button.btn.btn-primary.float-right {:hx-delete "root"} "Quit"]
   [:h2 "Welcome " user]]))
