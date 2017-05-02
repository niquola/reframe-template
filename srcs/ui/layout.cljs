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

(defn human-name [n]
  (str/join " "
            (concat
             (or (:given (first n)) [])
             (or (:family (first n)) []))))

(defn badge [pt]
  [:div.badge
   [styles/style
    [:.badge
     {:text-align "center"}
     [:.inform {:text-align "center" :font-size "18px"}]
     [:.avatar {:text-align "center"}
      [:i {:font-size "80px" :color "gray"}]]]]
   [:div.avatar [wgt/icon :user-circle]]
   [:br]
   [:div.inform
    [:a.nav-link {:href (href :patients (:id pt))}
     (human-name (:name pt))]]])

(defn layout [content]
  (let [current-pt (rf/subscribe [:patients/current-patient])
        breadcrumbs (rf/subscribe [:route-map/breadcrumbs]) ]
    (fn []
      [:div.app
       [:style styles/basic-style]
       [styles/style [:.secondary-nav {:border-left "1px solid #ddd"}]]
       [menu]
       [:div.container
        [:div.container-fluid
         [:ol.breadcrumb
          (for [b @breadcrumbs]
            [:li.breadcrumb-item
             [:a {:href (str "#" (:uri b))} (:breadcrumb b)]])] ]]
       (if-let [pt @current-pt]
         [:div.container
          [:div.row
           [:div.col-md-9 content]
           [:div.col-md-3.secondary-nav
            [badge pt]
            [:hr]
            [:ul.nav.flex-column
             [:li.nav-item
              [:a.nav-link {:href (href :patients (:id pt) :edit)}
               (wgt/icon :user)
               " Demographics"]]
             [:li.nav-item
              [:a.nav-link {:href (href :patients (:id pt) :coverages)}
               (wgt/icon :usd)
               " Insurances"]]

             [:li.nav-item
              [:a.nav-link {:href (href :patients (:id pt) :coverages)}
               (wgt/icon :ambulance)
               " Encounters"]]

             [:li.nav-item
              [:a.nav-link {:href (href :patients (:id pt) :coverages)}
               (wgt/icon :ambulance)
               " Allergies"]]

             ]]]]
         [:div.container content])])))
