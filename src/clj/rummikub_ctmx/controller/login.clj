(ns rummikub-ctmx.controller.login
  (:require
    ctmx.response
    [rummikub-ctmx.middleware.store :as store]
    [rummikub-ctmx.service.name :as name]
    [rummikub-ctmx.service.sse :as sse]
    [rummikub-ctmx.service.state :as state]))

(defn login! [user password]
  (if (not= "rummikub" (some-> password .toLowerCase))
    :invalid-password
    (try
      (let [user (or (not-empty user) (name/new-name))]
        (state/pick-up-new! user)
        (sse/refresh-all user)
        user)
      (catch AssertionError e :user-exists))))

(defn quit [req]
  (let [{:keys [user]} (:session req)]
    (state/quit! user)
    (sse/refresh-all user) ;;because we wish to refresh with a session reset
    (assoc ctmx.response/hx-refresh :session {})))

(defn delete-user [user]
  (state/quit! user)
  (store/destroy-user user)
  (sse/refresh-all)
  nil)

(defn reset-game []
  (state/reset-game!)
  (sse/refresh-all)
  nil)
