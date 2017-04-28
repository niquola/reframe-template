(ns ui.routes
  (:require [clojure.string :as str]
            [route-map.core :as route-map]))

(def routes {:. :core/index
             "profile"  {:. :user/profile}
             "notifications" {:. :core/notifications}
             "db" {:. :database/index}
             "patients" {:. :patients/index
                         "new" {:. :patients/new}
                         [:pt/id]  {:context :patients/current-patient
                                    :. :patients/show
                                    "edit" {:. :patients/edit}
                                    "coverages" {:. :coverages/index
                                                 [:coverage/id] {:context :coverages/current-coverage
                                                                 :. :coverages/show} } }}})

(defn href [& parts]
  (let [url (str "/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts)))]
    (when-not  (route-map/match [:. url] routes)
      (.error js/console (str url " is not matches routes")))
    (str "#" url)))
