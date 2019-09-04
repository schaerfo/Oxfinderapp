package com.example.oxfinder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.JsonReader
import java.io.InputStreamReader

class MainActivity :  AppCompatActivity(){
    data class ElementInfo (
        var symbol: String,
        var atomicNumber: Int,
        var name: String,
        var period: Int,
        var block: Char,
        var atomicMass: Double,
        var en: Double,
        var preferredState: Int,
        var states: Array<Int>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.readElementList()
    }

    fun readElementList(){
        val files = assets.list("/")
        println("${files}")
        val r = JsonReader(InputStreamReader(assets.open("element_list.json")))
        val newElementList: MutableList<ElementInfo> = mutableListOf()
        r.beginArray()
        while (r.hasNext()){
            var symbol: String = ""
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
                    "symbol" -> symbol = r.nextString()
                }
            }
            r.endObject()
            newElementList.add(ElementInfo(symbol = symbol, atomicNumber = atomicNumber, name = name, period = period,
                block = block, atomicMass = atomicMass, en = en, preferredState = preferredState, states = states.toTypedArray()))
        }
        r.endArray()
        elementList = newElementList.toTypedArray()
    }

    var elementList: Array<ElementInfo>? = null
}