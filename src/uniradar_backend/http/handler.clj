(ns uniradar-backend.http.handler
  (:require [uniradar-backend.http.tables-handler :refer [make-table-handlers]]
            [malli.util :as mu]
            [reitit.dev.pretty :as pretty]
            [muuntaja.core :as muuntaja-core]
            reitit.coercion.malli
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            reitit.ring.malli
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn root-handler
  [request deps]
  {:body "Hello World!"})

(defn inject-handler-deps
  [handler deps]
  (fn [request]
    (handler request deps)))

(defn get-routes
  [deps]
  [["/"
    {:get
     {:responses {200 {:body string?}}
      :handler (inject-handler-deps root-handler deps)}}]
   (into ["/tables"]
         (map (fn [table] (make-table-handlers table deps))
              ["vehicle" "user"]))])

(def options
  {;;:reitit.middleware/transform dev/print-request-diffs ;; pretty diffs
   ;;:validate spec/validate ;; enable spec validation for route data
   ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
   :exception pretty/exception
   :data {:coercion (reitit.coercion.malli/create
                     {;; set of keys to include in error messages
                      :error-keys #{#_:type :coercion :in :schema :value :errors :humanized #_:transformed}
                      ;; schema identity function (default: close all map schemas)
                      :compile mu/closed-schema
                      ;; strip-extra-keys (effects only predefined transformers)
                      :strip-extra-keys true
                      ;; add/set default values
                      :default-values true
                      ;; malli options
                      :options nil})
          :muuntaja muuntaja-core/instance
          :middleware [
                       ;; swagger feature
                       ;; swagger/swagger-feature
                       ;; query-params & form-params
                       parameters/parameters-middleware
                       ;; content-negotiation
                       muuntaja/format-negotiate-middleware
                       ;; encoding response body
                       muuntaja/format-response-middleware
                       ;; exception handling
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
                       ;; decoding request body
                       muuntaja/format-request-middleware
                       ;; coercing response bodys
                       coercion/coerce-response-middleware
                       ;; coercing request parameters
                       coercion/coerce-request-middleware
                       ;; multipart
                       multipart/multipart-middleware
                       ;; cors
                       [wrap-cors
                        :access-control-allow-origin [#".*"]
                        :access-control-allow-methods [:get :post :patch :delete]]]}})

(defn get-handler
  [deps]
  (let [routes (get-routes deps)]
    (ring/ring-handler
     (ring/router routes options)
     (constantly {:status 404, :body "Not found."}))))

