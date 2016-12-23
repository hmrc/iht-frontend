val isInvalidCommaPosition = """^[0-9]?{1,3}(,?[0-9]{3})*(\.[0-9]?[0-9]?)?$""".r
val a = ".99"
val b = "1.99"
val c = "23.99"
val d = "23"
val e = "234"
val f = "12000"
val g = "121000"
val h = "100,23"
val i = "1.1"
val j = "100."
val k = "-45.78"
val l = ".345"
val m = "23.345"

isInvalidCommaPosition.findFirstMatchIn(a)


val ex = """(^(\d*)(\.\d{0,2})?$)|(^(\d{1,3},(\d{3},)*\d{3}(\.\d{1,2})?|\d{1,3}(\.\d{1,2})?)$)""".r
ex.findFirstMatchIn(a)
ex.findFirstMatchIn(b)
ex.findFirstMatchIn(c)
ex.findFirstMatchIn(d)
ex.findFirstMatchIn(e)
ex.findFirstMatchIn(f)
ex.findFirstMatchIn(g)
ex.findFirstMatchIn(h)
ex.findFirstMatchIn(i)
ex.findFirstMatchIn(j)
ex.findFirstMatchIn(k)
ex.findFirstMatchIn(l)
ex.findFirstMatchIn(m)