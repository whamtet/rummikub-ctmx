(ns rummikub-ctmx.routes.home
  (:require
    [ctmx.core :as ctmx]
    [rummikub-ctmx.controller.login :as controller.login]
    [rummikub-ctmx.render :as render]
    [rummikub-ctmx.util :as util]
    [rummikub-ctmx.views.login :as login]
    [rummikub-ctmx.views.room :as room]))

(defn logged-in? [req]
  (-> req :session :user boolean))

(ctmx/defcomponent ^:endpoint root [req user]
  (ctmx/with-req req
    (if delete?
      (controller.login/quit req)
      [:div.container.mt-3
       (if (logged-in? req)
         (room/room req)
         (login/login req))])))

(defn home-routes []
  (ctmx/make-routes
    "/"
    (fn [req]
      (render/html5-response
        (root req)))))
