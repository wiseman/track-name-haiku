(defproject songku "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://songku.herokuapp.com"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [ring/ring-devel "1.2.2"]
                 [environ "0.5.0"]
                 [selmer "0.7.9"]
                 [freebase-clj "0.1.0"]
                 [com.lemonodor/pronouncing "0.0.4"]
                 [org.clojure/math.combinatorics "0.0.8"]
                 [org.apache.lucene/lucene-analyzers-common "4.9.0"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "songku-standalone.jar"
  :profiles {:uberjar {:aot :all}
             :production {:env {:production true}}})
