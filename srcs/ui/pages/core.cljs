(ns ui.pages.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [clojure.string :as str]))

(defn index [params]
  (let [route @(rf/subscribe [:route-map/current-route])]
    [:div.index
     [:h3 "Welcome to re-frame"]
     [:a {:href "#/patients"} "Patients"]]))

(defn href [& parts]
  (str "#/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts))))

(defn patients [params]
  (let [route @(rf/subscribe [:route-map/current-route])]
    [:div.index
     [:h3 "List of patients"]
     [:a {:href (href)} "home"]
     [:div
      [:a {:href (href :patients 1)} "Patient 1"]]
     [:div
      [:a {:href (href :patients 2)} "Patient 2"]]]))

(defn show-patient [params]
  [:div.index
   [:a {:href (href :patients)} "Back"]
   [:h3 "Patient" (pr-str params)]])

(def routes {:. :index
             "patients" {:. :patients
                         [:pt/id] {:. :show-patient}}})

(def pages {:index index
            :patients patients
            :show-patient show-patient})
