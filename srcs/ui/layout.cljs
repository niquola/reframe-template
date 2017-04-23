(ns ui.layout
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.styles :as styles]
   [re-frame.core :as rf]
   [ui.routes :refer [href]]
   [clojure.string :as str]))


(defn menu []
  [:nav.navbar.navbar-default
   [:div.container-fluid
    [:div.navbar-header
     [:button.navbar-toggle.collapsed
      {:type "button"}
      [:span.sr-only "Toggle navigation"]
      [:span.icon-bar]
      [:span.icon-bar]
      [:span.icon-bar]]
     [:a.navbar-brand {:href "#/"} "Brand"]]
    [:div.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li.active [:a {:href (href :entities)} "Entities" [:span.sr-only "(current)"]]]
      [:li [:a {:href (href :entities :new)} "New Entity"]]]
     [:form.navbar-form.navbar-left
      [:div.form-group
       [:input.form-control {:placeholder "Search", :type "text"}]]
      [:button.btn.btn-default {:type "submit"} "Submit"]]
     [:ul.nav.navbar-nav.navbar-right
      [:li [:a {:href (href :notifications)} "Notifications"]]
      [:li [:a {:href (href :profile)} "Profile"]]
      #_[:li.dropdown
       [:a.dropdown-toggle
        {:aria-expanded "false",
         :aria-haspopup "true",
         :role "button",
         :data-toggle "dropdown",
         :href "#"}
        "Dropdown "
        [:span.caret]]
       [:ul.dropdown-menu
        [:li [:a {:href "#"} "Action"]]
        [:li [:a {:href "#"} "Another action"]]
        [:li [:a {:href "#"} "Something else here"]]
        [:li.divider {:role "separator"}]
        [:li [:a {:href "#"} "Separated link"]]]]]]]]

  )

(defn layout [content]
  [:div.app
   [:style styles/basic-style]
   [menu]
   [:div.container-fluid content]])
