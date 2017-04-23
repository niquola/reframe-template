(ns ui.routes
  (:require [clojure.string :as str]
            [route-map.core :as route-map]))

(def routes {:. :core/index
             "profile"  {:. :user/profile}
             "notifications" {:. :core/notifications}
             "entities" {:. :entities/index
                         "new" {:. :entities/new}
                         [:pt/id] {:. :entities/show-entity}}})

(defn href [& parts]
  (let [url (str "/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts)))]
    (when-not  (route-map/match [:. url] routes)
      (.error js/console (str url " is not matches routes")))
    (str "#" url)))
