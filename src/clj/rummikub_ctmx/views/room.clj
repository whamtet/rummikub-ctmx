(ns rummikub-ctmx.views.room
  (:require
    [ctmx.core :as ctmx]
    ctmx.response
    ctmx.rt
    [rummikub-ctmx.service.sse :as sse]
    [rummikub-ctmx.service.state :as state]
    [rummikub-ctmx.util :as util]))

(def ^:private base-style {:width "2em" :height "3em" :border "1px solid grey" :margin "5px"})
(defn tile [[number color]]
  [:div {:style (-> base-style (assoc :color color) util/fmt-style)}
   (case number :joker ":)" number)])

(def ^:private board-style {:border "1ps solid black"})
(defn board [tiles]
  [:div {:style (util/fmt-style board-style)}
   (map tile tiles)])

(ctmx/defcomponent ^:endpoint delete-row [req i user]
  (ctmx/with-req req
    (if delete?
      (sse/logout user)
      [:form {:hx-delete "delete-row"
              :hx-confirm (format "Delete %s?" user)
              :hx-trigger "click"}
       [:input {:type "hidden" :name "user" :value user}]
       [:a {:href "javascript:void(0)"} user]])))

(ctmx/defcomponent control-panel [req user]
  [:div.float-right
   [:button.btn.btn-primary
    {:hx-delete "root" :hx-trigger "click, sse:logout"}
    "Quit"]
   (when (= "Matt" user)
     [:div.mt-2
      (->> (state/users)
           (remove #(= % "Matt"))
           (ctmx.rt/map-indexed delete-row req))])])

(ctmx/defcomponent room [req]
  (let [{:keys [user]} (:session req)]
    [:div {:hx-sse (str "connect:/api/sse?user=" user)}
     [:div {:hx-get "/api/refresh" :hx-trigger "sse:refresh"}]
     (control-panel req user)
     [:h2 "Welcome " user]]))
