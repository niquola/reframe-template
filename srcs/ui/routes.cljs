(ns ui.routes
  (:require [clojure.string :as str]))

(defn href [& parts]
  (str "#/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts))))
