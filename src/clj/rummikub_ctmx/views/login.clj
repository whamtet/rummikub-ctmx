(ns rummikub-ctmx.views.login
  (:require
    [ctmx.core :as ctmx]
    ctmx.response
    [rummikub-ctmx.controller.login :as login]))

(ctmx/defcomponent ^:endpoint login [req user password]
  (ctmx/with-req req
    (let [result (and post? (login/login! user password))]
      (if (= :ok result)
        (assoc ctmx.response/hx-refresh :session {:user user})
        [:form.mt-5 {:hx-post "login"}
         [:div.form-group
          [:label "Username"]
          [:input.form-control
           {:type "text" :name "user" :value user :placeholder "Enter Username" :required true}]]
         [:div.form-group
          [:label "Password"]
          [:input.form-control
           {:type "password" :name "password" :placeholder "Enter Password" :required true}]]
         [:button.btn.btn-primary.mr-2 "Submit"]
         (when (= :invalid-password result)
           [:div.badge.badge-danger "Incorrect Password"])
         (when (= :user-exists result)
           [:div.badge.badge-danger "User Exists"])]))))
