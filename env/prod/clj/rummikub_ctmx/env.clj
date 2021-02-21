(ns rummikub-ctmx.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[rummikub-ctmx started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[rummikub-ctmx has shut down successfully]=-"))
   :middleware identity})
