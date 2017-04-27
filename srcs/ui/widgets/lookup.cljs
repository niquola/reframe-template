(ns ui.widgets.lookup
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ui.routes :refer [href]]
            [ui.styles :as styles]
            [clojure.string :as str]
            [cljs.pprint :as pp]
            [re-form.core :as form]
            [clojure.string :as str]))

(def base-url "http://cleoproto.aidbox.io/$terminology/$npi/Organization")


(rf/reg-event-fx
 :organization/search
 (fn [coef [_ query]]
   {:dispatch-debounce {:key :organization/search
                        :event [:organization/search-do query] 
                        :delay 400}}))

(rf/reg-event-fx
 :organization/search-do
 (fn [coef [_ query]]
   {:json/fetch {:uri base-url
                 :params {:_mask "%7B%22name%22%3Atrue%2C%22id%22%3Atrue%2C%22extension%22%3Atrue%7D"
                          :name query
                          :_extended true}
                 :success {:event :organization/search-results}}}))

(rf/reg-event-db
 :organization/search-results
 (fn [db [_ result]]
   (assoc db :organization/search-results
          (mapv :resource (get-in result [:data :entry])))))

(rf/reg-sub-raw
 :organization/search-results
 (fn [db _]
   (reaction (get @db :organization/search-results))))

(rf/reg-event-fx
 :organization/selection
 (fn [{db :db} [_ path org]]
   {:dispatch [:re-form/change path 
               {:reference (str "Organization/" (:id org))
                :text (:name org)}]
    :db (assoc db :organization/search-results [])}))

(defn lookup [{b-pth :base-path pth :path}]
  (let [orgs (rf/subscribe [:organization/search-results])
        sub (rf/subscribe [:re-form/data (into b-pth pth)])]
    (fn []
      [:div
       [:b (:text @sub)]
       [styles/style
        [:.item {:cursor "pointer"
                 :padding (styles/px 5)
                 :border-bottom (styles/color :border)}

         [:&:hover {:background-color (styles/color :hover-background)
                    :color (styles/color :hover-color)}]
         [:.results {}]]]

       [:input.form-control {:placeholder "Search Organization"
                             :on-change (fn [ev] (rf/dispatch [:organization/search (.. ev -target -value)]))}]
       [:div.results
        (for [o @orgs]
          [:div.item {:key (:id o) :on-click #(rf/dispatch [:organization/selection (into b-pth pth) o])}
           (:name o)])]])))
