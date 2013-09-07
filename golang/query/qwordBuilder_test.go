package query_test

import (
	"encoding/json"
	"github.com/pangwu86/woods/golang/query"
	"strings"
	"testing"
)

const DEBUG = true

func printQWBuilder(qb *query.QWordBuilder, t *testing.T) {
	if DEBUG {
		t.Log(qb.String())
	}
}

func Test_QWBuilder(t *testing.T) {
	qb := query.QWBuilder()
	if qb.GOr != "OR" {
		t.Errorf("gOr is %s", qb.GOr)
	}
	if qb.GAnd != "AND" {
		t.Errorf("gAnd is %s", qb.GAnd)
	}
	_, err := json.Marshal(qb)
	if err != nil {
		t.Error(err)
	}
	printQWBuilder(qb, t)
}

func Test_QWBuilder_Setup(t *testing.T) {
	setup := `
	{
		"gOr":  "||",
		"gAnd": "&&",
		"sepOr" : ["@@", "%%"],
		"sepAnd" : ["##", "$$"]
	}
	`
	qb := query.QWBuilder().Setup(setup)
	if qb.GOr != "||" {
		t.Errorf("gOr is %s", qb.GOr)
	}
	if qb.GAnd != "&&" {
		t.Errorf("gAnd is %s", qb.GAnd)
	}
	if len(qb.SepOr) != 2 || qb.SepOr[0] != "@@" {
		t.Error("sepOr is ", qb.SepOr)
	}
	if len(qb.SepAnd) != 2 || qb.SepAnd[1] != "$$" {
		t.Error("sepAnd is ", qb.SepAnd)
	}
	printQWBuilder(qb, t)
}

func Test_QWBuilder_LoadRules(t *testing.T) {
	rulesStr := `
		# 测试
		#--------------
		# "@zozoh"
		$user  : ^(@)([a-z]+[a-z0-9]{3,})$
			${2} = String
		#---------------------------------------------------------
		# "@A:zozoh"     # admin
		$admin : ^(@A:)([a-z]+[a-z0-9]{3,})$
			${2} = String
	`
	reader := strings.NewReader(rulesStr)
	qb := query.QWBuilder()
	qb.LoadRules(reader)
	printQWBuilder(qb, t)
}
