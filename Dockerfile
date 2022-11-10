FROM clojure:temurin-19-tools-deps

WORKDIR /app
COPY . ./

RUN clojure -P
RUN clojure -A:build -P
RUN clojure -T:build jar > ./ENV_CLASSPATH
RUN find ./target -type f -name "*.jar" > ./ENV_JAR

CMD java -cp $(cat ./ENV_CLASSPATH):$(cat ./ENV_JAR) uniradar_backend.main