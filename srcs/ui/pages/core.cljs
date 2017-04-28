(ns ui.pages.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.pages.patients :as patients]
   [ui.pages.coverages :as coverages]
   [ui.pages.user :as user]
   [ui.pages.database :as database]
   [re-frame.core :as rf]
   [ui.widgets :as wgt]
   [clojure.string :as str]))

(defn widget []
  [:div
   [:ul
    [:li [:a {:href "#/"} "one"]]
    [:li [:a {:href "#/"} "two"]]
    [:li [:a {:href "#/"} "three"]]]])

(defn index [params]
  (let [route @(rf/subscribe [:route-map/current-route])]
    [:div.container
     [:div.row
      [:div.col-md-4
       [:h4 (wgt/icon :user) " My patients"]
       [:hr]
       [widget]]
      [:div.col-md-4
       [:h4 (wgt/icon :bell) " Notifications"]
       [:hr]
       [widget]]
      [:div.col-md-4
       [:h4 (wgt/icon :scheduled) " Tasks"]
       [:hr]
       [widget]]]]))

(defn notifications [params]
  [:div.container
   [:h3 "Notifications"]])

(def pages (merge {:core/index index
                   :core/notifications notifications}
                  patients/pages
                  coverages/pages
                  database/pages
                  user/pages))
