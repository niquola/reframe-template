(ns ui.routes
  (:require [clojure.string :as str]
            [route-map.core :as route-map]))

(def routes {:. :core/index
             "profile"  {:. :user/profile}
             "notifications" {:. :core/notifications}
             "patients" {:. :patients/index
                         "new" {:. :patients/new}
                         [:pt/id]  {:. :patients/show
                                    "edit" {:. :patients/edit}}}})

(defn href [& parts]
  (let [url (str "/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts)))]
    (when-not  (route-map/match [:. url] routes)
      (.error js/console (str url " is not matches routes")))
    (str "#" url)))
