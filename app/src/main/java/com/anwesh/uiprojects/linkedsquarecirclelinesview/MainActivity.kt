package com.anwesh.uiprojects.linkedsquarecirclelinesview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.squarecirclelinesview.SquareCircleLinesView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SquareCircleLinesView.create(this)
    }
}
