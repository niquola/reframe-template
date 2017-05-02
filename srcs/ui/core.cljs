(ns ui.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [clojure.string :as str]
   [cljsjs.react]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [frames.routing]
   [frames.xhr]
   [frames.debounce]
   [frames.cookies :as cookies]
   [frames.openid :as openid]
   [frames.redirect :as redirect]
   [ui.db]
   ;; [ui.pages.core :as pages]
   [ui.pages :as pages]
   [ui.patients.core]
   [ui.coverage.core]
   [ui.database.core]
   [ui.dashboard.core]
   [ui.user.core]
   [ui.routes :as routes]
   [ui.layout :as layout]
   [ui.fhir :as fhir]))

(def open-id-keys
  {:client-id "646067746089-6ujhvnv1bi8qvd7due8hdp3ob9qtcumv.apps.googleusercontent.com"
   :uri "https://accounts.google.com/o/oauth2/v2/auth"})

;; (def base-url "http://cleoproto.aidbox.io/fhir")
(def base-url "http://cleoproto.aidbox.io/fhir")

;; (def open-id-keys
;;   {:client-id "khI6JcdsQ3dgHMdWJnej0OZjr5DXGWRU"
;;    :uri "https://aidbox.auth0.com/authorize"})


;; this is the root component wich switch pages
;; using current-route key from database
(defn current-page []
  (let [{page :match params :params} @(rf/subscribe [:route-map/current-route])]
    (if page
      (if-let [cmp (get @pages/pages page)]
        [:div [cmp params]]
        [:div.not-found (str "Page not found [" (str page) "]" )])
      [:div.not-found (str "Route not found ")])))

;; this is first event, which should initialize
;; application
;; handler use coefects cookies & openid to check for
;; user in cookies or in location string (after OpenId redirect)

(rf/reg-event-fx
 ::initialize
 [(rf/inject-cofx ::cookies/get :auth)
  (rf/inject-cofx ::openid/jwt :auth)]
 (fn [{jwt :jwt {auth :auth} :cookie :as cofx} _]
   (if (and (nil? jwt) (nil? auth))
     ;; if no user we redirect to openid endpoint
     ;; for SignIn
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
  (mount-root))
