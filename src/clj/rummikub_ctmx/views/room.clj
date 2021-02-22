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

(defn control-panel [user]
  [:div.float-right
   [:button.btn.btn-primary
    {:hx-delete "root" :hx-trigger "click, sse:logout"}
    "Quit"]
   (when (= "Matt" user)
     [:div "panel"])])

(defn room [req]
  (let [{:keys [user]} (:session req)]
    [:div {:hx-sse (str "connect:/api/sse?user=" user)}
     (control-panel user)
     [:h2 "Welcome " user]]))
