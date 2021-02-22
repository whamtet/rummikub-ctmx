(ns rummikub-ctmx.routes.api
  (:require
    [rummikub-ctmx.service.sse :as sse]
    [org.httpkit.server :as httpkit]))

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
      (httpkit/on-close channel (fn [_] (sse/remove-connection user channel))))))

(defn api-routes []
  ["/api"
   ["/sse" sse]])
