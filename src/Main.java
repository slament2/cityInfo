import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javax.swing.*;
import java.awt.*;
import java.util.Currency;
import java.util.Locale;

public class Main {
  public static void main(String[] args) {
    Locale.setDefault(Locale.ENGLISH);

    SwingUtilities.invokeLater(
            () -> {
              JFrame jf = new JFrame();
              jf.setVisible(true);
              jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
              jf.setLayout(new FlowLayout());
              jf.setPreferredSize(new Dimension(820, 685));
              jf.setTitle("CityInfo");
              jf.setResizable(false);

              JLabel cityLabel = new JLabel("City: ");
              JLabel countryLabel = new JLabel("Country: ");
              JLabel currencyLabel = new JLabel("Currency: ");

              JTextField cityTextField = new JTextField();
              JTextField countryTextField = new JTextField();
              JTextField currencyTextField = new JTextField();
              cityTextField.setPreferredSize(new Dimension(80,20));
              countryTextField.setPreferredSize(new Dimension(80,20));
              currencyTextField.setPreferredSize(new Dimension(80,20));
              countryTextField.setText("Poland");

              jf.add(cityLabel);
              jf.add(cityTextField);
              jf.add(countryLabel);
              jf.add(countryTextField);
              jf.add(currencyLabel);
              jf.add(currencyTextField);

              JButton submitButton = new JButton("Submit");
              jf.add(submitButton);

              cityTextField.setText("Otwock");
              currencyTextField.setText("JPY");

              JFXPanel  jfxPanel = new JFXPanel();
              jf.add(jfxPanel);

                submitButton.addActionListener((e) ->{
                    String msg;
                    try {
                        Service actionService = new Service(countryTextField.getText(), args[0]);

                        String json = actionService.getWeather(cityTextField.getText());

                        JsonParser parser = new JsonParser();
                        JsonObject object = (JsonObject) parser.parse(json);
                        JsonObject main = (JsonObject) object.get("main");
                        JsonArray weather = (JsonArray) object.get("weather");
                        JsonObject weatherObject = (JsonObject) weather.get(0);
                        JsonPrimitive desc = (JsonPrimitive) weatherObject.get("description");
                        JsonObject wind = (JsonObject) object.get("wind");
                        JsonPrimitive windSpeed = (JsonPrimitive) wind.get("speed");

                        int temp = (int) Math.round(Double.parseDouble(main.get("temp").toString()) - 273.15);
                        int sensedTemp = (int) Math.round(Double.parseDouble(main.get("feels_like").toString()) - 273.15);
                        String description = ("" + desc.toString().charAt(1)).toUpperCase() + desc.toString().substring(2, desc.toString().length() - 1);
                        int windSpeedInt = (int) Math.round(Double.parseDouble(windSpeed.toString()) * 3.6);

                        msg = "Temperature: " + temp + "°C\nSensed Temperature: " + sensedTemp + "°C\nDescription: " + description + "\nWind speed: " + windSpeedInt + " km/h";

                        boolean currencyCodeCorrect = false;

                        for (Locale locale : Locale.getAvailableLocales()) {
                            try {
                                if (Currency.getInstance(locale).getCurrencyCode().equals(currencyTextField.getText())) {
                                    msg += "\n\n" + currencyTextField.getText() + " to " + actionService.currencyCode + " rate: " + actionService.getRateFor(currencyTextField.getText());
                                    currencyCodeCorrect = true;
                                    break;
                                }
                            } catch (Exception ignored) {
                            }
                        }

                        if (!cityTextField.getText().equals("") && !countryTextField.getText().equals("") && !json.contains("Incorrect data") && actionService.countryCode != null && currencyCodeCorrect) {
                            Platform.runLater(() ->
                            {
                                WebView webView = new WebView();
                                Scene scene = new Scene(webView);
                                jfxPanel.setScene(scene);
                                webView.getEngine().load("https://wikipedia.org/wiki/" + cityTextField.getText());
                            });

                            jf.repaint();

                            msg += "\nNBP rate: " + actionService.getNBPRate();

                        } else
                            msg = "Incorrect data";
                    }catch (Exception execption){
                        msg="Incorrect data";
                    }
                    JOptionPane.showMessageDialog(null,msg);
                });

              jf.pack();
            }
    );
  }
}
