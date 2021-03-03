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
   {:hx-post "play-area"
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
      (update-table "#todo" params user)
      [:div {:id id :style "height: 400px"}
       [:div#table-update {:hx-patch "table-div"}]
       (map tile/tile (state/table-for user))])))

(ctmx/defcomponent ^:endpoint next-turn [req]
  (util/with-user req
    (if post?
      (board/next-turn id)
      (tile/turn-panel id user (state/current)))))

#_(ctmx/defcomponent ^:endpoint board-row [req ^:int i tiles]
    (let [tiles (or tiles (board/drop-into-board req i))]
      (list
        (when top-level?
          (rummikub-oob (hash "../..") req))
        [:div {:id id
               :class (str "board" i)
               :style "border: 1px solid black; min-height: 60px"}
         [:div {:hx-patch "board-row" :hx-target (hash ".") :hx-include (format ".board%s input" i)}]
         [:input {:type "hidden" :name "i" :value i}]
         (ctmx.rt/map-indexed #(tile %1 %2 [%3]) req tiles)])))

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
      (prn 'board command))
    (let [rows (state/rows-for user)]
      [:div {:id id}
       (buttons req (->> rows (apply concat) empty?))
       ])))

#_(ctmx/defcomponent ^:endpoint play-area [req command]
    (ctmx/with-req req
      (let [user (-> req :session :user)
            _ (when post? (board/sort-tray user command))
            {table-tiles :table :keys [rows current]} (state/player-state user)
            empty? (empty? (apply concat rows))]
        [:div.play-area {:id id}
         (table-div req table-tiles)
         (buttons (hash ".") current empty?)
         (ctmx.rt/map-indexed board-row req rows)])))

(ctmx/defcomponent room [req]
  (let [{:keys [user]} (:session req)]
    [:div {:hx-ws (format "connect:ws:%s/api/sse?user=%s" (config/host) user)}
     [:script#script]
     (control-panel/control-panel req user)
     [:h2 "Welcome " user]
     (table-div req)
     (board req)]))
