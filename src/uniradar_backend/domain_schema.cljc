(ns uniradar-backend.domain-schema
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:require [malli.core :as m]))

(def vehicle-type
  [:enum :car :truck :bus :motorcycle])

(def vehicle
  {:schema [:map
            [:vehicle-id string?]
            [:make string?]
            [:model string?]
            [:fabrication-year pos?]
            [:vehicle-type ::vehicle-type]
            [:assigned-user-id {:optional true} string?]]
   :required #{::vehicle-type}})


(def email string?)

(def cpf string?)

(def address string?)

(def phone-number string?)

(def user
  {:schema [:map
            [:id string?]
            [:name string?]
            [:email ::email]
            [:cpf ::cpf]
            [:address ::address]
            [:phone-number ::phone-number]]
   :required #{::email ::cpf ::address ::phone-number}})

(def vechicle-loc
  {:schema [:map
            [:lat number?]
            [:lon number?]
            [:sattelites-used number?]
            [:precision number?]
            [:vehicle-id ::vehicle-id]]
   :required #{::vechicle-id}})

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(def registry
  (reduce
   (fn [registry var]
     (let [schema (or (:schema @var) @var)
           required (:required @var)
           options (if required
                     {:registry (select-keys registry required)}
                     nil)]
       (assoc registry
              (->> var meta :name (keyword (namespace ::x)))
              (if (some? options)
                [:schema options schema]
                schema))))
   {}
   [#'vehicle-type #'vehicle #'email #'cpf #'address #'phone-number #'user #'vechicle-loc]))

