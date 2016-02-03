(ns warfter-ws.core
  (:require
   [grafter.rdf :refer [s add prefixer]]
   [grafter.tabular :refer :all]
   [grafter.rdf.io :as ses]
   [grafter.rdf.templater :refer [graph]]
   [grafter.tabular.common :refer [read-dataset*]]
   [grafter.vocabularies.rdf :refer :all]
   [grafter.vocabularies.foaf :refer :all]
   [grafter.rdf.formats :refer :all]
   [clojure.string :refer [trim]]
   )
   (:gen-class)
)
;; Start of generated code ----

(def alo (prefixer "http://warfter.sintef.no/"))


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
(def make-graph (graph-fn [{:keys [name gender person-uri]}] (graph "http://www.warfter.no/#/" [person-uri [rdf:a foaf:Person] [foaf:name name] [foaf:gender gender]])))

(defpipe my-pipe "Pipeline to convert tabular persons data into a different tabular format." [data-file] (-> (read-dataset data-file) (make-dataset [:name :gender]) (drop-rows 1) (derive-column :person-uri [:name] alo) (mapc {:gender transform-gender :name string-literal})))

(defgraft my-graft "Pipeline to convert the tabular persons data sheet into graph data." my-pipe make-graph)

;; end of generated code ----

(defn import-data
  [quads-seq destination ]
  (add (ses/rdf-serializer destination) quads-seq))

(defn warfterTransformer [& [path output]]
  (when-not (and path output)
    (println "Usage: lein run <input-file.csv> <output-file.(nt|rdf|n3|ttl)>")
    (str "Something went wrong... I don't know why!\n"))
  
  (import-data
    (make-graph (my-pipe (read-datasets path :format :csv)))
  output)
)

(defn streamWarfterTransformer [& [instream outfile]]
  (when-not (and instream outfile)
    (println "Usage: (streamGrafterTransformer instream output)")
  )
  (import-data
    (make-graph (my-pipe (read-dataset instream :format :csv)))
  outfile)
  
)

