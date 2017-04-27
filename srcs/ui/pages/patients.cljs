(ns ui.pages.patients
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ui.routes :refer [href]]
            [ui.widgets :as wgt]
            [ui.widgets.lookup :as lookup]
            [ui.styles :as styles]
            [clojure.string :as str]
            [cljs.pprint :as pp]
            [re-form.core :as form]
            [clojure.string :as str]))

(rf/reg-event-fx
 :patients/index
 (fn [coef [_ query]]
   {:dispatch [:fhir/search {:resourceType "Patient" :query query}]}))

(defn format-date [x] x)

(defn patients-view [patients]
  (-> (fn [pt]
        {:id (:id pt)
         :name (str/join " " (into  (get-in pt [:name 0 :given])
                                    (get-in pt [:name 0 :family])))
         :temporal (:temporal pt)
         :gender (or (:gender pt) "Unknown")
         :birthDate (format-date (:birthDate pt))})
      (mapv patients)))

(rf/reg-sub-raw
 :patients/index
 (fn [db _] (reaction (patients-view (vals (:Patient @db))))))

(defn index [params]
  (rf/dispatch [:patients/index])
  (let [patients (rf/subscribe [:patients/index])]
    (fn [params]
      [:div.index.container
       [:h3 "Patient list"]
       [:input.form-control {:type "text"
                             :placeholder "Search Patient"
                             :on-key-down (fn [ev]
                                            (when (= 13 (.-which ev))
                                              (rf/dispatch [:patients/index (.. ev -target -value)])))}]
       [:table.table
        [:thead
         [:tr
          [:th "Name"]
          [:th "BirthDate"]
          [:th "Gender"]]]
        [:tbody
         (for [pt @patients]
           [:tr {:key (:id pt)}
            [:td [:a {:href (href :patients (:id pt) :edit)}
                  (:name pt)
                  (when (:temporal pt) "(local)")]]
            [:td (:birthDate pt)]
            [:td (:gender pt)]])]]])))

(rf/reg-sub-raw
 :patients/show
 (fn [db [_ pt-id]]
   (reaction (get-in @db [:Patient pt-id]))))


(defn show-patient [params]
  (rf/dispatch [:fhir/read {:resourceType "Patient" :id (:pt/id params)}])
  (let [pt (rf/subscribe [:patients/show (:pt/id params)])]
    (fn [params]
      [:div.index.container
       [:h3 "Patient " [:a.btn.btn-default {:href (href :patients (:id @pt) :edit)} "Edit"]]
       [wgt/pp @pt]])))

(rf/reg-event-db
 :patient/save
 (fn [db [_ pt-id path value]] db))


(rf/reg-event-fx
 :patient/save
 (fn [{db :db} [_ path]]
   {:dispatch [:fhir/save   {:resource (get-in db path)
                             :success  {:event :patient/saved}}]}))

(rf/reg-event-fx
 :patient/saved
 (fn [coef [_ {path :path}]]
   {:route-map/redirect (href :patients)}))

(rf/reg-event-fx
 :patient/cancel
 (fn [coef [_ path]]
   {:route-map/redirect (href :patients)}))

(defn validate-pt [pt]
  (if (and (str/blank? (get-in pt [:name 0 :family 0]))
           (str/blank? (get-in pt [:name 0 :given 0])))
    {:name [{:given [["Given or Family is required"]]
             :family [["Given or Family is required"]]}]}
    {}))


(defn validate-telecom [tels]
  (reduce
   (fn [acc [idx tel]]
     (if (str/blank? (:system tel))
       (assoc-in acc [idx :system] ["required"])
       acc))
   {} (map-indexed vector tels)))

(defn telecom-form [{:b-pth :base-path pth :path :as opts}]
  (let [add-telecom (fn [ev])
        sub (rf/subscribe [:re-form/data (into b-pth pth)])
        errors (reaction (validate-telecom @sub))]
    (fn [opts]
      [:div.form-group
       [:h3 "Telecom " [:button.btn.btn-primary {:on-click #(rf/dispatch [:re-form/change (into b-pth (into pth [(count @sub)])) {}])} "+"]]
       (doall
        (for [[idx tel] (map-indexed vector @sub)]
          [:div.telecom.row {:key (str idx)}
           [:div.col-md-6 {:class (when (form/has-errors? errors [idx :system]) "has-error")}
            [form/input {:type "text" :placeholder "system" :base-path b-pth :path (into pth [idx :system])}]
            [form/errors-hint errors [idx :system]]]
           [:div.col-md-6
            [form/input {:type "text" :placeholder "value" :base-path b-pth :path (into pth [idx :value])}]]]))])))

(defn patient-form [base-path]
  (let [pt (rf/subscribe [:db/by-path base-path])
        save-fn #(rf/dispatch [:patient/save  base-path])
        cancel-fn #(rf/dispatch [:patient/cancel base-path])
        errors (reaction
                (.log js/console "validate" @pt)
                (validate-pt @pt))]

    (fn [params]
      [:div.index.container
       [:h3 "Update Patient"]
       [:div.form
        [form/row {:path [:name 0 :given 0]
                   :errors errors
                   :label "Given"
                   :base-path base-path
                   :as form/input}]

        [form/row {:path [:name 0 :family 0]
                   :errors errors
                   :label "Family"
                   :base-path base-path
                   :as form/input}]

        ;; example of hand crafted row
        ;; with custom control
        (let [path [:organization]]
          [:div.form-group
           {:class (when (form/has-errors? errors path) "has-error")}
           [:label "Organization"]
           [lookup/lookup {:type "text" :base-path base-path :path path}]
           [form/errors-hint errors path]])

        [telecom-form {:base-path base-path :path [:telecom]}]

        [:hr]
        [:div.form-group
         [form/submit-btn save-fn "Save"]
         " "
         [form/cancel-btn cancel-fn "Cancel"]]

        [:div.debug
         [styles/style [:.debug {:background-color "#f1f1f1" :padding "10px" :border "1px solid #ddd"}]]
         [:b (pr-str base-path)]
         [wgt/pp @pt]]]])))

(defn edit-patient [{pid :pt/id}]
  (let [path [:form :Patient pid]]
    (rf/dispatch [:db/write path {}])
    (rf/dispatch [:fhir/read {:resourceType "Patient"
                              :id pid
                              :into path}])
    (fn [] [patient-form path])))

(defn new-patient [params]
  (let [path [:form :Patient :new]]
    (rf/dispatch [:db/write path {:resourceType "Patient"}])
    (fn [params]
      [patient-form path])))

(def pages {:patients/index index
            :patients/new new-patient
            :patients/edit edit-patient
            :patients/show show-patient})
