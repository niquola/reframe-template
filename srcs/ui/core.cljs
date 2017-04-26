(ns ui.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [clojure.string :as str]
   [cljsjs.react]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-frisk.core :refer [enable-re-frisk!]]
   [frames.routing]
   [frames.cookeis :as cookies]
   [frames.openid :as openid]
   [frames.redirect :as redirect]
   [ui.pages.core :as pages]
   [ui.routes :as routes]
   [ui.layout :as layout]
   [ui.fhir :as fhir]
   [devtools.core :as devtools]))

(devtools/install!)

;; (def open-id-keys
;;   {:client-id "646067746089-6ujhvnv1bi8qvd7due8hdp3ob9qtcumv.apps.googleusercontent.com"
;;    :uri "https://accounts.google.com/o/oauth2/v2/auth"})

(def open-id-keys
  {:client-id "khI6JcdsQ3dgHMdWJnej0OZjr5DXGWRU"
   :uri "https://aidbox.auth0.com/authorize"})

(defn current-page []
  (let [{page :match params :params} @(rf/subscribe [:route-map/current-route])]
    (if page
      (if-let [cmp (get pages/pages page)]
        [:div [cmp params]]
        [:div.not-found (str "Page not found [" (str page) "]" )])
      [:div.not-found (str "Route not found ")])))

(rf/reg-event-fx
 ::initialize
 [(rf/inject-cofx ::cookies/get :auth)
  (rf/inject-cofx ::openid/jwt :auth)]
 (fn [{jwt :jwt {auth :auth} :cookie :as cofx} _]
   (if (and (nil? jwt) (nil? auth))
     {::redirect/page-redirect
      {:uri (:uri open-id-keys)
       :params {:redirect_uri (first (str/split (.. js/window -location -href) #"#"))
                :client_id (:client-id open-id-keys) 
                :scope "openid profile email"
                :nonce "ups"
                :response_type "id_token"}}}
     {:dispatch [:route-map/init routes/routes]
      ::cookies/set {:key :auth :value (or jwt auth)}
      :db           (merge (:db cofx) {:auth (or jwt auth)})})))


(defn- mount-root []
  (reagent/render
   [layout/layout [current-page]]
   (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch [::initialize])
  (enable-re-frisk!)
  (mount-root))
