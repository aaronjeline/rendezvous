FROM clojure:latest

RUN mkdir /opt/rendezvous

COPY . /opt/rendezvous

WORKDIR /opt/rendezvous

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/clj", "-M:run", "-i", "0.0.0.0"]

