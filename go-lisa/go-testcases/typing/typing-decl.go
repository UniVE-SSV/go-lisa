package main

import "fmt"

func simpleTyping(x, y int) int {
	var a int = 1 + 1
	var b float32 = 2 + 2
	var c int = a * a
	var d float32 = 2 - 2
	var e float32 =  +d
	var x string = "a" + "b"

}

func comparisonTyping(x,y int) {
	a := x == y
	b := 2 == 2
	var d int = 2 + 2
	e := d == 2
	c := 2 != 2
}


func typeError1(x, y int) int {
	var i int = 1 + 1.2
}

func typeError2(x, y int) int {
	var i int = "a" + "b"
}
