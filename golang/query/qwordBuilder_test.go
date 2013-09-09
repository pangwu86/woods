package query_test

import (
	"encoding/json"
	z "github.com/nutzam/zgo"
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

func Test_Before(t *testing.T) {
	if DEBUG {
		z.DebugOn()
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
		"gOr":  "或",
		"gAnd": "与",
		"sepOr" : ["@@", "%%"],
		"sepAnd" : ["##", "$$"]
	}
	`
	qb := query.QWBuilder().Setup(setup)
	if qb.GOr != "或" {
		t.Errorf("gOr is %s", qb.GOr)
	}
	if qb.GAnd != "与" {
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
		# "^a.*"
		$nm : ^([\^].+)$
			${1} = Regex
		#-----------------------------------------------------
		# 像 age 可以这样简写
		$age :: age
		    ${2} = IntRegion
		#---------------------------------------------------------
		# "C(2013-09-10 12:34:15, 2013-09-11 13:45:12)"
		# "C(2013-09-10, 2013-08-23)"
		$ctm :: C
		    ${2} = DateRegion

	`
	reader := strings.NewReader(rulesStr)
	qb := query.QWBuilder()
	qb.LoadRules(reader)
	printQWBuilder(qb, t)
}

func Test_QWBuilder_Parse(t *testing.T) {
	qb := query.QWBuilder().LoadRulesStr(`
		$user  : ^(@)(.*)$
			${2} = String
	`)
	qb.Parse("	@pw		C(2013-09-22, 2015-07-19)")
	printQWBuilder(qb, t)
}
