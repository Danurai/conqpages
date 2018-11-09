(in-ns 'dansite.pages)

(defn cardpage [code]
  (h/html5 
    misc/pretty-head
    (h/include-js "/js/whk_cards.js")
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
                  [:a.btn.btn-outline-secondary.card-tooltip {:href (str "/card/" (:code card-prev)) :data-code (:code card-prev) :hidden (nil? card-prev)} (:name card-prev)]]
                [:span 
                  [:a.btn.btn-outline-secondary {:href (str "/pack/" (:pack_code r))} (:pack r)]]
                [:span 
                  [:a.btn.btn-outline-secondary.card-tooltip {:href (str "/card/" (:code card-next)) :data-code (:code card-prev) :hidden (nil? card-next)} (:name card-next)]]]
              [:div.row
                [:div.col-sm
                  [:div.card  
                    [:div.card-header [:h2 (if (:unique r) [:img.unique-icon.mr-1 {:src "/img/skull.png"}]) (:name r)]]
                    [:div.card-body (:text r)]
                    [:div.card-footer.text-muted.d-flex.justify-content-between
                      [:span (:faction r)]
                      [:span (str (:pack r) " #" (-> r :position Integer.))]]]]
                [:div.col-sm
                  [:img {:src (:img r) :alt (:name r)}]]]
              ; Signature Squad cards/links
                (if (some? (:signature_squad r))
                  [:div.mb-2
                    [:div.h3 "Signature Squad"]
                    [:div.row.d-flex.justify-content-between.my-2
                      (map (fn [s]
                        [:div
                          [:a.btn.btn-outline-secondary.card-tooltip {:data-code (:code s) :href (str "/card/" (:code s))} 
                            (str (:name s) (if (not= (:type_code s) "warlord_unit") (str " x" (:quantity s))))]]
                      ) (->> misc/cards :data (filter #(= (:signature_squad %) (:signature_squad r))) (sort-by :code)))]])
            ])) (get-card-from-code code))
        ]]))