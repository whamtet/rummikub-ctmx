(ns rummikub-ctmx.middleware
  (:require
    [rummikub-ctmx.env :refer [defaults]]
    [clojure.tools.logging :as log]
    [rummikub-ctmx.layout :refer [error-page]]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [rummikub-ctmx.middleware.formats :as formats]
    [muuntaja.middleware :refer [wrap-format wrap-params]]
    [rummikub-ctmx.config :refer [env]]
    [rummikub-ctmx.middleware.store :refer [store]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
    [ring.util.response :as response]))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "Invalid anti-forgery token"})}))


(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn redirect-http [handler]
  (fn [req]
    (if-let [url (and (-> req :headers (get "x-forwarded-proto") (= "http")) (:url env))]
      (response/redirect url)
      (handler req))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store] store)))
      redirect-http
      wrap-internal-error))
