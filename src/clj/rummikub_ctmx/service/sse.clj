(ns rummikub-ctmx.service.sse
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [ctmx.render :as render]
    [org.httpkit.server :as httpkit]
    [rummikub-ctmx.service.state :as state]
    [rummikub-ctmx.util :as util]
    [rummikub-ctmx.views.tile :as tile]))

(defonce connections (atom {}))

(defn add-connection [user connection]
  (swap! connections assoc user connection))
(defn remove-connection [user]
  (swap! connections dissoc user))

(defn- script-str [script]
  (render/html [:script#script script]))

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

(def refresh-all (partial send-script-all! "location.reload();"))
(def pass-all (partial send-script-all! "pass();"))

(defn update-tile [tile position & exceptions]
  (send-all!
    (render/html (tile/tile [tile position]))
    exceptions))

(defn rummikub! [user]
  (send-script-all! (format "rummikub('%s');" user)))
