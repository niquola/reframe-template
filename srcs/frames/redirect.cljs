(ns frames.redirect
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(defn page-redirect [url]
  (set! (.-href (.-location js/window)) url))

(rf/reg-fx
 ::page-redirect
 (fn [opts]
   (println "REDIRECT" opts)
   (page-redirect (str (:uri opts)
                       (when-let [params (:params opts)]
                         (->> params
                              (map (fn [[k v]] (str (name k) "=" (js/encodeURIComponent v))))
                              (str/join "&")
                              (str "?")))))))
