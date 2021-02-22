(ns rummikub-ctmx.routes.home
  (:require
    [ctmx.core :as ctmx]
    ctmx.response
    [rummikub-ctmx.render :as render]
    [rummikub-ctmx.service.state :as state]
    [rummikub-ctmx.util :as util]
    [rummikub-ctmx.views.login :as login]
    [rummikub-ctmx.views.room :as room]))

(defn logged-in? [req]
  (-> req :session :user boolean))
(defn login [user]
  (assert (not-empty user))
  (state/pick-up-new! user)
  (assoc ctmx.response/hx-refresh :session {:user user}))
(defn quit [req]
  (let [{:keys [session]} req
        logged-out (dissoc session :user)]
    (-> session :user state/quit!)
    (assoc ctmx.response/hx-refresh :session logged-out)))


(ctmx/defcomponent ^:endpoint root [req user]
  (ctmx/with-req req
    (case request-method
      :post
      (login user)
      :delete
      (quit req)
      [:div.container
       (if (logged-in? req)
         (room/room req)
         (login/login req))])))

(defn home-routes []
  (ctmx/make-routes
    "/"
    (fn [req]
      (render/html5-response
        (root req)))))
