(ns warfter-ws.handler
  (:require [compojure.handler :as handler]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults ]]
            [ring.middleware.params :refer :all]
            [ring.util.io :as ring-io]
            [clojure.string :as str]
            [clojure.core :as core]
            [clojure.java.io :as io]
            [warfter-ws.core :refer [warfterTransformer streamWarfterTransformer]]))
;; replace is already defined in the handler namespace, so we have to refer to clojure.string/replace with its full name

(defn chewThatRequest [input]
  (str "REFLECTOR :D \n" (str/replace (str/capitalize (str/trim (ring.util.request/body-string input))) "e" "E") "\n"))

(defn grafterMagic [input]
  (str "Here comes transformed data of:\n" (ring.util.request/body-string input) "\n")
  (-> "Did it work?\n"))

(defn hcGrafterMagic []
  (warfterTransformer "/home/ubuntu/data/hcData.csv" "/home/ubuntu/data/output.ttl")
  (-> "Did the hardcoded example work?\n"))

(defn semiLauncher [inputFileName outputFileName] 
  (warfterTransformer inputFileName outputFileName)
  (str " - Found input " inputFileName "\n - Found output " outputFileName "\n")
)

(defn shcGrafterMagic [request]
  (def args (str/split (ring.util.request/body-string request) #" " ))
  (def numArgs (core/count args))
  (def out1 (str "Found " numArgs " arguments\n"))
  (if-not (== numArgs 2) 
    (str out1 "Please provide two strings as input!\n")
    (semiLauncher (core/get args 0) (core/get args 1))
  )
  ;;(def inputLocation (core/get args 0))
  ;;(def outputLocation (core/get args 1))
  ;;(-> (str out1 " - Found input " inputLocation "\n - Found output " outputLocation "\n"))
)

(defn execTransform [request]
  ;;(ring.util.request/body-string request)
  ;;(slurp (:body request))
  (def timestamp (quot (System/currentTimeMillis) 1000))
  (def inputName (str "/home/ubuntu/data/input_" timestamp ".csv"))
  (def outputName (str "/home/ubuntu/data/output_" timestamp ".rdf"))
  ;;(slurp "/home/ubuntu/data/hcData.csv")
  (io/copy (ring.util.request/body-string request) (io/file inputName))
  (semiLauncher inputName outputName)
  (slurp outputName)
)

(defn streamTransform [request]
  (def timestamp (quot (System/currentTimeMillis) 1000))
  (def outputName (str "/tmp/output_" timestamp ".rdf"))
  ;;(ring.util.request/body-string request)
  (streamWarfterTransformer (ring-io/string-input-stream (ring.util.request/body-string request)) 
                            outputName)
  (slurp outputName)
)

(defn formTransform [params]
  (def timestamp (quot (System/currentTimeMillis) 1000))
  ;;(def requestMap (ring-io/string-input-stream (ring.util.request/body-string request)))
  (def params (wrap-params params))
  (str (-> params :form-params (get "csv")) "\nAnd now to the output:\n" (-> params :form-params (get "output")))
)

(defroutes app-routes
  ;;(GET "/" [] "Hello there, mr World\n")
  ;;(GET "/dummy" [] "This is the dummy page :o\n")
  ;;(POST "/reflector" request (chewThatRequest request))
  ;;(POST "/transform" request (grafterMagic request))
  ;;(POST "/execTransform" request (execTransform request))
  ;;(GET "/hardcodedTransform" [] (hcGrafterMagic))
  ;;(POST "/semiHCTransform" request (shcGrafterMagic request))
  ;;(POST "/formTransform" {params :params} (formTransform params)) 

  ;; /streamTransform is the only end-point that makes sense in production
  (POST "/streamTransform" request (streamTransform request))
  (route/not-found "404 Not Found - The transformation is in another URL (try /streamTransform)!\n"))


(def app
  (-> (handler/api app-routes)))

;;(def app
;;  (wrap-defaults app-routes site-defaults))

;;(defn chewThatRequest [input]
;;  (str "REFLECTOR :D \n" (capitalize (trim (ring.util.request/body-string request))) "\n"))
