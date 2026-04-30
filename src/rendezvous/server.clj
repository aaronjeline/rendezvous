(ns rendezvous.server
  (:require [cheshire.core :as json]
            [rendezvous.mailbox :as mailbox]))

(defn valid-code? [code]
  (boolean (re-matches #"[a-zA-Z0-9]{6}" code)))

(defn- start-purge-thread [store]
  (doto (Thread. (fn []
                   (while true
                     (Thread/sleep mailbox/ttl-ms)
                     (swap! store mailbox/purge))))
    (.setDaemon true)
    (.start)))

(defn- parse-mailbox-post [{:keys [ip body]}]
  (try
      (let [parsed (json/parse-string (slurp body) true)
            entry {:id (:id parsed) :ip ip :port (:port parsed)}]
        (if (and (:id entry) (:port parsed))
          entry nil))
      (catch com.fasterxml.jackson.core.JsonParseException e
        nil)))

(defn make-app []
  (let [store (atom (mailbox/new-store))]
    (start-purge-thread store)
     (fn [request]
      (let [{:keys [uri request-method remote-addr headers body]} request
            remote-addr (or (get headers "x-forwarded-for") remote-addr)
            [_ code] (re-matches #"/rendezvous/([^/]+)" uri)]
        (cond
          (nil? code)
          {:status 404 :body "Not found"}

          (not (valid-code? code))
          {:status 404 :body "Invalid code"}

          (not remote-addr)
          {:status 500 :body "Could not determine client IP"}

          (= request-method :get)
          (let [entries (mailbox/get-entries @store code)]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string entries)})

          (= request-method :post)
          (let [entry (parse-mailbox-post {:body body :ip remote-addr})]
            (if entry
              (do
                (swap! store mailbox/add-entry code entry)
                (let [entries (mailbox/get-entries @store code)]
                {:status 200
                  :headers {"Content-Type" "application/json"}
                  :body (json/generate-string entries)}))
              {:status 400 :body "Invalid data"}))

          :else
          {:status 405 :body "Method not allowed"})))))

      

