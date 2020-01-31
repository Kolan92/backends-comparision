FROM golang:latest as build-stage
RUN mkdir /app 
ADD . /app/ 
WORKDIR /app 
RUN go mod download
RUN go build -o main . 

FROM ubuntu:latest  
EXPOSE 8080

COPY --from=build-stage /app /app
CMD ["/app/main"]

