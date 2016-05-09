(defproject jarfter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :repositories {"local" ~(str (.toURI (java.io.File. "maven_repository")))}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [grafter "0.5.0"]
                 [grafter/vocabularies "0.1.0"]
                 [ww-geo-coords "1.0"]
                 [clj-time "0.11.0"]
                 [grafterizer/tabular_functions "0.1.2"]
                 ]

  :main jarfter.core
  :aot [jarfter.core])
