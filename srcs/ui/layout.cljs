(ns ui.layout
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.styles :as styles]
   [re-frame.core :as rf]
   [ui.routes :refer [href]]
   [clojure.string :as str]))

(rf/reg-sub-raw
 :auth
 (fn [db _] (reaction (:auth @db))))

(defn current-page? [route key]
  (= (:match route) key))

(defn toggle-button []
  [:button.navbar-toggle.collapsed
   {:type "button"}
   [:span.sr-only "Toggle navigation"]
   [:span.icon-bar]
   [:span.icon-bar]
   [:span.icon-bar]])

(defn menu []
  (let [auth (rf/subscribe [:auth])
        route (rf/subscribe [:route-map/current-route])]
    (fn []
      [:nav.navbar.navbar-default
       [:div.container-fluid
        [:div.navbar-header
         [toggle-button]
         [:a.navbar-brand {:href (href)} "EHR"]]
        [:div.collapse.navbar-collapse
         [:ul.nav.navbar-nav
          [:li {:class (when (current-page? @route :patients/index) "active")}
           [:a {:href (href :patients)} "Patients"]]
          [:li {:class (when (current-page? @route :patients/new) "active")}
           [:a {:href (href :patients :new)} "New Patient"]]
          [:li {:class (when (current-page? @route :database/index) "active")}
           [:a {:href (href :db)} "db"]]]

         [:ul.nav.navbar-nav.navbar-right
          [:style ".avatar {width: 20px; height: 2opx; margin: 0 5px; border-radius: 50%;}"]
          [:li [:a {:href (href :notifications)} "Notifications"]]
          [:li [:a {:href (href :profile)}
                (if-let [pic (:picture @auth)] [:img.avatar {:src pic}] "(*_*)")
                (when-let [a @auth] (or (:nickname a) (:email a)))]]]]]])))

(defn layout [content]
  [:div.app
   [:style styles/basic-style]
   [menu]
   [:div.container-fluid content]])
