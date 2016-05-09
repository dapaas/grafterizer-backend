(ns grafterizer.transformation
  (:require
    [grafter.rdf :refer [prefixer s]]
    [grafter.tabular :refer :all]
    [grafter.rdf.io :as ses]
    [grafter.rdf.templater :refer [graph]]
    [grafter.tabular.common :refer [read-dataset*]]
    [grafter.vocabularies.rdf :refer :all]
    [grafter.vocabularies.qb :refer :all]
    [grafter.vocabularies.sdmx-measure :refer :all]
    [grafter.vocabularies.sdmx-attribute :refer :all]
    [grafter.vocabularies.skos :refer :all]
    [grafter.vocabularies.foaf :refer :all]
    [grafter.vocabularies.owl :refer :all]
    [grafter.vocabularies.dcterms :refer :all]
    [grafter.rdf.formats :refer :all]
    [clojure.string :refer [capitalize lower-case upper-case trim trim-newline triml trimr]]
    [tabular_functions.datatypes :as datatypes]
    [tabular_functions.pipeline :as new-tabular]
    )
  )

;; Start of generated code ----

(def alo (prefixer "http://alo.no/"))


(defn add-filename-to-column [ds destination-column] (let [fname (:grafter.tabular/data-source (meta ds))] (add-column ds destination-column fname)))
(defn integer-literal [s] (Integer/parseInt s))
(defn join [& strings] (clojure.string/join " " strings))
(defn organize-date "Transform date dd/mm/yyyy ~> yyyy-mm-dd" [date] (when (seq date) (let [[d m y] (clojure.string/split date (read-string "#\"/\""))] (apply str (interpose "-" [y m d])))))
(defn remove-blanks [s] (when (seq s) (clojure.string/replace s " " "")))
(defn remove-columns [ds cols] (columns ds (remove (fn [item] (some (fn [a] (= item a)) cols)) (column-names ds))))
(defn replace-varible-string [cell] (-> cell (clojure.string/replace (read-string "#\".* #\"") "number") (clojure.string/replace (read-string "#\"[0-9]{4} \"") "")))
(def string-literal s)
(defn stringToNumeric [x] (if (= "" x) nil (if (.contains x ".") (Double/parseDouble x) (Integer/parseInt x))))
(defn titleize [st] (when (seq st) (let [a (clojure.string/split st (read-string "#\" \"")) c (map clojure.string/capitalize a)] (->> c (interpose " ") (apply str) trim))))
(def transform-gender {"f" (s "female") "m" (s "male")})
(def make-graph (graph-fn [{:keys [name gender person-uri]}] (graph "http://www.example.no/#/" [person-uri [rdf:a foaf:Person] [foaf:name name] [foaf:gender gender]])))

(defpipe my-pipe "Pipeline to convert tabular persons data into a different tabular format." [data-file] (-> (read-dataset data-file) (make-dataset [:name :gender]) (drop-rows 1) (derive-column :person-uri [:name] alo) (mapc {:gender transform-gender :name string-literal})))

(defgraft my-graft "Pipeline to convert the tabular persons data sheet into graph data." my-pipe make-graph)

;; end of generated code ----


(defn import-data
  [quads-seq destination]
  (add (ses/rdf-serializer destination) quads-seq))



(defn my-transformation [dataset output]

  (import-data 
    (make-graph (my-pipe dataset))
    output)

  )
