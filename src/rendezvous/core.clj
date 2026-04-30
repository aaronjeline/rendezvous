(ns rendezvous.core
  (:require [rendezvous.server :as server]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn parse-args [args]
  (let [args (vec args)
        i    (.indexOf args "-i")]
    (cond
      (= i -1)
      (throw (ex-info "Missing required flag: -i <interface>" {}))

      (>= (inc i) (count args))
      (throw (ex-info "Flag -i requires a value" {}))

      :else
      {:interface (nth args (inc i))})))

(defn -main [& args]
  (let [{:keys [interface]} (parse-args args)
        app                 (server/make-app)]
    (println (str "Starting rendezvous on " interface ":8000"))
    (run-jetty app {:host interface :port 8000 :join? true})))
