package query

import (
	"bufio"
	"encoding/json"
	"fmt"
	z "github.com/nutzam/zgo"
	"io"
	"os"
	"strings"
)

type QWordBuilder struct {
	GOr          string       `json:"gOr"`
	GAnd         string       `json:"gAnd"`
	SepOr        []string     `json:"sepOr"`
	SepAnd       []string     `json:"sepAnd"`
	QuoteBegin   []string     `json:"quoteBegin"`
	QuoteEnd     []string     `json:"quoteEnd"`
	BracketBegin []string     `json:"bracketBegin"`
	BracketEnd   []string     `json:"bracketEnd"`
	Rules        []*QWordRule `json:"rules"`
}

type QWordRule struct {
}

// 返回一个使用默认参数的qwordBuilder
func QWBuilder() *QWordBuilder {
	return &QWordBuilder{
		"OR",
		"AND",
		[]string{" "},
		[]string{","},
		[]string{"\"", "'"},
		[]string{"\"", "'"},
		[]string{"{", "[", "("},
		[]string{"}", "]", ")"},
		[]*QWordRule{},
	}
}

// 设置qwordBuilder的参数, 使用json格式字符串
// 仅仅可以修改 gOr, gAnd, sepOr, sepAnd 四个参数
func (qb *QWordBuilder) Setup(sjson string) *QWordBuilder {
	setup := new(QWordBuilder)
	err := json.Unmarshal([]byte(sjson), setup)
	if err != nil {
		panic(err)
	}
	qb.GOr = z.SBlank(setup.GOr, qb.GOr)
	qb.GAnd = z.SBlank(setup.GAnd, qb.GAnd)
	if setup.SepOr != nil {
		qb.SepOr = setup.SepOr
	}
	if setup.SepAnd != nil {
		qb.SepAnd = setup.SepAnd
	}
	return qb
}

// 读取解析规则
func (qb *QWordBuilder) LoadRules(rfile *os.File) *QWordBuilder {
	reader := bufio.NewReader(rfile)
	var line string
	var err error
	var lnum int
	for true {
		lnum++
		line, err = reader.ReadString('\n')
		if err == io.EOF {
			break
		} else if err != nil {
			panic(err)
		}
		line = strings.TrimSpace(line)
		// 忽略空行与注释行
		if z.IsBlank(line) || strings.HasPrefix(line, "#") {
			continue
		}
		fmt.Printf("n.%d : %s", lnum, line)
		// 解析行信息
		if strings.HasPrefix(line, "$") {

		}
	}
	return qb
}

// 返回一个qwordBuilder的描述信息
func (qb *QWordBuilder) String() string {
	sb := z.SBuilder()
	kwidth := 15
	sb.Append("QWorldBuilder Setup").AppendByte('\n')
	sb.Append(z.AlignLeft("gOr", kwidth, ' ')).Append(": ").Append(qb.GOr).AppendByte('\n')
	sb.Append(z.AlignLeft("gAnd", kwidth, ' ')).Append(": ").Append(qb.GAnd).AppendByte('\n')
	sb.Append(z.AlignLeft("sepOr", kwidth, ' ')).Append(": ").AppendStringArray(qb.SepOr).AppendByte('\n')
	sb.Append(z.AlignLeft("sepAnd", kwidth, ' ')).Append(": ").AppendStringArray(qb.SepAnd).AppendByte('\n')
	sb.Append(z.AlignLeft("quoteBegin", kwidth, ' ')).Append(": ").AppendStringArray(qb.QuoteBegin).AppendByte('\n')
	sb.Append(z.AlignLeft("quoteEnd", kwidth, ' ')).Append(": ").AppendStringArray(qb.QuoteEnd).AppendByte('\n')
	sb.Append(z.AlignLeft("bracketBegin", kwidth, ' ')).Append(": ").AppendStringArray(qb.BracketBegin).AppendByte('\n')
	sb.Append(z.AlignLeft("bracketEnd", kwidth, ' ')).Append(": ").AppendStringArray(qb.BracketEnd).AppendByte('\n')
	// TODO QWordRule
	return sb.String()
}
