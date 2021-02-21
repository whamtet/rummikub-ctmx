(ns rummikub-ctmx.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [rummikub-ctmx.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[rummikub-ctmx started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[rummikub-ctmx has shut down successfully]=-"))
   :middleware wrap-dev})
