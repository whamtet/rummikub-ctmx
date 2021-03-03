(ns rummikub-ctmx.views.room
  (:require
    [clojure.string :as string]
    [ctmx.core :as ctmx]
    ctmx.rt
    [rummikub-ctmx.config :as config]
    [rummikub-ctmx.controller.board :as board]
    [rummikub-ctmx.service.state :as state]
    [rummikub-ctmx.util :as util]
    [rummikub-ctmx.views.control-panel :as control-panel]
    [rummikub-ctmx.views.tile :as tile]))

(defn rummikub [hash empty?]
  [:button#rummikub
   {:hx-post "board"
    :hx-target hash
    :class [:btn :btn-danger (when-not empty? :d-none)]
    :hx-vals {:command "rummikub"}}
   "Rummikub!"])

(defn- rummikub-oob [hash user]
  (as-> user $
        (state/rows-for $)
        (apply concat $)
        (empty? $)
        (rummikub hash $)
        (assoc-in $ [1 :hx-swap-oob] "true")))

(ctmx/defn-parse update-table [hash {:keys [^:int x ^:int y tile]} user]
  (board/update-table tile x y user)
  (rummikub-oob hash user))

(ctmx/defcomponent ^:endpoint table-div [req]
  (util/with-user req
    (if patch?
      (update-table (hash "../board") params user)
      [:div {:style "height: 400px"}
       [:div.table-update {:id id :hx-patch "table-div" :hx-swap "innerHTML" :hx-target (hash ".")}]
       (map tile/tile (state/table-for user))])))

(ctmx/defcomponent ^:endpoint next-turn [req]
  (util/with-user req
    (if (and post? top-level?)
      (board/next-turn id)
      (tile/turn-panel id user (state/current)))))

(ctmx/defcomponent board-row [req ^:int i tiles]
  [:div {:class (str "board" i)
         :style "border: 1px solid black; min-height: 60px"}
   [:div {:hx-patch "board"
          :hx-target (hash "../..")
          :hx-vals {:command "drop"}
          :hx-include (format ".board%s input" i)}]
   [:input {:type "hidden" :name "i" :value i}]
   (map tile/tile-board tiles)])

(ctmx/defcomponent buttons [req empty?]
  [:div.mb-2
   (next-turn req)
   [:button#pickup.btn.btn-primary.mr-2
    {:hx-post "board"
     :hx-target (hash "..")
     :hx-vals (util/write-str {:command "pick-up"})}
    "Pick Up"]
   [:button.btn.btn-primary.mr-2
    {:hx-post "board"
     :hx-target (hash "..")
     :hx-vals (util/write-str {:command "sort"})}
    "Sort"]
   (rummikub (hash "..") empty?)])

(ctmx/defcomponent ^:endpoint board [req command]
  (util/with-user req
    (when command
      (board/sort-tray req user command))
    (when (not= "rummikub" command)
      (let [rows (state/rows-for user)]
        (list
          (when top-level?
            (rummikub-oob (hash ".") user))
          [:div {:id id}
           (buttons req (->> rows (apply concat) empty?))
           (ctmx.rt/map-indexed board-row req rows)])))))

(ctmx/defcomponent room [req]
  (let [{:keys [user]} (:session req)]
    [:div {:hx-ws (format "connect:%s/api/sse?user=%s" (config/ws-host) user)}
     [:script#script]
     (control-panel/control-panel req user)
     [:h2 "Welcome " user]
     (table-div req)
     (board req)]))
