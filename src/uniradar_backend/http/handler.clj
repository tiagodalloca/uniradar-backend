(ns uniradar-backend.http.handler
  (:require [malli.util :as mu]
            [muuntaja.core :as m]
            reitit.coercion.malli
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            reitit.ring.malli
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]))

(defn root-handler
  [request deps]
  {:body "Hello World!"})

(defn inject-handler-deps
  [handler deps]
  (fn [request]
    (handler request deps)))

(defn get-routes
  [deps]
  ["/"
   {:get
    {:responses {200 {:body string?}}
     :handler (inject-handler-deps root-handler deps)}}])

(def options
  {:data
   {:coercion
    (reitit.coercion.malli/create
     {:error-keys
      #{:type :coercion :in :schema :value :errors :humanized :transformed}
      :compile mu/open-schema
      :strip-extra-keys false
      :default-values true
      :options nil})

    :muuntaja m/instance
    :middleware [parameters/parameters-middleware
                 muuntaja/format-negotiate-middleware
                 muuntaja/format-response-middleware
                 (exception/create-exception-middleware
                  (merge
                   exception/default-handlers
                   {clojure.lang.ExceptionInfo
                    (fn [ex _]
                      {:status 500
                       :body (ex-data ex)})
                    ::exception/wrap (fn [handler e request]
                                       (.printStackTrace e)
                                       (handler e request))}))
                 muuntaja/format-request-middleware
                 coercion/coerce-request-middleware
                 multipart/multipart-middleware]}})

(defn get-handler
  [deps]
  (let [routes (get-routes deps)]
    (ring/ring-handler
     (ring/router routes options)
     (constantly {:status 404, :body "Not found."}))))

