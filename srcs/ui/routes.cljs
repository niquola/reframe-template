(ns ui.routes
  (:require [clojure.string :as str]
            [route-map.core :as route-map]))

;; application routes represented as hash-map (see https://github.com/niquola/route-map)
;; each leaf is key which mapped to page component thro pages hash-map

;; You could define custom re-frame event under :context  key,
;; while matching routes all :context events will be fired with parameter :init
;; We also save previos contexts and fire event with :deinit flag for disposed contexts
;; You could use context to init common state for some branch of routes

(def routes {:. :core/index
             :breadcrumb "Home"
             "profile"  {:. :user/profile}
             "notifications" {:. :core/notifications}
             "db" {:. :database/index}
             "patients" {:breadcrumb "Patients"
                         :. :patients/index
                         "new" {:. :patients/new}
                         [:pt/id]  {:context :patients/current-patient
                                    :. :patients/show
                                    "edit" {:. :patients/edit}
                                    "coverages" {:. :coverages/index
                                                 :breadcrumb "Insurance"
                                                 "new" {:. :coverages/new}
                                                 [:coverage/id] {:context :coverages/current-coverage
                                                                 :. :coverages/show}

                                                 } }}})

(defn href
  ;; helper function to build urls also check url
  ;; is valid for current routing
  ;; (href :patients 5) => #/patients/5
  [& parts]
  (let [url (str "/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts)))]
    (when-not  (route-map/match [:. url] routes)
      (.error js/console (str url " is not matches routes")))
    (str "#" url)))
