package query_test

import (
	"encoding/json"
	z "github.com/nutzam/zgo"
	"github.com/pangwu86/woods/golang/query"
	"strings"
	"testing"
)

const DEBUG = true

func printQWBuilder(qb *query.QWBuilder, t *testing.T) {
	if DEBUG {
		t.Log(qb.String())
	}
}

func Test_Before(t *testing.T) {
	if DEBUG {
		z.DebugOn()
	}
}

func Test_QWordBuilder(t *testing.T) {
	qb := query.QWordBuilder()
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

func Test_QWordBuilder_Setup(t *testing.T) {
	setup := `
	{
		"gOr":  "或",
		"gAnd": "与",
		"sepOr" : ["@@", "%%"],
		"sepAnd" : ["##", "$$"]
	}
	`
	qb := query.QWordBuilder().Setup(setup)
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

func Test_QWordBuilder_LoadRules(t *testing.T) {
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
	qb := query.QWordBuilder()
	qb.LoadRules(reader)
	printQWBuilder(qb, t)
}

func Test_QWordBuilder_Parse(t *testing.T) {
	qb := query.QWordBuilder().LoadRulesStr(`
		$user  : ^(@)(.*)$
			${2} = String
		$ctm   :: C
			${2} = DateRegion
		$test :: T
		    $(2) = IntEnum
	`)
	qword := qb.Parse("	@pw		C(2013-09-22, 2015-07-19)  T(12,33,55,67)")
	z.DebugPrint(qword)
}

func Test_QWordBuilder_Parse2(t *testing.T) {
	qb := query.QWordBuilder(strings.NewReader(`
		$user  : ^(@)(.*)$
			${2} = String
		$test :: S
		    $(2) = StringEnum
	`))
	qword := qb.Parse("	@zozoh C(2013-09-22, 2015-07-19)  S(abc,hha, erer, jdgg gdg)")
	z.DebugPrint(qword)
}

func Test_QWord_Each(t *testing.T) {
	qb := query.QWordBuilder(strings.NewReader(`
		$user  : ^(@)(.*)$
			${2} = String
		$test :: S
		    $(2) = StringEnum
	`))
	qword := qb.Parse("@zozoh ,     S(abc,hha, erer, jdgg gdg)")
	qword.Each(func(index int, qc *query.QCnd, prevIsAnd bool) {
		z.DebugPrintf("no.%d prevIsAnd.%v \n", index, prevIsAnd)
		z.DebugPrintln(qc.String())
	})
}

func Test_QWord_Region(t *testing.T) {
	qb := query.QWordBuilder(strings.NewReader(`
		$ct  :: C
			${2} = IntRegion
		$st  :: S
			${2} = IntRegion
		$usr : ^(@)([a-zA-Z]+)$
			${2} = String
	`))
	qword := qb.Parse("C[10,15], S(12,) , @pw")
	qword.Each(func(index int, qc *query.QCnd, prevIsAnd bool) {
		z.DebugPrintf("no.%d prevIsAnd.%v \n", index, prevIsAnd)
		z.DebugPrintln(qc.String())
	})
	qc := qword.Get("usr")
	if qc == nil {
		t.Error("can't find qcnd by key")
	}
}
