(ns rummikub-ctmx.controller.login
  (:require
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
