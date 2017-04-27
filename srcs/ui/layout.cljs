(ns ui.layout
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.styles :as styles]
   [re-frame.core :as rf]
   [ui.routes :refer [href]]
   [ui.styles :as styles]
   [ui.widgets :as wgt]
   [clojure.string :as str]))

(rf/reg-sub-raw
 :auth
 (fn [db _] (reaction (:auth @db))))

(defn current-page? [route key]
  (= (:match route) key))

(defn nav-item [route k path title]
  [:li.nav-item {:class (when (current-page? @route k) "active")}
   [:a.nav-link {:href (apply href path)} title]])

(defn menu []
  (let [auth (rf/subscribe [:auth])
        route (rf/subscribe [:route-map/current-route])]
    (fn []
      [:nav.navbar.navbar-toggleable-md.navbar-light.bg-faded
       (styles/style [:nav.navbar {:border-bottom "1px solid #ddd"
                                   :padding-bottom 0}
                      [:li.nav-item {:border-bottom "3px solid transparent"}
                       [:&.active {:border-color "#555"}]]
                      [:.avatar {:width "20px"
                                 :height "20px"
                                 :margin "0 5px"
                                 :border-radius "50%"}]])
       [:div.container
        [:a.navbar-brand {:href "#/"} "MyEHR"]
        [:div.collapse.navbar-collapse
         [:ul.nav.navbar-nav.mr-auto
          [nav-item route :patients/index [:patients] "Patients"]
          [nav-item route :patients/new [:patients :new] "New Patient"]]
         [:ul.nav.navbar-nav.my-2.my-lg-0
          [nav-item route :core/notifications [:notifications] (wgt/icon :bell)]
          [nav-item route :database/index [:db] (wgt/icon :database)]
          [nav-item route :profile/index [:profile]
           [:span
            (if-let [pic (:picture @auth)] [:img.avatar {:src pic}] "(*_*)")
            (when-let [a @auth] (or (:nickname a) (:email a)))]]]]]])))

(defn layout [content]
  [:div.app
   [:style styles/basic-style]
   [menu]
   content])
