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
   {:dispatch [:fhir/search {:resourceType "Patient" :query {:name query}}]}))

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
      [:div.index.container-fluid
       [:input.form-control {:type "text"
                             :placeholder "Search Patient"
                             :on-key-down (fn [ev]
                                            (when (= 13 (.-which ev))
                                              (rf/dispatch [:patients/index (.. ev -target -value)])))}]
       [:br]
       [:table.table
        [:thead
         [:tr
          [:th "Name"]
          [:th "BirthDate"]
          [:th "Gender"]]]
        [:tbody
         (for [pt @patients]
           [:tr {:key (:id pt)}
            [:td [:a {:href (href :patients (:id pt))}
                  (:name pt)
                  (when (:temporal pt) "(local)")]]
            [:td (:birthDate pt)]
            [:td (:gender pt)]])]]])))

(rf/reg-sub-raw
 :patients/show
 (fn [db [_ pt-id]]
   (reaction (get-in @db [:Patient pt-id]))))

(rf/reg-sub-raw
 :patients/current-patient
 (fn [db _]
   (reaction (get @db :current-patient))))

(rf/reg-event-fx
 :patients/current-patient
 (fn [coef [_ phase params]]
   (if (= :init phase)
     {:dispatch  [:fhir/read {:resourceType "Patient" :id (:pt/id params) :into [:current-patient]}]}
     {:dispatch  [:db/write [:current-patient] nil]})))

(defn show-patient [params]
  (let [pt (rf/subscribe [:patients/current-patient])]
    (fn [params]
      [:div.index.container-fluid
       [:h3 "Patient " [:a.btn.btn-default {:href (href :patients (:id @pt) :edit)} "Edit"]]
       [:hr]

       [:img {:src "https://www.smashingmagazine.com/images/graphs-icon-set/graphs.jpg"}]

       [:hr]
       [wgt/pp @pt]])))


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
     (cond-> acc
       (str/blank? (:system tel))
       (assoc-in [idx :system] ["required"])

       (str/blank? (:value tel))
       (assoc-in [idx :value] ["required"])

       (not (contains? #{"fax" "email" "phone" "url" "sms" "pager"} (:system tel)) )
       (assoc-in [idx :system] ["should be one of phone | fax | email | pager | url | sms | other"])

       (not (contains? #{"temp" "work" "home" "old" "mobile"} (:use tel)) )
       (assoc-in [idx :use] ["should be one of home | work | temp | old | mobile"])))

   {} (map-indexed vector tels)))

(defn telecom-form [{b-pth :base-path pth :path :as opts}]
  (let [add-telecom (fn [ev])
        sub (rf/subscribe [:re-form/data (into b-pth pth)])
        base-path (into b-pth pth)
        errors (reaction (validate-telecom @sub))]
    (fn [opts]
      [:div.form-group
       [:h3 "Telecom "
        [:button.btn.btn-primary
         {:on-click #(rf/dispatch [:re-form/change (into b-pth (into pth [(count @sub)])) {}])}
         (wgt/icon :plus)]]
       (doall
        (for [[idx tel] (map-indexed vector @sub)]
          [:div.telecom.row {:key (str idx)}
           [:div.col-md-4
            [form/row {:path [idx :use]
                       :errors errors
                       :label "Use"
                       :base-path base-path
                       :as form/input}]]
           [:div.col-md-4
            [form/row {:path [idx :system]
                       :errors errors
                       :label "System"
                       :base-path base-path
                       :as form/input}]]
           [:div.col-md-4
            [form/row {:path [idx :value]
                       :errors errors
                       :label "Value"
                       :base-path base-path
                       :as form/input}]]]))])))

(defn patient-form [base-path]
  (let [pt (rf/subscribe [:db/by-path base-path])
        save-fn #(rf/dispatch [:patient/save  base-path])
        cancel-fn #(rf/dispatch [:patient/cancel base-path])
        errors (reaction
                (.log js/console "validate" @pt)
                (validate-pt @pt))]

    (fn [params]
      [:div.index.container
       [:div.form
        [:div.row
         [:div.col-md-6
          [form/row {:path [:name 0 :given 0]
                     :errors errors
                     :label "Given"
                     :base-path base-path
                     :as form/input}]]

         [:div.col-md-6
          [form/row {:path [:name 0 :family 0]
                     :errors errors
                     :label "Family"
                     :base-path base-path
                     :as form/input}]]]

        ;; example of hand crafted row
        ;; with custom control
        (let [path [:organization]]
          [:div.form-group
           {:class (when (form/has-errors? errors path) "has-error")}
           [:label "Organization"]
           [lookup/lookup {:type "text" :base-path base-path :path path}]
           [form/errors-hint errors path]])

        [:hr]
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
                              :id pid :into path}])
    (fn [_]
      [:div
       [:h3 "Update patient"]
       [:hr]
       [patient-form path]])))

(defn new-patient [params]
  (let [path [:form :patient :new]]
    (rf/dispatch [:db/write path {:resourcetype "patient"}])
    (fn [_]
      [:div
       [:h3 "register patient"]
       [:hr]
       [patient-form path]])))

(def pages {:patients/index index
            :patients/new new-patient
            :patients/edit edit-patient
            :patients/show show-patient })
