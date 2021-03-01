(ns rummikub-ctmx.views.room
  (:require
    [clojure.string :as string]
    [ctmx.core :as ctmx]
    ctmx.rt
    [rummikub-ctmx.controller.board :as board]
    [rummikub-ctmx.service.state :as state]
    [rummikub-ctmx.util :as util]
    [rummikub-ctmx.views.control-panel :as control-panel]))

(def ^:private base-style {:width "2em"
                           :height "3em"
                           :border "1px solid grey"
                           :margin "5px"
                           :display "inline-block"})
(defn tile-style [color x y]
  (util/fmt-style
    (if x
      (assoc base-style :color color :left (str x "px") :top (str y "px") :position "absolute")
      (assoc base-style :color color))))

(defn rummikub [empty?]
  [:button#rummikub
   {:hx-post "play-area"
    :hx-target hash
    :class [:btn :btn-danger (when-not empty? :d-none)]
    :hx-vals {:command "rummikub"}}
   "Rummikub!"])

(defn- rummikub-oob [req]
  (as-> req $
        (:session $)
        (:user $)
        (state/player-state $)
        (:rows $)
        (apply concat $)
        (empty? $)
        (rummikub $)
        (assoc-in $ [1 :hx-swap-oob] "true")))

(defn- tile-update [req]
  (board/update-table req)
  (rummikub-oob req))

(ctmx/defcomponent ^:endpoint tile [req _ [[color number suffix] [x y]]]
  (ctmx/with-req req
    (if (and patch? top-level?)
      (tile-update req)
      (let [n (string/join "-" [(name color) number suffix])
            color (case color :yellow "gold" (name color))]
        [:div.text-center.tile {:id n :style (tile-style color x y)}
         [:input {:type "hidden" :name "position"}]
         [:div {:hx-patch "tile" :hx-include (format "#%s input" n)}]
         [:input {:type "hidden" :name "tile" :value n}]
         (case number 0 ":)" number)]))))

(ctmx/defcomponent table-div [req table-tiles]
  [:div {:style "height: 400px"}
   (ctmx.rt/map-indexed tile req table-tiles)])

(ctmx/defcomponent ^:endpoint board-row [req ^:int i tiles]
  (let [tiles (or tiles (board/drop-into-board req i))]
    (list
      (when top-level?
        (rummikub-oob req))
      [:div {:id id
             :class (str "board" i)
             :style "border: 1px solid black; min-height: 60px"}
       [:input {:type "hidden" :name "position"}]
       [:input {:type "hidden" :name "tile"}]
       [:div {:hx-patch "board-row" :hx-target (hash ".") :hx-include (format ".board%s input" i)}]
       [:input {:type "hidden" :name "i" :value i}]
       (ctmx.rt/map-indexed #(tile %1 %2 [%3]) req tiles)])))

(defn next-turn [hash current]
  [:div.float-right
   "Current turn: " current
   [:button.btn.btn-primary.ml-2
    {:hx-post "play-area"
     :hx-target hash
     :onclick "pass()"
     :hx-vals (util/write-str {:command "next"})}
    "Pass"]])

(defn buttons [hash current empty?]
  [:div.mb-2
   (next-turn hash current)
   [:button#pickup.btn.btn-primary.mr-2
    {:hx-post "play-area"
     :hx-target hash
     :hx-vals (util/write-str {:command "pick-up"})}
    "Pick Up"]
   [:button.btn.btn-primary.mr-2
    {:hx-post "play-area"
     :hx-target hash
     :hx-vals (util/write-str {:command "sort"})}
    "Sort"]
   (rummikub empty?)])

(ctmx/defcomponent ^:endpoint play-area [req command]
  (ctmx/with-req req
    (let [user (-> req :session :user)
          _ (when post? (board/sort-tray user command))
          {table-tiles :table :keys [rows current]} (state/player-state user)
          empty? (empty? (apply concat rows))]
      [:div.play-area {:id id}
       [:div {:hx-put "play-area"
              :hx-trigger "sse:play-area"
              :hx-target (hash ".")}]
       (table-div req table-tiles)
       (buttons (hash ".") current empty?)
       (ctmx.rt/map-indexed board-row req rows)])))

(ctmx/defcomponent room [req]
  (let [{:keys [user]} (:session req)]
    [:div {:hx-sse (str "connect:/api/sse?user=" user)}
     [:div {:hx-sse "swap:script" :hx-swap "innerHTML"}]
     (control-panel/control-panel req user)
     [:h2 "Welcome " user]
     (play-area req)]))
