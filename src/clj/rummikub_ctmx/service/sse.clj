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

(defn- script-tag [script]
  [:script#script script])

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
  (send-retry (render/html event) (set/intersection (set recipients) (state/users))))
(defn send-all! [event exceptions]
  (send-retry (render/html event) (set/difference (state/users) (set exceptions))))

(defn send-script! [script & recipients]
  (send! (script-tag script) recipients))
(defn send-script-all! [script & exceptions]
  (send-all! (script-tag script) exceptions))

(def refresh-all (partial send-script-all! "location.reload();"))
(defn pass-all [id {:keys [current players]}]
  (doseq [player players]
    (send! (tile/turn-panel id player current) [player]))
  (as-> (rand-int 100) i
        (- i 84)
        (max i 0)
        (format "pass(%s)" i)
        (send-script-all! i)))

(defn update-tile [tile position & exceptions]
  (send-all!
    (tile/tile [tile position])
    exceptions))

(defn rummikub! [user]
  (send-script-all! (format "rummikub('%s');" user)))
