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
        val charge: Int = when {
            s.contains(',') -> {
                var chargeStr = s.substring(s.indexOf(',')+1 until s.length)

                if (charArrayOf('-', '+').any{ chargeStr.endsWith(it) })
                    chargeStr = chargeStr.last() + chargeStr.dropLast(1)

                chargeStr.toInt()
            }
            s.contains('+') -> 1
            s.contains('-') -> -1
            else -> 0
        }

        return Ion(listOf(), charge)
    }

    fun calculateMolarMass(view: View) {
        val s = equationInput.text.toString()
        val molecule = ionFromString(s)
        resultDisplay.text = "${molecule.mCharge}"
    }

    var mElementCatalog: Map<String, ElementInfo>? = null
}