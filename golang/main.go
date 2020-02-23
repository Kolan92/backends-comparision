package main

import (
	"database/sql"
	"fmt"
	"net/http"

	"goji.io"
	"goji.io/pat"

	"encoding/json"
	"io/ioutil"
	"log"

	"time"

	"sync"

	_ "github.com/lib/pq"
)

const (
	host     = "172.18.0.5"
	port     = 5432
	user     = "postgres"
	password = "postgres"
	dbname   = "testdatabase"
)

type BodyInfo struct {
	Height     int       `json:"height"`
	Weight     int       `json:"weight"`
	MeasuredOn time.Time `json:"measuredOn"`
}

var conn *sql.DB // Set package-wide, but not exported
var once sync.Once

func GetConnection() *sql.DB {
	once.Do(func() {
		var err error
		if conn, err = sql.Open("postgres", connectionString); err != nil {
			log.Panic(err)
		}

	})
	return conn
}

func parseBody(request *http.Request, value *BodyInfo) error {
	body, err := ioutil.ReadAll(request.Body)
	if err != nil {
		return err
	}
	fmt.Printf("%#v\n", body)
	err = json.Unmarshal(body, value)
	fmt.Printf("%#v\n", value)
	return err
}

func hello(w http.ResponseWriter, r *http.Request) {
	name := pat.Param(r, "name")
	fmt.Fprintf(w, "Hello, %s!", name)
}

func saveBodyInfo(w http.ResponseWriter, request *http.Request) {
	bodyInfo := BodyInfo{}
	err := parseBody(request, &bodyInfo)
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	db := GetConnection()

	//defer db.Close()
	//	writeBodyInfoToDb(&bodyInfo, db)
	for i := 0; i < 10; i++ {
		fmt.Printf("%d gorutines!\n", i+1)
		go writeBodyInfoToDb(&bodyInfo, db)
	}

	json, err := json.Marshal(bodyInfo)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.Write(json)
}

func writeBodyInfoToDb(value *BodyInfo, db *sql.DB) {
	err := db.Ping()
	if err != nil {
		panic(err)
	}

	var insertCommand string = "insert into body_info (measured_on, weight_kg, height_cm) values ($1, $2, $3)"

	stmt, err := db.Prepare(insertCommand)
	if err != nil {
		log.Fatal(err)
	}

	res, err := stmt.Exec(value.MeasuredOn, value.Weight, value.Height)
	if err != nil || res == nil {
		log.Fatal(err)
	}
	fmt.Println("Inserted row successfully")
	stmt.Close()

}

var connectionString string

func main() {

	// if len(os.Args) <= 1 {
	// 	log.Fatal("No connection string")
	// }

	fmt.Println("Starting the server!")

	// connectionString := os.Args[1]
	// fmt.Println(connectionString)

	connectionString = fmt.Sprintf("host=%s port=%d user=%s "+
		"password=%s dbname=%s sslmode=disable",
		host, port, user, password, dbname)

	mux := goji.NewMux()
	mux.HandleFunc(pat.Get("/api/hello/:name"), hello)
	mux.HandleFunc(pat.Post("/api/bmi"), saveBodyInfo)

	err := http.ListenAndServe("localhost:8080", mux)
	log.Fatal(err)
}
