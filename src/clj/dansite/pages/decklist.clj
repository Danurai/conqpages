(in-ns 'dansite.pages)
            
(defn- deck-card [deck]
  (let [cards-in-deck (map #(-> (key %) get-card-from-code (assoc :qty (val %))) (-> deck :data json/read-str))
        warlord     (->> cards-in-deck (filter #(= (:type_code %) "warlord_unit")) first)]
    [:div.card 
      [:div.px-3.py-1 {:data-toggle "collapse" :data-target (str "#" (:uid deck))}
        [:div.row
          [:div.col-sm-9
            [:div.h5.mt-2 (:name deck)]
            [:div.text-muted (:name warlord)]
            [:div
              (map (fn [x] [:a.badge.badge-secondary.text-light.mr-1 x]) (re-seq #"\w+" (:tags deck)))]]
          [:div.col-sm-3.d-none.d-sm-block
            [:div.warlord-thumb.ml-auto.border.border-secondary.rounded {:style (str "background-image: url(" (:img warlord) ");")}]]]]
      [:div.collapse.px-3.py-1 {:data-parent "#accordian" :id (:uid deck)}
        ; Decklist
        [:div.row
          [:div.col-sm-12
            [:div.text-muted (str "Cards " (->> cards-in-deck (filter #(not= "warlord_unit" (:type_code %))) (map :qty) (reduce +)) "/50")]]]
        [:div.row.mb-2
          [:div.col-sm-6 
            (deck-card-list-by-type "army_unit" cards-in-deck)]
          [:div.col-sm-6
            (deck-card-list-by-type "attachment" cards-in-deck)
            (deck-card-list-by-type "event" cards-in-deck)
            (deck-card-list-by-type "support" cards-in-deck)
            ]]
        [:div.row.mb-2
          [:div.small.col-sm-12.text-muted (str "Created on " (-> deck :created c/from-long))]
          [:div.small.col-sm-12.text-muted (str "Updated on " (-> deck :updated c/from-long))]]
        [:div.row.mb-2
          [:div.col-sm-12
            [:a.btn.btn-sm.btn-primary.mr-1 {:href (str "/decks/edit/" (:uid deck))} [:i.fas.fa-edit.mr-1] "Edit"]
            [:a.btn.btn-sm.btn-success.mr-1 {:href (str "/decks/view/" (:uid deck))} [:i.far.fa-eye.mr-1] "View"]
            [:button.btn.btn-sm.btn-danger.btn-delete.mr-1 {:data-deckuid (:uid deck) :data-deckname (:name deck)} [:i.fas.fa-times.mr-1] "Delete"]
            [:button.btn.btn-sm.btn-outline-secondary.btn-export {:type "button" :data-toggle "modal" :data-target "#exportdeck" :data-deckid (:uid deck)} "Export"]]]
        ]]))

(defn decklist [req]
  (let [user-decks (db/get-user-decks (-> req misc/get-authentications :uid))]
    (h/html5
      misc/pretty-head
      (h/include-js "/js/whk_deck_list.js")
      (h/include-js "/js/whk_qtip.js")
      [:body
        (misc/navbar req)
        [:div.container.my-2
          (misc/show-alert)
          [:div.row
            [:div.col-md-8
              [:div.mb-2
                [:span.h3.mr-2 "Your Army Roster"]
                [:span (str "(" (count user-decks) ")")]]
              [:div#roster.accordian
                (map #(deck-card %) user-decks)]]
            [:div.col-md-4
              [:a.btn.btn-primary.m-2 {:href "/decks/new"} "New Deck"]
              [:button.btn.btn-warning.m-2 {:data-toggle "modal" :data-target "#loaddeck"} "Import Deck"]]]]
        [:div#deletedeck.modal {:role "dialog"}
          [:div.modal-dialog {:role "document"}
            [:div.modal-content 
              [:div.modal-header  "Delete Deck"
                [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
                  [:span {:aria-hidden "true"} "&times;"]]]
              [:div#deletealert.modal-body]
              [:div.modal-footer
                [:button.btn.btn-secondary {:type "button" :data-dismiss "modal"} "Close"]
                [:form {:action "/decks/delete" :method "post"}
                  [:input#deletedeckuid {:name "deletedeckuid" :hidden true}]
                  [:button.btn.btn-danger {:type "submit" } "Delete"]]]]]]
        [:div#loaddeck.modal {:role "dialog"}
          [:div.modal-dialog.modal-lg {:role "document"}
            [:div.modal-content
              [:div.modal-header "Load Deck"
                [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
                  [:span {:aria-hidden "true"} "x"]]]
              [:div.modal-body
                [:div.container-fluid
                  [:div.row
                    [:div.col-sm-6 
                      [:div "Paste Decklist below"]
                      [:textarea#importdecklist.form-control {:rows "10"}]]
                    [:div.col-sm-6
                      [:div#parseddecklist.border.border-secondary {:style "overflow-y:scroll;height:280px;white-space:pre-wrap;"}]]]]]
              [:div.modal-footer
                [:form {:action "/decks/new/" :method "post"}
                  [:input#deckjson {:hidden true :name "deck"}]
                  [:button.btn.btn-primary {:type "submit"} "Load Deck"]]
                [:button.btn.btn-secondary {:type "button" :data-dismiss "modal"} "Close"]]]]]
        [:div#exportdeck.modal {:role "dialog"}
          [:div.modal-dialog {:role "document"}
            [:div.modal-content
              [:div.modal-header "Export Deck"
                [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
                  [:span {:aria-hidden "true"} "x"]]]
              [:div.modal-body 
                [:textarea.form-control {:rows "10"}]]
              [:div.modal-footer
                [:button.btn.btn-secondary {:type "button" :data-dismiss "modal"} "Close"]]]]]])))
