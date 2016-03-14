(ns core.core
  (:import [goog.net XhrIo EventType])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [clojure.browser.repl :as repl]
   [goog.net.XhrIoPool :as gxhr-pool]
   [goog.net.XhrIo :as gxhr]
   [goog.dom :as gdom]
   [goog.events :as gevents]
   [goog.style :as gstyle]
   [cljs.core.async :refer [put! chan <! >! timeout close!] :as async]))

;; (defonce conn
;;   (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(defn build-xhr
  ""
  [out sync]
  (let [xhr (goog.net.XhrIo.)]
    (.setResponseType xhr XhrIo.ResponseType.BLOB)
    (.listen xhr EventType.COMPLETE
             (fn [event]
               (let [url (-> event
                             .-target
                             .getResponse
                             js/window.URL.createObjectURL)]
                 (go (>! out url)
                     (>! sync "next")))))
    xhr))

(defn load-bg
  "Load background"
  [in out]
  (go-loop [sync (chan)
            xhr (build-xhr out sync)]
    (let [url (<! in)]
      (.send xhr url)
      (<! sync))
    (recur sync xhr))
  out)

(defn cascade-update-bg
  "Update background"
  [in sync element]
  (go-loop []
    (let [url (<! in)]
      (gstyle/setStyle element "background-image" (str "url(" url  ")"))
      (<! sync))
    (recur)))


(let [urls (chan)
      sync (chan)
      bg (chan (async/sliding-buffer 1))
      body (gdom/getElement "body")]
  (cascade-update-bg (load-bg urls bg) sync body)
  (gevents/listen body
                  goog.events.EventType.TRANSITIONEND
                  (fn [event] (go (>! sync "next"))))
  (go (>! urls "./img/new-main_robin__last_1-min.png"))
  (go (>! urls "./img/new-main_robin__last_2-min.png"))
  (go (>! urls "./img/new-main_robin__last_3-min.png"))
  (go (>! urls "./img/new-main_robin__last_4-min.png"))
  (go (>! urls "./img/new-main_robin__last_5-min.png")))


