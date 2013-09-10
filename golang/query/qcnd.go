package query

import (
	"errors"
	"fmt"
	z "github.com/nutzam/zgo"
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
	sb := z.SBuilder()
	sb.Append("{").EndLine()
	sb.Append("    ").Append("key   : ").Append(qc.Key).EndLine()
	sb.Append("    ").Append("plain : ").Append(qc.Plain).EndLine()
	sb.Append("    ").Append("type  : ").Append(qc.Type.String()).EndLine()
	sb.Append("    ").Append("value : ").Append(qc.Value).EndLine()
	sb.Append("}")
	return sb.String()
}
