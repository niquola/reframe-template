(ns ui.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [clojure.string :as str]
   [cljsjs.react]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [frames.routing]
   [frames.xhr]
   [frames.cookies :as cookies]
   [frames.openid :as openid]
   [frames.redirect :as redirect]
   [ui.db]
   [ui.pages :as pages]
   [ui.dashboard.core]
   [ui.patients.core]
   [ui.routes :as routes]
   [ui.layout :as layout]))

(def open-id-keys
  {:client-id "sansara"
   :uri "https://sansara.health-samurai.io/oauth2/authorize"})

;;patient@com.com
;;patient

(def base-url "https://sansara.health-samurai.io/")

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
   {:dispatch [:route-map/init routes/routes]
    ::cookies/set {:key :auth :value (or jwt auth)}
    :db           (merge (:db cofx) {:base-url base-url})}))


(defn- mount-root []
  (reagent/render
   [layout/layout [current-page]]
   (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch [::initialize])
  (mount-root))
