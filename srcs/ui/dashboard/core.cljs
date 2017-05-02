(ns ui.dashboard.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
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

(pages/reg-page :core/index index)
(pages/reg-page :core/notifications notifications)
