(ns ui.pages.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.pages.entities :as entities]
   [ui.pages.user :as user]
   [re-frame.core :as rf]
   [clojure.string :as str]))

(defn index [params]
  (let [route @(rf/subscribe [:route-map/current-route])]
    [:div.index
     [:h3 "Welcome to re-frame"]
     [:a {:href "#/entities"} "Entities"]]))

(defn notifications [params]
  [:div.index
   [:h3 "Notifications"]])

(defn href [& parts]
  (str "#/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts))))

(def pages (merge {:core/index index
                   :core/notifications notifications}
                  entities/pages user/pages))
