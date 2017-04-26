(ns ui.fhir 
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))

(rf/reg-event-fx
 :fhir/search
 (fn [coef [_ args]]
   (.log js/console "FHIR" args)
   (if (:Patient (:db coef))
     coef
     {:dispatch
      [:fhir/loaded args
       [{:id "pt-1" :birthDate "1980" :name [{:given ["Nikolai"] :family []} :gender "Male"]}
        {:id "pt-2" :telecom [] :name [{:given ["Marat"] :family []}]  :gender "Male"}
        {:id "pt-3" :name [{:given ["Lara"] :family []}] :gender "Female"}]]})))

(rf/reg-event-fx
 :fhir/read
 (fn [coef args]
   (.log js/console "FHIR get" args)
   coef))

(rf/reg-event-db
 :fhir/reset
 (fn [db [_ path]]
   (assoc-in db path (get-in db (into [:original] path)))))

(rf/reg-event-db
 :fhir/commit
 (fn [db [_ path]]
   (assoc-in db (into [:original] path) (get-in db path))))

(rf/reg-event-db
 :fhir/loaded
 (fn [db [_ {rt :resourceType :as args} resp]]
   (.log js/console "FHIR loaded" args resp)
   (let [idx (reduce (fn [acc res]
                       (assoc acc (:id res) res)) {} resp)]
     (-> db
         (assoc-in [(keyword rt)] idx)
         (assoc-in [:original (keyword rt)] idx)))))
