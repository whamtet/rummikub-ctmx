(ns rummikub-ctmx.middleware.store
  "A session storage engine that stores session data in memory."
  (:require
    [ring.middleware.session.store :refer [SessionStore]]
    [rummikub-ctmx.util :as util])
  (:import [java.util UUID]))

(deftype MemoryStore [session-map]
  SessionStore
  (read-session [_ key]
    (@session-map key))
  (write-session [_ key data]
    (let [key (or key (str (UUID/randomUUID)))]
      (swap! session-map assoc key data)
      key))
  (delete-session [_ key]
    (swap! session-map dissoc key)
    nil))

(ns-unmap *ns* '->MemoryStore)

(def store-atom (atom {}))
(def store (MemoryStore. store-atom))

(defn destroy-user [user]
  (swap! store-atom
         (fn [m]
           (util/filter-vals
             #(-> % :user (not= user))
             m))))
