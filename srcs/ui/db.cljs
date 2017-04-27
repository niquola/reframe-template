(ns ui.db 
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))

(rf/reg-sub-raw :route (fn [db _] (reaction (:route @db))))
(rf/reg-sub-raw :db (fn [db] db))
