(ns rummikub-ctmx.views.login
  (:require
    [ctmx.core :as ctmx]))

(ctmx/defcomponent login [req]
  [:form.mt-5 {:id id :hx-post "root"}
   [:div.form-group
    [:label "Username"]
    [:input.form-control
     {:type "text" :name "user" :placeholder "Enter Username" :required true}]]
   [:button.btn.btn-primary "Submit"]])
