package se.purplescout.pong.competition.lan.client.workhistory;

import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class WorkList extends HBox {

    private final Map<Object, Label> workItems = new HashMap<>();
    private final VBox itemList = new VBox();

    public WorkList() {
        itemList.minWidthProperty().bind(this.minWidthProperty());

        this.getChildren().add(clearButton());
        this.getChildren().add(itemList);

        this.setAlignment(Pos.BOTTOM_CENTER);
    }

    private Node clearButton() {
        Node button = new ImageView("se/purplescout/pong/competition/lan/client/workhistory/trash.png");
        button.setOnMouseClicked((e) -> clear());
        return button;
    }

    public void clear() {
        itemList.getChildren().clear();
        workItems.clear();
    }

    public void addWorkData(Object workId, String data) {
        Label item = workItems.get(workId);
        if (workId == null || item == null) {
            item = buildSingleMessageWidget(data, itemList.getChildren().size());
            workItems.put(workId, item);
            itemList.getChildren().add(item);
        } else {
            if (isSingleMessageWidget(item)) {
                promoteToMultiMessageWidgetAndAdd(workId, item, data);
            } else {
                appendToMultiMessageWidget(item, data);
            }
        }
    }

    private Label buildSingleMessageWidget(String data, int placeInList) {
        Label singleMessageWidget = new Label(data);

        if (placeInList == 0) {
            singleMessageWidget.setStyle("-fx-padding: 0 5mm 0 5mm;");
        } else {
            singleMessageWidget.setStyle("-fx-border-color: black;"
                    + "-fx-border-insets: 2mm 5mm 0mm 5mm;"
                    + "-fx-border-width: 0.1mm 0 0 0;"
                    + "-fx-border-style: solid;"
                    + "-fx-padding: 2mm 0 0 0;");
        }
        singleMessageWidget.minWidthProperty().bind(itemList.widthProperty());
        return singleMessageWidget;
    }

    private boolean isSingleMessageWidget(Label item) {
         // TODO: Funkar detta eller måste jag använda regexp?
        return item.getText().contains("\\n");
    }

    private void promoteToMultiMessageWidgetAndAdd(Object workId, Label item, String data) {
        Label multiMessageWidget = buildAndPopulateMultiMessageWidget(item, data);

        // Move the widget to the bottom
        itemList.getChildren().remove(item);
        itemList.getChildren().add(multiMessageWidget);

        workItems.put(workId, multiMessageWidget);
    }

    private Label buildAndPopulateMultiMessageWidget(Label item, String data) {
        String previousData = item.getText();
        String text = previousData + "\n" + data;
        item.setText(text);

        return item;
    }

    private void appendToMultiMessageWidget(Label item, String data) {
        String previousData = item.getText();
        String text = previousData + "\n" + data;
        item.setText(text);
    }
}
