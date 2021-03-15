package com.example.myminesweeperapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        loadGame()

        val start: Button = findViewById(R.id.start)
        val customBoard: Button = findViewById(R.id.customboardbutton)
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        val rows: EditText = findViewById(R.id.rows)
        val columns: EditText = findViewById(R.id.columns)
        val mines: EditText = findViewById(R.id.mines)

        rows.isVisible = false
        columns.isVisible = false
        mines.isVisible = false

        // If "Make Custom Board" button is clicked, radio group is unchecked and edit text views for input of number of rows, columns and mines are visible
        customBoard.setOnClickListener{
            radioGroup.clearCheck()
            rows.isVisible = true
            columns.isVisible = true
            mines.isVisible = true

        }
        // If any radio button is clicked, edit text views for input of number of rows, columns and mines are invisible
        radioGroup.setOnCheckedChangeListener{ radioGroup: RadioGroup, i: Int ->
            rows.isVisible = false
            columns.isVisible = false
            mines.isVisible = false
        }

        start.setOnClickListener{

            radioGroup?.let{
                val checkedID = it.checkedRadioButtonId

                //If easy option is selected
                if(checkedID==R.id.easy){
                    val intent = Intent(this, GameActivity::class.java).apply {
                        putExtra("NAME", intArrayOf(10,8,12))
                    }
                    startActivity(intent)
                }
                //If medium option is selected
                else if(checkedID==R.id.medium){
                    val intent = Intent(this, GameActivity::class.java).apply {
                        putExtra("NAME",intArrayOf(15,12,30))
                    }
                    startActivity(intent)
                }
                //If hard option is selected
                else if(checkedID==R.id.hard){
                    val intent = Intent(this, GameActivity::class.java).apply {
                        putExtra("NAME",intArrayOf(20,16,64))
                    }
                    startActivity(intent)
                }
                //If custom input is provided
                else{
                    val nrow = rows.text.toString()
                    val ncol = columns.text.toString()
                    val nmine = mines.text.toString()

                    //To check if none of the edit texts are empty
                    if(!nrow.trim().isEmpty()&&!ncol.trim().isEmpty()&&!nmine.trim().isEmpty()){

                        val rowcount: Int = nrow.toInt()
                        val colcount: Int = ncol.toInt()
                        val minecount: Int = nmine.toInt()
                        val ratio: Double = (rowcount*colcount*1.0)/minecount
                        // Constraints for custom input
                        if(rowcount>22||colcount>18||ratio<4){
                            Toast.makeText(this,"Invalid Input",Toast.LENGTH_LONG).show()
                        }
                        else{
                            val intent = Intent(this, GameActivity::class.java).apply {
                                putExtra("NAME",intArrayOf(rowcount,colcount,minecount))
                            }
                            startActivity(intent)

                        }
                    }
                }
            }
        }
    }
    // To load game if home page is restarted after pressing back button from next page
    override fun onRestart() {
        super.onRestart()
        loadGame()
    }

    // To set best time and last game time
    private fun loadGame(){
        val bestTime: TextView = findViewById(R.id.BestTime)
        val lastGameTime: TextView = findViewById(R.id.LastGameTime)
        val sharedPreferences = getSharedPreferences("data", MODE_PRIVATE )
        val bt = sharedPreferences.getLong("BESTTIME", 100000)
        val lgt = sharedPreferences.getLong("LASTGAMETIME", 100000)
        if(bt < 100000) bestTime.text = "Best Time: $bt sec"
        if(lgt < 100000) lastGameTime.text = "Last Game Time: $lgt sec"
    }
}