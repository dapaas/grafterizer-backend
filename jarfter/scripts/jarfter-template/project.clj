(defproject jarfter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;;[org.marianoguerra/clj-rhino "0.2.2"]
                 [grafter "0.5.0"]
                 [grafter/vocabularies "0.1.0"]]

  :main jarfter.core
  :aot [jarfter.core])
