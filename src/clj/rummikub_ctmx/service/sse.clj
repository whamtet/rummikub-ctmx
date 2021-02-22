(ns rummikub-ctmx.service.sse
  (:require
    [org.httpkit.server :as httpkit]))

(defonce connections (atom {}))

(defn add-connection [user connection]
  (swap! connections assoc user connection))
(defn remove-connection [user]
  (swap! connections dissoc user))

(defn- msg-str [event]
  (format "event: %s\ndata: \n\n" event))

(defn send! [event & recipients]
  (doseq [recipient recipients
          :let [connection (@connections recipient)]
          :when connection]
    (httpkit/send! connection (msg-str event) false)))

(defn logout [user]
  (send! "logout" user))
(defn refresh [user]
  (send! "refresh" user))
