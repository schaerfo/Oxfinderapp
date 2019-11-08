package com.example.oxfinder

import android.annotation.SuppressLint
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

    @SuppressLint("SetTextI18n")
    @Suppress("UNUSED_PARAMETER")
    fun calculateMolarMass(view: View) {
        val s = equationInput.text.toString()
        try {
            val molecule = Ion(s)
            resultDisplay.text = "%.3f %s".format(mElementCatalog?.let { molecule.calculateMolarMass(it) }, resources.getString(R.string.ui_g_per_mol))
        } catch (e: NumberFormatException) {
            e.printStackTrace(System.err)
            val dialog = ErrorDialog("${resources.getString(R.string.ui_incorrect_charge_format)}: ${e.localizedMessage}")
            dialog.show(supportFragmentManager, "message")
        } catch (e: Ion.InsufficientBracketsException){
            e.printStackTrace(System.err)
            val dialog = ErrorDialog(resources.getString(R.string.ui_insufficient_closing_braces))
            dialog.show(supportFragmentManager, "message")
        } catch (e: Ion.UnexpectedCharacterException) {
            e.printStackTrace(System.err)
            val dialog = ErrorDialog("${resources.getString(R.string.ui_unexpected_character)}: ${e.localizedMessage}")
            dialog.show(supportFragmentManager, "message")
        } catch (e: Exception) {
            e.printStackTrace(System.err)
            val dialog = ErrorDialog(" ${resources.getString(R.string.ui_exception)}: ${e.localizedMessage}")
            dialog.show(supportFragmentManager, "message")
        }
    }

    private var mElementCatalog: Map<String, ElementInfo>? = null
}