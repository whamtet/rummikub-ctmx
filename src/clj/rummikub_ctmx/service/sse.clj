(ns rummikub-ctmx.service.sse
  (:require
    [hiccup.core :as hiccup]
    [org.httpkit.server :as httpkit]))

(defonce connections (atom {}))

(defn add-connection [user connection]
  (swap! connections assoc user connection))
(defn remove-connection [user]
  (swap! connections dissoc user))

(defn- msg-str [event]
  (format "event: %s\ndata: \n\n" event))
(defn- script-str [script]
  (format "event: script\ndata: %s\n\n" (hiccup/html [:script script])))

(defn send! [event recipients]
  (doseq [recipient recipients
          :let [connection (@connections recipient)]
          :when connection]
    (httpkit/send! connection event false)))

(defn send-all! [event exceptions]
  (let [exceptions (set exceptions)]
    (doseq [[user connection] @connections
            :when (-> user exceptions not)]
      (httpkit/send! connection event false))))

(defn send-script! [script & recipients]
  (send! (script-str script) recipients))
(defn send-script-all! [script & recipients]
  (send-all! (script-str script) recipients))

(def refresh (partial send-script! "location.reload();"))
(def refresh-all (partial send-script-all! "location.reload();"))
