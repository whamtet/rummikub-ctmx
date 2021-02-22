(ns rummikub-ctmx.views.control-panel
  (:require
    [ctmx.core :as ctmx]
    ctmx.rt
    [rummikub-ctmx.service.sse :as sse]
    [rummikub-ctmx.service.state :as state]))

(ctmx/defcomponent ^:endpoint delete-row [req i user]
  (ctmx/with-req req
    (if delete?
      (sse/logout user)
      [:form
       [:input {:type "hidden" :name "user" :value user}]
       [:a {:href "javascript:void(0)"
            :hx-delete "delete-row"
            :hx-confirm (format "Delete %s?" user)}
        user]])))

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
