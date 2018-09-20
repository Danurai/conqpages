(in-ns 'dansite.pages)

(defn findcards [q]
  (h/html5
    misc/pretty-head
    [:body
      (misc/navbar nil)
      [:div.container.my-2
        [:div.row
          [:form.form-inline.my-2 {:action "/find" :method "get"}
            [:div.input-group
              [:input.form-control.qtip-search {:type "text" :name "q" :value q :placeholder "Search"}]
              [:div.input-group-append
                [:button.btn.btn-primary {:type "submit"} "Search"]]]]]
        [:div.row
          [:table.table.table-sm.table-hover
            [:thead [:tr [:td "Name"][:td "Faction"][:td "Type"][:td "Cost"][:td [:i.fas.fa-cog]][:td "Set"]]]
            [:tbody
              (map (fn [r]
                [:tr
                  [:td [:a.card-tooltip {:href (str "/card/" (:code r)) :data-code (:code r)} (:name r)]]
                  [:td (:faction r)]
                  [:td (:type r)]
                  [:td (:cost r)]
                  [:td {:title (:signature_loyal r)} (case (:signature_loyal r) "Signature" [:i.fas.fa-cog.icon-sig] "Loyal" [:i.fas-fa-crosshairs.icon-loyal] "")]
                  [:td (str (:pack r) " #" (-> r :position Integer.))]
                 ])
                (tools/cardfilter misc/cards q))]]]]]))

(defn cardpage [code]
  (h/html5 
    misc/pretty-head
    [:body
      (misc/navbar nil)
      [:div.container.my-2
        ((fn [r]
          (let [code-card (Integer/parseInt (:code r))
               card-next (get-card-from-code (format "%06d" (inc code-card)))
               card-prev (get-card-from-code (format "%06d" (dec code-card)))]
            [:div.col
              [:div.row-fluid.d-flex.justify-content-between.my-3
                [:span
                  [:a.btn.btn-outline-secondary {:href (str "/card/" (:code card-prev)) :hidden (nil? card-prev)} (:name card-prev)]]
                [:span 
                  [:a.btn.btn-outline-secondary {:href (str "/pack/" (:pack_code r))} (:pack r)]]
                [:span 
                  [:a.btn.btn-outline-secondary {:href (str "/card/" (:code card-next)) :hidden (nil? card-next)} (:name card-next)]]]
              [:div.row
                [:div.col-sm
                  [:div.card  
                    [:div.card-header [:h2 (if (:unique r) [:img.unique-icon.mr-1 {:src "/img/skull.png"}]) (:name r)]]
                    [:div.card-body (:text r)]
                    [:div.card-footer.text-muted.d-flex.justify-content-between
                      [:span (:faction r)]
                      [:span (str (:pack r) " #" (-> r :position Integer.))]]]]
                [:div.col-sm
                  [:img {:src (:img r) :alt (:name r)}]]]]))
        (get-card-from-code code))]]))

(defn searchpage [req]
  (h/html5
    misc/pretty-head
    [:body  
      (misc/navbar req)
      [:div.container.my-2
        [:div.col-md-6
          [:div.row-fluid
            (for [cycle (:data misc/cycles)]
              [:div
                [:a {:href (str "/cycle/" (:position cycle))} (:name cycle)]
                [:ol
                  (for [pack (->> misc/packs :data (remove #(= (:code %) (:code cycle))) (filter #(= (:cycle_code %) (:code cycle))))]
                    [:li [:a {:href (str "pack/" (:code pack))} (:name pack)]])]])
          ]]]]))