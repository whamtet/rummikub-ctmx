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
                           :display "inline-block"
                           :position "absolute"})

(defn tile [[[color number _] [x y]]]
  (let [n (string/join "-"
                       (if (= :joker number)
                         [(name color) "joker"]
                         [(name color) number _]))
        color (case color :yellow "gold" (name color))
        left (some-> x (str "px"))
        top (some-> y (str "px"))
        style (util/fmt-style
                (assoc base-style :color color :left left :top top))]
    [:div.text-center.tile {:id n :style style}
     [:input {:type "hidden" :name n}]
     (case number :joker ":)" number)]))

(defn- board-row [i tiles]
  [:div {:id (str "board" i) :style "border: 1px solid black; height: 60px"}
   (map #(-> % list tile) tiles)])

(defn table-div [table-tiles]
  [:div#table {:style "height: 450px"}
   (map tile table-tiles)])

(ctmx/defcomponent ^:endpoint play-area [req]
  (ctmx/with-req req
    (when patch?
      (board/update-board req))
    (let [user (-> req :session :user)
          {table-tiles :table rows :rows} (state/player-state user)]
      [:form.play-area {:id id :hx-patch "play-area"}
       [:input#drop-row {:type "hidden" :name "drop-row"}]
       [:input#drop-tile {:type "hidden" :name "drop-tile"}]
       [:input#play-area-submit.d-none {:type "submit"}]
       (table-div table-tiles)
       (map-indexed board-row rows)])))

(ctmx/defcomponent room [req]
  (let [{:keys [user]} (:session req)]
    [:div {:hx-sse (str "connect:/api/sse?user=" user)}
     [:div {:hx-sse "swap:script" :hx-swap "innerHTML"}]
     (control-panel/control-panel req user)
     [:h2 "Welcome " user]
     (play-area req)]))
