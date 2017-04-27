(ns ui.pages.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.pages.patients :as patients]
   [ui.pages.user :as user]
   [ui.pages.database :as database]
   [re-frame.core :as rf]
   [clojure.string :as str]))

(defn index [params]
  (let [route @(rf/subscribe [:route-map/current-route])]
    [:div.index
     [:h3 "Dashboard"]]))

(defn notifications [params]
  [:div.index
   [:h3 "Notifications"]])

(def pages (merge {:core/index index
                   :core/notifications notifications}
                  patients/pages
                  database/pages
                  user/pages))
