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

(defn- rummikub-oob [hash req]
  (as-> req $
        (:session $)
        (:user $)
        (state/rows-for $)
        (apply concat $)
        (empty? $)
        (rummikub hash $)
        (assoc-in $ [1 :hx-swap-oob] "true")))

(ctmx/defn-parse update-table [hash {:keys [^:int x ^:int y tile]} user]
  (board/update-table tile x y user)
  nil)

(defmacro with-user [req & body]
  `(ctmx/with-req ~req
     (let [{:keys [~'user]} ~'session]
       ~@body)))

(ctmx/defcomponent ^:endpoint table-div [req]
  (with-user req
    (if patch?
      (update-table "#todo" params user)
      [:div {:id id :style "height: 400px"}
       [:div#table-update {:hx-patch "table-div"}]
       (map tile/tile (state/table-for user))])))

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

(defn next-turn [hash current]
  [:div.float-right
   "Current turn: " current
   [:button#pass.btn.btn-primary.ml-2
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
   (rummikub hash empty?)])

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
     (table-div req)]))
