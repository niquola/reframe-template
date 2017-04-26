(ns re-form.core 
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))

(rf/reg-event-db
 :re-form/change
 (fn [db [_ path value]]
   (assoc-in db path value)))

(rf/reg-sub-raw
 :re-form/data
 (fn [db [_ path]] (reaction (get-in @db path))))


(defn input [{b-pth :base-path pth :path :as props}]
  (let [sub (rf/subscribe [:re-form/data (into b-pth pth)])]
    (fn []
      (let [on-change (fn [ev]
                        (rf/dispatch [:re-form/change (into b-pth pth) (.. ev -target -value)]))]
        (fn [props]
          [:input.form-control {:type "text" :value @sub  :on-change on-change}])))))

(defn form [{path :path validate :validate :as opts} body]
  (.log js/console "INIT form" opts)
  (let [on-submit (fn [ev]
                    (.log js/console "SUBMIT" opts)
                    (.preventDefault ev)
                    (when-let [f (:on-submit opts)] (f)))]
    (fn []
      [:form.form (merge (dissoc opts :validate :path) {:on-submit on-submit})
       (into [:div body])])))
