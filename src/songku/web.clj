(ns songku.web
  (:require [cemerick.drawbridge :as drawbridge]
            [clojure.java.io :as io]
            [compojure.core :refer [ANY GET defroutes]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [freebase.core :as freebase]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [ring.middleware.params :as params]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.middleware.stacktrace :as trace]
            [selmer.parser :as selmer]
            [songku.haiku :as haiku]))


(defn- authenticated? [user pass]
  ;; TODO: heroku config:add REPL_USER=[...] REPL_PASSWORD=[...]
  (= [user pass] [(env :repl-user false) (env :repl-password false)]))

(def ^:private drawbridge
  (-> (drawbridge/ring-handler)
      (session/wrap-session)
      (basic/wrap-basic-authentication authenticated?)))


(defn artist-tracks [artist]
  (set
   (:track
    (freebase/query
     {:name artist
      :type "/music/artist"
      :track []
      :limit 1}))))




(defn haiku-handler [artist]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (selmer/render-file
          "haiku.tmpl"
          {:artist artist
           :tracks (map (fn [name]
                          {:name name
                           :syllables (haiku/syllables name)})
                        (if (not artist)
                          nil
                          (artist-tracks artist)))})})


(defroutes app
  (ANY "/repl" {:as req}
       (drawbridge req))
  (GET "/" {params :params} (haiku-handler (:artist params)))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(defn wrap-app [app]
  ;; TODO: heroku config:add SESSION_SECRET=$RANDOM_16_CHARS
  (let [store (cookie/cookie-store {:key (env :session-secret)})]
    (-> app
        params/wrap-params
        ((if (env :production)
           wrap-error-page
           trace/wrap-stacktrace))
        (site {:session {:store store}}))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (wrap-app #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
