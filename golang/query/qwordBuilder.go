package query

import (
	"bufio"
	"encoding/json"
	"errors"
	"fmt"
	z "github.com/nutzam/zgo"
	"io"
	"regexp"
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
	Key   string         `json:"key"`
	Regex *regexp.Regexp `json:"regex"`
	Type  QCndType       `json:"type"`
	Seg   string         `json:"seg"`
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

// 读取解析规则, 传入一个实现类io.Reader的对象即可
func (qb *QWordBuilder) LoadRules(reader io.Reader) *QWordBuilder {
	bufreader := bufio.NewReader(reader)
	var line string
	var lnum int
	var err error
	var rules []*QWordRule = make([]*QWordRule, 0)
	for true {
		line, err = bufreader.ReadString('\n')
		if err == io.EOF {
			err = nil
			break
		} else if err != nil {
			break
		}
		lnum++
		// 忽略空行与注释行
		line = strings.TrimSpace(line)
		if z.IsBlank(line) || strings.HasPrefix(line, "#") {
			continue
		}
		// FIXME 记得删了
		fmt.Printf("%3d : %s\n", lnum, line)
		// 解析行信息
		if strings.HasPrefix(line, "$") {
			qwRule := new(QWordRule)
			// 获得key
			var fpos int = strings.Index(line, ":")
			if fpos < 0 {
				err = errors.New(fmt.Sprintf("invalid rule line %d : %s", lnum, line))
				break
			}
			qwRule.Key = strings.TrimSpace(line[1:fpos])

			// 获得regex
			var expr string
			if line[fpos+1] == ':' {
				// 简要模式
				expr = "^(" + strings.TrimSpace(line[fpos+2:]) + ")(.*)$"
			} else {
				// 普通模式
				expr = strings.TrimSpace(line[fpos+1:])
			}
			regex, regerr := regexp.Compile(expr)
			if regerr != nil {
				err = regerr
				break
			}
			qwRule.Regex = regex

			// 读取下一行,必须存在哟
			line, err = bufreader.ReadString('\n')
			if err == io.EOF {
				err = errors.New(fmt.Sprintf("rule line(%d) miss next line", lnum))
				break
			} else if err != nil {
				break
			}
			lnum++
			line = strings.TrimSpace(line)
			spos := strings.Index(line, "=")
			if z.IsBlank(line) || !strings.HasPrefix(line, "$") || spos < 0 {
				err = errors.New(fmt.Sprintf("invalid rule line %d : %s", lnum, line))
				break
			}
			// 获得seg
			qwRule.Seg = line[:spos]
			// 获得type
			qcType, qcterr := QCType(strings.TrimSpace(line[spos+1:]))
			if qcterr != nil {
				err = qcterr
				break
			}
			qwRule.Type = qcType
			// 加入到rules
			rules = append(rules, qwRule)
		}
	}
	// 是否有错误
	if err != nil {
		panic(err)
	}
	qb.Rules = rules
	return qb
}

// 解析查询字符串
func (qb *QWordBuilder) Parse(kwd string) *QWord {
	kwd = z.TrimExtraSpace(kwd)
	if len(kwd) == 0 {
		return nil
	}
	qword := new(QWord)

	return qword
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
	// QWordRule
	if len(qb.Rules) > 0 {
		sb.Append(z.AlignLeft("rules", kwidth, ' ')).Append(": ").AppendByte('\n')
		for i := 0; i < len(qb.Rules); i++ {
			rule := qb.Rules[i]
			sb.Append(z.DupChar(' ', kwidth)).Append(fmt.Sprintf(" %2d. {", i))
			sb.Append(z.DupChar(' ', kwidth)).Append(z.DupChar(' ', kwidth)).Append(fmt.Sprint("{")).AppendByte('\n')
			sb.Append(z.DupChar(' ', kwidth+9)).Append(fmt.Sprintf("key    : %s", rule.Key)).AppendByte('\n')
			sb.Append(z.DupChar(' ', kwidth+9)).Append(fmt.Sprintf("regex  : %s", rule.Regex.String())).AppendByte('\n')
			sb.Append(z.DupChar(' ', kwidth+9)).Append(fmt.Sprintf("seg    : %s", rule.Seg)).AppendByte('\n')
			sb.Append(z.DupChar(' ', kwidth+9)).Append(fmt.Sprintf("type   : %v", rule.Type)).AppendByte('\n')
			sb.Append(z.DupChar(' ', kwidth+5)).Append(fmt.Sprint("}")).AppendByte('\n')
		}
	}
	return sb.String()
}
