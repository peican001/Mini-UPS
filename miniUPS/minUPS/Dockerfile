FROM gradle:7.6.1-jdk17-alpine
RUN apk add --update --no-cache protobuf
WORKDIR /app
ADD ./ ./
RUN chmod +x start.sh
CMD ["./start.sh"]