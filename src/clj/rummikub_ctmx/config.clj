(ns rummikub-ctmx.config
  (:require
    [cprop.core :refer [load-config]]
    [cprop.source :as source]
    [mount.core :refer [args defstate]]))

(defstate env
  :start
  (load-config
    :merge
    [(args)
     (source/from-system-props)
     (source/from-env)]))

(defn host []
  (-> (:url env "http://localhost:3000")
      (.split "//")
      second))
