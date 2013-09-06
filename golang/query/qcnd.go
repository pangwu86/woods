package query

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
)

type QCnd struct {
	Key    string
	Origin string
	Plain  string
	Type   QCndType
	Value  interface{}
}
