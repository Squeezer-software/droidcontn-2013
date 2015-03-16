package tn.droidcon.workshop.tictactoe.app;

import tn.droidcon.workshop.tictactoe.core.ImageAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class DroidConTicTacToe extends Activity {

    /**
     * codes for game logic
     */
    private final int EMPTY_BOX_CODE = 0;
    private final int CROSS_BOX_CODE = 1;
    private final int CIRCLE_BOX_CODE = 2;

    /**
     * the game state Ids
     */
    private static final int CIRCLE_STATE_ID = 0;
    private static final int CROSS_STATE_ID = 1;

    /**
     * the grid size
     */
    private static final int COLUMNS_NUMBER = 3;
    private static final int LINES_NUMBER = 3;

    /**
     * matrix that will hold the game logic
     */
    private int[][] gameMatrix;

    /**
     * the game state
     */
    private int state;

    /**
     * the game UI
     */
    private GridView gameGrid;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        gameGrid = (GridView) findViewById(R.id.gameGrid);
        gameGrid.setNumColumns(COLUMNS_NUMBER);

        // initialize the game
        initializeGame();

        gameGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                playTurn(position);
            }
        });

        // set the game initial state, the circle player play first
        state = CIRCLE_STATE_ID;

    }

    /**
     * update the game UI
     * @param position
     */
    private void playTurn(int position) {

        if (state == CIRCLE_STATE_ID) {
            if (fillMatrix(position, state)) {
                ImageAdapter imgAdapter = (ImageAdapter) gameGrid.getAdapter();
                imgAdapter.setItem(position, R.drawable.circle);
                gameGrid.setAdapter(imgAdapter);
                matrixCheck(position);
                state = CROSS_STATE_ID;
            }
        } else {

            if (fillMatrix(position, state)) {
                ImageAdapter imgAdapter = (ImageAdapter) gameGrid.getAdapter();
                imgAdapter.setItem(position, R.drawable.cross);
                gameGrid.setAdapter(imgAdapter);
                matrixCheck(position);
                state = CIRCLE_STATE_ID;
            }
        }

    }

    /**
     * fill the game logic matrix
     * @param position
     * @param state
     * @return
     */
    private boolean fillMatrix(int position, int state) {
        int line = position / LINES_NUMBER;
        int column = position % LINES_NUMBER;
        boolean result = true;

        if (state == CIRCLE_STATE_ID) {
            if (gameMatrix[line][column] == 0) {
                gameMatrix[line][column] = CIRCLE_BOX_CODE;
                result = true;
            } else {
                result = false;
            }
        } else {
            if (gameMatrix[line][column] == 0) {
                gameMatrix[line][column] = CROSS_BOX_CODE;
                result = true;
            } else {
                result = false;
            }

        }
        return result;
    }

    /**
     * check the game logic matrix to find a winning state
     * @param position
     */
    private void matrixCheck(int position) {
        int line = position / LINES_NUMBER;
        int column = position % LINES_NUMBER;

        if (isWinningLine(line) || isWinningColumn(column)
                || isWinningDiagonal()) {
            String message = String.format(
                    getResources().getString(
                            R.string.dialog_winning_message_text),
                    String.valueOf(state));

            showPopup(
                    getResources()
                            .getString(R.string.dialog_title_victory_text),
                    message);
            return;
        }
        // verify if grid is not full, else reinitialize the table
        boolean isGridFull = true;
        for (int j = 0; j < LINES_NUMBER; j++) {
            for (int i = 0; i < COLUMNS_NUMBER; i++) {
                if (gameMatrix[j][i] == 0) {
                    isGridFull = false;
                    return;
                }
            }
        }
        if (isGridFull) {
            showPopup(
                    getResources().getString(
                            R.string.dialog_title_game_end_text),
                    getResources().getString(R.string.dialog_full_grid_text));
        }

    }

    /**
     * check if the grid diagonals are winning
     * @return true if the diagonals allow user to win, false otherwise
     */
    private boolean isWinningDiagonal() {
        boolean result = false;
        if (((gameMatrix[0][0] != 0) && (gameMatrix[0][0] == gameMatrix[1][1]) && (gameMatrix[0][0] == gameMatrix[2][2]))
                || ((gameMatrix[0][2] != 0)
                        && (gameMatrix[0][2] == gameMatrix[1][1]) && (gameMatrix[0][2] == gameMatrix[2][0]))) {
            result = true;
        }
        return result;
    }

    /**
     * check if the specified line is a winning one
     * @param line index
     * @return true if the line allows user to win, false otherwise
     */
    private boolean isWinningLine(int line) {
        boolean result = false;

        if ((gameMatrix[line][0] != 0)
                && (gameMatrix[line][0] == gameMatrix[line][1])
                && (gameMatrix[line][0] == gameMatrix[line][2])) {

            result = true;
        }
        return result;
    }

    /**
     * check if the specified column is a winning one
     * @param column index
     * @return true if the column allows user to win, false otherwise
     */
    private boolean isWinningColumn(int column) {
        boolean result = false;
        if ((gameMatrix[0][column] != 0)
                && (gameMatrix[0][column] == gameMatrix[1][column])
                && (gameMatrix[0][column] == gameMatrix[2][column])) {

            result = true;
        }
        return result;
    }

    /**
     * initialize the game grid
     */
    private void initializeGame() {
        // initialize the grid
        gameGrid.setAdapter(new ImageAdapter(this, COLUMNS_NUMBER
                * LINES_NUMBER));

        // initialize gameMatrix
        gameMatrix = new int[LINES_NUMBER][COLUMNS_NUMBER];
        for (int j = 0; j < LINES_NUMBER; j++) {
            for (int i = 0; i < COLUMNS_NUMBER; i++) {
                gameMatrix[j][i] = EMPTY_BOX_CODE;
            }
        }
    }

    /**
     * easily create an OK Cancel dialog
     * @param title
     * @param message
     */
    private void showPopup(String title, String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(title);
        alert.setMessage(message);

        alert.setPositiveButton(getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        initializeGame();
                    }
                });

        alert.setNegativeButton(
                getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        finish();
                    }
                });

        alert.show();
    }
}
