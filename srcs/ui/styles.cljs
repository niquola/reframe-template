(ns ui.styles
  (:require [garden.core :as garden]
            [garden.units :as units]))

(def px units/px)

(def *colors
  {:border "#ddd"
   :hover-color "white"
   :error "#d9534f"
   :hover-background "#0275d8"})

(defn color [nm]
  (get *colors nm))

(defn style [css]
  [:style (garden/css css)])

(def basic-style
  (garden/css
   [:body
    [:nav {:margin-bottom (units/px 25)}]
    [:.control-errors {:color (color :error) :font-size (px 12)}]]))


