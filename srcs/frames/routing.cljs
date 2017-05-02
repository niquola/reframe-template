(ns frames.routing
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [clojure.set :as set]
            [route-map.core :as route-map]))

(defn dispatch-routes [_]
  (let [fragment (.. js/window -location -hash)]
    (rf/dispatch [:fragment-changed fragment])))

(rf/reg-sub-raw
 :route-map/breadcrumbs
 (fn [db _] (reaction (:route-map/breadcrumbs @db))))
(rf/reg-sub-raw
 :route-map/current-route
 (fn [db _] (reaction (:route-map/current-route @db))))

(defn contexts-diff [old-contexts new-contexts params old-params]
  (let [n-idx (into #{} new-contexts)
        o-idx (into #{} old-contexts)
        to-dispose (set/difference o-idx n-idx)]
    (concat
     (mapv (fn [x] [x :init params]) new-contexts)
     (mapv (fn [x] [x :deinit old-params]) to-dispose))))

(defn mk-breadcrumbs [route]
  (->> (:parents route)
       (reduce (fn [acc v]
                 (let [prev (last acc)
                       uri (:uri prev)
                       p  (last (for [[k route] prev :when (= (:. route) (:. v))] k))
                       p (or (get-in route [:params (first p)]) p) ]
                   (conj acc (assoc v :uri (str uri p "/")))))
               [])
       (filter :breadcrumb)
       (mapv #(select-keys % [:uri :breadcrumb]))))

(rf/reg-event-fx
 :fragment-changed
 (fn [{db :db} [k fragment]]
   (if-let [route (route-map/match [:. (str/replace fragment #"^#" "")] (:route-map/routes db))]
     (let [contexts (->> (:parents route) (mapv :context) (filterv identity))
           breadcrumbs (mk-breadcrumbs route)
           dispose-context (or  [])]
       {:db (assoc db :fragment fragment
                   :route/context contexts
                   :route-map/breadcrumbs breadcrumbs
                   :route-map/current-route route)
        :dispatch-n (contexts-diff (:route/context db)
                                   contexts
                                   (:params route)
                                   (get-in db [:route-map/current-route :params]))})
     {:db (assoc db :fragment fragment :route-map/current-route nil)})))

(rf/reg-event-fx
 :route-map/init
 (fn [cofx [_ routes]]
   {:db (assoc (:db cofx) :route-map/routes routes)
    :history {}}))

(rf/reg-fx
 :history
 (fn [_]
   (aset js/window "onhashchange" dispatch-routes)
   (dispatch-routes nil)))

(rf/reg-fx
 :route-map/redirect
 (fn [href]
   (.log js/console "REDIRECT FX" href)
   (aset (.-location js/window) "hash" href)))
