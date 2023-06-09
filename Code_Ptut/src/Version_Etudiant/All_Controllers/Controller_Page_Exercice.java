package Version_Etudiant.All_Controllers;

import java.awt.Desktop;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Version_Etudiant.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.media.*;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Duration;

public class Controller_Page_Exercice implements Initializable{

	public static final String STYLE_MENU = "/Version_Etudiant/FXML_Files/MenuAndButtonStyles.css";
	public static final String TEST_CSS = "/Version_Etudiant/FXML_Files/darkModeTest.css";
	//Variables qui vont contenir les diff�rentes informations sur l'exercice
	//Informations textuelles
	public static String contenuTranscription;
	public static String contenuConsigne;
	public static Media contenuMedia;
	public static String caractereOccul;

	//Options de l'exercice
	public static boolean sensiCasse;
	public static boolean entrainement;
	public static boolean evaluation;
	public static String nbMin;
	public static boolean solution;
	public static boolean motDecouverts;
	public static boolean motIncomplet;
	public static boolean lettres_2;
	public static boolean lettres_3;
	public static Image contenuImage;

	//TextFields et autre composants qui contiennent les informations de l'exercice
	@FXML private TextArea transcription;
	@FXML private TextArea consigne;
	@FXML private ImageView imageView;
	@FXML private MediaView mediaView;
	@FXML private Text time;
	@FXML private Label titleTime;
	@FXML private TextField motPropose;

	//Ce qui concerne le media
	@FXML private ImageView firstPlay;
	@FXML private ImageView playOrPause;
	MediaPlayer mediaPlayer = new MediaPlayer(contenuMedia);
	Image play = new Image("/Image/Play.png");
	Image pause = new Image("/Image/Pause.png");
	Image sonCoupe = new Image("/Image/VolumeCoupe.png");
	Image sonPasCoupe = new Image("/Image/Volume.png");
	@FXML private Slider sliderSon;
	@FXML private Slider sliderVideo;
	@FXML private ImageView son;

	//Gestion du timer
	private Timeline timer;
	private Integer sec = 0;
	private Integer min;
	private boolean timerEstDeclenche = false;

	//Autres boutons
	@FXML private Button ButtonAide;
	@FXML private Button ButtonSolution;
	@FXML private ImageView alertSolution;

	//Listes des mots pour l'�tudiant
	private ArrayList<String> lesMots = new ArrayList<>();
	private ArrayList<String> lesMotsSensiCasse = new ArrayList<>();
	private ArrayList<String> lesMotsEtudiant = new ArrayList<>();
	private ArrayList<Integer> estDecouvert = new ArrayList<>();
	private String encryptedText;
	
	public String getEncryptedText() {
		return encryptedText;
	}

	public String getClearText() {
		return clearText;
	}


	private String clearText = contenuTranscription;
	public int numberPartialReplacement;

	//Tout ce qui concerne la barre de progression
	@FXML private ProgressBar progressBar;
	@FXML private Label pourcentageMots;
	@FXML private Label labelMotsDecouverts;
	private float nbMotsDecouverts = 0;
	private float nbMotsTotal;

	//Tooltip
	@FXML private ImageView questionConsigne;
	@FXML private ImageView questionTranscription;
	@FXML private ImageView questionProposition;

	@FXML private CheckMenuItem dark;
	@FXML private Button validateButton;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		int i;
		String mot = "", motCrypte = "";

		encryptedText = encryptText();
		numberPartialReplacement = 0;
		//On fait en sorte � ce que le texte ne d�passe pas du cadre
		transcription.setWrapText(true);


		//On initialise la liste estDecouvert
		for(String w : lesMots) {
			if(regexPoint(w)) {
				estDecouvert.add(1);
			} else {
				estDecouvert.add(0);
			}
		}

		//On passe les mots comparatifs en minuscule dans une autre liste
		for(String word : lesMots) {
			lesMotsSensiCasse.add(word.toLowerCase());

			if(!regexPoint(word)) {
				nbMotsTotal++;
			}
		}

		//On load la consigne
		if(contenuConsigne != null) {
			consigne.setText(contenuConsigne);
		}

		//On load le media
		if(contenuMedia != null) {
			mediaView.setMediaPlayer(mediaPlayer);
		}

		//On load l'image quand il s'agit d'un mp3
		if(contenuImage != null) {
			imageView.setImage(contenuImage);
		}

		//On load le temps n�cessaire si c'est en mode Evaluation
		if(evaluation) {
			min = Integer.parseInt(nbMin);
			time.setText(min + ":" + sec);

			//On masque les boutons qui ne sont pr�sent que ne mode entrainement
			ButtonAide.setVisible(false);
			ButtonSolution.setVisible(false);
			alertSolution.setVisible(false);
			//Si l'enseignant n'a pas souhait� l'affichage de mots d�couverts en temps r�el
			progressBar.setVisible(false);
			pourcentageMots.setVisible(false);
			labelMotsDecouverts.setVisible(false);

		} 
		//Sinon cela veut dire que l'on est en mode Entrainement
		else {

			titleTime.setText("Temps Ecoul�");
			min = 00;
			time.setText("00:00");

			//Si l'enseignant n'a pas souhait� autoriser l'affichage de la solution
			if(solution) {
				ButtonSolution.setVisible(false);
				alertSolution.setVisible(false);
			}

			//Si l'enseignant n'a pas souhait� l'affichage de mots d�couverts en temps r�el
			if(motDecouverts) {
				progressBar.setVisible(false);
				pourcentageMots.setVisible(false);
				labelMotsDecouverts.setVisible(false);
			}

			if(lettres_2) {
				numberPartialReplacement = 2;
			} else if(lettres_3){
				numberPartialReplacement = 3;
			}
		}

		//On fait appara�tre une fen�tre pour que l'�tudiant rentre son nom et pr�nom en vue du futur enregistrement
		//Note : Seulement si l'exercice est en mode Entrainement
		try {
			if(evaluation) {
				popUpEnregistrement();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		sliderSonChange();
		sliderVideoChange();

		validateButton.setOnAction(ActionEvent -> {
			try {
				verify(motPropose.getText());
				motPropose.setText("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		transcription.setText(encryptedText);

	}

	private String encryptText() {
		String constructString = "";
		for (String string : clearText.split("")) {
			if (string.matches("[a-zA-Z]") || (string.matches("[0-9]"))) {
				constructString += caractereOccul;
			}else {
				constructString += string;
			}
		}
		return constructString;
	}

	public void sliderSonChange() {
		// Change le volume sonore selon la valeur du slider
		sliderSon.valueProperty().addListener((o -> {
			mediaPlayer.setVolume(sliderSon.getValue() / 100.0); 

			if(sliderSon.getValue() == 0) {
				son.setImage(sonCoupe);
			} else {
				son.setImage(sonPasCoupe);
			}
		}));
	}

	//Fonction qui fait avancer le slider en fonction de la video
	public void sliderVideoChange() {

		mediaPlayer.setOnReady(new Runnable() {

			@Override
			public void run() {
				sliderVideo.setMax(mediaPlayer.getTotalDuration().toSeconds());
			}
		});

		// Ecoute sur le slider. Quand il est modifi�, modifie le temps du media player.
		InvalidationListener sliderChangeListener = o -> {
			Duration seekTo = Duration.seconds(sliderVideo.getValue());
			mediaPlayer.seek(seekTo);
		};
		sliderVideo.valueProperty().addListener(sliderChangeListener);

		// Lie le temps du media player au slider
		mediaPlayer.currentTimeProperty().addListener(l -> {

			sliderVideo.valueProperty().removeListener(sliderChangeListener);

			// Met a jour la valeur de temps du m�dia avec la position du slider.
			Duration currentTime = mediaPlayer.getCurrentTime();
			sliderVideo.setValue(currentTime.toSeconds());

			// R�activation de l'�coute du slider
			sliderVideo.valueProperty().addListener(sliderChangeListener);

		});
	}

	//Fonction qui permet de mute le son
	@FXML
	public void sonCoupe(MouseEvent event) {

		if(mediaPlayer.getVolume() != 0) {
			son.setImage(sonCoupe);
			mediaPlayer.setVolume(0);
		} else {
			son.setImage(sonPasCoupe);
			mediaPlayer.setVolume(sliderSon.getValue() / 100);
		}

	}

	//Fonction qui lance le media pour la premiere fois 
	@FXML
	public void firstPlay(MouseEvent event) {

		mediaPlayer.play();
		setKeyboardShortcut();
		

		if(timerEstDeclenche) {
			gestionTimer();
			timerEstDeclenche = true;
		}

		firstPlay.setVisible(false);
	}

	//Fonction qui play / pause le media
	@FXML
	public void playOrPause(MouseEvent event) {

		if(mediaPlayer.getStatus() == Status.PLAYING) {
			mediaPlayer.pause();
			playOrPause.setImage(play);
		}

		if(mediaPlayer.getStatus() == Status.PAUSED) {
			mediaPlayer.play();
			playOrPause.setImage(pause);
		}

	}

	//Fonction qui regarde si le mot contient un caract�re de ponctuation
	private boolean regexPoint(String mot) {

		for(int i = 0; i < mot.length(); i++) {
			if((mot.charAt(i) + "").matches("[.,;!?:]")) {
				return true;
			}
		}
		return false;
	}

	//M�thode qui permet de se rendre au manuel utilisateur == tuto
	@FXML
	public void tuto() throws IOException, URISyntaxException {

		InputStream is = MainEtudiant.class.getResourceAsStream("Manuel_Utilisateur.pdf");

		File pdf = File.createTempFile("Manuel Utilisateur", ".pdf");
		pdf.deleteOnExit();
		try(OutputStream out = new FileOutputStream(pdf)) {

			byte[] buffer = new byte[4096];
			int bytesRead = 0;

			while (is.available() != 0) {
				bytesRead = is.read(buffer);
				out.write(buffer, 0, bytesRead);
			}

			is.close();
		}
		Desktop.getDesktop().open(pdf);

	}

	//M�thode qui fait appara�tre la popUp pour que l'�tudiant rentre ses infos pour l'enregistrement
	public void popUpEnregistrement() throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/Version_Etudiant/FXML_Files/EnregistrementApresOuverture.fxml"));
		Stage stage = new Stage();
		Rectangle rect = new Rectangle(900,500);
		rect.setArcHeight(20.0);
		rect.setArcWidth(20.0);
		root.setClip(rect);

		//On bloque sur cette fen�tre
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.TRANSPARENT);
		Scene scene = new Scene(root, 900, 500);
		scene.setFill(Color.TRANSPARENT);
		darkModeActivation(scene);

		//On bloque le resize
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
		DeplacementFenetre.deplacementFenetre((Pane) root, stage);
	}

	//M�thode qui regarde si le darkMode est actif et l'applique en cons�quence � la scene
	public void darkModeActivation(Scene scene) {
		if(Controller_Menu.isDark) {
			scene.getStylesheets().removeAll(getClass().getResource(STYLE_MENU).toExternalForm());
			scene.getStylesheets().addAll(getClass().getResource(TEST_CSS).toExternalForm());
			dark.setSelected(true);
		} else {
			scene.getStylesheets().removeAll(getClass().getResource(TEST_CSS).toExternalForm());
			scene.getStylesheets().addAll(getClass().getResource(STYLE_MENU).toExternalForm());
			dark.setSelected(false);
		}
	}

	//M�thode pur afficher l'aide propos�e par l'enseignant
	@FXML
	public void affichageAide() throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/Version_Etudiant/FXML_Files/Aides.fxml"));
		Stage stage = new Stage();
		Rectangle rect = new Rectangle(400,600);
		rect.setArcHeight(20.0);
		rect.setArcWidth(20.0);
		root.setClip(rect);

		//On bloque sur cette fen�tre
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.TRANSPARENT);
		Scene scene = new Scene(root, 400, 600);
		scene.setFill(Color.TRANSPARENT);
		darkModeActivation(scene);

		stage.setScene(scene);
		stage.show();
		DeplacementFenetre.deplacementFenetre((Pane) root, stage);
	}

	//M�thode pour afficher la solution
	@FXML
	public void affichageSolution() throws IOException {

		retourMenu();

		Parent root = FXMLLoader.load(getClass().getResource("/Version_Etudiant/FXML_Files/Solution.fxml"));
		Stage stage = new Stage();
		Rectangle rect = new Rectangle(600,400);
		rect.setArcHeight(20.0);
		rect.setArcWidth(20.0);
		root.setClip(rect);

		//On bloque sur cette fen�tre
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.TRANSPARENT);
		Scene scene = new Scene(root, 600, 400);
		scene.setFill(Color.TRANSPARENT);
		darkModeActivation(scene);

		stage.setScene(scene);
		stage.show();
		DeplacementFenetre.deplacementFenetre((Pane) root, stage);

	}

	private void verify(String text) throws IOException {
		if (text == null) {
			return;
		}
		String[] encrypted = encryptedText.split("[ \\t\\n\\x0B\\f\\r]");
		String[] clear = clearText.split("[ \\t\\n\\x0B\\f\\r]");
		Pattern punctionLessPattern = Pattern.compile("[^\\p{Punct}&&[^'-]]*");
		Matcher clearMatcher;
		for (int i = 0; i < clear.length; i++) {
			clearMatcher = punctionLessPattern.matcher(clear[i]);
			if (clearMatcher.find() && clearMatcher.group(0).toLowerCase().equals(text.toLowerCase())) {
				if (sensiCasse && !clearMatcher.group(0).equals(text)) {
					continue;
				}
				encrypted[i]=clear[i];
			}
			Pattern numberCharPattern = Pattern.compile(".{4,}");
			Matcher numberCharMatcher = numberCharPattern.matcher(clear[i]);
			if (numberCharMatcher.find() && numberPartialReplacement > 0 && text.length() >= numberPartialReplacement 
					&& encrypted[i].substring(0,text.length()).contains(""+caractereOccul) 
					&& numberCharMatcher.group().substring(0,text.length()).equals(text)) {

				encrypted[i] = numberCharMatcher.group(0).substring(0,text.length());
				for (int j = text.length(); j < clearMatcher.group(0).length(); j++) {
					encrypted[i] += caractereOccul;
				}
				encrypted[i] += clear[i].substring(clearMatcher.group(0).length());
			}
		}
		encryptedText = "";
		int length =0;
		for (int i = 0; i < encrypted.length; i++) {
			encryptedText += encrypted[i];
			if (length + clear[i].length() < clearText.length()) {
				length += clear[i].length();
			}
			if (Character.isWhitespace(clearText.charAt(length)) || Character.isSpaceChar(clearText.charAt(length))) {
				encryptedText += clearText.charAt(length);
			}
			length++;
		}

		int ok = 0;

		if(motDecouverts) {
			int numberWord = clear.length;
			int numberFoundWord = 0;
			for (String string : encrypted) {
				if (!string.contains(caractereOccul)) {
					numberFoundWord++;
				}
			}
			progressBar.setProgress( (double) numberFoundWord / (double) numberWord);
			pourcentageMots.setText(Math.round(( (double) numberFoundWord / (double) numberWord) * 100)  + "%");

			if (Math.round(( (double) numberFoundWord / (double) numberWord) * 100) == 100){
				ok = 1;
			}
		}

		if(ok == 1) {

			//Si c'est le cas, on enregistre son exercice, puis on load une popUp
			retourMenu();

			if(evaluation) {
				finExercice();
				enregistrementExo();
			}
		}

		transcription.setText(encryptedText);
	}


	//M�thode pour quitter l'application
	@FXML
	public void quitter(ActionEvent event) {
		Platform.exit();
	}

	public ArrayList<String> getLesMots() {
		return lesMots;
	}

	public ArrayList<String> getLesMotsEtudiant() {
		return lesMotsEtudiant;
	}

	//M�thode qui va load le temps �coul� pour le mode �valuation
	public void finExercice() throws IOException {
		Stage stage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/Version_Etudiant/FXML_Files/ValidationEnregistrement.fxml"));
		//On bloque sur cette fen�tre
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.TRANSPARENT);
		DeplacementFenetre.deplacementFenetre((Pane) root, stage);
		Scene scene = new Scene(root, 350, 180);
		stage.setScene(scene);
		darkModeActivation(scene);
		stage.show();
	}


	//M�thode permettant de cr�er un timer pour que l'�tudiant voit le temps qui d�file en mode Evaluation
	public void gestionTimer() {
		timer = new Timeline();
		timer.setCycleCount(Timeline.INDEFINITE);
		if(evaluation) {
			gestionTimerEvaluation();
		}
		else {
			gestionTimerExercice();
		}
	}

	private void gestionTimerExercice() {
		timer.getKeyFrames().add(
				new KeyFrame(Duration.seconds(1),
						new EventHandler<ActionEvent>() {
					// KeyFrame event handler
					@Override
					public void handle(ActionEvent arg0) {
						sec++;
						if (sec > 59) {
							min++;
							sec = 00;
						}

						// update timerLabel
						time.setText(min +":"+ sec +"s");

					}
				}));
		timer.playFromStart();
	}

	private void gestionTimerEvaluation() {
		timer.getKeyFrames().add(
				new KeyFrame(Duration.seconds(1),
						new EventHandler<ActionEvent>() {
					// KeyFrame event handler
					@Override
					public void handle(ActionEvent arg0) {
						sec--;
						if (sec < 0) {
							min--;
							sec=59;
						}
						// update timerLabel
						time.setText(min +":"+ sec +"s");

						//S'il ne reste plus de temps, on load la fenetre d'enregistrement
						if (sec <= 0 && min<=0) {
							timer.stop();
							try {
								retourMenu();
								loadEnregistrement();
								enregistrementExo();
							} catch (IOException e) {
								e.printStackTrace();
							}
							return;
						}

					}
				}));
		timer.playFromStart();
	}

	//M�thode qui survient lorsque le timer est �coul� en mode Evaluation
	public void loadEnregistrement() throws IOException {
		Stage stage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/Version_Etudiant/FXML_Files/TempsEcoule.fxml"));
		//On bloque sur cette fen�tre
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.UNDECORATED);
		DeplacementFenetre.deplacementFenetre((Pane) root, stage);
		//On bloque le resize
		stage.setResizable(false);
		Scene scene = new Scene(root, 500, 300);
		stage.setScene(scene);
		darkModeActivation(scene);
		stage.show();
	}

	//M�thode qui va enregistrer l'exercice de l'�tudiant
	public void enregistrementExo() throws IOException {

		File file = new File(Controller_EnregistrementApresOuverture.repertoireEtudiant + "\\" + Controller_EnregistrementApresOuverture.nomExo
				+ "_" + Controller_EnregistrementApresOuverture.nomEtudiant + "_" + Controller_EnregistrementApresOuverture.prenEtudiant + ".rct");
		FileWriter fwrite = new FileWriter(file);
		try(BufferedWriter buffer = new BufferedWriter(fwrite)) {

			buffer.write(transcription.getText());
			buffer.newLine();
			buffer.write(Double.toString(Math.round((nbMotsDecouverts / nbMotsTotal) * 100)) + '%');
			fwrite.close();
		}
	}

	//M�thode pour retourner au menu
	public void retourMenu() throws IOException {
		Stage stage = (Stage) alertSolution.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/Version_Etudiant/FXML_Files/Menu.fxml"));
		Scene scene = new Scene(root,  MainEtudiant.width, MainEtudiant.height - 60);
		stage.setScene(scene);
		darkModeActivation(scene);
		stage.show();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	//M�thode pour affiicher une toolTip consigne + redimension d'image
	@FXML
	public void tipConsigneEnter() {
		Tooltip t = new Tooltip("Il s'agit de la consigne donn�e par le professeur");
		questionConsigne.setFitWidth(questionConsigne.getFitWidth() + 2);
		questionConsigne.setFitHeight(questionConsigne.getFitHeight() + 2);
		Tooltip.install(questionConsigne, t);
	}

	//M�thode pour redimensionner l'image de la consigne quand on sort du champ
	@FXML
	public void tipConsigneExit() {
		questionConsigne.setFitWidth(questionConsigne.getFitWidth() - 2);
		questionConsigne.setFitHeight(questionConsigne.getFitHeight() - 2);
	}

	//M�thode pour affiicher une toolTip transcription + redimension d'image
	@FXML
	public void tipTranscriptionEnter() {
		Tooltip t = new Tooltip("Il s'agit du script de la vid�o que vous devez essayer de retrouver");
		questionTranscription.setFitWidth(questionTranscription.getFitWidth() + 2);
		questionTranscription.setFitHeight(questionTranscription.getFitHeight() + 2);
		Tooltip.install(questionTranscription, t);
	}

	//M�thode pour redimensionner l'image de la transcription quand on sort du champ
	@FXML
	public void tipTranscriptionExit() {
		questionTranscription.setFitWidth(questionTranscription.getFitWidth() - 2);
		questionTranscription.setFitHeight(questionTranscription.getFitHeight() - 2);
	}

	//M�thode pour affiicher une toolTip transcription + redimension d'image
	@FXML
	public void tipPropositionEnter() {
		Tooltip t = new Tooltip("Rentrez ici les mots que vous pensez entendre dans le document audio ou vid�o");
		questionProposition.setFitWidth(questionProposition.getFitWidth() + 2);
		questionProposition.setFitHeight(questionProposition.getFitHeight() + 2);
		Tooltip.install(questionProposition, t);
	}

	//M�thode pour redimensionner l'image de la transcription quand on sort du champ
	@FXML
	public void tipPropositionExit() {
		questionProposition.setFitWidth(questionProposition.getFitWidth() - 2);
		questionProposition.setFitHeight(questionProposition.getFitHeight() - 2);
	}

	//M�thode pour passer ou non le darkMode
	@FXML
	public synchronized void darkMode() {

		if(dark.isSelected()) {
			ButtonAide.getScene().getStylesheets().removeAll(getClass().getResource(STYLE_MENU).toExternalForm());
			ButtonAide.getScene().getStylesheets().addAll(getClass().getResource(TEST_CSS).toExternalForm());
			Controller_Menu.isDark = true;
		} else {
			ButtonAide.getScene().getStylesheets().removeAll(getClass().getResource(TEST_CSS).toExternalForm());
			ButtonAide.getScene().getStylesheets().addAll(getClass().getResource(STYLE_MENU).toExternalForm());
			Controller_Menu.isDark = false;
		}
	}


	private void setKeyboardShortcut() {
		ButtonAide.getScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (isTextFieldAndNotNull(ButtonAide.getScene().focusOwnerProperty().get())
				&& isKeyEnterOrSpace(event.getCode())
				   && !motPropose.getText().isEmpty()) {
					try {
						verify(motPropose.getText());
						motPropose.setText("");
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}


				 else if (event.getCode() == KeyCode.SPACE) {
					if (mediaView.getMediaPlayer().getStatus() == Status.PAUSED) {
						mediaView.getMediaPlayer().play();
						playOrPause.setImage(pause);
					}
					if (mediaView.getMediaPlayer().getStatus() == Status.PLAYING) {
						mediaView.getMediaPlayer().pause();
						playOrPause.setImage(play);
					}

				}
				if (event.getCode() == KeyCode.RIGHT && mediaView.getMediaPlayer().getTotalDuration().greaterThan(mediaView.getMediaPlayer().getCurrentTime().add(new Duration(5000)))) {
					mediaView.getMediaPlayer().seek(mediaView.getMediaPlayer().getCurrentTime().add(new Duration(5000)));
				}
				if (event.getCode() == KeyCode.LEFT && new Duration(0).lessThan(mediaView.getMediaPlayer().getCurrentTime().subtract(new Duration(5000)))) {
					mediaView.getMediaPlayer().seek(mediaView.getMediaPlayer().getCurrentTime().subtract(new Duration(5000)));
				}
				if (event.getCode() == KeyCode.UP) {
					sliderSon.setValue(sliderSon.getValue() + 3);
				}
				if (event.getCode() == KeyCode.DOWN) {
					sliderSon.setValue(sliderSon.getValue() - 3);
				}
			}

		});
		
		ButtonAide.getScene().addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				 if ((ButtonAide.getScene().focusOwnerProperty().get() instanceof TextField)
				 && isKeyEnterOrSpace(event.getCode())) {
	    						motPropose.setText("");
	                    }
	                }

		});
	}

	public boolean isKeyEnterOrSpace(KeyCode keyCode) {
		return (keyCode == KeyCode.ENTER) || (keyCode == KeyCode.SPACE);
	}

	public boolean isTextFieldAndNotNull(Node obj) {
		return (obj instanceof TextField);
	}
}
