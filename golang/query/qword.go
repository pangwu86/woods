package query

type QWord struct {
	Rels []string `json:"rels"`
	Cnds []*QCnd  `json:"cnds"`
}
