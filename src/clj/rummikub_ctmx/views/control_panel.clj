(ns rummikub-ctmx.views.control-panel
  (:require
    [ctmx.core :as ctmx]
    ctmx.rt
    [rummikub-ctmx.controller.login :as login]
    [rummikub-ctmx.service.state :as state]))

(ctmx/defcomponent ^:endpoint delete-row [req i user]
  (ctmx/with-req req
    (if delete?
      (login/delete-user user)
      [:form
       [:input {:type "hidden" :name "user" :value user}]
       [:a {:href "javascript:void(0)"
            :hx-delete "delete-row"
            :hx-confirm (format "Delete %s?" user)}
        user]])))

(ctmx/defcomponent ^:endpoint control-panel [req user]
  (ctmx/with-req req
    (if delete?
      (login/reset-game)
      [:div.float-right
       [:button.btn.btn-primary
        {:hx-delete "root"
         :hx-confirm "Quit?"} "Quit"] [:br]
       [:button.btn.btn-primary.mt-2
        {:hx-delete "control-panel"
         :hx-confirm "Reset Game?"} "Reset"]
       [:div.mt-2
        (->> (state/users)
             (remove #(= % user))
             (ctmx.rt/map-indexed delete-row req))]])))
