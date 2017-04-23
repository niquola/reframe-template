(ns ui.styles
  (:require [garden.core :as garden]))

(def form-style
  [:body
   [:.form-input.search {:display "block"
                         :border-radius "24px"
                         :width "99%"
                         :line-height "2.6em"
                         :padding "0 21px"
                         :margin-right "21px"
                         :color "black"
                         :border  "1px solid #ddd"}
    [:&:focus {:outline "none"
               :$color :black
               :margin-right "21px"
               :$border [1 :dark-blue-hover]}]]
   [:.form
    [:h2 {:font-weight "normal" :$push-bottom 2}]
    [:h3 {:font-weight "bold" :$text 1 :margin-bottom "19px"  :margin-top "19px"}]

    [:.CodeMirror-gutters {:$bg-color :gray} :border "none"]
    [:.CodeMirror-linenumber {:$color :black}]

    [:.form-row
     {:padding-top "22px" :padding-bottom "11px" :min-height "40px" :position "relative"}

     [:&.checkbox
      {:padding-top 0}
      [:label {:position "static"
               :top "auto"
               :display "inline"
               :user-select "none"}]
      [:input {:display "none" :width "auto"}]
      [:.dash {:display "none"}]
      [:span.xradio
       {:border-radius "50%" :border "#333"}
       [:&:checked {:color "green"}]]
      [:svg {:margin-right "10px" :vertical-align "text-top" :cursor "pointer"}]

      [(keyword "input:not(:checked) ~ label svg.checkbox-unchecked") { :display "inline-block" }]
      [(keyword "input:not(:checked) ~ label svg.checkbox-checked") { :display "none" }]
      [(keyword "input:checked ~ label svg.checkbox-checked") { :display "inline-block" }]
      [(keyword "input:checked ~ label svg.checkbox-unchecked") { :display "none" }]]]

    [:label
     {:font-size "13px"
      :line-height "20px"
      :color "#666"
      :display "block"
      :position "absolute"
      :top 0}]

    [:.error
     [:label :.dash {:color  "red" :border "1px solid red"}]
     [:.hint {:display "none"}] ]

    [:span.check {:border-radius "3px"}
     [:span.checked {:border-radius "3px"}]]

    [:span.radio {:border-radius "50%"}
     [:span.checked {:border-radius "50%"}]]

    [:.radio-btn :.check-box
     [:&:hover [:.radio :.check {:border-color "#5b97de"}
                [:span.checked {:background-color "#5b97de"}]]]
     [:span.radio :span.check
      {:width 13
       :height 13
       :margin "0 5px 0 0"
       :margin-top -1
       :vertical-align "middle";
       :display "inline-block"
       :border "1px solid #888"}

      [:span.checked
       {:width 9
        :height 9
        :display "inline-block"
        :background "#666"
        :margin-top 2
        :margin-left 2}]]]

    [:.check-boxes :.radio-buttons
     [:label {:$push-bottom 0.5 :$text [1 1.5]}]
     [:.check-box :.radio-btn {:margin-top "2px" :margin-bottom "2px"}
      [:&:hover {:cursor "pointer"}]
      [:svg {:vertical-align "top" :margin-right "10px"}] ] ]

    [:.under-ctrl {:margin-top "5px" :$min-height 1}]
    [:.hint :.error-container
     {:font-size "10px" :line-height "14px" :color "#666"}
     [:.error {:font-size "10px" :color "red"}]]

    [:input :textarea :select :.check-boxes-container :.radio-buttons-container
     {:line-height "1.5em"
      :width "100%"
      :background-color :transparent}
     [:&:focus {:outline "none"}]
     [:&.error [:&:focus {}]]
     [:&:disabled {:cursor "not-allowed" :opacity 0.8}]]

    [:.search-input
     {:$border [:solid 1 :border-gray] :border-radius "30px" :$padding 0.5  :margin-left "-10px" :padding-left "14px"}]

    [:.check-boxes-container :.radio-buttons-container :.CodeMirror ]

    [(keyword ".dash")
     {:$border [:top :solid 1 :gray] :width "100%"}]
    [(keyword ".dash")
     {:$border [:bottom :solid 1 :transparent]}]
    [(keyword "input:focus ~ .dash") {:border-color "blue"}]
    [(keyword "input:focus ~ label") {:color "blue"}]

    [(keyword "div.textarea-wrapper.focused ~ .dash") {:border-color "blue"}]
    [(keyword "div.textarea-wrapper.focused ~ label") {:color "blue"}]


    [:textarea {:line-height "1.4"
                :padding-top "10px"
                :resize "vertical"
                :padding-bottom "10px"
                :font-family "Hack, monospace"}]]])

(def basic-style
  (garden/css
   form-style))


