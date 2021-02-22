(ns rummikub-ctmx.views.login
  (:require
    [ctmx.core :as ctmx]
    [rummikub-ctmx.service.state :as state]))

(defn login! [user password]
  (when (some-> password .toLowerCase (= "rummikub"))
    (assert (not-empty user))
    (state/pick-up-new! user)
    (assoc ctmx.response/hx-refresh :session {:user user})))

(ctmx/defcomponent ^:endpoint login [req user password]
  (ctmx/with-req req
    (or (login! user password)
        [:form.mt-5 {:hx-post "login"}
         [:div.form-group
          [:label "Username"]
          [:input.form-control
           {:type "text" :name "user" :placeholder "Enter Username" :required true}]]
         [:div.form-group
          [:label "Password"]
          [:input.form-control
           {:type "password" :name "password" :placeholder "Enter Password" :required true}]]
         [:button.btn.btn-primary "Submit"]
         (when post?
           [:div.badge.badge-danger "Incorrect Password"])])))
