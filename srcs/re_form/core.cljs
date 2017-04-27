(ns re-form.core 
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))

(defn insert-by-path [m [k & ks :as path] value]
  (if ks
    (if (int? k)
      (assoc (or m []) k (insert-by-path (get m k) ks value))
      (assoc (or m {}) k (insert-by-path (get m k) ks value)))
    (if (int? k)
      (assoc (or m []) k value)
      (assoc (or m {}) k value))))

;; TODO: use db/write
;; TODO: use db/write
(rf/reg-event-db
 :re-form/change
 (fn [db [_ path value]]
   (.log js/console "ups")
   (insert-by-path db path value)))

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

(defn has-errors? [errors path]
  (not (empty? (get-in @errors path))))

(defn errors-hint [errors path]
  (when-let [e (get-in @errors path)]
    [:div.control-errors (str/join ";" e)]))

(defn row [{errors :errors lbl :label base-path  :base-path path :path input :as :as opts}]
  [:div.form-group
   {:class (when (has-errors? errors path) "has-error")}
   (when lbl [:label lbl])
   [input {:type "text" :base-path base-path :path path}]

   [errors-hint errors path]])

(defn submit-btn [submit-fn title]
  [:button.btn.btn-primary {:type "submit" :on-click submit-fn} title])

(defn cancel-btn [submit-fn title]
  [:button.btn.btn-secondary {:type "submit" :on-click submit-fn} title])
