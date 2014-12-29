package se.purplescout.pong.competition.lan.server;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import se.purplescout.pong.competition.headless.OnPaddleRemovedListener;
import se.purplescout.pong.competition.paddlecache.PaddleCache;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.competition.headless.AutoFight;

import java.util.*;

public class HighScore extends GridPane {

    public static final Font FONT = new Font(null, 30);
    public static final int ROWS_PER_COL = 10;

    private List<AutoFight> fights;
    private OnPaddleRemovedListener onPaddleRemovedListener;

    public HighScore() {
        this.setVgap(10);
        showNoFightsMessage();
    }

    public void setOnPaddleRemovedListener(OnPaddleRemovedListener onPaddleRemovedListener) {
        this.onPaddleRemovedListener = onPaddleRemovedListener;
    }

    public void setFights(List<AutoFight> fights) {
        this.fights = fights;
        if (fights.isEmpty()) {
            showNoFightsMessage();
        } else {
            SortedMap<Paddle, Integer> scores = calculateAndSortScoresForEachPaddle(fights);
            renderPaddlesAndScores(scores);
        }
    }

    private void showNoFightsMessage() {
        Label noFightsMessage = new Label("Not enough paddles yet. Bring it ooooon!");
        noFightsMessage.setFont(FONT);
        noFightsMessage.setTextFill(Color.WHITE);

        this.getChildren().clear();
        this.getChildren().add(noFightsMessage);
    }

    public static SortedMap<Paddle, Integer> calculateAndSortScoresForEachPaddle(Iterable<AutoFight> fights) {
        Map<Paddle, Integer> unsortedScores = new HashMap<>();
        for (AutoFight fight : fights) {
            Integer leftScore = unsortedScores.get(fight.getLeftPaddle());
            Integer rightScore = unsortedScores.get(fight.getRightPaddle());

            if (leftScore == null) {
                leftScore = 0;
            }
            if (rightScore == null) {
                rightScore = 0;
            }

            leftScore += fight.getLeftScore();
            rightScore += fight.getRightScore();

            unsortedScores.put(fight.getLeftPaddle(), leftScore);
            unsortedScores.put(fight.getRightPaddle(), rightScore);
        }

        SortedMap<Paddle, Integer> sortedScores = new TreeMap<>(new ValueComparator(unsortedScores));
        sortedScores.putAll(unsortedScores);
        return sortedScores;
    }

    private void renderPaddlesAndScores(SortedMap<Paddle, Integer> scores) {
        this.getChildren().clear();
        this.getRowConstraints().clear();
        int x = 0;
        int y = 0;

        int place = 1;
        for (final Map.Entry<Paddle, Integer> entry : scores.entrySet()) {
            Label placeLabel = buildPlaceLabel(FONT, place);
            Label name = buildTeamNameLabel(FONT, place, entry);
            Label totalNumberOfWins = buildScoreLabel(FONT, entry);

            Region fightDetails = buildFightDetailsFor(entry.getKey(), (removedPaddle) -> {
                if (onPaddleRemovedListener != null) {
                    this.getChildren().clear();
                    onPaddleRemovedListener.paddleRemoved(entry.getKey());
                }
            });

            final double realPref = fightDetails.getPrefHeight();
            final RowConstraints fightDetailsRow = new RowConstraints(0);
            fightDetails.setVisible(false);

            name.setOnMouseClicked((event) -> {
                fightDetails.setVisible(!fightDetails.isVisible());

                if (fightDetails.isVisible()) {
                    fightDetailsRow.setPrefHeight(realPref);
                } else {
                    fightDetailsRow.setPrefHeight(0);
                }
            });

            this.add(placeLabel, x, y);
            this.add(name, x + 1, y);
            this.add(totalNumberOfWins, x + 2, y);
            this.add(fightDetails, x, y + 1);

            this.getRowConstraints().add(new RowConstraints());
            this.getRowConstraints().add(fightDetailsRow);
            GridPane.setColumnSpan(fightDetails, 3);

            GridPane.setHalignment(placeLabel, HPos.RIGHT);
            GridPane.setHgrow(placeLabel, Priority.ALWAYS);
            GridPane.setHgrow(name, Priority.NEVER);
            GridPane.setHgrow(totalNumberOfWins, Priority.ALWAYS);

            y += 2;
            if (false && y == ROWS_PER_COL) {
                y = 0;
                x += 3;
            }

            place++;
        }
    }

    private Label buildPlaceLabel(Font font, int place) {
        Label placeLabel = new Label("#" + place);
        placeLabel.setTextFill(Color.GRAY);
        placeLabel.setFont(font);
        return placeLabel;
    }

    private Label buildTeamNameLabel(Font font, int place, Map.Entry<Paddle, Integer> entry) {
        String teamName = entry.getKey().getTeamName();
        Label name = new Label(teamName);
        name.setTextFill(Color.WHITE);
        name.setFont(font);
        name.setStyle("-fx-padding: 0 1cm 0 5mm");
        if (place == 1) {
            name.setGraphic(new ImageView("se/purplescout/pong/competition/lan/server/medal_gold.png"));
        } else if (place == 2) {
            name.setGraphic(new ImageView("se/purplescout/pong/competition/lan/server/medal_silver.png"));
        } else if (place == 3) {
            name.setGraphic(new ImageView("se/purplescout/pong/competition/lan/server/medal_bronze.png"));
        }

        return name;
    }

    private Label buildScoreLabel(Font font, Map.Entry<Paddle, Integer> entry) {
        Label totalNumberOfWins = new Label(String.valueOf(entry.getValue()));
        totalNumberOfWins.setTextFill(Color.WHITE);
        totalNumberOfWins.setFont(font);
        return totalNumberOfWins;
    }

    private Region buildFightDetailsFor(Paddle pov, OnPaddleRemovedListener onPaddleRemovedListener) {
        GridPane fightGrid = new GridPane();
        fightGrid.setAlignment(Pos.CENTER);

        int column = 0;
        // TODO: Sort fights according to something?
        for (int i = 0; i < fights.size(); i++) {
            AutoFight fight = fights.get(i);
            if (fight.getLeftPaddle() == pov || fight.getRightPaddle() == pov) {
                Paddle opponent = getOpponent(fight, pov);

                Label opponentName = new Label(PaddleCache.getTeamName(opponent.getClass()));
                opponentName.setTextFill(Color.WHITE);
                Region result = getNodeFromFightState(fight, pov, opponent);

                String resultRightBorderWidth = "0";
                if (isPartOfRemainingFights(pov, i)) {
                    opponentName.setStyle("-fx-border-width: 0 0.5mm 0 0; -fx-border-style: solid; -fx-border-color: white;");
                    resultRightBorderWidth = "0.5mm";
                }
                opponentName.setStyle(opponentName.getStyle() + "-fx-padding: 1mm;");
                result.setStyle("-fx-border-width: 0.5mm " +resultRightBorderWidth+ " 0 0; -fx-border-style: solid; -fx-border-color: white; -fx-padding: 1mm;");

                GridPane.setHalignment(opponentName, HPos.CENTER);
                GridPane.setHalignment(result, HPos.CENTER);
                fightGrid.add(opponentName, column, 0);
                fightGrid.add(result, column, 1);

                column++;
            }
        }

        alignCellWidths(fightGrid);

        Label removeButton = new Label(null, new ImageView("se/purplescout/pong/competition/lan/server/delete.png"));
        removeButton.setOnMouseClicked((e) -> onPaddleRemovedListener.paddleRemoved(pov));
        removeButton.setStyle("-fx-padding: 0 0 0 10mm;");

        BorderPane root = new BorderPane(fightGrid);
        root.setRight(removeButton);
        return root;
    }

    private Label whiteLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        return label;
    }

    private boolean isPartOfRemainingFights(Paddle pov, int fromIndexExclusive) {
        for (int i = fromIndexExclusive+1; i < fights.size(); i++) {
            AutoFight fight = fights.get(i);
            if (fight.getLeftPaddle() == pov || fight.getRightPaddle() == pov) {
                return true;
            }
        }
        return false;
    }

    private void alignCellWidths(GridPane grid) {
        for (Node n: grid.getChildren()) {
            if (n instanceof Control) {
                Control control = (Control) n;
                control.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                control.setStyle(control.getStyle() + "-fx-alignment: center;");
            }
            if (n instanceof Pane) {
                Pane pane = (Pane) n;
                pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                pane.setStyle(pane.getStyle() + "-fx-alignment: center;");
            }
        }
    }

    private Paddle getOpponent(AutoFight fight, Paddle pov) {
        if (fight.getLeftPaddle() == pov) {
            return fight.getRightPaddle();
        } else {
            return fight.getLeftPaddle();
        }
    }

    private Region getNodeFromFightState(AutoFight fight, Paddle pov, Paddle opponent) {
        switch (fight.getState()) {
            case BEFORE_FIGHT:
                return whiteLabel("Not fighting yet");
            case FIGHTING:
                return whiteLabel("Fighting...");
            case DONE_FIGHTING:
                return döneFajtäng(fight, pov, opponent);
        }

        return whiteLabel("?");
    }

    private Region döneFajtäng(AutoFight fight, Paddle pov, Paddle opponent) {
        boolean povIsLeft = fight.getLeftPaddle() == pov;

        switch (fight.getResult()) {
            case FIGHT_TOOK_TOO_LONG:
                return whiteLabel("Fight took too long");
            case LEFT_PADDLE_THREW_EXCEPTION:
                if (povIsLeft) {
                    return whiteLabel("You threw an exception");
                } else {
                    return whiteLabel("Opponent threw an exception");
                }
            case LEFT_PADDLE_TOOK_TOO_LONG:
                if (povIsLeft) {
                    return whiteLabel("You were too slow");
                } else {
                    return whiteLabel("Opponent was too slow");
                }
            case RIGHT_PADDLE_THREW_EXCEPTION:
                if (povIsLeft) {
                    return whiteLabel("Opponent threw an exception");
                } else {
                    return whiteLabel("You threw an exception");
                }
            case RIGHT_PADDLE_TOOK_TOO_LONG:
                if (povIsLeft) {
                    return whiteLabel("Opponent was too slow");
                } else {
                    return whiteLabel("You were too slow");
                }
            case SUCCESS:
                String s;
                if (povIsLeft) {
                    s = fight.getLeftScore() + " - " + fight.getRightScore();
                } else {
                    s = fight.getRightScore() + " - " + fight.getLeftScore();
                }
                return whiteLabel(s);
            case UNDETERMINED:
                // Only valid if fight.getState() != DONE_FIGHTING which is handled in getNodeFromFightState
                return whiteLabel("N/A");
            case UNKNOWN_ERROR:
                return whiteLabel("Unknown error");

        }

        return whiteLabel("??");
    }

    private static class ValueComparator implements Comparator<Paddle> {

        Map<Paddle, Integer> base;
        public ValueComparator(Map<Paddle, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(Paddle a, Paddle b) {

            Integer valueA = base.get(a);
            Integer valueB = base.get(b);
            if (valueA == null) {
                return 1;
            }

            if (valueB == null) {
                return -1;
            }

            int scoreDiff = valueB - valueA;
            if (scoreDiff == 0) {
                return PaddleCache.getTeamName(a.getClass()).compareTo(PaddleCache.getTeamName(b.getClass()));
            }
            return scoreDiff;
        }
    }
}
