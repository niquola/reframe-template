(ns ui.pages.database
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ui.routes :refer [href]]
            [clojure.string :as str]))

(rf/reg-sub
 :debug/db
 (fn [db] db))

(defn database [params]
  (let [db (rf/subscribe [:debug/db])]
    (fn []
      [:div.container
       [:h3 "Database"]
       [:pre (.stringify js/JSON (clj->js @db) nil " ")]])))

(def pages {:database/index database})
