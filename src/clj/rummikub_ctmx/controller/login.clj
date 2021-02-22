(ns rummikub-ctmx.controller.login
  (:require
    ctmx.response
    [rummikub-ctmx.service.sse :as sse]
    [rummikub-ctmx.service.state :as state]))

(defn login! [user password]
  (if (not= "rummikub" (some-> password .toLowerCase))
    :invalid-password
    (try
      (state/pick-up-new! user)
      (sse/refresh "Matt")
      :ok
      (catch AssertionError e :user-exists))))

(defn quit [req]
  (let [{:keys [user]} (:session req)]
    (state/quit! user)
    (sse/refresh-all user) ;;because we wish to refresh with a session reset
    (assoc ctmx.response/hx-refresh :session {})))
