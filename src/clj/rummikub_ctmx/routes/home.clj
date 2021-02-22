(ns rummikub-ctmx.routes.home
  (:require
    [ctmx.core :as ctmx]
    ctmx.response
    [rummikub-ctmx.render :as render]
    [rummikub-ctmx.service.sse :as sse]
    [rummikub-ctmx.service.state :as state]
    [rummikub-ctmx.util :as util]
    [rummikub-ctmx.views.login :as login]
    [rummikub-ctmx.views.room :as room]))

(defn logged-in? [req]
  (-> req :session :user boolean))

(defn quit [req]
  (let [{:keys [user]} (:session req)]
    (state/quit! user)
    (sse/refresh-all user) ;;because we wish to refresh with a session reset
    (assoc ctmx.response/hx-refresh :session {})))

(ctmx/defcomponent ^:endpoint root [req user]
  (ctmx/with-req req
    (case request-method
      :delete
      (quit req)
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
