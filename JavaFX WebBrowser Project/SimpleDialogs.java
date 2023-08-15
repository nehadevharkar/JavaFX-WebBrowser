import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import java.util.Optional;
import java.util.function.Supplier;

public class SimpleDialogs {
    public static boolean custom(Node content, String title) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().setContent(content);
        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static boolean vetoableInput(Node inputNode, String headerText, Supplier<String> testForErrorString) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Input is Requested");
        dialog.setHeaderText(headerText);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().setContent(inputNode);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, e -> {
             if (testForErrorString.get() != null) {
                 e.consume();
                 message("Input is incorrect: " + testForErrorString.get());
             }
        });
        
        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void message(String text) {
        message(text,"Message");
    }

    public static void message(String text, String title) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setGraphic(null);
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(makeText(text));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().setPadding(new Insets(10,10,0,10));
        dialog.showAndWait();
    }

    public static String prompt(String promptText) {
        return prompt(promptText,"Request for Input",null);
    }

    public static String prompt(String promptText, String title) {
        return prompt(promptText,title,null);
    }

    public static String prompt(String promptText, String title, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setGraphic(null);
        dialog.setHeaderText(promptText);
        Optional<String> reply = dialog.showAndWait();
        if (reply.isPresent())
            return reply.get();
        else
            return null;
    }

    public static String confirm(String promptText) {
        return confirm(promptText, "Confirmation needed (or cancel)");
    }

    public static String confirm(String message, String title) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.CANCEL, ButtonType.NO, ButtonType.YES);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES)
            return "yes";
        else if (result.isPresent() && result.get() == ButtonType.NO)
            return "no";
        else
            return "cancel";
    }

    public static Color colorChooser( Color initialColor ) {
        return colorChooser(initialColor, "Select a Color");
    }

    public static Color colorChooser( Color initialColor, String headerText ) {
        ColorChooserPane chooser = new ColorChooserPane(initialColor);
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Color Picker");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().setContent(chooser);
        dialog.setHeaderText(headerText);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK )
            return chooser.getColor();
        else
            return null;
    }

    private final static int TEXT_WIDTH = 450;

    //private final static int TEXT_WIDTH = 450;

    private static class ColorChooserPane extends GridPane {

        private Slider hueSlider, brightnessSlider, saturationSlider, redSlider, greenSlider, blueSlider;

        private Label hueLabel, brightnessLabel, saturationLabel, redLabel, greenLabel, blueLabel;

        private Pane colorPatch;  

        private Color currentColor;
        
        public ColorChooserPane(Color initialColor) {
            hueSlider = new Slider(0,360,0);
            saturationSlider = new Slider(0,1,1);
            brightnessSlider = new Slider(0,1,1);
            redSlider = new Slider(0,1,1);
            greenSlider = new Slider(0,1,0);
            blueSlider = new Slider(0,1,0);

            hueSlider.valueProperty().addListener( e -> newColor(hueSlider) );
            saturationSlider.valueProperty().addListener( e -> newColor(saturationSlider) );
            brightnessSlider.valueProperty().addListener( e -> newColor(brightnessSlider) );
            redSlider.valueProperty().addListener( e -> newColor(redSlider) );
            greenSlider.valueProperty().addListener( e -> newColor(greenSlider) );
            blueSlider.valueProperty().addListener( e -> newColor(blueSlider) );

            hueLabel = makeText(String.format(" Hue = %1.3f", 0.0));
            saturationLabel = makeText(String.format(" Saturation = %1.3f", 1.0));
            brightnessLabel = makeText(String.format(" Brightness = %1.3f", 1.0));
            redLabel = makeText(String.format(" Red = %1.3f", 1.0));
            greenLabel = makeText(String.format(" Green = %1.3f", 0.0));
            blueLabel = makeText(String.format(" Blue = %1.3f", 0.0));

            colorPatch = new Pane();
            colorPatch.setStyle("-fx-background-color:red; -fx-border-color:black; -fx-border-width:2px");
            
            GridPane root = this;
            ColumnConstraints c1 = new ColumnConstraints();
            c1.setPercentWidth(33);
            ColumnConstraints c2 = new ColumnConstraints();
            c2.setPercentWidth(34);
            ColumnConstraints c3 = new ColumnConstraints();
            c3.setPercentWidth(33);
            root.getColumnConstraints().addAll(c1, c2, c3);
            
            root.add(hueSlider, 0, 0);
            root.add(saturationSlider, 0, 1);
            root.add(brightnessSlider, 0, 2);
            root.add(redSlider, 0, 3);
            root.add(greenSlider, 0, 4);
            root.add(blueSlider, 0, 5);
            root.add(hueLabel, 1, 0);
            root.add(saturationLabel, 1, 1);
            root.add(brightnessLabel, 1, 2);
            root.add(redLabel, 1, 3);
            root.add(greenLabel, 1, 4);
            root.add(blueLabel, 1, 5);
            root.add(colorPatch, 2, 0, 1, 6);  // occupies 6 rows!
            root.setStyle("-fx-padding:5px; -fx-border-color:darkblue; -fx-border-width:2px; -fx-background-color:#DDF");
            
            setColor(initialColor == null? Color.BLACK : initialColor);
        }
               
        public Color getColor() {
            return currentColor;
        }
        
        public void setColor(Color color) {
            if (color == null)
                return;
            hueSlider.setValue(color.getHue());
            brightnessSlider.setValue(color.getBrightness());
            saturationSlider.setValue(color.getSaturation());
            redSlider.setValue(color.getRed());
            greenSlider.setValue(color.getGreen());
            blueSlider.setValue(color.getBlue());
            String colorString = String.format("#%02x%02x%02x", (int)(255*color.getRed()),
                    (int)(255*color.getGreen()), (int)(255*color.getBlue()) );
            colorPatch.setStyle("-fx-border-color:black; -fx-border-width:2px; -fx-background-color:" + colorString);
            hueLabel.setText(String.format(" Hue = %1.3f", color.getHue()));
            saturationLabel.setText(String.format(" Saturation = %1.3f", color.getSaturation()));
            brightnessLabel.setText(String.format(" Brightness = %1.3f", color.getBrightness()));
            redLabel.setText(String.format(" Red = %1.3f", color.getRed()));
            greenLabel.setText(String.format(" Green = %1.3f", color.getGreen()));
            blueLabel.setText(String.format(" Blue = %1.3f", color.getBlue()));
            currentColor = color;
        }
        
        private Label makeText(String message) {
               // Make a label to show a given message shown in bold, with some padding
               // between the text and the border of the label.
            Label text = new Label(message);
            text.setStyle("-fx-padding: 6px 10px 6px 10px; -fx-font-weight:bold");
            return text;
        }
        
        private void newColor(Slider whichSlider) {
                // Adjust the GUI to a new color value, when one of the sliders has changed.
            if ( ! whichSlider.isValueChanging() ) {
                return; // Don't respond to change if it was set programmatically;
                        // only respond if it was set by user dragging the slider.
            }
            Color color;
            if (whichSlider == redSlider || whichSlider == greenSlider || whichSlider == blueSlider) {
                color = Color.color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
                hueSlider.setValue(color.getHue());
                brightnessSlider.setValue(color.getBrightness());
                saturationSlider.setValue(color.getSaturation());
            }
            else {
                color = Color.hsb(hueSlider.getValue(), saturationSlider.getValue(), brightnessSlider.getValue());
                redSlider.setValue(color.getRed());
                greenSlider.setValue(color.getGreen());
                blueSlider.setValue(color.getBlue());
            }
            currentColor = color;
            String colorString = String.format("#%02x%02x%02x", (int)(255*color.getRed()),
                    (int)(255*color.getGreen()), (int)(255*color.getBlue()) );
            colorPatch.setStyle("-fx-border-color:black; -fx-border-width:2px; -fx-background-color:" + colorString);
            hueLabel.setText(String.format(" Hue = %1.3f", color.getHue()));
            saturationLabel.setText(String.format(" Saturation = %1.3f", color.getSaturation()));
            brightnessLabel.setText(String.format(" Brightness = %1.3f", color.getBrightness()));
            redLabel.setText(String.format(" Red = %1.3f", color.getRed()));
            greenLabel.setText(String.format(" Green = %1.3f", color.getGreen()));
            blueLabel.setText(String.format(" Blue = %1.3f", color.getBlue()));
        }    
    } 
}