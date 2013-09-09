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

type QWordRule struct {
	Key   string         `json:"key"`
	Regex *regexp.Regexp `json:"regex"`
	Type  QCndType       `json:"type"`
	Seg   string         `json:"seg"`
}

type QWord struct {
	Rels     []string `json:"rels"`
	Cnds     []*QCnd  `json:"cnds"`
	Unmatchs []string `json:"unmatchs"`
}

func (qword *QWord) String() string {
	// TODO
	sb := z.SBuilder()
	return sb.String()
}

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

// 读取解析规则字符串
func (qb *QWordBuilder) LoadRulesStr(str string) *QWordBuilder {
	return qb.LoadRules(strings.NewReader(str))
}

// 读取解析规则, 传入一个实现了io.Reader的对象即可
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
		z.DebugPrintf("rule %2d : %s", lnum, line)
		// 忽略空行与注释行
		line = strings.TrimSpace(line)
		if z.IsBlank(line) || strings.HasPrefix(line, "#") {
			continue
		}
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
			z.DebugPrintf("rule %2d : %s", lnum, line)
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
	isGOr, isGAnd, flds, seps := qb.extractFldsAndSeps(kwd)
	// 解析为QWord
	qword := new(QWord)
	sseps := len(seps)
	for i, fld := range flds {
		// 分隔符
		if i < sseps {
			if isGOr {
				qword.Rels = append(qword.Rels, "|")
			} else if isGAnd {
				qword.Rels = append(qword.Rels, "&")
			}
		}
		// qcnd
		fmt.Println(fld)
	}
	return qword
}

// 提取查询语句与连接符
func (qb *QWordBuilder) extractFldsAndSeps(kwd string) (isGOr, isGAnd bool, flds []string, seps []byte) {
	// 判断全局参数 (先强制认为 'xxx:')
	// FIXME 后面改成正则匹配, 可以多加空格
	isGOr = strings.HasPrefix(kwd, qb.GOr+":")
	isGAnd = strings.HasPrefix(kwd, qb.GAnd+":")
	if isGOr || isGAnd {
		kwd = kwd[strings.Index(kwd, ":")+1:]
	}
	// 分割查询字符串
	flds = make([]string, 0, 5)
	seps = make([]byte, 0, 5)
	kszie := len(kwd)
	fld := z.SBuilder()
	for i := 0; i < kszie; i++ {
		c := kwd[i]
		z.DebugPrintf("extract kwd %3d -> [%s]\n", i, string(c))
		// 如果是引用
		if pos := z.IndexOfStrings(qb.QuoteBegin, string(c)); pos > 0 {
			// 这里面的空格啥的就不会被跳过了, 会一直取到引用结束, 逃逸字符会调整一下
			for i++; i < kszie; i++ {
				c = kwd[i]
				if string(c) == qb.QuoteEnd[pos] {
					break
				} else if c == '\\' {
					// FIXME 这里没有做防守
					i++
					fld.AppendByte(kwd[i])
				} else {
					fld.AppendByte(c)
				}
			}
			continue
		}
		// 如果是括弧
		if z.IsInStrings(qb.BracketBegin, string(c)) {
			fld.AppendByte(c)
			for i++; i < kszie; i++ {
				c = kwd[i]
				fld.AppendByte(c)
				if z.IsInStrings(qb.BracketEnd, string(c)) {
					break
				}
			}
			continue
		}
		// 如果是分隔符
		if z.IsInStrings(qb.SepAnd, string(c)) || z.IsInStrings(qb.SepOr, string(c)) {
			cfld := z.Trim(fld.String())
			if len(cfld) > 0 {
				flds = append(flds, cfld)
				seps = append(seps, c)
				fld = z.SBuilder()
			}
			continue
		}
		// 没什么特殊情况,那就加入到sb
		fld.AppendByte(c)
	}
	// 加入最后一个条件
	lfld := z.Trim(fld.String())
	if len(lfld) > 0 {
		flds = append(flds, lfld)
	}
	// 打印一下
	if z.IsDebugOn() && len(flds) > 0 {
		z.DebugPrintf("kwd : %s\n", kwd)
		for _, f := range flds {
			z.DebugPrintf("fld : %s\n", f)
		}
		for _, s := range seps {
			z.DebugPrintf("sep : '%s'\n", string(s))
		}
	}
	return isGOr, isGAnd, flds, seps
}
