(ns ui.db
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [cljsjs.react]
   [clojure.string :as str]
   [reagent.core :as reagent]))

(rf/reg-sub-raw :route (fn [db _] (reaction (:route @db))))
(rf/reg-sub-raw :db (fn [db _] (reaction @db)))
