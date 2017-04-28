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
                 :params (if (and (:query args) (not (str/blank? (:query args))))
                           {:name (:query args)} {})
                 :success {:event :fhir/search-results
                           :resourceType (:resourceType args)}}}))

(rf/reg-event-fx
 :fhir/read
 (fn [coef [_ args]]
   (.log js/console "READ" args)
   {:json/fetch {:uri (str base-url "/" (:resourceType args) "/" (:id args))
                 :success (assoc args :event :fhir/read-results)}}))

(rf/reg-event-db
 :fhir/read-results
 (fn [db [_ args]]
   (let [res (:data args)]
     (if-let [path (:into args)]
       (assoc-in db path res)
       (assoc-in db [(keyword (:resourceType res)) (:id res)] res)))))

(rf/reg-event-fx
 :fhir/save
 (fn [{db :db} [_ {res :resource succ :success :as args}]]
   (when res
     (if-let [id (:id res)]
       {:json/fetch {:uri (str base-url "/" (:resourceType res) "/" (:id res))
                     :method "put"
                     :body res
                     :success {:event :fhir/saved :success succ}}}
       {:json/fetch {:uri (str base-url "/" (:resourceType res))
                     :method "post"
                     :body res
                     :success {:event :fhir/saved :success succ}}}))))

(rf/reg-event-fx
 :fhir/saved
 (fn [{db :db} [_ {succ :success data :data :as args}]]
   {:dispatch [(:event succ) (assoc succ :resource data)]}))

(rf/reg-event-db
 :fhir/search-results
 (fn [db [_ {rt :resourceType bundle :data :as args}]]
   (let [idx (reduce (fn [acc res]
                       (assoc acc (:id res) res)) {}
                     (map :resource (:entry bundle)))]
     (-> db
         (assoc-in [(keyword rt)] idx)
         (assoc-in [:original (keyword rt)] idx)))))
