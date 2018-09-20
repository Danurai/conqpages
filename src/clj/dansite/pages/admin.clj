(in-ns 'dansite.pages)

(defn login [req]
  (h/html5
    misc/pretty-head
    [:body  
      (misc/navbar req)
      [:div.container
        [:div.row.my-2
          [:div.col-sm-6.mx-auto
            [:div.card.mt-2
              [:div.card-header "Login"]
              [:div.card-body
                [:form {:action "login" :method "post"}
                  [:div.form-group
                    [:label {:for "username"} "Name"]
                    [:input#username.form-control {:type "text" :name "username" :placeholder "Username or email address" :auto-focus true}]]
                  [:div.form-group
                    [:label {:for "password"} "Password"]
                    [:input#userpassword.form-control {:type "password" :name "password" :placeholder "Password"}]]
                  [:button.btn.btn-warning.mr-2 {:type "submit"} "Login"]
                  [:a.btn-btn-link.float-right {:href "/register"} "Register"]]]]]]]]))
                  
(defn register [req]
  (h/html5
    misc/pretty-head
    [:body 
      (misc/navbar req)
      [:div.container
        [:div.h3 "Registration for this site is not open yet."]]]))
        
        
(defn useradmin [req]
  (h/html5
    misc/pretty-head
    [:body
      (misc/navbar req)
      [:div.container.mt-1
        (misc/show-alert)
        [:h2.mt-2 "User Admin"]
        [:ul.list-group
          [:li.list-group-item ;"Add user"
            [:form.form-inline.justify-content-between.needsvalidation {:action "admin/adduser" :method "post"}
              [:div.form-row.align-items-center
                [:div.col-auto
                  [:input.form-control {:name "username" :type "text" :placeholder "Username" :required true}]]
                [:div.col-auto
                  [:input.form-control {:name "password" :type "password" :placeholder "Password" :required true}]]
                [:div.col-auto
                  [:input.form-control {:name "confirm" :type "password" :placeholder "Password" :required true}]]
                [:div.col-auto
                  [:div.form-check
                    [:input.form-check-input {:name "admin" :type "checkbox"}]
                    [:label.form-check-label "Admin"]]]]
              [:button.btn.btn-primary.float-right {:role "submit"} [:i.fas.fa-plus-circle.mr-1] "Create User"]]]
          (for [user (db/get-users)]
            ^{:key (:uid user)}[:li.list-group-item
                [:div.row-fluid.mb-1
                  [:span.h3.mr-2 (:username user)]
                  (if (or (true? (:admin user)) (= 1 (:admin user)))
                    [:i.fas.fa-user-plus.text-primary.align-top]
                    [:i.fas.fa-user.align-top])
                    (if (not= (:uid user) 1001) [:button.btn.btn-danger.float-right [:i.fas.fa-times.mr-1] "Delete"])]
                [:div.row
                  [:div.col-sm-2
                    (if (not= (:uid user) 1001)
                      [:form.form-inline {:action "admin/updateadmin" :method "post"}
                        [:input {:type "text" :name "uid" :value (:uid user) :readonly true :hidden true}]
                        (if (or (true? (:admin user)) (= 1 (:admin user)))
                          [:button.btn.btn-danger {:role "submit"} [:i.fas.fa-minus-circle.mr-1] "Admin"]
                          [:button.btn.btn-primary {:role "submit" :name "admin"} [:i.fas.fa-plus-circle.mr-1] "Admin"])])]
                  [:div.col-auto
                    [:form.form-inline {:action "admin/updatepassword" :method "post"}
                      [:input.form-control {:name "uid" :value (:uid user) :readonly true :hidden true}]
                      [:div.form-group 
                        [:label.mr-2 "Reset password"]
                        [:input.form-control {:name "password" :type "password" :placeholder "new password"}]
                        [:input.form-control {:name "confirm" :type "password" :placeholder "confirm password"}]]
                      [:button.btn.btn-warning [:i.fas.fa-edit.mr-1] "Reset"]]]
                ]])]]]))