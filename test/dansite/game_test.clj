(ns dansite.game-test
  (:require [dansite.misc :refer :all]
            [dansite.database :as db :refer [users get-authentications rnd-deckid]]
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
        