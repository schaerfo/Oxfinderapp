package com.example.oxfinder

class Ion (atoms: List<Pair<String, Int?>>, charge: Int = 0) {
    class InsufficientBracketsException : Exception()

    class UnexpectedCharacterException (c : Char) : Exception(){
        private val mChar = c

        override fun getLocalizedMessage(): String {
            return String(charArrayOf(mChar))
        }
    }

    class UnknownElementException (el : String) : Exception(){
        private val mElement = el

        override fun getLocalizedMessage(): String {
            return mElement
        }
    }

    private val mAtoms: MutableList<Pair<String, Int?>> = atoms.toMutableList()
    private val mCharge: Int = charge

    constructor(p: Pair<MutableList<Pair<String, Int?>>, Int>) : this(p.first, p.second)

    constructor(s: String) : this(ionFromString(s))

    fun calculateMolarMass(catalog: Map<String, MainActivity.ElementInfo>): Double {
        var result = 0.0
        mAtoms.forEach {
            result += catalog[it.first]?.atomicMass ?: throw UnknownElementException(it.first)
        }
        return result
    }

    companion object {
        private fun ionFromString(s: String): Pair<MutableList<Pair<String, Int?>>, Int> {
            var (charge, workingCopy) = this.extractCharge(s)
            workingCopy = this.resolveParentheses(workingCopy)
            val elementList = readElements(workingCopy)

            val resList = mutableListOf<Pair<String, Int?>>()
            elementList.forEach { resList.add(Pair(it, null)) }
            return Pair(resList, charge)
        }

        private fun extractCharge(s: String): Pair<Int, String> {
            return when {
                s.contains(',') -> {
                    val i = s.indexOf(',')
                    var chargeStr = s.substring(i + 1 until s.length)

                    if (charArrayOf('-', '+').any { chargeStr.endsWith(it) })
                        chargeStr = chargeStr.last() + chargeStr.dropLast(1)

                    Pair(chargeStr.toInt(), s.substring(0 until i))
                }
                s.endsWith('+') -> {
                    Pair(1, s.dropLast(1))
                }
                s.endsWith('-') -> {
                    Pair(-1, s.dropLast(1))
                }
                else -> Pair(0, s)
            }
        }

        private val charToInt = { c: Char ->
            c.toInt() - '0'.toInt()
        }

        private fun resolveParentheses(s: String): String {
            var res = s

            while (res.contains('(')) {
                val start = res.indexOf('(')
                var end = res.indexOf(')')
                if (end == -1)
                    throw InsufficientBracketsException()

                val before = res.substring(0 until start)
                val between = res.substring(start + 1 until end)

                var number = 1
                var firstDigit = true
                while (res.length > end + 1 && res[end + 1].isDigit()) {
                    number = if (firstDigit) {
                        firstDigit = false
                        res[end + 1].let(charToInt)
                    } else 10 * number + res[end + 1].let(charToInt)
                    ++end
                }

                val after = res.substring(end + 1)
                res = before + between.repeat(number) + after
            }

            return res
        }

        private fun readElements(s: String): List<String> {
            val res = mutableListOf<String>()
            var currElement: String? = null
            for (c in s) {
                when {
                    c.isUpperCase() -> {
                        currElement?.let { res.add(it) }
                        currElement = c.toString()
                    }
                    c.isLowerCase() -> currElement += c
                    c.isDigit() -> {
                        currElement?.let {
                            for (i in 0 until charToInt(c)) res.add(it)
                        }
                        currElement = null
                    }
                    else -> throw UnexpectedCharacterException(c)
                }
            }
            currElement?.let { res.add(it) }
            return res
        }
    }
}