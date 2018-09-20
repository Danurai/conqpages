(ns dansite.tools
  (:require [clojure.string :as str]))

; TODO
; Numeric search = < > INT
; cost attack hp shields command_icons
; OR |

(def find-regex #".*?(?=\s[a-z]:)|.+")
(def field-regex #"([a-z]):(.+)")

(def filter-synonyms 
  {:type_code {"w" "warlord_unit" "a" "army_unit" "t" "attachment" "e" "event" "s" "support" "y" "synapse_unit"}
   :faction_code {"s" "space_marines" "a" "astra_militarum" "o" "orks" "c" "chaos" "e" "eldar" "d" "dark_eldar" "t" "tau" "n" "neutral" "y" "tyranids" "r" "necrons"}})

(defn fmap [qry]
"returns a collection of maps including {:id 'field name' :val 'match')" 
  (map #(let [field-flt  (->> % (re-seq field-regex) first)
             field-name (case (get field-flt 1)
                            "a" :attack
                            "c" :command_icons
                            "e" :pack_code
                            "f" :faction_code
                            "h" :hp
                            "r" :cost ; resources
                            "s" :shields
                            "t" :type_code
                            "x" :text
                            "y" :cycle_id
                            :name)
             field-val  (get field-flt 2)]
    {:id field-name
     :val (or (case field-name (:attack :command_icons :hp :cost :shields :position) (read-string field-val) field-val)
              %)})
    (->> qry (re-seq find-regex) (remove clojure.string/blank?))))
    
(defn cardfilter [cards q]
(let [op =]
  (sort-by :code
    (reduce
      (fn [data {:keys [id val]}]        
        (case id
          (:name :text) (filter #(some? (re-find (re-pattern (str "(?i)" val)) (id %))) data)
          (:pack_code :type_code :faction_code) (filter (fn [x] 
                                                    (some 
                                                      #(= (id x) (get-in filter-synonyms [id %] %)) 
                                                      (str/split val #"\|"))) 
                                                    data)
          (filter #(op (id %) val) data)))
      (:data cards)
      (fmap q)))))