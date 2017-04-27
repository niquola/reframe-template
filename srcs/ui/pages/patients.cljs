(ns ui.pages.patients
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ui.routes :refer [href]]
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

(defn show-data [data]
  [:pre (.stringify js/JSON (clj->js data) nil " ")])

(defn show-patient [params]
  (rf/dispatch [:fhir/read {:resourceType "Patient" :id (:pt/id params)}])
  (let [pt (rf/subscribe [:patients/show (:pt/id params)])]
    (fn [params]
      [:div.index.container
       [:h3 "Patient " [:a.btn.btn-default {:href (href :patients (:id @pt) :edit)} "Edit"]]
       [show-data @pt]])))

(rf/reg-event-db
 :patient/save
 (fn [db [_ pt-id path value]] db))


(rf/reg-event-fx
 :patient/save
 (fn [coef [_ path]]
   {:dispatch [:fhir/update {:path path
                             :success {:event :patient/saved
                                       :path path}}]}))

(rf/reg-event-fx
 :patient/saved
 (fn [coef [_ {path :path}]]
   (.log js/console "Saved")
   {:route-map/redirect (href :patients)}))

(rf/reg-event-fx
 :patient/reset
 (fn [coef [_ path]]
   (.log js/console "Cancel" path)
   {:dispatch [:fhir/reset path]
    :route-map/redirect (href :patients)}))

(defn validate-pt [pt]
  (if (and (str/blank? (get-in pt [:name 0 :family 0]))
           (str/blank? (get-in pt [:name 0 :given 0])))
    {:name [{:given [["Given or Family is required"]]
             :family [["Given or Family is required"]]}]}
    {}))

(rf/reg-event-fx
 :organization/search
 (fn [coef [_ query]]
   (.log js/console "Search org" query)
   {:db (assoc (:db coef) :organization/search-results
               (if (str/blank? query) []
                   [{:name query}
                    {:name (str query "o")}
                    {:name (str query "oo")}
                    {:name (str query "ooo")}]))}))

(rf/reg-sub-raw
 :organization/search-results
 (fn [db _]
   (reaction (get @db :organization/search-results))))

(defn lookup [{b-pth :base-path pth :path}]
  (let [orgs (rf/subscribe [:organization/search-results])
        sub (rf/subscribe [:re-form/data (into b-pth pth)])]
    (fn []
      [:div
       [:span "Current orgnanization" (pr-str @sub)]

       [styles/style
        [:.item {:cursor "pointer"
                 :padding (styles/px 5)
                 :border-bottom (styles/color :border)}

         [:&:hover {:background-color (styles/color :hover-background)
                    :color (styles/color :hover-color)}]]]

       [:input.form-control {:placeholder "Search Organization"
                             :on-change (fn [ev] (rf/dispatch [:organization/search (.. ev -target -value)]))}]
       [:div.results
        (for [o @orgs]
          [:div.item {:key (:name o) :on-click #(rf/dispatch [:re-form/change (into b-pth pth) o])}
           (:name o)])]])))

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


(defn patient-form [pt-id]
  (let [pt (rf/subscribe [:patients/show pt-id])
        base-path [:Patient pt-id]
        save-fn #(rf/dispatch [:patient/save  base-path])
        cancel-fn #(rf/dispatch [:patient/reset base-path])
        errors (reaction (validate-pt @pt))]

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
           [lookup {:type "text" :base-path base-path :path path}]
           [form/errors-hint errors path]])

        [telecom-form {:base-path base-path :path [:telecom]}]

        [:div.form-group
         [form/submit-btn save-fn "Save"]
         " "
         [form/cancel-btn cancel-fn "Save"]]

        [show-data @pt]]])))

(defn edit-patient [params]
  (rf/dispatch [:patients/index])
  (rf/dispatch [:fhir/read {:resourceType "Patient" :id (:pt/id params)}])
  (fn [] [patient-form (:pt/id params)]))

(rf/reg-event-db
 :patients/init
 (fn [db [_ pt]]
   (assoc-in db [:Patient (:id pt)] pt)))

(defn new-patient [params]
  (let [tmp-id (str (gensym))]
    (rf/dispatch [:patients/init {:id tmp-id
                                  :temporal true
                                  :telecom []
                                  :resourceType "Patient"
                                  :name [{:given [] :family []}]}])
    (fn [params]
      (fn [] [patient-form tmp-id]))))

(def pages {:patients/index index
            :patients/new new-patient
            :patients/edit edit-patient
            :patients/show show-patient})
