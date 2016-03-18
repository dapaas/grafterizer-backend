(defproject warfter-ws "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :repositories {"local" ~(str (.toURI (java.io.File. "maven_repository")))}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [ring/ring-core "1.3.2"]
                 [grafter "0.5.0"]
		 [ww-geo-coords "1.0"]
		 [clj-time "0.11.0"]
                 [grafter/vocabularies "0.1.2"]]
                 ;;[org.slf4j/slf4j-jdk14 "1.7.5"]]
  :plugins [[lein-ring "0.9.6"]]
  :ring {:handler warfter-ws.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
