(ns warfter-ws.prefix
  (:require [grafter.rdf :refer [prefixer]]))

;; Defines what will be useful for our next data transformations

(def base-domain (prefixer "http://my-domain.com"))

(def base-graph (prefixer (base-domain "/graph/")))

(def base-id (prefixer (base-domain "/id/")))

(def base-vocab (prefixer (base-domain "/def/")))

(def base-data (prefixer (base-domain "/data/")))
