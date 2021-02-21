(ns rummikub-ctmx.views.login)

(defn login [req]
  [:form.mt-5 {:hx-post "root"}
   [:div.form-group
    [:label "Username"]
    [:input.form-control
     {:type "text" :name "user" :placeholder "Enter Username" :required true}]]
   [:button.btn.btn-primary "Submit"]])
