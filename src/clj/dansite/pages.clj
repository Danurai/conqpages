(ns dansite.pages
  (:require
    [hiccup.page :as h]
    [clojure.data.json :as json]
    [cemerick.friend :as friend]
    [clj-time.coerce :as c]
    [dansite.misc :as misc]
    [dansite.database :as db :refer [get-user-decks get-user-deck]]
    [dansite.tools :as tools :refer [cardfilter]]))
    
(defn get-card-from-code [code] (->> misc/cards :data (filter #(= (:code %) code)) first))

(defn- deck-card-list-by-type [type_code cards-in-deck]
  (let [cid-by-type (filter #(= (:type_code %) type_code) cards-in-deck)]
    [:div
      [:div [:b (str (-> cid-by-type first :type) " (" (->> cid-by-type (map :qty) (reduce +)) ")")]]
      (map (fn [r] 
            [:div (str (:qty r) "x ")
              [:a.card-tooltip {:href"#" :data-code (:code r) :class (:faction_code r)} (if (:unique r) [:i.fas.fa-skull.fa-xs.mr-1]) (:name r)]
              (if (= (:signature_loyal r) "Signature") [:i.fa.fa-sm.fa-cog.ml-1.icon-sig])
              (if (= (:signature_loyal r) "Loyal") [:i.fa.fa.sm.fa-cog.ml-1.icon-loyal])
            ]) cid-by-type)]))

(load "pages/admin")
(load "pages/search")
(load "pages/cardpage")
(load "pages/decklist")
;(load "pages/deckbuilder")


(defn- icon-svg [faction_code]
  [:svg.icon-faction.align-bottom {:class faction_code :xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 200 200"}
    [:path {:d ((keyword faction_code) misc/svg)}]])
    
    
(defn- get-deck-data
  ; id is numeric 5 digits - new deck
  ; id is alphanumeric 6 digits - existing deck
  ; TODO req contains deckdata - load deck POSTed
  [req]
  (if (some? (re-matches #"/decks/new/[0-9]{6}" (-> req :uri)))
      {:data (-> req :params :id misc/signature-squad-decklist json/write-str)
       :tags (-> req :params :id misc/faction-code)}
      (let [deck (get-user-deck (-> req :params :id))]
        (if (some? deck)
            deck
            (if (some? (-> req :params :deck))
              (let [deck (json/read-str (-> req :params :deck) :key-fn keyword)]
                (assoc deck :data (-> deck :data json/write-str)))
              {})))))
            
(defn home [req]
  (h/html5 
    misc/pretty-head
    [:body
      (misc/navbar req)
      [:div.container.my-2
        [:ul
          [:li [:span.h5 "Decks: "] "Create or Edit Deck Lists"]
          [:li [:span.h5 "Cards: "] "View or search cards"]
          [:li [:span.h5 "Collection: "] "Browse collection in virtual folders"]
          [:li [:span.h5 "Litmus: "] "Test decks"]
        ]]]))
        
(defn deckviewer [req]
  (let [deck        (get-deck-data req)
       cards-in-deck (map #(-> (key %) get-card-from-code (assoc :qty (val %))) (-> deck :data json/read-str))
       warlord      (->> cards-in-deck (filter #(= (:type_code %) "warlord_unit")) first)]
    (h/html5
      misc/pretty-head
      (h/include-js "/js/whk_deck_view.js")
      [:body 
        (misc/navbar req)
        [:div.container.my-2
          [:div.row
            [:div.col-sm-8.offset-md-2
              [:div.row
                [:div.col-sm-12 
                  [:div.h2.my-1 (:name deck)]
                  [:a.h4.card-tooltip {:data-code (:code warlord) :href "#"} [:i.fas.fa-skull.fa-xs.mr-2] (:name warlord)]
                  [:input#deckcards {:type "text" :hidden true :value (:data deck)}]]]
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
                [:div.small.col-sm-12.text-muted (str "Updated on " (-> deck :updated c/from-long))]]]]]])))

(defn newdeck [req]
  (h/html5
    misc/pretty-head
    (h/include-js "/js/whk_deck_new.js")
    [:body
      (misc/navbar req)
      [:div.container.my-2
        [:div.row
          [:div.col-sm-8
            [:div.card
              [:div.card-header "Choose your Warlord"]
              [:div.list-group
                (map (fn [f]
                  (for [x (->> misc/cards :data (sort-by :code) (filter #(and (= (:faction_code %) f) (= (:type_code %) "warlord_unit"))))]
                    [:a.list-group-item {:href (str "/decks/new/" (:code x)) :data-code (:code x)} 
                      (icon-svg (:faction_code x)) (:name x)]))
                  (->> misc/cards :data (sort-by :code) (map :faction_code) distinct))]]]
          [:div.col-sm-4.d-none.d-sm-block
            [:div#warlordcards.row.sticky-top]]]]]))

            
(defn deckbuilder [req]
  (let [deck (get-deck-data req)]
    (h/html5
      misc/pretty-head
      (h/include-js "https://cdnjs.cloudflare.com/ajax/libs/showdown/1.8.6/showdown.min.js")  ; Markdown Converter
      (h/include-css "/css/deckstyle.css")
      (h/include-js "/js/externs/typeahead.js")
      (h/include-js "/js/whk_tools.js")
      (h/include-js "/js/whk_qtip.js")
      (h/include-js "/js/whk_deckbuilder.js")
      (h/include-js "https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.22.2/moment.min.js")
      (h/include-js "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.2/Chart.bundle.min.js")
      (h/include-js "/js/externs/chartjs-plugin-labels.min.js")
      [:body
        (misc/navbar req)
        [:div#cardmodal.modal {:role "dialog" :tabindex -1}]
        [:div.container
          [:div.alert.alert-dismissible.fade.show {:role "alert"}]
          [:div.row.my-2
        ;; DECKLIST
            [:div.col-md-6.decklist
              [:div#decklist.row-fluid.my-1 
                [:div.h5 "Empty Deck"]]
              [:div.row-fluid.my-1.border-dark.border-top
                [:form#save_form.form.needs-validation {:method "post" :action "/decks/save" :role "form" :novalidate true}
                  [:input#deck-id      {:type "text" :name "deck-id"      :value (:uid deck) :readonly true :hidden true}]
                  [:input#deck-content {:type "text" :name "deck-content" :value (:data deck)  :readonly true :hidden true}]
                  [:input#deck-tags    {:type "text" :name "deck-tags"    :value (:tags deck) :readonly true :hidden true}]
                  [:input#deck-notes   {type "text"  :name "deck-notes"   :value (:notes deck) :readonly true :hidden true}]
                  [:div.form-group
                    [:label {:for "#deck-name" :required true} "Army Name"]
                    [:input#deck-name.form-control {:type "text" :name "deck-name" :placeholder "New Deck" :required true :value (:name deck)}]
                    [:div.invalid-feedback "You must name your Army"]]
                  [:button.btn.btn-warning.mr-2 {:role "submit"} "Save"]
                  [:a.btn.btn-light.mr-2 {:href "/decks"} "Cancel Edits"]]]]
        ;; OPTIONS
            [:div.col-md-6
              [:ul.nav.nav-tabs.nav-fill  
                [:li.nav-item [:a.nav-link.active {:data-toggle "tab" :href "#deckbuild"} "Build"]]
                [:li.nav-item [:a.nav-link {:data-toggle "tab" :href "#decknotes"} "Notes"]]
                [:li.nav-item [:a.nav-link {:data-toggle "tab" :href "#deckcheck"} "Check"]]
                [:li.nav-item [:a.nav-link {:data-toggle "tab" :href "#decksets"} "Sets"]]]
              [:div.tab-content.my-2
            ;; BUILD
                [:div#deckbuild.tab-pane.active {:role "tabpanel"}
                  [:div.row.my-1
                    [:div.col
                      [:div#factionfilter.btn-toolbar {:role "toolbar"}
                        [:div.btn-group-sm.btn-group.btn-group-toggle.mr-1.my-1 {:data-toggle "buttons"}
                          [:label.btn.btn-outline-secondary {:title "Space Marines" :name "space_marines"} [:input {:type "checkbox" :autocomplete "off" :name "space_marines"} (icon-svg "space_marines")]]
                          [:label.btn.btn-outline-secondary {:title "Astra Militarum" :name "astra_militarum"} [:input {:type "checkbox" :autocomplete "off" :name "astra_militarum"} (icon-svg "astra_militarum")]]
                          [:label.btn.btn-outline-secondary {:title "Orks" :name "orks"} [:input {:type "checkbox" :autocomplete "off" :name "orks"} (icon-svg "orks")]]
                          [:label.btn.btn-outline-secondary {:title "Chaos" :name "chaos"} [:input {:type "checkbox" :autocomplete "off" :name "chaos"} (icon-svg "chaos")]]
                          [:label.btn.btn-outline-secondary {:title "Dark Eldar" :name "dark_eldar"} [:input {:type "checkbox" :autocomplete "off" :name "dark_eldar"} (icon-svg "dark_eldar")]]
                          [:label.btn.btn-outline-secondary {:title "Eldar" :name "eldar"} [:input {:type "checkbox" :autocomplete "off" :name "eldar"} (icon-svg "eldar")]]
                          [:label.btn.btn-outline-secondary {:title "Tau" :name "tau"} [:input {:type "checkbox" :autocomplete "off" :name "tau"} (icon-svg "tau")]]
                          [:label.btn.btn-outline-secondary {:title "Neutral"} [:input {:type "checkbox" :autocomplete "off" :name "neutral"} [:i.fas.fa-plus]]]]
                        
                        [:div.btn-group-sm.btn-group.btn-group-toggle.mr-1.my-1 {:data-toggle "buttons"}  
                          [:label.btn.btn-outline-secondary {:title "Tyranids"} [:input {:type "checkbox" :autocomplete "off" :name "tyranids"} (icon-svg "tyranids")]]
                          [:label.btn.btn-outline-secondary {:title "Necrons"} [:input {:type "checkbox" :autocomplete "off" :name "necrons"} (icon-svg "necrons")]]]]
                      [:div#typefilter.btn-group.btn-group-sm.btn-group-toggle.mr-auto.my-1 {:data-toggle "buttons"}
                        [:label.btn.btn-outline-secondary {:title "Warlord"} [:input {:type "checkbox" :autocomplete "off" :name "warlord_unit"} [:i.fas.fa-user-circle]]]
                        [:label.btn.btn-outline-secondary.active {:title "Army"} [:input {:type "checkbox" :autocomplete "off" :name "army_unit" :checked true} [:i.fas.fa-users]]]
                        [:label.btn.btn-outline-secondary {:title "Attachment"} [:input {:type "checkbox" :autocomplete "off" :name "attachment"} [:i.fas.fa-user-plus]]]
                        [:label.btn.btn-outline-secondary {:title "Event"} [:input {:type "checkbox" :autocomplete "off" :name "event"} [:i.fas.fa-bolt]]]
                        [:label.btn.btn-outline-secondary {:title "Support"} [:input {:type "checkbox" :autocomplete "off" :name "support"} [:i.fas.fa-building]]]  ;fa-hands-helping]]]
                        [:label.btn.btn-outline-secondary {:title "Synapse"} [:input {:type "checkbox" :autocomplete "off" :name "synapse_unit"} [:i.fas.fa-dna]]]]]]
                  [:div.row
                    [:div.col-md  
                      [:input#filterlist.form-control.my-1 {:type "text" :placeholder "Filter Results (or search for card)" :title "* name\nx: text\ne: set code\nf: faction code\nr? cost\ns? shields\nc? command icons\na? attack\nh? hp\nu:true|false unique\nl:true|false loyal\n\n? operators : > < !"}]]]
                  [:div.row
                    [:div.col
                      [:table#cardtbl.table.table-hover.table-sm
                        [:thead.thead-dark
                          [:tr
                            [:td "Quantity"]
                            [:td.sortable {:title "Name" :data-field "name"} "Name"]
                            [:td.sortable {:title "Type" :data-field "type"} "Type"]
                            [:td.sortable {:title "Faction" :data-field "faction"} "Fac."]
                            [:td.sortable {:title "Cost" :data-field "cost"} [:i.fas.fa-cog]]
                            [:td.sortable {:title "Command" :data-field "command_icons"} [:i.fas.fa-gavel]]
                            [:td.sortable {:title "Shields" :data-field "shields"} [:i.fas.fa-shield-alt]]
                            [:td.sortable {:title "Attack" :data-field "attack"} [:i.fas.fa-bolt]]
                            [:td.sortable {:title "HP" :data-field "hp"} [:i.fas.fa-heartbeat]]]]
                        [:tbody#tablebody]]]]]
            ;; NOTES
                [:div#decknotes.tab-pane {:role "tabpanel"}
                  [:div.row
                    [:div.col
                      [:div.form-group
                        [:label {:for "#tags"} "Tags"]
                        [:input#tags.form-control {:type "text"}]]
                      [:div.form-group
                        [:label {:for "#notes"} "Notes"]
                        [:textarea#notes.form-control {:rows 15}]]
                      [:div.card.bg-light.text-muted
                        [:div#notes-preview.card-body
                        [:span "Preview. Showdown markup syntax "
                        [:a {:href "https://github.com/showdownjs/showdown/wiki/Showdown's-Markdown-syntax" :target "_blank"} "here"]]]]]]]
            ;; CHECK      
                [:div#deckcheck.tab-pane {:role "tabpanel"}                  
                  [:div.row
                    [:div.col-md
                    ;; Sample Draw
                      [:div.row.justify-content-between.border-bottom.border-primary.mb-2
                        [:i.fas.fa-play]
                        [:a {:href "#sampledraw" :data-toggle "collapse"} "Sample Draw"]]
                      [:div#sampledraw.collapse.show
                        [:div.row.justify-content-center.my-2
                          [:div.btn-group
                            [:button.btn.btn-sm.btn-light {:type "button" :disabled "true"} "Draw:"]
                            [:button#draw1.btn.btn-sm.btn-light.btn-draw {:type "button" :val "1"} "1"]
                            [:button#draw1.btn.btn-sm.btn-light.btn-draw {:type "button" :val "2"} "2"]
                            [:button#draw1.btn.btn-sm.btn-light.btn-draw {:type "button" :val "7"} "7"]
                            [:button#draw1.btn.btn-sm.btn-light.btn-draw {:type "button" :val "all"} "All"]
                            [:button#draw1.btn.btn-sm.btn-light.btn-draw {:type "button" :val "0"} "Reset"]]]
                        [:div#hand]]
                    ;; Planets
                      [:div.row.justify-content-between.border-bottom.border-primary.mb-2
                        [:i.fas.fa-globe]
                        [:a {:href "#planets" :data-toggle "collapse"} "Planets"]]
                      [:div#planets.collapse]
                    ;; Charts
                      [:div.row.justify-content-between.border-bottom.border-primary.mb-2
                        [:i.fas.fa-chart-bar]
                        [:a {:href "#charts" :data-toggle "collapse"} "Charts"]]
                      [:div#charts.collapse
                        [:div.row
                          [:div.col-sm-6
                            [:canvas#pieFact {:width "400" :height "400"}]]
                          [:div.col-sm-6
                            [:canvas#pieType {:width "400" :height "400"}]]]
                        [:div.row
                          [:div.col-sm-6
                            [:canvas#pieCommand {:width "400" :height "400"}]]
                          [:div.col-sm-6
                            [:canvas#pieShield {:width "400" :height "400"}]]]
                        [:div.row
                          [:canvas#lineCost {:width "400" :height "300"}]]
                      ]]]]
            ;; SETS
                [:div#decksets.tab-pane {:role "tabpanel"}
                  [:div.row 
                    [:div#setlist.ml-3]]]]]]]
        [:div.border-top.border-dark.bg-secondary.text-light.px-3.pb-5 "Information"]])))
   
(defn collection [req]
  (h/html5
    misc/pretty-head
    (h/include-css "css/folderstyle.css")
    (h/include-js "js/whk_folders.js")
    [:body
      (misc/navbar req)
      [:div.container
        [:div.row.my-2
          [:div.col-md-4
            [:h2 "Packs"]
            [:div#packlist]]
          [:div.col-md-8
            [:div.row.justify-content-between
              [:h2 "Virtual Folders"]
              [:div.form-check
                [:input#showimg.form-check-input {:type "checkbox"}]
                [:label.form-check-label "Show Card Images"]]]
            [:div#foldersections.row.justify-content-between]
            [:div#folderpager.row.justify-content-between]
            [:div#folderpages.row.justify-content-between
              [:span.chaos-loader]]]]]]))

(defn litmus [req]
  (h/html5 
    [:head
      misc/pretty-head
      (h/include-css "css/litmusstyle.css")
      [:body
        (misc/navbar req)
          [:div#app]
          (h/include-js "js/compiled/cljsapp.js")]]))