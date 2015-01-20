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


;; Looks up artist info in freebase using MQL.
(def %get-artist-info
  (memoize
   (fn [artist]
     (if artist
       (let [base-args {:name artist
                        :type "/music/artist"
                        :track []
                        :limit 1}
             args (if-let [api-key (env :freebase-api-key)]
                    (assoc base-args :key api-key)
                    base-args)]
         (freebase/query args))
       nil))))

(defn get-artist-info [artist]
  (try
    (%get-artist-info artist)
    (catch clojure.lang.ExceptionInfo e
      (binding [*out* *err*]
        (println "Got exception while trying to get artist info")
        (println e)
        (println (.getStackTrace e)))
      :error)))


(defn parse-long [s]
  (Long/parseLong s))


;; Freebase seems to return results in HTML, sometimes.  Instead of
;; doing full entity resolution, we'll just do search-and-replace to
;; handle the one case we see.
(defn fix-html-entities [name]
  (string/replace name #"&amp;" "&"))


(def try-again-later
  (str "Sorry, I think we hit our freebase API quota.  Try again in a minute "
       "or two."))


(defn haiku-handler [params]
  (let [artist (:artist params)
        artist-info (get-artist-info artist)
        tracks (set (map fix-html-entities (remove nil? (:track artist-info))))
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
             :error-message (if (= artist-info :error)
                              try-again-later
                              nil)
             :show-haikus (or (= artist-info :error)
                              (not artist)
                              (not (empty? haikus)))
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
    (println "Trying to start on port" port "env PORT=" (env :port))
    (jetty/run-jetty (wrap-app #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
