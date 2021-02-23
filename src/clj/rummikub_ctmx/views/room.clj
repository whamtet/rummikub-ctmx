(ns rummikub-ctmx.views.room
  (:require
    [clojure.string :as string]
    [ctmx.core :as ctmx]
    ctmx.rt
    [rummikub-ctmx.service.state :as state]
    [rummikub-ctmx.util :as util]
    [rummikub-ctmx.views.control-panel :as control-panel]))

(def ^:private base-style {:width "2em"
                           :height "3em"
                           :border "1px solid grey"
                           :margin "5px"
                           :display "inline-block"})
(defn tile [i [color number _]]
  (let [n (string/join "-"
                       (if (= :joker number)
                         [i (name color) "joker"]
                         [i (name color) number _]))
        color (case color :yellow "gold" (name color))
        style (-> base-style (assoc :color color) util/fmt-style)]
    [:div.text-center.tile {:id n :style style}
     [:input {:type "hidden" :name n}]
     (case number :joker ":)" number)]))

(defn- board-row [i tiles]
  [:div {:id (str "board" i) :style "border: 1px solid black"}
   (map #(tile i %) tiles)])

(defn rearrange-board [])

(ctmx/defcomponent ^:endpoint board [req rows]
  (ctmx/with-req req
    (if patch?
      (prn params)
      [:form.board {:id id :hx-patch "board"}
       [:input#drop-row {:type "hidden" :name "drop-row"}]
       [:input#drop-tile {:type "hidden" :name "drop-tile"}]
       [:input#board-submit.d-none {:type "submit"}]
       (map-indexed board-row rows)])))

(defn table-div [table]
  [:form#table {:style "height: 450px"}
   [:input#table-submit.d-none {:type "submit"}]])

(ctmx/defcomponent room [req]
  (let [{:keys [user]} (:session req)
        {:keys [table rows]} (state/player-state user)]
    [:div {:hx-sse (str "connect:/api/sse?user=" user)}
     [:div {:hx-sse "swap:script" :hx-swap "innerHTML"}]
     (control-panel/control-panel req user)
     [:h2 "Welcome " user]
     (table-div table)
     (board req rows)]))
