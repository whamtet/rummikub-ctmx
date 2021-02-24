(ns rummikub-ctmx.views.login
  (:require
    [ctmx.core :as ctmx]
    ctmx.response
    [rummikub-ctmx.controller.login :as login]))

(ctmx/defcomponent ^:endpoint login [req user password]
  (ctmx/with-req req
    (let [result (and post? (login/login! user password))]
      (if (string? result)
        (assoc ctmx.response/hx-refresh :session {:user result})
        [:form {:hx-post "login"}
         [:h3 "Rummikub Login"]
         [:h6.mt-3 "Built with " [:a {:href "https://ctmx.info" :_target "blank"} "CTMX"] ". Light, fast, secure"]
         [:div.form-group.mt-4
          [:label "Player name"]
          [:input.form-control
           {:type "text" :name "user" :value user :placeholder "Chose name or leave blank..."}]]
         [:div.form-group
          [:label "Password"]
          [:input.form-control
           {:type "password" :name "password" :placeholder "Enter Password" :required true}]]
         [:button.btn.btn-primary.mr-2 "Submit"]
         (when (= :invalid-password result)
           [:div.badge.badge-danger "Incorrect Password"])
         (when (= :user-exists result)
           [:div.badge.badge-danger "User Exists"])]))))
