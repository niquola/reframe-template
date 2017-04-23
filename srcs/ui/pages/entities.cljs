(ns ui.pages.entities
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ui.routes :refer [href]]
            [clojure.string :as str]
            [cljs.pprint :as pp]
            [chloroform.core :as f]
            [clojure.string :as str]))

(defn entities [params]
  (let [route @(rf/subscribe [:route-map/current-route])]
    [:div.index
     [:h3 "List of entities"]
     [:table.table
      [:tbody
       [:tr [:td [:a {:href (href :entities 1)} "Entity 1"]]]
       [:tr [:td [:a {:href (href :entities 2)} "Entity 2"]]]]]]))

(defn show-entity [params]
  [:div.index
   [:h3 "Entity" (pr-str params)]])

(defn desc [h] [:pre (with-out-str (pp/pprint h))])

(defn new-entity [params]
  (let [form (f/form-atom {:data "{\"foo\": 42}"}) ]
    (fn []
      [:div.container
       [:div.row
        [:div.col-md-8
         [f/form form
          {:on-submit #(.log js/console (clj->js (:data @form)))
           :class "form"
           :validate   (fn [form]  (when-not (= (:name form) "Marat")
                                     {[:name] ["Should be Marat"]}))}

          [:h2 "Form H2 header"]

          [:$row {:name [:foo :bar] :as :string :$required true}]

          [:$row {:name :email :as :string
                  :$required true
                  :hint "Enter your Email"
                  :messages {:required "Email is required"}}]

          [:$row {:name :name :as :string
                  :$min-length 5
                  :hint "Enter your name. It should be Marat"
                  :$max-length 10
                  :messages {:min-length "Length should be great then 5"}}]

          [:h3 "Inputs without hint"]

          [:$row {:name :last_mame :as :string
                  :messages {:min-length "Length should be great then 5"}}]

          [:$row {:name :first_mame :as :string
                  :messages {:min-length "Length should be great then 5"}}]

          [:h3 "Radio and checkboxes"]

          [:$row {:name :roles
                  :as :check-boxes
                  :text-fn (fn [x] [:span [:b (:id x)] (:text x)])
                  :collection [{:id 1 :text "Admin"}
                               {:id 2 :text "User"}]}]

          [:$row {:name :gender_select
                  :as :select
                  :$required true
                  :hint "Select your gender throw drop down"
                  :messages {:required "Select your gender"}
                  :collection ["Male" "Female" "Trap" "Unknown"]}]

          [:$row {:name :numbers :as :radio-buttons
                  :collection [{:id 1 :label "One"} {:id 2 :label "Two"}]
                  :value-fn :id
                  :text-fn :label}]

          [:$row {:name :comments :as :text :$max-length 1000}]

          ;; [:$row {:name :data :as :json}]

          [:$row {:name :after :as :string
                  :messages {:min-length "Length should be great then 5"}}]

          [:$row {:name :more :as :string
                  :messages {:min-length "Length should be great then 5"}}]

          [:button.btn-primary {:type "submit"} "Save"]]]

        [:div.col-md-4
         [:pre (.stringify js/JSON (clj->js @form) nil 2)]]
        ]])))

(def pages {:entities/index entities
            :entities/new  new-entity
            :entities/show-entity show-entity})
