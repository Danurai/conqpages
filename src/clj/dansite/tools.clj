(ns dansite.tools)

(defn- thisfilter [data field match]
  (filter #(some? (re-find (re-pattern (str "(?i)" match)) (field %))) data))
  
(def filterdef
  [{:id :pack_code :qryid "e" :numeric false}
   {:id :text :qryid "x" :numeric false}
   {:id :type_code :qryid "t" :numeric false}])

(def filter-synonyms 
  {"w" "warlord_unit" "a" "army_unit" "t" "attachment" "e" "event" "s" "support" "y" "synapse_unit"})
   
(defn filter-map 
"returns a collection of maps including {:id 'field name' :val 'match')" 
  [query]
  (remove #(nil? (:val %))
    (map 
      (fn [x]
        (assoc x :val (second (re-find  ; FIRST INSTANCE
                                (re-pattern (str "(?i)" (str (:qryid x) ":") "(\\w+)"))
                                query))))
      filterdef)))

(defn attrfilter [cards q]
  (sort-by :code
    (reduce 
      (fn [data {:keys [id val]}]
        (filter #(= (id %) val) data))
      (:data cards)
      (filter-map q))))
  
(defn cardfilter [cards q]
  (sort-by :code 
    (filter
      #(some? (re-find (re-pattern (str "(?i)" q)) (:name %)))
      (:data cards))))
      
; (->> cards :data (filter #(some? (re-find (re-pattern (str "(?i)" q)) (:name %))))))