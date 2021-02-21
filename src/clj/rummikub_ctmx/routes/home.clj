(ns rummikub-ctmx.routes.home
  (:require
    [ctmx.core :as ctmx]
    ctmx.response
    [rummikub-ctmx.render :as render]
    [rummikub-ctmx.util :as util]
    [rummikub-ctmx.views.login :as login]
    [rummikub-ctmx.views.room :as room]))

(defn logged-in? [req]
  (-> req :session :user boolean))

(ctmx/defcomponent ^:endpoint root [req user]
  (ctmx/with-req req
    (if post?
      (assoc ctmx.response/hx-refresh :session {:user user})
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
