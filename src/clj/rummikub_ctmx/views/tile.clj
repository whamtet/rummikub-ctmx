(ns rummikub-ctmx.views.tile
  (:require
    [clojure.string :as string]
    [rummikub-ctmx.util :as util]))

(def ^:private base-style {:width "2em"
                           :height "3em"
                           :border "1px solid grey"
                           :margin "5px"
                           :display "inline-block"})
(defn tile-style [color x y]
  (case x
    :hidden (assoc base-style :display "none")
    (assoc base-style :color color :left (str x "px") :top (str y "px") :position "absolute")))

(defn tile [[[color number suffix] [x y]]]
  (let [n (string/join "-" [(name color) number suffix])
        color (case color :yellow "gold" (name color))]
    [:div.text-center.tile {:id n :style (tile-style color x y)}
     [:input {:type "hidden" :name "position"}]
     [:input {:type "hidden" :name "tile" :value n}]
     (case number 0 ":)" number)]))

(defn tile-board [[color number suffix]]
  (let [n (string/join "-" [(name color) number suffix])
        color (case color :yellow "gold" (name color))
        style (assoc base-style :color color)]
    [:div.text-center.tile {:style style}
     [:input {:type "hidden" :name "position"}]
     [:input {:type "hidden" :name "tile" :value n}]
     (case number 0 ":)" number)]))

(defn turn-panel [id user current]
  [:form.float-right {:id id :hx-post "next-turn"}
   "Current turn: " current
   [:input#pass.btn.btn-primary.ml-2
    {:type "submit" :value "Pass" :disabled (not= user current)}]])
