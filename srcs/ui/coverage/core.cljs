(ns ui.coverage.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ui.routes :refer [href]]
            [ui.widgets :as wgt]
            [ui.widgets.lookup :as lookup]
            [ui.styles :as styles]
            [clojure.string :as str]
            [cljs.pprint :as pp]
            [re-form.core :as form]
            [ui.pages :as pages]
            [clojure.string :as str]))


(rf/reg-sub-raw
  :coverages
  (fn [db _] (reaction (:Coverage @db))))

(defn index [{pid :pt/id}]
  (rf/dispatch [:fhir/search {:resourceType "Coverage" :query {:planholder-reference (str "Patient/" pid)}}])
  (let [path [:form :patient :indsurance]
        coverages (rf/subscribe [:coverages]) ]
    (fn [_]
      [:div.index.container-fluid
       [:h3 "Patient insurance"]
       [:br]
         (if (empty? @coverages)
           [:div [:b "Tere are not patient insurance"]
            [:a.btn.btn-primary "Add insurance"]]

           [:table.table
            [:thead
             [:tr
              [:th "Bin"]
              [:th "Status"]
              [:th "Ref"]]]

            [:tbody
             (for [[id i] @coverages]
               [:tr {:key (:id i)}
                [:td [:a {:href (href :patients pid :coverages (:id i))}
                      (or (:bin i) "Some bin") ]]
                [:td (:status i)]
                [:td (pr-str (get-in i [:planholderReference :reference]))]])]])]
       )))

(rf/reg-sub-raw
 :coverages/current-coverage
 (fn [db _]
   (reaction (get @db :current-coverage))))

(rf/reg-event-fx
 :coverages/current-coverage
 (fn [coef [_ phase params]]
   (if (= :init phase)
     {:dispatch  [:fhir/read {:resourceType "Coverage" :id (:coverage/id params) :into [:current-coverage]}]}
     {:dispatch  [:db/write [:current-coverage] nil]})))


(defn validate [_] {})
(defn show [{id :coverage/id}]
  (let [path [:form :Coverage id]
        cv (rf/subscribe [:db/by-path path])
        errors (reaction (validate @cv)) ]
    (rf/dispatch [:db/write path {}])
    (rf/dispatch [:fhir/read {:resourceType "Coverage" :id id :into path}])
    (fn [_]
      [:div.form-group
       [form/row {:path [:bin]
                  :errors errors
                  :label "BIN"
                  :base-path path
                  :as form/input}]
       [:hr]
       ;[:div.form-group
         ;[form/submit-btn save-fn "Save"]
         ;" "
         ;[form/cancel-btn cancel-fn "Cancel"]]
       ])))

(pages/reg-page :coverages/index index)
(pages/reg-page :coverages/show show)
