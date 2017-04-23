(ns ui.pages.user
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ui.routes :refer [href]]
            [clojure.string :as str]))

(defn profile [params]
  [:div.index [:h3 "User Profile"]])


(def pages {:user/profile profile})
