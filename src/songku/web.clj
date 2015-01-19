(ns songku.web
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [compojure.core :refer [ANY GET defroutes]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [freebase.core :as freebase]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as params]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.middleware.stacktrace :as trace]
            [selmer.parser :as selmer]
            [songku.haiku :as haiku]))


(defn- authenticated? [user pass]
  ;; TODO: heroku config:add REPL_USER=[...] REPL_PASSWORD=[...]
  (= [user pass] [(env :repl-user false) (env :repl-password false)]))


(def get-artist-info
  (memoize
   (fn [artist]
     (if artist
       (freebase/query
        {:name artist
         :type "/music/artist"
         :track []
         :limit 1})
       nil))))


(defn parse-long [s]
  (Long/parseLong s))


(defn haiku-handler [params]
  (let [artist (:artist params)
        artist-info (get-artist-info artist)
        tracks (set (remove nil? (:track artist-info)))
        artist-name (:name artist-info)
        all-haikus (haiku/haikus tracks)
        haikus (if (:all params)
                 all-haikus
                 (take
                  (if-let [n (:n params)]
                    (parse-long n)
                    1)
                  (shuffle all-haikus)))]
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (selmer/render-file
            "haiku.tmpl"
            {:debug (:debug params)
             :artist artist
             :artist-name artist-name
             :tracks (map
                      (fn [track]
                        {:name track
                         :syllables (let [s (haiku/syllables track)]
                                      (if (empty? s)
                                        "Unknown"
                                        (string/join
                                         " or "
                                         (sort (haiku/syllables track)))))})
                      tracks)
             :show-haikus (or (not artist) (not (empty? haikus)))
             :haikus (map vec haikus)})}))


(defroutes app
  (GET "/" {params :params} (haiku-handler params))
  (route/resources "/")
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
