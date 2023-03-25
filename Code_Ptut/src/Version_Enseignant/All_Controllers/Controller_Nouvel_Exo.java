package Version_Enseignant.All_Controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import Version_Enseignant.MainEnseignant;
import Version_Etudiant.DeplacementFenetre;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Controller_Nouvel_Exo implements Initializable{


	public static final String TEST_CSS = "/Version_Enseignant/FXML_Files/darkModeTest.css";
	public static final String STYLE_MENU = "/Version_Enseignant/FXML_Files/MenuAndButtonStyles.css";
	@FXML private TextField repertoire;
	@FXML private TextField nomExo;
	@FXML private Button okNouvelExo;
	
	public static String contenuRepertoire;
	public static String contenuNomExo;
	
	@FXML private CheckMenuItem dark;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////			INITIALISATION		////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//Méthode d'initialisation de la page
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		//On rempli les champs s'il ne sont pas null (si l'enseignant revient en arrière)
		if(contenuRepertoire != null) {
			repertoire.setText(contenuRepertoire);
		}
		
		if(contenuNomExo != null) {
			nomExo.setText(contenuNomExo);
		}
		
		//Si les deux champs sont remplis, on met le bouton cliquable
		if(contenuRepertoire != null && nomExo != null) {
			okNouvelExo.setDisable(false);
		}

		//Désactive le bouton "okNouvelExo" si l'utilisateur a définit un repertoire et un nom
		repertoire.textProperty().addListener((argument0,oldValue,newvalue) ->
			okNouvelExo.setDisable(repertoire.getText().isEmpty() || nomExo.getText().isEmpty()));
		
		//On regarde si le textField du Nom de l'exercice est vide ou non

		if (nomExo != null) nomExo.textProperty().addListener((argument0, oldValue, newValue) -> okNouvelExo.setDisable(nomExo.getText().isEmpty() || repertoire.getText().isEmpty()));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////			METHDOES GENERALES		////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//Bouton Quitter qui permet à l'enseignant de quitter l'application (disponible sur toutes les pages)
	@FXML
	public void quitter(ActionEvent event) throws IOException {
		
		Stage primaryStage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/Version_Enseignant/FXML_Files/ConfirmationQuitter.fxml"));
		Scene scene = new Scene(root, 400, 200);
		//On bloque sur cette fenêtre
		primaryStage.initModality(Modality.APPLICATION_MODAL);
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		scene.setFill(Color.TRANSPARENT);
		
		//Bordure
		Rectangle rect = new Rectangle(400,200); 
		rect.setArcHeight(20.0); 
		rect.setArcWidth(20.0);  
		root.setClip(rect);
		
		DeplacementFenetre.deplacementFenetre((Pane) root, primaryStage);
		primaryStage.setScene(scene);
		darkModeActivation(scene);
		primaryStage.show();
	}
	
	//Méthode qui permet de se rendre au manuel utilisateur == tuto
	@FXML
	public void tuto() throws MalformedURLException, IOException, URISyntaxException {
		
		InputStream is = MainEnseignant.class.getResourceAsStream("Manuel_Utilisateur.pdf");

		File pdf = File.createTempFile("Manuel Utilisateur", ".pdf");
		pdf.deleteOnExit();
        try (OutputStream out = new FileOutputStream(pdf)) {
			;

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
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////		METHODES SPECIFIQUES A LA PAGE		////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Bouton Nouveau qui permet de créer un nouvel exercice
	@FXML
	public void pageNouvelExo() throws IOException {
		
		//Réinitialisation des variables
		Controller_Page_Accueil c = new Controller_Page_Accueil();
		c.delete();
		
		Stage primaryStage = (Stage) repertoire.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/Version_Enseignant/FXML_Files/NouvelExo.fxml"));
		Scene scene = new Scene(root, MainEnseignant.width, MainEnseignant.height - 60);
		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		darkModeActivation(scene);
		primaryStage.show();
	}
	
	@FXML
	public void retourAccueil() throws IOException {
		Stage primaryStage = (Stage) repertoire.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/Version_Enseignant/FXML_Files/Menu.fxml"));
		Scene scene = new Scene(root, MainEnseignant.width, MainEnseignant.height - 60);
		darkModeActivation(scene);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//Méthode pour choisir le répertoire dans lequel l'enseignant enregistrera son fichier
	@FXML
	public void choisirRepertoire(ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory;
		directoryChooser.setTitle("Choisissez un répertoire pour l'enregistrement");
		selectedDirectory = directoryChooser.showDialog(null);
		if(selectedDirectory != null) {
			repertoire.setText(selectedDirectory.getAbsolutePath());
		}
	}
	
	//Méthode pour aller sur la page d'importation de la ressource
	@FXML
	public synchronized void pageImportationRessource(ActionEvent event) throws IOException {
		
		//Au moment d'aller sur la page d'après, on récupère le contenu des TextFields
		contenuRepertoire = repertoire.getText();
		contenuNomExo = nomExo.getText();
		
		Stage primaryStage = (Stage) repertoire.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("/Version_Enseignant/FXML_Files/ImporterRessource.fxml"));
		Scene scene = new Scene(root, MainEnseignant.width, MainEnseignant.height - 60);
		darkModeActivation(scene);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//Méthode pour passer ou non le darkMode
	@FXML
	public synchronized void darkMode() {

		if(dark.isSelected()) {
			nomExo.getScene().getStylesheets().removeAll(getClass().getResource(STYLE_MENU).toExternalForm());
			nomExo.getScene().getStylesheets().addAll(getClass().getResource(TEST_CSS).toExternalForm());
			Controller_Page_Accueil.isDark = true;
		} else {
			nomExo.getScene().getStylesheets().removeAll(getClass().getResource(TEST_CSS).toExternalForm());
			nomExo.getScene().getStylesheets().addAll(getClass().getResource(STYLE_MENU).toExternalForm());
			Controller_Page_Accueil.isDark = false;
		}
		
	}

	//Méthode qui regarde si le darkMode est actif et l'applique en conséquence à la scene
	public void darkModeActivation(Scene scene) {
		if(Controller_Page_Accueil.isDark) {
			scene.getStylesheets().removeAll(getClass().getResource(STYLE_MENU).toExternalForm());
			scene.getStylesheets().addAll(getClass().getResource(TEST_CSS).toExternalForm());
			dark.setSelected(true);
		} else {
			scene.getStylesheets().removeAll(getClass().getResource(TEST_CSS).toExternalForm());
			scene.getStylesheets().addAll(getClass().getResource(STYLE_MENU).toExternalForm());
			dark.setSelected(false);
		}
	}
}


