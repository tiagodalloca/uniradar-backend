(ns user
  (:require uniradar-backend.system.config
            [integrant.core :as ig]
            [integrant.repl :refer [clear go halt init prep reset reset-all]]))

(def config
  {:http/server {:opts {:port 8989}
                 :handler (ig/ref :http/handler)}
   :http/handler {:db/instance (ig/ref :db/instance)}
   :db/instance {}})

(integrant.repl/set-prep! (constantly config))

(comment (prep)
         (init)
         (reset)
         (halt)
         @(:db/instance integrant.repl.state/system))
