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

(defn menu []
  (let [auth (rf/subscribe [:auth])]
    (fn []
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
          [:li.active [:a {:href (href :patients)} "Patients" [:span.sr-only "(current)"]]]
          [:li [:a {:href (href :patients :new)} "New Patient"]]]
         #_[:form.navbar-form.navbar-left
            [:div.form-group
             [:input.form-control {:placeholder "Search", :type "text"}]]
            [:button.btn.btn-default {:type "submit"} "Submit"]]
         [:ul.nav.navbar-nav.navbar-right
          [:style ".avatar {width: 20px; height: 2opx; margin: 0 5px; border-radius: 50%;}"]
          [:li [:a {:href (href :notifications)} "Notifications"]]
          [:li [:a {:href (href :profile)}
                (if-let [pic (:picture @auth)] [:img.avatar {:src pic}] "(*_*)")
                (when-let [a @auth] (or (:nickname a) (:email a)))]]]]]]))

  )

(defn layout [content]
  [:div.app
   [:style styles/basic-style]
   [menu]
   [:div.container-fluid content]])
