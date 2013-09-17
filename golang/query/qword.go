package query

import (
	"errors"
	"fmt"
	z "github.com/nutzam/zgo"
	"regexp"
	"strings"
)

type QCndType int

const (
	Regex QCndType = iota
	String
	IntRegion
	LongRegion
	DateRegion
	StringEnum
	IntEnum
	Json
	Unsupport
)

// 获得字面量
func (qcType QCndType) String() string {
	switch qcType {
	case Regex:
		return "Regex"
	case String:
		return "String"
	case IntRegion:
		return "IntRegion"
	case LongRegion:
		return "LongRegion"
	case DateRegion:
		return "DateRegion"
	case StringEnum:
		return "StringEnum"
	case IntEnum:
		return "IntEnum"
	case Json:
		return "Json"
	}
	return "Unsupport"
}

// 根据字面量获得对应的QCndType
func QCType(tpstr string) (QCndType, error) {
	lowstr := strings.ToLower(tpstr)
	switch lowstr {
	case "regex":
		return Regex, nil
	case "string":
		return String, nil
	case "intregion":
		return IntRegion, nil
	case "longregion":
		return LongRegion, nil
	case "dateregion":
		return DateRegion, nil
	case "stringenum":
		return StringEnum, nil
	case "intenum":
		return IntEnum, nil
	case "json":
		return Json, nil
	}
	// 一个都没找到??
	return Unsupport, errors.New(fmt.Sprintf("invalid qCndType [%s]", tpstr))
}

type QCnd struct {
	Key    string      `json:"key"`
	Origin string      `json:"origin"`
	Plain  string      `json:"plain"`
	Type   QCndType    `json:"type"`
	Value  interface{} `json:"value"`
}

func (qc *QCnd) String() string {
	sb := z.StringBuilder()
	sb.Append("{").EOL()
	sb.Append("    ").Append("key   : ").Append(qc.Key).EOL()
	sb.Append("    ").Append("plain : ").Append(qc.Plain).EOL()
	sb.Append("    ").Append("type  : ").Append(qc.Type.String()).EOL()
	sb.Append("    ").Append("value : ").Append(qc.Value).EOL()
	sb.Append("}")
	return sb.String()
}

type QWordRule struct {
	Key   string         `json:"key"`
	Regex *regexp.Regexp `json:"regex"`
	Type  QCndType       `json:"type"`
	Seg   string         `json:"seg"`
}

type QWord struct {
	Rels     []string         `json:"rels"`
	Cnds     []*QCnd          `json:"cnds"`
	Unmatchs []string         `json:"unmatchs"`
	CndMap   map[string]*QCnd `json:"cndmap"`
}

func (qword *QWord) Get(key string) *QCnd {
	return qword.CndMap[key]
}

func (qword *QWord) IsAllAnd() bool {
	for _, rel := range qword.Rels {
		if rel == "|" {
			return false
		}
	}
	return true
}

func (qword *QWord) IsAllOr() bool {
	for _, rel := range qword.Rels {
		if rel == "&" {
			return false
		}
	}
	return true
}

func (qword *QWord) SetAll(char byte) *QWord {
	var set string
	if char == '&' {
		set = string(char)
	} else {
		set = "|"
	}
	for i := 0; i < len(qword.Rels); i++ {
		qword.Rels[i] = set
	}
	return qword
}

func (qword *QWord) Size() int {
	return len(qword.Cnds)
}

func (qword *QWord) IsEmpty() bool {
	return qword.Size() == 0
}

// 循环变量Cnds
func (qword *QWord) Each(each func(index int, qc *QCnd, prevIsAnd bool)) {
	if !qword.IsEmpty() && each != nil {
		for i, eqc := range qword.Cnds {
			if i > 0 {
				each(i, eqc, qword.Rels[i-1] == "&")
			} else {
				each(i, eqc, false)
			}
		}
	}
}

func (qword *QWord) String() string {
	sb := z.StringBuilder()
	sb.Append("rels is :").Append(qword.Rels).EOL()
	sb.Append("cnds is :\n")
	for _, cnd := range qword.Cnds {
		sb.Append(cnd.String()).EOL()
	}
	if len(qword.Unmatchs) > 0 {
		sb.Append("unmathchs is :\n")
		for _, um := range qword.Unmatchs {
			sb.Append("    " + um).EOL()
		}
	}
	return sb.String()
}
