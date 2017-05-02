(ns ui.database.core
  (:require-macros [reagent.ratom :refer [reaction]]
                   [cljs.pprint :as pp])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ui.routes :refer [href]]
            [ui.widgets :as wgt]
            [ui.pages :as pages]
            [clojure.string :as str]))

(rf/reg-sub
 :debug/db
 (fn [db] db))

(defn database [params]
  (let [db (rf/subscribe [:debug/db])]
    (fn []
      [:div.container
       [:h3 "Database"]
       [wgt/pp @db]])))

(pages/reg-page :database/index database)
