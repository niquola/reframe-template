(ns ui.pages.patients
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ui.routes :refer [href]]
            [clojure.string :as str]))

(defn patients [params]
  (let [route @(rf/subscribe [:route-map/current-route])]
    [:div.index
     [:h3 "List of patients"]
     [:a {:href (href)} "home"]
     [:div [:a {:href (href :patients 1)} "Patient 1"]]
     [:div [:a {:href (href :patients 2)} "Patient 2"]]]))

(defn show-patient [params]
  [:div.index
   [:a {:href (href :patients)} "Back"]
   [:h3 "Patient" (pr-str params)]])


(def routes {:. :patients/index
             [:pt/id] {:. :patients/show-patient}})

(def pages {:patients/index patients
            :patients/show-patient show-patient})
