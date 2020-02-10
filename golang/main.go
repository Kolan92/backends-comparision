package main

import (
	"fmt"
	"net/http"

	"goji.io"
	"goji.io/pat"

	"encoding/json"
	"io/ioutil"
	"log"

	"time"
)

type PersonalInfo struct {
	Name      string
	BirthDate time.Time
}

type BodyInfo struct {
	Height     int
	Weight     int
	MeasuredOn time.Time
}

type test_struct struct {
	Test string
}

func parseBody(req *http.Request, value *interface{}) error {
	body, err := ioutil.ReadAll(req.Body)
	if err != nil {
		return err
	}
	log.Println(string(body))
	err = json.Unmarshal(body, &value)
	return err
}

func hello(w http.ResponseWriter, r *http.Request) {
	name := pat.Param(r, "name")
	fmt.Fprintf(w, "Hello, %s!", name)
}

func main() {
	mux := goji.NewMux()
	mux.HandleFunc(pat.Get("/api/hello/:name"), hello)

	http.ListenAndServe("localhost:8080", mux)
}
