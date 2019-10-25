package com.example.oxfinder

import android.os.Bundle
import android.support.v7.app.AlertDialog
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

    private fun ionFromString(s: String): Ion {
        var workingCopy = s

        val charge: Int = when {
            s.contains(',') -> {
                val i = s.indexOf(',')
                workingCopy = s.substring(0 until i)
                var chargeStr = s.substring(i+1 until s.length)

                if (charArrayOf('-', '+').any{ chargeStr.endsWith(it) })
                    chargeStr = chargeStr.last() + chargeStr.dropLast(1)

                chargeStr.toInt()
            }
            s.endsWith('+') -> {
                workingCopy = s.dropLast(1)
                1
            }
            s.endsWith('-') -> {
                workingCopy = s.dropLast(1)
                -1
            }
            else -> 0
        }

        while (workingCopy.contains('(')) {
            val start = workingCopy.indexOf('(')
            var end = workingCopy.indexOf(')')
            if (end == -1)
                throw Exception(resources.getString(R.string.ui_insufficient_closing_braces))

            val before = workingCopy.substring(0 until start)
            val between = workingCopy.substring(start+1 until end)

            var number = 1
            var firstDigit = true
            while (workingCopy[end+1].isDigit()) {
                val charToInt = { c: Char ->
                    c.toInt() - '0'.toInt()
                }

                number = if (firstDigit) {
                    firstDigit = false
                    workingCopy[end + 1].let(charToInt)
                } else 10 * number + workingCopy[end + 1].let(charToInt)
                ++end
            }

            val after = workingCopy.substring(end+1)
            workingCopy = before + between.repeat(number) + after
        }

        return Ion(listOf(), charge)
    }

    @Suppress("UNUSED_PARAMETER")
    fun calculateMolarMass(view: View) {
        val s = equationInput.text.toString()
        try {
            val molecule = ionFromString(s)
            resultDisplay.text = "${molecule.mCharge}"
        } catch (e: NumberFormatException) {
            val dialog = ErrorDialog("${resources.getString(R.string.ui_incorrect_charge_format)}: ${e.localizedMessage}")
            dialog.show(supportFragmentManager, "message")
        } catch (e: Exception) {
            val dialog = ErrorDialog(e.localizedMessage)
            dialog.show(supportFragmentManager, "message")
        }
    }

    private var mElementCatalog: Map<String, ElementInfo>? = null
}