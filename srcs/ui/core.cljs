(ns ui.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [cljsjs.react]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-frisk.core :refer [enable-re-frisk!]]
   [frames.routing]
   [ui.pages.core :as pages]
   [ui.layout :as layout]
   [devtools.core :as devtools]))

(devtools/install!)

(defn current-page []
  (let [{page :match params :params} @(rf/subscribe [:route-map/current-route])]
    (if-let [cmp (get pages/pages page)]
      [:div [cmp params]]
      [:div.not-found (str "Page not found ")])))

(rf/reg-event-fx
 ::initialize
 (fn [cofx [_ ev]]
   {:dispatch [:route-map/init pages/routes]}))


(defn- mount-root []
  (reagent/render
   [layout/layout [current-page]]
   (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch [::initialize])
  (enable-re-frisk!)
  (mount-root))
