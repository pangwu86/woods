package query

import (
	"bufio"
	"encoding/json"
	"errors"
	"fmt"
	z "github.com/nutzam/zgo"
	"io"
	"regexp"
	"strconv"
	"strings"
)

type QWBuilder struct {
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
func (qb *QWBuilder) String() string {
	sb := z.StringBuilder()
	kwidth := 15
	sb.Append("QWorldBuilder Setup").EOL()
	sb.Append(z.AlignLeft("gOr", kwidth, ' ')).Append(": ").Append(qb.GOr).EOL()
	sb.Append(z.AlignLeft("gAnd", kwidth, ' ')).Append(": ").Append(qb.GAnd).EOL()
	sb.Append(z.AlignLeft("sepOr", kwidth, ' ')).Append(": ").Append(qb.SepOr).EOL()
	sb.Append(z.AlignLeft("sepAnd", kwidth, ' ')).Append(": ").Append(qb.SepAnd).EOL()
	sb.Append(z.AlignLeft("quoteBegin", kwidth, ' ')).Append(": ").Append(qb.QuoteBegin).EOL()
	sb.Append(z.AlignLeft("quoteEnd", kwidth, ' ')).Append(": ").Append(qb.QuoteEnd).EOL()
	sb.Append(z.AlignLeft("bracketBegin", kwidth, ' ')).Append(": ").Append(qb.BracketBegin).EOL()
	sb.Append(z.AlignLeft("bracketEnd", kwidth, ' ')).Append(": ").Append(qb.BracketEnd).EOL()
	// QWordRule
	if len(qb.Rules) > 0 {
		sb.Append(z.AlignLeft("rules", kwidth, ' ')).Append(": ").EOL()
		for i := 0; i < len(qb.Rules); i++ {
			rule := qb.Rules[i]
			sb.Append(z.DupChar(' ', kwidth)).Append(fmt.Sprintf(" %2d. {", i))
			sb.Append(z.DupChar(' ', kwidth)).Append(z.DupChar(' ', kwidth)).Append(fmt.Sprint("{")).EOL()
			sb.Append(z.DupChar(' ', kwidth+9)).Append(fmt.Sprintf("key    : %s", rule.Key)).EOL()
			sb.Append(z.DupChar(' ', kwidth+9)).Append(fmt.Sprintf("regex  : %s", rule.Regex.String())).EOL()
			sb.Append(z.DupChar(' ', kwidth+9)).Append(fmt.Sprintf("seg    : %s", rule.Seg)).EOL()
			sb.Append(z.DupChar(' ', kwidth+9)).Append(fmt.Sprintf("type   : %v", rule.Type)).EOL()
			sb.Append(z.DupChar(' ', kwidth+5)).Append(fmt.Sprint("}")).EOL()
		}
	}
	return sb.String()
}

// 返回一个使用默认参数的qwordBuilder
func QWordBuilder(rule ...io.Reader) *QWBuilder {
	qb := &QWBuilder{
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
	if len(rule) > 0 {
		// 加载rule
		qb.LoadRules(rule[0])
	}
	return qb
}

// 设置qwordBuilder的参数, 使用json格式字符串
// 仅仅可以修改 gOr, gAnd, sepOr, sepAnd 四个参数
func (qb *QWBuilder) Setup(sjson string) *QWBuilder {
	setup := new(QWBuilder)
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
	// TODO 需要check一下设置的项
	return qb
}

// 读取解析规则字符串
func (qb *QWBuilder) LoadRulesStr(str string) *QWBuilder {
	return qb.LoadRules(strings.NewReader(str))
}

// 读取解析规则, 传入一个实现了io.Reader的对象即可
func (qb *QWBuilder) LoadRules(reader io.Reader) *QWBuilder {
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
			qwRule.Seg = z.Trim(line[:spos])
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
func (qb *QWBuilder) Parse(kwd string) *QWord {
	kwd = z.TrimExtraSpace(kwd)
	if len(kwd) == 0 {
		return nil
	}
	isGOr, isGAnd, flds, seps := qb.extractFldsAndSeps(kwd)
	// 解析为QWord
	qword := new(QWord)
	qword.CndMap = map[string]*QCnd{}
	sseps := len(seps)
	for i, fld := range flds {
		// qcnd
		qc := qb.evalQCnd(fld)
		if qc != nil {
			qword.Cnds = append(qword.Cnds, qc)
			qword.CndMap[qc.Key] = qc
			// 分隔符
			if i < sseps {
				sp := "&"
				if isGOr {
					sp = "|"
				} else if isGAnd {
					sp = "&"
				} else if z.IsInStrings(qb.SepOr, string(seps[i])) {
					sp = "|"
				} else if z.IsInStrings(qb.SepAnd, string(seps[i])) {
					sp = "&"
				}
				qword.Rels = append(qword.Rels, sp)
			}
		} else {
			qword.Unmatchs = append(qword.Unmatchs, fld)
		}
	}
	return qword
}

// 配备并重新组合查询语句, 给出QCnd对象
func (qb *QWBuilder) evalQCnd(fld string) *QCnd {
	for _, rule := range qb.Rules {
		if rule.Regex.MatchString(fld) {
			z.DebugPrintf("fld [%s] match regex [%s]\n", fld, rule.Regex.String())
			groups := rule.Regex.FindStringSubmatch(fld)
			for _, grp := range groups {
				z.DebugPrintf("    g_%s\n", grp)
			}
			extractStr := z.Trim(findMatchStr(rule.Seg, groups))
			// 生成QCnd
			qc := new(QCnd)
			qc.Key = rule.Key
			qc.Origin = fld
			qc.Plain = extractStr
			qc.Type = rule.Type
			switch qc.Type {
			case String:
				qc.Value = extractStr
			case Regex:
				qc.Value = regexp.MustCompile(extractStr)
			case IntRegion, LongRegion, DateRegion:
				qc.Value = z.MakeRegion(extractStr)
			case StringEnum:
				senum := extractStr[1 : len(extractStr)-1]
				qc.Value = z.SplitIgnoreBlank(senum, ",")
			case IntEnum:
				ienum := extractStr[1 : len(extractStr)-1]
				iarray := make([]int, 0, 5)
				for _, ie := range z.SplitIgnoreBlank(ienum, ",") {
					ione, _ := strconv.Atoi(ie)
					iarray = append(iarray, ione)
				}
				qc.Value = iarray
			case Json:
				jmap := new(map[string]interface{})
				jerr := z.JsonFromString(extractStr, jmap)
				if jerr != nil {
					qc.Value = jmap
				}
			default:
				// unsupport
				qc.Value = nil
			}
			return qc
		}
	}
	z.DebugPrintf("fld [%s] miss match\n", fld)
	return nil
}

// 简单的查找出对应的group
func findMatchStr(seg string, groups []string) string {
	// FIXME 这里先简单实现下, 默认格式都是 '#{x}'
	seg = seg[2 : len(seg)-1]
	pos, err := strconv.Atoi(seg)
	if err != nil {
		panic(err)
	}
	return groups[pos]
}

// 提取查询语句与连接符
func (qb *QWBuilder) extractFldsAndSeps(kwd string) (isGOr, isGAnd bool, flds []string, seps []byte) {
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
	// FIXME 这个地方需要在好好想想
	// 保证一个特殊情况下的正确性, 比如或是" "与是"," kwd中有类似"xxx , yyy"这样的语句,需要把处理
	// 这个特例是一搬在" "当做或的情况下
	if z.IsInStrings(qb.SepOr, " ") {
		for _, sAnd := range qb.SepAnd {
			ss := " " + sAnd + " "
			kwd = strings.Replace(kwd, ss, sAnd, -1)
		}
	}
	// 开始解析
	kszie := len(kwd)
	fld := z.StringBuilder()
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
					fld.Append(kwd[i])
				} else {
					fld.Append(c)
				}
			}
			continue
		}
		// 如果是括弧
		if z.IsInStrings(qb.BracketBegin, string(c)) {
			fld.Append(c)
			for i++; i < kszie; i++ {
				c = kwd[i]
				fld.Append(c)
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
				fld = z.StringBuilder()
			}
			continue
		}
		// 没什么特殊情况,那就加入到sb
		fld.Append(c)
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
