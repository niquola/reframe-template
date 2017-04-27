(ns ui.fhir 
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))


(def base-url "http://cleoproto.aidbox.io/fhir")

(rf/reg-event-fx
 :fhir/search
 (fn [coef [_ args]]
   {:json/fetch {:uri (str base-url "/" (:resourceType args))
                 :success {:event :fhir/search-results
                           :resourceType (:resourceType args)}}}))

(rf/reg-event-fx
 :fhir/read
 (fn [coef args] coef))

(rf/reg-event-db
 :fhir/reset
 (fn [db [_ path]] (assoc-in db path (get-in db (into [:original] path)))))

(rf/reg-event-db
 :fhir/commit
 (fn [db [_ path]]
   (assoc-in db (into [:original] path) (get-in db path))))

(rf/reg-event-fx
 :fhir/update
 (fn [{db :db} [_ {path :path succ :success :as args}]]
   (.log js/console "update" args)
   (when-let [res (get-in db path)]
     {:json/fetch {:uri (str base-url "/" (:resourceType res) "/" (:id res))
                   :method "put"
                   :body res
                   :success {:event :fhir/updated
                             :path path
                             :success succ}}})))

(rf/reg-event-fx
 :fhir/updated
 (fn [{db :db} [_ {succ :success :as args}]]
   {:dispatch [(:event succ) succ]}))

(rf/reg-event-db
 :fhir/search-results
 (fn [db [_ {rt :resourceType bundle :data :as args}]]
   (let [idx (reduce (fn [acc res]
                       (assoc acc (:id res) res)) {}
                     (map :resource (:entry bundle)))]
     (-> db
         (assoc-in [(keyword rt)] idx)
         (assoc-in [:original (keyword rt)] idx)))))
