(ns rummikub-ctmx.render
  (:require
    [hiccup.core :as hiccup]
    [hiccup.page :refer [html5]]))

(defn html-response [body]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body body})

(defn html5-response
  ([body] (html5-response nil body))
  ([js body]
   (html-response
     (html5
       [:head
        [:meta {:charset "utf-8"}]
        [:meta {:name "viewport"
                :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
        [:link {:rel "stylesheet"
                :href "/css/screen.css"}]
        [:link {:rel "stylesheet"
                :href "https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
                :integrity "sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2"
                :crossorigin "anonymous"}]]
       [:body body]
       [:script {:src "/js/htmx.min.js"}]
       [:script {:type "module" :src "/js/common.js"}]
       (when js [:script {:src (str "/js" js)}])))))
