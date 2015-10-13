package railview.swarmintelligence;


import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

 class SideBar extends AnchorPane {
  
    public Button getControlButton() { return controlButton; }
    private final Button controlButton;

    SideBar() {

      setStyle("-fx-padding: 10;-fx-background-color:yellow; -fx-border-color: derive(mistyrose, -10%); -fx-border-width: 3;");
      setVisible(false);

      controlButton = new Button("Show");
      controlButton.setMaxWidth(100);
   
      controlButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent actionEvent) {
          final double startWidth = getWidth();
          final Animation hideSidebar = new Transition() {
            { setCycleDuration(Duration.millis(250)); }
            protected void interpolate(double frac) {
              final double curWidth = startWidth * (1.0 - frac);
              setTranslateX(-startWidth + curWidth);
            }
          };
          hideSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
              setVisible(false);
              controlButton.setText("Show");
            }
          });
  
 
          final Animation showSidebar = new Transition() {
            { setCycleDuration(Duration.millis(250)); }
            protected void interpolate(double frac) {
              final double curWidth = startWidth * frac;
              setTranslateX(-startWidth + curWidth);
            }
          };
          showSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
              controlButton.setText("Hide");
            }
          });
  
          if (showSidebar.statusProperty().get() == Animation.Status.STOPPED && hideSidebar.statusProperty().get() == Animation.Status.STOPPED) {
            if (isVisible()) {
              hideSidebar.play();
            } else {
              setVisible(true);
              showSidebar.play();
            }
          }
        }
      });
    }
  }
  