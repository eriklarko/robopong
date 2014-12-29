package se.purplescout.pong.competition.server;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import se.purplescout.pong.competition.headless.AutoFight;
import se.purplescout.pong.game.Paddle;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class FightMatrix extends GridPane {

    public FightMatrix() {
    }

    public void setFights(Iterable<AutoFight> fights) {
        List<Paddle> uniquePaddles = getUniquePaddlesOrderedByName(fights);

        int rowAndCol = 1;
        for (Paddle paddle : uniquePaddles) {
            addPaddleNameToRowAndColAndMarkIllegalCell(paddle, rowAndCol++);
        }

        for (AutoFight fight : fights) {
            int row = uniquePaddles.indexOf(fight.getLeftPaddle()) + 1;
            int col = uniquePaddles.indexOf(fight.getRightPaddle()) + 1;

            addFight(row, col, fight, fight.getLeftPaddle(), fight.getRightPaddle());
            addFight(col, row, fight, fight.getRightPaddle(), fight.getLeftPaddle());
        }
    }

    private void addPaddleNameToRowAndColAndMarkIllegalCell(Paddle paddle, int rowAndCol) {
        this.add(new Label(paddle.getTeamName()), 0, rowAndCol);
        this.add(new Label(paddle.getTeamName()), rowAndCol, 0);

        this.add(new Label("|X"), rowAndCol, rowAndCol);
    }

    private List<Paddle> getUniquePaddlesOrderedByName(Iterable<AutoFight> fights) {
        SortedSet<Paddle> uniquePaddles = new TreeSet<>((Paddle p1, Paddle p2) -> p1.getTeamName().compareTo(p2.getTeamName()));
        for (AutoFight fight : fights) {
            uniquePaddles.add(fight.getLeftPaddle());
            uniquePaddles.add(fight.getRightPaddle());
        }
        return new ArrayList<>(uniquePaddles);
    }

    private void addFight(int row, int col, AutoFight fight, Paddle rowPaddle, Paddle colPaddle) {
        Node node = getNodeFromFightState(fight, rowPaddle, colPaddle);

        this.add(node, col, row);
    }

    private Node getNodeFromFightState(AutoFight fight, Paddle rowPaddle, Paddle colPaddle) {
        switch (fight.getState()) {
            case BEFORE_FIGHT:
                return new Label("Not fighting yet");
            case FIGHTING:
                return new Label("Fighting...");
            case DONE_FIGHTING:
                return döneFajtäng(fight, rowPaddle, colPaddle);
        }

        return unknownState();
    }

    private Node döneFajtäng(AutoFight fight, Paddle rowPaddle, Paddle colPaddle) {
        switch (fight.getResult()) {
            case FIGHT_TOOK_TOO_LONG:
                return new Label("Fight took too long");
            case LEFT_PADDLE_THREW_EXCEPTION:
                return new Label("LEFT_PADDLE_THREW_EXCEPTION");
            case LEFT_PADDLE_TOOK_TOO_LONG:
                return new Label("LEFT_PADDLE_TOOK_TOO_LONG");
            case RIGHT_PADDLE_THREW_EXCEPTION:
                return new Label("RIGHT_PADDLE_THREW_EXCEPTION");
            case RIGHT_PADDLE_TOOK_TOO_LONG:
                return new Label("RIGHT_PADDLE_TOOK_TOO_LONG");
            case SUCCESS:
                String s;
                if (fight.getLeftPaddle() == rowPaddle) {
                    s = fight.getLeftScore() + " - " + fight.getRightScore();
                } else {
                    s = fight.getRightScore() + " - " + fight.getLeftScore();
                }
                return new Label("|" + s);
            case UNDETERMINED:
                return new Label("UNDETERMINED");
            case UNKNOWN_ERROR:
                return new Label("UNKNOWN_ERROR");

        }

        return new Label("??");
    }

    private Node unknownState() {
        return new Label("?");
    }
}
