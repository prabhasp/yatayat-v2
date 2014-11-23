(ns yy.test.handler
  (:use clojure.test
        ring.mock.request
        yy.handler))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (re-find #"Loading" (:body response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
