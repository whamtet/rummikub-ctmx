(ns rummikub-ctmx.routes.api
  (:require
    ctmx.response
    [org.httpkit.server :as httpkit]
    [rummikub-ctmx.service.sse :as sse]))

(defn sse [req]
  (let [user (-> req :params :user)]
    (httpkit/with-channel req channel
      (httpkit/send!
        channel
        {:status 200
         :headers {"Content-Type" "text/event-stream"
                   "Cache-Control" "no-cache"}}
        false)
      (sse/add-connection user channel)
      (httpkit/on-close channel (fn [_] (sse/remove-connection user))))))

(defn api-routes []
  ["/api"
   ["/sse" sse]])
