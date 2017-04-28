(ns ui.pages.coverages
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
       [:h3 "Patient coverages"]
       [:br]
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
            [:td (pr-str (get-in i [:planholderReference :reference]))]])]]]
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

(defn show [{id :coverage/id}]
  (let [coverage (rf/subscribe [:coverages/current-coverage])]
    (fn [_]
      [:div
       [:pre (pr-str @coverage)]
       [:div "sshow " id]])))

(def pages {:coverages/index index
            :coverages/show show })
