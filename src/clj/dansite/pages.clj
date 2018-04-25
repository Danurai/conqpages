(ns dansite.pages
  (:require
    [hiccup.page :as h]
    [cemerick.friend :as friend]
    [dansite.misc :as misc]))
                  
(defn deckbuilder [req]
    (h/html5
      misc/pretty-head
      (h/include-css "css/deckstyle.css")
      (h/include-js "js/whk_tools.js")
      (h/include-js "js/whk_qtip.js")
      (h/include-js "js/whk_deckbuilder.js")
      (h/include-js "js/externs/moment.min.js")
      (h/include-js "js/externs/Chart.min.js")
      [:body
        (misc/navbar req)
        [:div.container
          [:div.row.my-2
            [:div.col-md.decklist
              [:div.row
                [:div#decklist.col "Empty Deck"]]
              [:div.row 
                [:form#save_form.form-inline {:method "post" :action "/decks/save" :role "form"}
                  [:input#deck-content {:type "text" :name "deck-content" :value "" :hidden true}]
                  [:input#deck-name.form-control {:type "text" :name "deck-name" :placeholder "New Deck"}]
                  [:button.btn.btn-warning {:role "submit"} "Save Deck"]]]
              [:div.row.my-2 
                [:button#newdeck.btn.btn-danger.pull-right "New Deck"]]]
            [:div.col-md
              [:ul.nav.nav-tabs.nav-fill  
                [:li.nav-item [:a.nav-link.active {:data-toggle "tab" :href "#deckbuild"} "Build"]]
                [:li.nav-item [:a.nav-link {:data-toggle "tab" :href "#deckcheck"} "Check"]]
                [:li.nav-item [:a.nav-link {:data-toggle "tab" :href "#decksets"} "Sets"]]
                [:li.nav-item [:a.nav-link {:data-toggle "tab" :href "#deckloader"} "Load"]]]
              [:div.tab-content.my-2
            ;; BUILD
                [:div#deckbuild.tab-pane.active {:role "tabpanel"}
                  [:div.row
                    [:div.col-md  
                      [:input#filterlist.form-control.my-1 {:type "text" :placeholder "Filter List"}]]]
                  [:div.row.my-1
                    [:div.col-md  
                      [:div#factionfilter.btn-group.btn-group-toggle {:data-toggle "buttons"}
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Space Marines"} [:input {:type "checkbox" :autocomplete "off" :name "space_marines"} "SM"]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Astra Militarum"} [:input {:type "checkbox" :autocomplete "off" :name "astra_militarum"} "AM"]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Orks"} [:input {:type "checkbox" :autocomplete "off" :name "orks"} "Or"]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Chaos"} [:input {:type "checkbox" :autocomplete "off" :name "chaos"} [:i.fas.fa-asterisk]]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Dark Eldar"} [:input {:type "checkbox" :autocomplete "off" :name "dark_eldar"} "DE"]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Eldar"} [:input {:type "checkbox" :autocomplete "off" :name "eldar"} "El"]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Tau"} [:input {:type "checkbox" :autocomplete "off" :name "tau"} [:i.far.fa-circle]]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Tyranids"} [:input {:type "checkbox" :autocomplete "off" :name "tyranids"} "Ty"]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Necrons"} [:input {:type "checkbox" :autocomplete "off" :name "necrons"} "Ne"]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Neutral"} [:input {:type "checkbox" :autocomplete "off" :name "neutral"} "Ne"]]]
                      [:div#typefilter.btn-group.btn-group-toggle {:data-toggle "buttons"}
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Warlord"} [:input {:type "checkbox" :autocomplete "off" :name "warlord_unit"} [:i.fas.fa-user-circle]]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Army"} [:input {:type "checkbox" :autocomplete "off" :name "army_unit"} [:i.fas.fa-users]]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Attachment"} [:input {:type "checkbox" :autocomplete "off" :name "attachment"} [:i.fas.fa-user-plus]]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Event"} [:input {:type "checkbox" :autocomplete "off" :name "event"} [:i.fas.fa-bolt]]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Support"} [:input {:type "checkbox" :autocomplete "off" :name "support"} [:i.fas.fa-hand-helping]]]
                        [:label.btn.btn-sm.btn-outline-secondary {:title "Synapse"} [:input {:type "checkbox" :autocomplete "off" :name "synapse_unit"} [:i.fas.fa-dna]]]]]]
                  [:div.row
                    [:div.col-sm
                      [:table#cardtbl.table.table-hover.table-sm
                        [:thead.thead-dark
                          [:tr
                            [:td "Quantity"]
                            [:td "Name"]
                            [:td "Type"]
                            [:td "Fac."]
                            [:td {:title "Cost"} [:i.fas.fa-cog]]
                            [:td {:title "Command"} [:i.fas.fa-gavel]]
                            [:td {:title "Shields"} [:i.fas.fa-shield-alt]]
                            [:td {:title "Attack"} [:i.fas.fa-bolt]]
                            [:td {:title "HP"} [:i.fas.fa-heartbeat]]]]
                        [:tbody#tablebody]]]]]
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
                        [:canvas#lineCost {:width "400" :height "400"}]
                        [:canvas#barIcons {:width "400" :height "400"}]
                        [:canvas#lineStr {:width "400" :height "400"}]
                        [:canvas#pieFact {:width "400" :height "400"}]]]]]
            ;; SETS
                [:div#decksets.tab-pane {:role "tabpanel"}
                  [:div.row 
                    [:div#setlist.ml-3]]]
            ;; LOAD
                [:div#deckloader.tab-pane {:role "tabpanel"}
                  [:div.form-row
                    [:textarea#deckload.form-control {:type "text" :rows "20"}]]
                  [:div.form-row.my-1.float-right
                    [:button#loaddeck.btn.btn-warning "Load"]]]]]]]
        [:div.border-top.border-dark.bg-secondary.text-light.px-3.pb-5 "Information"]
        [:div#cardmodal.modal {:role "dialog"}]]))
   
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
            [:div#folderpages.row.justify-content-between]]]]]))