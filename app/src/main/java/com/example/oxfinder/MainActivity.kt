package com.example.oxfinder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.JsonReader
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStreamReader

class MainActivity :  AppCompatActivity(){
    data class ElementInfo (
        val atomicNumber: Int,
        val name: String,
        val period: Int,
        val block: Char,
        val atomicMass: Double,
        val en: Double,
        val preferredState: Int,
        val states: Array<Int>
    )

    class Ion (atoms: List<Pair<String, Int?>>, charge: Int = 0) {
        val mAtoms: MutableList<Pair<String, Int?>> = atoms.toMutableList()
        val mCharge: Int = charge
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mElementCatalog = this.readCatalog()
    }

    private fun readCatalog(): Map<String, ElementInfo>{
        val r = JsonReader(InputStreamReader(assets.open("element_list.json")))
        val newCatalog: MutableMap<String, ElementInfo> = mutableMapOf()
        r.beginObject()
        while (r.hasNext()){
            val symbol = r.nextName()
            var atomicNumber: Int = 0
            var name: String = ""
            var period: Int = 0
            var block: Char = '0'
            var atomicMass: Double = 0.0
            var en: Double = 0.0
            var preferredState: Int = 0
            val states: MutableList<Int> = mutableListOf()
            r.beginObject()
            while (r.hasNext()){
                when (r.nextName()) {
                    "atomicmass" -> atomicMass = r.nextDouble()
                    "atomicno" -> atomicNumber = r.nextInt()
                    "block" -> block = r.nextString()[0]
                    "en" -> en = r.nextDouble()
                    "name" -> name = r.nextString()
                    "period" -> period = r.nextInt()
                    "prefstate" -> preferredState = r.nextInt()
                    "states" -> {
                        r.beginArray()
                        while (r.hasNext())
                            states.add(r.nextInt())
                        r.endArray()
                    }
                }
            }
            r.endObject()
            newCatalog[symbol] = ElementInfo(atomicNumber = atomicNumber, name = name, period = period,
                block = block, atomicMass = atomicMass, en = en, preferredState = preferredState, states = states.toTypedArray())
        }
        r.endObject()
        return newCatalog
    }

    private fun extractCharge(s: String): Pair<Int, String>{
        return when {
            s.contains(',')  -> {
                val i = s.indexOf(',')
                var chargeStr = s.substring(i+1 until s.length)

                if (charArrayOf('-', '+').any{ chargeStr.endsWith(it) })
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

    private fun resolveParentheses(s: String): String{
        var res = s

        while (res.contains('(')) {
            val start = res.indexOf('(')
            var end = res.indexOf(')')
            if (end == -1)
                throw Exception(resources.getString(R.string.ui_insufficient_closing_braces))

            val before = res.substring(0 until start)
            val between = res.substring(start+1 until end)

            var number = 1
            var firstDigit = true
            while (res.length > end+1 && res[end + 1].isDigit()) {
                number = if (firstDigit) {
                    firstDigit = false
                    res[end + 1].let(charToInt)
                } else 10 * number + res[end + 1].let(charToInt)
                ++end
            }

            val after = res.substring(end+1)
            res = before + between.repeat(number) + after
        }

        return res
    }

    private fun readElements(s: String): List<String> {
        val res = mutableListOf<String>()
        var currElement : String? = null
        for (c in s){
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
                else -> throw Exception("${resources.getString(R.string.ui_unknown_character)}: $c")
            }
        }
        currElement?.let { res.add(it) }
        return res
    }

    private fun ionFromString(s: String): Ion {
        var (charge, workingCopy) = this.extractCharge(s)
        workingCopy = this.resolveParentheses(workingCopy)
        val elementList = readElements(workingCopy)

        val resList = mutableListOf<Pair<String, Int?>>()
        elementList.forEach { resList.add(Pair(it, null)) }
        return Ion(resList, charge)
    }

    @Suppress("UNUSED_PARAMETER")
    fun calculateMolarMass(view: View) {
        val s = equationInput.text.toString()
        try {
            val molecule = ionFromString(s)
            resultDisplay.text = "${molecule.mCharge}"
        } catch (e: NumberFormatException) {
            e.printStackTrace(System.err)
            val dialog = ErrorDialog("${resources.getString(R.string.ui_incorrect_charge_format)}: ${e.localizedMessage}")
            dialog.show(supportFragmentManager, "message")
        } catch (e: Exception) {
            e.printStackTrace(System.err)
            val dialog = ErrorDialog(" ${resources.getString(R.string.ui_exception)}: ${e.localizedMessage}")
            dialog.show(supportFragmentManager, "message")
        }
    }

    private var mElementCatalog: Map<String, ElementInfo>? = null
}