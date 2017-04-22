(ns ui.pages.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.pages.patients :as patients]
   [re-frame.core :as rf]
   [clojure.string :as str]))

(defn index [params]
  (let [route @(rf/subscribe [:route-map/current-route])]
    [:div.index
     [:h3 "Welcome to re-frame"]
     [:a {:href "#/patients"} "Patients"]]))

(defn href [& parts]
  (str "#/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts))))


(def routes {:. :index
             "patients" patients/routes})

(def pages (merge {:index index} patients/pages))
