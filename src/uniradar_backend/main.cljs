(ns uniradar-backend.main
  (:require ["express" :as express]))

(def port (or (-> js/process .-env .-PORT) 3000))

(defonce app*
  (atom nil))

(defonce server*
  (atom nil))

(defn init
  [& args]
  (reset! app* (express))
  (doto ^js @app*
    (.get "/" (fn [req res]
                (.send res "Hello World!")))
    (-> (.listen port (fn []
                        (println "Started listening on port " port)))
        (some->> (reset! server*)))))

(comment
  (init)
  @server*)
