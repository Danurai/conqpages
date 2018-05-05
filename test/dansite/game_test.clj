(ns dansite.game-test
  (:require [dansite.misc :as misc :refer :all]
            [dansite.database :as db :refer [users get-user-decks]]
            [dansite.pages :as pages]
            [clojure.data.json :as json]
            [expectations :refer :all]))
            
(expect (json/write-str [{:code "010001" :qty 1}
                         {:code "010008" :qty 4}
                         {:code "010009" :qty 1}
                         {:code "010010" :qty 2}
                         {:code "010011" :qty 1}])  
  (sig-squad "010001"))
  
(expect (json/write-str {:010001 1 :010008 4 :010009 1 :010010 2 :010011 1})
  (json/write-str (signature-squad-decklist "010001")))
  
(expect "{\"010001\":1,\"010008\":4,\"010009\":1,\"010010\":2,\"010011\":1}"
  (json/write-str (signature-squad-decklist "010001")))
  
(expect ["root" "dan"]
  (keys (db/users)))
 

(expect 100 ;100000
  (->> db/unique-deckid
      repeatedly
      (take 100)
      count))
        
(expect "space_marines"
  (misc/faction-code "010001"))
  
(expect #(> 10 %)
  (-> (db/get-user-decks "1002") count))
  
(expect #(> 4 %)
  (->> (db/get-user-decks "1002")
        first
        :data
        json/read-str
        (map #(pages/get-card %))
        first
        :qty))
  
;(expect []
;  (pages/deck-card {:uid "21CFFD", :name "xx", :author 1002, :data "{\"180043\":1,\"180044\":4,\"180045\":1,\"180046\":2,\"180047\":1}", :tags "astra_militarum", :notes "", :created "2018-05-04T12:42:24.749", :updated "2018-05-04T12:42:24.749"}
;  ))