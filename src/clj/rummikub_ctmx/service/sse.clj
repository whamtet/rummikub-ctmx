(ns rummikub-ctmx.service.sse
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [hiccup.core :as hiccup]
    [org.httpkit.server :as httpkit]
    [rummikub-ctmx.service.state :as state]
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

(defn- send-retry
  ([e recipients]
   (when (not-empty recipients)
     (future (send-retry e recipients 19))))
  ([e recipients retries]
   (let [available @connections
         leftovers (set/difference recipients (set (keys available)))]
     (doseq [[user connection] available :when (recipients user)]
       (httpkit/send! connection e false))
     (when (and (pos? retries) (not-empty leftovers))
       (Thread/sleep 500)
       (recur e leftovers (dec retries))))))

(defn send! [event recipients]
  (send-retry event (set/union (set recipients) (state/users))))
(defn send-all! [event exceptions]
  (send-retry event (set/difference (state/users) (set exceptions))))

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

(defn rummikub! [user]
  (send-script-all! (format "alert('%s says Rummikub!');" user)))
