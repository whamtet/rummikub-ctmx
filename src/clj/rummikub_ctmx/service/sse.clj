(ns rummikub-ctmx.service.sse
  (:require
    [clojure.string :as string]
    [hiccup.core :as hiccup]
    [org.httpkit.server :as httpkit]
    [rummikub-ctmx.util :as util]))

(defonce connections (atom {}))

(defn add-connection [user connection]
  (swap! connections assoc user connection))
(defn remove-connection [user]
  (swap! connections dissoc user))

(defn- event-str [event]
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
(defn send-script-all! [script & exceptions]
  (send-all! (script-str script) exceptions))

(defn send-event! [event & recipients]
  (send! (event-str event) recipients))
(defn send-event-all! [event & exceptions]
  (send-all! (event-str event) exceptions))

(def refresh (partial send-script! "location.reload();"))
(def refresh-all (partial send-script-all! "location.reload();"))
(def play-all (partial send-event-all! "play-area"))
(def pass-all (partial send-script-all! "pass();"))

(defmacro apply-remote [f & args]
  `(->> ~(vec args)
        (map util/write-str)
        (string/join ", ")
        (format "%s(%s)" ~(str f))))
