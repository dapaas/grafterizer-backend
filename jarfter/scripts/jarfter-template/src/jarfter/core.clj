(ns jarfter.core
  (:require [grafter.tabular :refer [read-dataset]])
  (:gen-class))

(defn find-transformation [trans]
  @(get (ns-map 'grafterizer.transformation) 'my-transformation))


(defn -main [& [path output]]
  (require 'grafterizer.transformation)
  (when-not (and path output)
    (println "Usage: lein run <input-file.csv> <output-file.(nt|rdf|n3|ttl)>")
    (System/exit 0))

  (let [transformation (find-transformation 'my-transformation)]

    (transformation  (read-dataset path) output)
  )

)
