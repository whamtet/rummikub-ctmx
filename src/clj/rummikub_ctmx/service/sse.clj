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

(defn send-all! [event & exceptions]
  (let [exceptions (set exceptions)]
    (doseq [[user connection] @connections
            :when (-> user exceptions not)]
      (httpkit/send! connection (msg-str event) false))))

(def refresh (partial send! "refresh"))

(def refresh-all (partial send-all! "refresh"))
