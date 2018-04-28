(ns ui.patients.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ui.routes :refer [href]]
            [ui.widgets :as wgt]
            [ui.styles :as styles]
            [clojure.string :as str]
            [cljs.pprint :as pp]
            [ui.pages :as pages]
            [clojure.string :as str]))

(pages/reg-page :patients/index (fn [route] [:h1 "patients index"]))
(pages/reg-page :patients/edit (fn [route] [:h1 "patients edit"]))
(pages/reg-page :patients/show (fn [route] [:h1 "patients show"]))
(pages/reg-page :patients/new (fn [route] [:h1 "patients new"]))
