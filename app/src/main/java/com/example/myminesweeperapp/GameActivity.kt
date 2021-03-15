package com.example.myminesweeperapp

import android.graphics.Color
import android.graphics.Color.parseColor
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.lang.Long.min


class GameActivity  : AppCompatActivity() {
    // Boolean variable to know if first button is revealed
    var flag: Boolean = false
    // Boolean variable to know if player has played his first move or not(for starting timer)
    var timerstarted: Boolean = false

    private var stoptime: Long = 0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        var rownum: Int = 8
        var colnum: Int = 8
        var minenum: Int = 12

        val boardArr: IntArray? = intent.getIntArrayExtra("NAME")
        boardArr?.let{

            rownum = it[0]
            colnum = it[1]
            minenum = it[2]
        }

        val game = Minesweeper(rownum, colnum, minenum)
        setupBoard(rownum, colnum, minenum, game)

        val restart: Button = findViewById(R.id.restart)
        // Restarting activity if restart button is clicked
        restart.setOnClickListener{
            val intent = intent
            finish()
            startActivity(intent)
        }



    }

    @RequiresApi(Build.VERSION_CODES.N)
    // To set up layout of the board
    private fun setupBoard(rownum: Int, colnum: Int, minenum: Int, game: Minesweeper){
        // Setting initial mine count, to display number of flags left to mark
        val mineCount: TextView = findViewById(R.id.mineCount)
        mineCount.text = minenum.toString()

        val board: LinearLayout = findViewById(R.id.board)
        val params1 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0
        )
        val params2 = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT
        )
        var counter = 0
        for(i in 0 until rownum){
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.layoutParams = params1
            params1.weight = 1.0F
            for(j in 0 until colnum){
                val button = Button(this)
                button.id = counter
                button.setPadding(-30, -30, -30, -30)
                params2.weight = 1.0F
                params2.setMargins(-8, -13, -8, -13)
                button.layoutParams = params2

                //Setting OnClickListener if a block is revealed
                button.setOnClickListener{
                    // Starting timer if it is a first move
                    if(!timerstarted){
                        timerstarted = true
                        startTimer()
                    }
                    // Setting mine after first block is revealed, to make sure first block is not a mine
                    if(!flag){
                        setmine(game, i, j, rownum, colnum, minenum)
                        flag=true
                    }
                    if(game.move(1, i, j)){
                        // Displaying board, saving game time and showing a toast, if player won after current chance
                        if(game.status == Status.WON){
                            stopTimer()
                            game.displayBoard()
                            Toast.makeText(this, "Congratulations, You Won!", Toast.LENGTH_LONG).show()
                            saveGameTimeIfWin()

                        }
                        // Displaying board, saving game time and showing a toast, if player lost after current chance
                        else if(game.status == Status.LOST){
                            stopTimer()
                            game.displayBoard()
                            Toast.makeText(this, "You Lose. Keep trying!", Toast.LENGTH_LONG).show()
                            saveGameTimeIfLost()
                        }
                    }
                }

                //Setting OnLongClickListener if a block is tried to mark as a flag
                button.setOnLongClickListener {
                    // Starting timer if it is a first move
                    if(!timerstarted){
                        timerstarted = true
                        startTimer()
                    }
                    if(game.move(2, i, j)){
                        // Displaying board, saving game time and showing a toast, if player won after current chance
                        if(game.status == Status.WON){
                            stopTimer()
                            game.displayBoard()
                            Toast.makeText(this, "Congratulations, You Won!", Toast.LENGTH_LONG).show()
                            saveGameTimeIfWin()
                        }
                        // Displaying board, saving game time and showing a toast, if player lost after current chance
                        else if(game.status == Status.LOST){
                            stopTimer()
                            game.displayBoard()
                            Toast.makeText(this, "You Lost!", Toast.LENGTH_LONG).show()
                            saveGameTimeIfLost()
                        }
                    }
                    true
                }
                counter++
                linearLayout.addView(button)
            }
            board.addView(linearLayout)

        }

    }
    // To start timer when game is started
    private fun startTimer(){
        val chronometer: Chronometer = findViewById(R.id.chronometer)
        chronometer.base = SystemClock.elapsedRealtime()+stoptime
        chronometer.start()
    }
    // To stop timer when game is completed
    private fun stopTimer(){
        val chronometer: Chronometer = findViewById(R.id.chronometer)
        stoptime = chronometer.base - SystemClock.elapsedRealtime()
        chronometer.stop()

    }
    @RequiresApi(Build.VERSION_CODES.N)
    // Saving last game time and updating best time if it is a win
    private fun saveGameTimeIfWin(){
        val chronometer: Chronometer = findViewById(R.id.chronometer)
        val gametime = chronometer.text
        var gameTimeInSec: Long = (gametime.substring(0, 2).toLong())*60 + gametime.substring(3, 5).toLong()
        if(gametime.length>5){
            gameTimeInSec *= 60
            gameTimeInSec += gametime.substring(6, 8).toLong()
        }
        var bestgametime = gameTimeInSec
        val sharedPreferences = getSharedPreferences("data", MODE_PRIVATE)
        val bestTime = sharedPreferences.getLong("BESTTIME", 100000)
        bestgametime = min(bestgametime, bestTime)
        with(sharedPreferences.edit()){
            putLong("BESTTIME", bestgametime)
            putLong("LASTGAMETIME", gameTimeInSec)
            commit()
        }
    }

    // Saving last game time if it is a lost
    private fun saveGameTimeIfLost(){
        val chronometer: Chronometer = findViewById(R.id.chronometer)
        val gametime = chronometer.text
        val gameTimeInSec: Long = (gametime.substring(0, 2).toLong())*60 + gametime.substring(3, 5).toLong()
        val sharedPreferences = getSharedPreferences("data", MODE_PRIVATE)
        with(sharedPreferences.edit()){
            putLong("LASTGAMETIME", gameTimeInSec)
            commit()
        }
    }
    // Setting mine by making sure all mines are at different positions and none of them are at position of first revealed block
    // i & j are coordinates of first revealed block
    private fun setmine(game: Minesweeper, i: Int, j: Int, rownum: Int, colnum: Int, minenum: Int){
        var k=0
        while(k!=minenum){
            val r1 = (0 until rownum).random()
            val r2 = (0 until colnum).random()
            if((r1!=i||r2!=j)&&game.setMine(r1, r2)){
                k++
            }
        }
    }



    inner class Minesweeper(private val numrow: Int, private val numcol: Int, private val nummine: Int){
        private val board = Array(numrow) { Array(numcol) { MineCell() }}
        private val MINE = -1
        private val movement = intArrayOf(-1, 0, 1)
        private var mineLeft = nummine
        var status = Status.ONGOINING
            private set

        //To set up mines
        fun setMine(row: Int, column: Int) : Boolean{
            if(board[row][column].value != MINE) {
                board[row][column].value = MINE
                updateNeighbours(row, column)
                return true
            }
            return false
        }

        //To update the values of the cells neighbouring to the mines
        private fun updateNeighbours(row: Int, column: Int) {
            for (i in movement) {
                for (j in movement) {
                    if(((row+i) in 0 until numrow) && ((column+j) in 0 until numcol) && board[row + i][column + j].value != MINE)
                        board[row + i][column + j].value++
                }
            }
        }

        fun move(choice: Int, x: Int, y: Int): Boolean{
            // If cell is tried to reveal
            if(choice==1){
                if(board[x][y].isRevealed||board[x][y].isMarked){
                    return false
                }
                if(board[x][y].value==MINE){
                    status=Status.LOST
                }
                else{
                    reveal(x, y)
                }
            }
            // If cell is tried to mark as flag or tried to unmark
            else if(choice==2){
                if(board[x][y].isRevealed) return false;
                val buttonId: Int = numcol*x+y
                val button: Button = findViewById(buttonId)
                // To mark the cell
                if(!board[x][y].isMarked&&mineLeft>0){
                    mineLeft--
                    button.text = getString(R.string.FLAG)
                    board[x][y].isMarked = !board[x][y].isMarked
                }
                // To unmark the cell
                else if(board[x][y].isMarked){
                    button.text = ""
                    mineLeft++
                    board[x][y].isMarked = !board[x][y].isMarked
                }
                val mineCount: TextView = findViewById(R.id.mineCount)
                mineCount.text = mineLeft.toString()
                if(!flag) return true
            }
            else return false;
            // To check is game completed with a win
            if(isComplete()&&status!=Status.LOST){
                status=Status.WON
            }
            return true
        }
        // To check if game is completed, by checking if all mines are marked or if all non mine cells are revealed
        private fun isComplete() : Boolean{
            var minesMarked = true
            board.forEach { row->
                row.forEach {
                    if(it.value == MINE){
                        if(!it.isMarked)
                            minesMarked = false
                    }
                }
            }
            var valuesRevealed = true
            board.forEach { row->
                row.forEach {
                    if(it.value != MINE){
                        if(!it.isRevealed)
                            valuesRevealed = false
                    }
                }
            }
            return minesMarked || valuesRevealed
        }

        // To reveal the button if clicked and to reveal all its neighbours if its value is zero
        private fun reveal(x: Int, y: Int) {
            if(!board[x][y].isRevealed && !board[x][y].isMarked) {
                board[x][y].isRevealed = true
                val buttonId: Int = numcol*x+y
                val button: Button = findViewById(buttonId)
                button.isEnabled = false
                button.setBackgroundColor(300)
                displayButtonText(button, board[x][y].value)
                if (board[x][y].value == 0) {
                    for (i in movement){
                        for (j in movement){
                            if ((i != 0 || j != 0) && ((x + i) in 0 until numrow) && ((y + j) in 0 until numcol)){
                                reveal(x + i, y + j)
                            }
                        }
                    }
                }
            }
        }




        //To display the board after end of the game
        fun displayBoard() {
            for(i in 0 until numrow){
                for(j in 0 until numcol){
                    if(!board[i][j].isRevealed&&board[i][j].value != MINE){
                        val buttonId: Int = numcol*i+j
                        val button: Button = findViewById(buttonId)
                        button.isEnabled = false
                        button.setBackgroundColor(300)
                        displayButtonText(button, board[i][j].value)
                    }
                    if(!board[i][j].isRevealed&&board[i][j].value == MINE){
                        val buttonId: Int = numcol*i+j
                        val button: Button = findViewById(buttonId)
                        button.isEnabled = false
                        button.setBackgroundColor(300)
                        button.text=""
                        button.setBackgroundResource(R.drawable.ic_mine)
                    }
                }
            }
        }
        // To Display button text and to set its color based on its value
        private fun displayButtonText(button: Button, num: Int){
            if(num == 0){
                button.text=""
                return
            }
            else if(num == 1){
                button.setTextColor(Color.BLUE)
            }
            else if(num == 2){
                button.setTextColor(Color.parseColor("#00A30E"))
            }
            else if(num == 3){
                button.setTextColor(parseColor("#DA2C2F"))
            }
            else if(num == 4){
                button.setTextColor(parseColor("#FFBD00"))
            }
            else if(num == 5){
                button.setTextColor(parseColor("#00ADEF"))
            }
            else if(num == 6){
                button.setTextColor(parseColor("#C28F2E"))
            }
            else if(num == 7){
                button.setTextColor(parseColor("#E20787"))
            }
            else if(num == 8){
                button.setTextColor(parseColor("#0099A7"))
            }
            button.text = num.toString()

        }
    }

}


// MineCell Data Class
data class MineCell(var value: Int = 0, var isRevealed: Boolean = false, var isMarked: Boolean = false)

// Game status
enum class Status{
    WON,
    ONGOINING,
    LOST
}

