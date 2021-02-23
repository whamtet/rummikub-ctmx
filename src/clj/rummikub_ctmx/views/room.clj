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

(defn tile [i [[color number _] [x y]]]
  (let [n (string/join "-"
                       (if (= :joker number)
                         [i (name color) "joker"]
                         [i (name color) number _]))
        color (case color :yellow "gold" (name color))
        style (assoc base-style :color color)
        style (if x
                (assoc style :left (str x "px") :top (str y "px") :position "absolute")
                style)]
    [:div.text-center.tile {:id n :style (util/fmt-style style)}
     [:input {:type "hidden" :name n}]
     (case number :joker ":)" number)]))

(defn- board-row [i tiles]
  [:div {:id (str "board" i) :style "border: 1px solid black; height: 60px"}
   (map #(tile i (list %)) tiles)])

(defn table-div [table-tiles]
  [:div {:style "height: 400px"}
   (map #(tile 2 %) table-tiles)])

(defn next-turn [hash current]
  [:div.float-right
   "Current turn: " current
   [:button.btn.btn-primary.ml-2
    {:hx-post "play-area"
     :hx-target hash
     :hx-vals (util/write-str {:command "next"})}
    "Pass"]])

(defn buttons [hash current]
  [:div.mb-2
   (next-turn hash current)
   [:button.btn.btn-primary.mr-2
    {:hx-post "play-area"
     :hx-target hash
     :hx-vals (util/write-str {:command "pick-up"})}
    "Pick Up"]
   [:button.btn.btn-primary
    {:hx-post "play-area"
     :hx-target hash
     :hx-vals (util/write-str {:command "sort"})}
    "Sort"]])

(ctmx/defcomponent ^:endpoint play-area [req command]
  (ctmx/with-req req
    (when patch?
      (board/update-board req))
    (let [user (-> req :session :user)
          _ (when post? (board/sort-tray user command))
          {table-tiles :table :keys [rows current]} (state/player-state user)]
      [:form.play-area {:id id :hx-patch "play-area"}
       [:div {:hx-put "play-area"
              :hx-trigger "sse:play-area"
              :hx-target (hash ".")}]
       [:input#drop-row {:type "hidden" :name "drop-row"}]
       [:input#drop-tile {:type "hidden" :name "drop-tile"}]
       [:input#play-area-submit.d-none {:type "submit"}]
       (table-div table-tiles)
       (buttons (hash ".") current)
       (map-indexed board-row rows)])))

(ctmx/defcomponent room [req]
  (let [{:keys [user]} (:session req)]
    [:div {:hx-sse (str "connect:/api/sse?user=" user)}
     [:div {:hx-sse "swap:script" :hx-swap "innerHTML"}]
     (control-panel/control-panel req user)
     [:h2 "Welcome " user]
     (play-area req)]))
