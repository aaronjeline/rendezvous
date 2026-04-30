(ns rendezvous.server-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [rendezvous.server :as server]))

(defn parse-json [body]
  (json/parse-string (if (string? body) body (slurp body)) true))

(deftest get-empty-mailbox
  (testing "GET on a fresh mailbox returns an empty list"
    (let [app      (server/make-app)
          response (app (mock/request :get "/rendezvous/abc123"))]
      (is (= 200 (:status response)))
      (is (= [] (parse-json (:body response)))))))

(deftest post-and-get
  (testing "POST adds an entry; subsequent GET returns it"
    (let [app      (server/make-app)
          post-req (-> (mock/request :post "/rendezvous/abc123")
                       (mock/content-type "application/json")
                       (mock/body (json/generate-string {:id "peer1" :port 1337})))
          post-res (app post-req)
          get-res  (app (mock/request :get "/rendezvous/abc123"))]
      (is (= 200 (:status post-res)))
      (is (= 200 (:status get-res)))
      (let [entries (parse-json (:body get-res))]
        (is (= 1 (count entries)))
        (is (= "peer1" (:id (first entries))))
        ;; server should record observed IP, not accept it from body
        (is (string? (:ip (first entries))))
        (is (integer? (:port (first entries))))))))

(deftest post-uses-observed-port
  (testing "the port recorded is the remote port from the request"
    (let [app      (server/make-app)
          post-req (-> (mock/request :post "/rendezvous/abc123")
                       (mock/content-type "application/json")
                       (mock/body (json/generate-string {:id "peer1" :port 1337}))
                       (assoc :remote-port 51234))
          _        (app post-req)
          entries  (parse-json (:body (app (mock/request :get "/rendezvous/abc123"))))]
      (is (= 1337 (:port (first entries)))))))

(deftest post-uses-observed-ip
  (testing "the ip recorded is the remote address from the request, not from the body"
    (let [app      (server/make-app)
          post-req (-> (mock/request :post "/rendezvous/abc123")
                       (mock/content-type "application/json")
                       (mock/body (json/generate-string {:id "peer1" :port 1337}))
                       (assoc :remote-addr "10.0.0.1"))
          _        (app post-req)
          entries  (parse-json (:body (app (mock/request :get "/rendezvous/abc123"))))]
      (is (= "10.0.0.1" (:ip (first entries)))))))

(deftest invalid-mailbox-code
  (testing "mailbox codes must be exactly 6 alphanumeric characters"
    (let [app (server/make-app)]
      (is (= 404 (:status (app (mock/request :get "/rendezvous/short")))))
      (is (= 404 (:status (app (mock/request :get "/rendezvous/toolongcode")))))
      (is (= 404 (:status (app (mock/request :get "/rendezvous/has!!!!"))))))))
