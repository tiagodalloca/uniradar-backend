(ns uniradar-backend.domain.schemas.vehicle
  (:require [malli.core :as m]))

(def vehicle-type
  [:enum :car :truck :bus :motorcycle])

(def vehicle
  [:map
   [:make string?]
   [:model string?]
   [:fabrication-year pos?]
   [:vehicle-type ::vehicle-type]])

