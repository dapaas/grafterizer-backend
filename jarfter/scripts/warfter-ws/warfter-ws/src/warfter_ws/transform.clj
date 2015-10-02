(ns warfter-ws.transform)

;;; You can specify transformation functions in this namespace for use
;;; within the pipeline.

(defn ->integer
  "An example transformation function that converts a string to an integer"
  [s]
  (Integer/parseInt s))
