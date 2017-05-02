(ns ui.pages
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [ui.widgets :as wgt]
   [clojure.string :as str]))

;; pages provide reg-page function,
;; which allows you to register page under
;; some keyword, which will be used as routing key

(defonce pages (atom {}))

(defn reg-page
  "register page under keyword for routing"
  [key f & [layout-key]]
  (swap! pages assoc key f))

