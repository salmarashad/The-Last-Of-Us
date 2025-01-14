package application;


import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import engine.Game;
import exceptions.InvalidTargetException;
import exceptions.MovementException;
import exceptions.NoAvailableResourcesException;
import exceptions.NotEnoughActionsException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.characters.Direction;
import model.characters.Explorer;
import model.characters.Fighter;
import model.characters.Hero;
import model.characters.Medic;
import model.characters.Character;
import model.characters.Zombie;
import model.collectibles.Supply;
import model.collectibles.Vaccine;
import model.world.Cell;
import model.world.CharacterCell;
import model.world.CollectibleCell;
import model.world.TrapCell;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator; 


public class Main extends Application {
	private Hero startingHero;
	private static Hero pickedHero;
    private static final int CELL_SIZE = 40;
    private static  Text characterDetails2 ;
    private static Character pickedTarget; 
    private static ImageView zombiepicview; 
    private static Character pickedHeal; 
    private static final String CUSTOM_FONT_PATH = "path/to/custom/font.ttf";
    private static Point [] location = new Point [5];
    private static int count =0; 
    private Image playerBackgroundImage = new Image("playersbackground.jpg");
    private ImageView playerBackgroundImageView = new ImageView(playerBackgroundImage);
    private Pane playersLayout = new Pane(playerBackgroundImageView);
    private DropShadow glowEffect = new DropShadow(20, Color.WHITE);
    private Text characterDetails = new Text();
    private static HashSet<CharacterMetaData> characterData = new HashSet<>(); 
    private BorderPane playLayout = new BorderPane();
    private         GridPane gridPane = new GridPane();
    private Image temp = new Image ("sideimg.jpg");
    private ImageView tempview = new ImageView (temp); 
    private Pane sidePane = new Pane(tempview);
    private static HashMap <String,String>  characterNameToImage = new HashMap<>(); 
	
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private void displayErrorAlert(String errorMessage) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinWidth(100);
        dialogPane.setMinHeight(50);
        alert.showAndWait();
    }
    
    private Text createText(String content, Font font, double x, double y, Color color) {
        Text text = new Text(content);
        text.setFont(font);
        text.setFill(color);
        text.setLayoutX(x);
        text.setLayoutY(y);
        return text;
    }
    
    private void showWinScreen(Stage primaryStage) {
        Font customFont = Font.loadFont(getClass().getResourceAsStream("/evil-empire-font/EvilEmpire-4BBVK.ttf"), 40);

        ImageView winBackground = new ImageView(new Image("winback.jpg"));
        winBackground.setFitWidth(945);
        winBackground.setFitHeight(600);
        winBackground.setPreserveRatio(true);

        Text winnerText = createText("You won!", customFont, 400, 200, Color.WHITE);

        Text quitButton = createText("Quit", customFont, 115, 430, Color.RED);
        quitButton.setOnMouseClicked(e -> primaryStage.close());
        quitButton.setOnMouseEntered(e -> quitButton.setStyle("-fx-font-weight: bold;"));
        quitButton.setOnMouseExited(e -> quitButton.setStyle(""));

        Pane winnerLayout = new Pane(winBackground, winnerText, quitButton);
        Scene winnerScene = new Scene(winnerLayout, 945, 600);

        primaryStage.setScene(winnerScene);
    }

    
    private void showLoseScreen(Stage primaryStage) {
        Font customFont = Font.loadFont(getClass().getResourceAsStream("/evil-empire-font/EvilEmpire-4BBVK.ttf"), 40);

        // Background setup
        ImageView loseBackground = new ImageView(new Image("startimg2.jpg"));
        loseBackground.setFitWidth(945);
        loseBackground.setFitHeight(600);
        loseBackground.setPreserveRatio(true);

        // Text for the lose message
        Text loseText = createText("You Lost!", customFont, 400, 200, Color.WHITE);

        // Quit button setup
        Text quitButton = createText("Quit", customFont, 115, 430, Color.RED);
        quitButton.setOnMouseClicked(e -> primaryStage.close());
        quitButton.setOnMouseEntered(e -> quitButton.setStyle("-fx-font-weight: bold;"));
        quitButton.setOnMouseExited(e -> quitButton.setStyle(""));

        // Layout and scene setup
        Pane loseLayout = new Pane(loseBackground, loseText, quitButton);
        Scene loseScene = new Scene(loseLayout, 945, 600);

        primaryStage.setScene(loseScene);
    }
    
    
    private static void updateGrid(GridPane gridPane, Cell[][] map) {
        DropShadow greenglow = new DropShadow(10, Color.GREEN);
        gridPane.getChildren().clear();

        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Cell cell = map[i][j];
                addCellToGrid(gridPane, i, j, cell);
                if (cell instanceof TrapCell && count < 5) {
                    location[count++] = new Point(i, j);
                }
                handleCellContent(gridPane, greenglow, i, j, cell);
            }
        }
    }

    private static void addCellToGrid(GridPane gridPane, int i, int j, Cell cell) {
        ImageView cellImageView = new ImageView(new Image("block.jpeg"));
        cellImageView.setFitWidth(CELL_SIZE);
        cellImageView.setFitHeight(CELL_SIZE);
        GridPane.setConstraints(cellImageView, j, 14 - i);
        gridPane.getChildren().add(cellImageView);
    }

    private static void handleCellContent(GridPane gridPane, DropShadow greenglow, int i, int j, Cell cell) {
        addingZombies(gridPane, greenglow, i, j, cell);
        addingVaccines(gridPane, i, j, cell);
        addingSupplies(gridPane, i, j, cell);
        addingHeroes(cell, gridPane, i, j);
    }


	private static void addingSupplies(GridPane gridPane, int i, int j, Cell cell) {
		if(cell instanceof CollectibleCell && ((CollectibleCell) cell).getCollectible() instanceof Supply)  
			if(((CollectibleCell) cell).isVisible()) {
				Image supplypic = new Image("supply.PNG");
		        ImageView supplypicview = new ImageView(supplypic);
		        supplypicview.setFitWidth(CELL_SIZE);
		        supplypicview.setFitHeight(CELL_SIZE);
		        GridPane.setConstraints(supplypicview, j, 14-i);
		        gridPane.getChildren().add(supplypicview);  
			}
	}

	private static void addingVaccines(GridPane gridPane, int i, int j, Cell cell) {
		if(cell instanceof CollectibleCell && ((CollectibleCell) cell).getCollectible() instanceof Vaccine)  
			if(((CollectibleCell) cell).isVisible()) {
				Image vaccinepic = new Image("vaccine.PNG");
		        ImageView vaccinepicview = new ImageView(vaccinepic);
		        vaccinepicview.setFitWidth(CELL_SIZE);
		        vaccinepicview.setFitHeight(CELL_SIZE);
		        GridPane.setConstraints(vaccinepicview, j, 14-i);
		        gridPane.getChildren().add(vaccinepicview);  
			}
	}

	private static void addCharacterToGrid(GridPane gridPane, int i, int j, Image image, DropShadow effect) {
	    ImageView characterView = new ImageView(image);
	    characterView.setEffect(effect);
	    characterView.setFitWidth(CELL_SIZE);
	    characterView.setFitHeight(CELL_SIZE);
	    GridPane.setConstraints(characterView, j, 14 - i);
	    gridPane.getChildren().add(characterView);
	}

	private static void addingZombies(GridPane gridPane, DropShadow greenglow, int i, int j, Cell cell) {
		if(cell instanceof CharacterCell && ((CharacterCell) cell).getCharacter() instanceof Zombie) 
        	if(((CharacterCell) cell).isVisible()) {
        		Image zombiepic = new Image("zombie1.PNG");
                zombiepicview = new ImageView(zombiepic);
                zombiepicview.setEffect(greenglow);
                zombiepicview.setFitWidth(CELL_SIZE);
                zombiepicview.setFitHeight(CELL_SIZE);
                GridPane.setConstraints(zombiepicview, j, 14-i);
                gridPane.getChildren().add(zombiepicview); 
                final CharacterCell zombieCell = (CharacterCell) cell;
                zombiepicview.setOnMouseClicked(e -> {
                    Character pickedZombie = zombieCell.getCharacter();
                    pickedTarget = pickedZombie;
                    
                });
        	}
	}

	private static void addingHeroes(Cell cell, GridPane gridPane, int i, int j) {
	    if (cell instanceof CharacterCell) {
	        CharacterCell characterCell = (CharacterCell) cell;
	        if (characterCell.getCharacter() instanceof Hero) {
	            String characterName = characterCell.getCharacter().getName();
	            if (characterNameToImage.containsKey(characterName)) {
	                Image heroImage = new Image(characterNameToImage.get(characterName));
	                addCharacterToGrid(gridPane, i, j, heroImage, null);

	                ImageView heroImageView = new ImageView(heroImage); 
	                heroImageView.setOnMouseClicked(e -> {
	                    pickedHeal = characterCell.getCharacter();
	                });
	            }
	        }
	    }
	}

    
    private static void getCharacterImage() {
    	characterNameToImage.put("Ellie Williams","ellie.PNG");
    	characterNameToImage.put("Tess", "tess.PNG");
    	characterNameToImage.put("Riley Abel","riley.PNG");
    	characterNameToImage.put("Tommy Miller","tommy.PNG");
    	characterNameToImage.put("Bill","bill.PNG");
    	characterNameToImage.put("David","david.PNG");
    	characterNameToImage.put("Henry Burell","henry.PNG");
    	characterNameToImage.put("Joel Miller","joel.PNG");
    	
    }

	private static void populateCharacterData() {
		characterData.add(new CharacterMetaData("Henry Burell", "henryplay.png", "henry.PNG",80, 90, 20, 420));
		characterData.add(new CharacterMetaData("David", "davicplay.png","david.PNG", 85, 85, 20, 500 ));
		characterData.add(new CharacterMetaData("Bill", "billplay.png", "bill.PNG",80, 75, 100, 420));
		characterData.add(new CharacterMetaData("Tess", "tessplay.png", "tess.PNG", 90, 80, 95, 500));
		characterData.add(new CharacterMetaData("Tommy Miller", "tommyplay.png","tommy.PNG", 85, 90, 173, 500));
		characterData.add(new CharacterMetaData("Riley Abel", "rileyplay.png","riley.PNG", 77, 77, 175, 425));
		characterData.add(new CharacterMetaData("Ellie Williams", "ellieplay.png","ellie.PNG", 85, 85, 245, 420));
		characterData.add(new CharacterMetaData("Joel Miller","joelplay.png", "joel.PNG", 85, 85, 250, 495));
	    
	}
    
    public void trapcheck(Hero pickedHero, Point [] location) {
        for (int i = 0; i < location.length; i++) 
            if (pickedHero.getLocation().equals(location[i])) {
                displayErrorAlert("You fell in a trap.");
                location[i] = null;
                return;
            }
    }
    

    
    public void prepareCharacters(Pane sidePane ,ArrayList<Hero> heroes, ArrayList<Hero> availableHeroes) {
    	
    	//effect we will use when it's not in the available heroes 
    	ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setSaturation(-1.0);

        DropShadow dropShadow = new DropShadow(4, 2, 2, Color.BLACK);

        Font customFont = Font.loadFont(getClass().getResourceAsStream("/evil-empire-font/EvilEmpire-4BBVK.ttf"), 40);
        
        Text characterDetails = new Text(40, 320, "");
        characterDetails.setFont(Font.font(customFont.getFamily(), 18));
        characterDetails.setFill(Color.WHITE);
        characterDetails.setEffect(dropShadow);
        sidePane.getChildren().add(characterDetails);
        
        characterDetails2 = new Text("");
        characterDetails2.setFont(Font.font(customFont.getFamily(), 18));
        characterDetails2.setFill(Color.WHITE);
        characterDetails2.setLayoutX(170);
        characterDetails2.setLayoutY(290);
        characterDetails2.setTextAlignment(TextAlignment.RIGHT);
        characterDetails2.setEffect(dropShadow);
        
        sidePane.getChildren().add(characterDetails2);
       
        
        //unpressable b&w
        for(int i = 0 ; i< Game.availableHeroes.size();i++) {
        	String n = Game.availableHeroes.get(i).getName();
        	Iterator<CharacterMetaData> iterator = characterData.iterator();
            while (iterator.hasNext()) {
            	CharacterMetaData element = iterator.next();
            	if(n.equals(element.name)) {
            		Image characterImage = new Image(element.sideImage); 
                	ImageView characterView = new ImageView(characterImage);
                	characterView.setFitHeight(element.locationX); characterView.setFitWidth(element.locationY);
                	characterView.setLayoutX(element.sizeX);characterView.setLayoutY(element.sizeY);
                	characterView.setEffect(colorAdjust);
                	sidePane.getChildren().add(characterView); 
                }
            }
        }
    	
        
        //pressable 
        for(int i = 0 ; i< Game.heroes.size();i++) {
        	String n = Game.heroes.get(i).getName();
        	Hero h = Game.heroes.get(i);
        	Iterator<CharacterMetaData> iterator = characterData.iterator();
            while (iterator.hasNext()) {
            	CharacterMetaData element = iterator.next();
            	if(n.equals(element.name)) {
            		Image characterImage = new Image(element.sideImage); 
                	ImageView characterView = new ImageView(characterImage);
                	characterView.setFitHeight(element.locationX); characterView.setFitWidth(element.locationY);
                	characterView.setLayoutX(element.sizeX);characterView.setLayoutY(element.sizeY);
                	sidePane.getChildren().add(characterView); 
                	characterView.setOnMouseEntered(event -> {
                		characterDetails.setText(h.getName() + "\n" + "Type:" + h.getType() + "\n" + "Attack Damage " + 
                		h.getAttackDmg() + "\n" + "Maximum Actions: " + h.getMaxActions() + "\n" + "Current HP: " +
                	     h.getCurrentHp() );
                    });
                	characterView.setOnMouseExited(event -> {
                		characterDetails.setText("");
                    });
                	
                	characterView.setOnMouseClicked(e -> {
                		pickedHero = h; 
                		updateData(sidePane, h, characterDetails2);
                			
                    });
                }
               
            }
            	
        }

    	
    }
    
    private void updateData(Pane sidePane, Hero h, Text t) {
    	  
        if (characterDetails2 != null) {
            sidePane.getChildren().remove(characterDetails2);
        }

        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.BLACK);
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(4);
        Font customFont = Font.loadFont(getClass().getResourceAsStream("/evil-empire-font/EvilEmpire-4BBVK.ttf"), 40);

        t.setText(h.getName() + "\n" + "Type:" + h.getType() + "\n" + "Attack Damage " +
                  h.getAttackDmg() + "\n" + "Action Points: " + h.getActionsAvailable() + "\n" + 
                  "Current HP: " + h.getCurrentHp() + "\n" + "Vaccines: " + h.getVaccineInventory().size() + "\n" +
                  "Supplies: " + h.getSupplyInventory().size());

        t.setFont(Font.font(customFont.getFamily(), 18));
        t.setFill(Color.WHITE);
        t.setLayoutX(170);
        t.setLayoutY(290);
        t.setTextAlignment(TextAlignment.RIGHT);
        t.setEffect(dropShadow);

        sidePane.getChildren().add(t);
        
        characterDetails2 = t;
    }

    
    private void createCharacterView(Stage primaryStage, String imagePath, int heroIndex, double fitWidth, double fitHeight, double layoutX, double layoutY) {
        Image heroPic = new Image(imagePath);
        ImageView heroView = new ImageView(heroPic);
        heroView.setFitWidth(fitWidth);
        heroView.setFitHeight(fitHeight);
        heroView.setLayoutX(layoutX);
        heroView.setLayoutY(layoutY);
        playersLayout.getChildren().add(heroView);

        heroView.setOnMouseEntered(event -> {
            heroView.setEffect(glowEffect);
            characterDetails.setText(
                Game.availableHeroes.get(heroIndex).getName() + "\n" +
                "Type: " + Game.availableHeroes.get(heroIndex).getType() + "\n" +
                "Attack Damage: " + Game.availableHeroes.get(heroIndex).getAttackDmg() + "\n" +
                "Maximum Actions: " + Game.availableHeroes.get(heroIndex).getMaxActions() + "\n" +
                "Maximum Hit Points: " + Game.availableHeroes.get(heroIndex).getMaxHp()
            );
        });

        heroView.setOnMouseExited(event -> {
            heroView.setEffect(null);
            characterDetails.setText("");
        });

        heroView.setOnMouseClicked(event -> {
            Scene playView = new Scene(playLayout, 945, 600);
            primaryStage.setScene(playView);
            startingHero = Game.availableHeroes.get(heroIndex);
            Game.startGame(startingHero);
            pickedHero = startingHero;
            updateGrid(gridPane, Game.map);
            prepareCharacters(sidePane, Game.heroes, Game.availableHeroes);
        });
    }
    
    private Text createButton(String text, Font customFont, Color color, Effect effect, double x, double y) {
        Text button = new Text(text);
        button.setFont(Font.font(customFont.getFamily(), 20));
        button.setFill(color);
        button.setEffect(effect);
        button.setLayoutX(x);
        button.setLayoutY(y);
        return button;
    }
    
    private void handleMovement(Direction direction, Pane sidePane, GridPane gridPane, Hero pickedHero, Text characterDetails2, Point [] location, Stage primaryStage) {
        try {
            pickedHero.move(direction);
        } catch (MovementException e) {
            displayErrorAlert("You can't move in this direction!");
            return;
        } catch (NotEnoughActionsException e) {
            displayErrorAlert("You don't have enough actions! End your turn.");
            return;
        }

        updateGrid(gridPane, Game.map);
        updateData(sidePane, pickedHero, characterDetails2);
        trapcheck(pickedHero, location);

        if (Game.checkWin()) {
            showWinScreen(primaryStage);
        }
    }


    public void start(Stage primaryStage) {
    	
    	try {
    	    Game.loadHeroes("src/Heros.csv");
    	} catch (IOException e1) {
    		displayErrorAlert("There is a problem with the game. Please reinstall");
    	}
    	
    
    	populateCharacterData();
    	getCharacterImage(); 

    	
    	// start background
    	Pane root = new Pane(new ImageView(new Image("startimg.jpg")));
    	Scene scene = new Scene(root, 945, 600);
    	ImageView backgroundImageView = (ImageView) root.getChildren().get(0);
    	backgroundImageView.setFitWidth(945);
    	backgroundImageView.setFitHeight(600);
    	backgroundImageView.setPreserveRatio(true);


        //styles
    	Font customFont = Font.loadFont(getClass().getResourceAsStream("/evil-empire-font/EvilEmpire-4BBVK.ttf"), 40);
    	DropShadow dropShadow = new DropShadow(4, 2, 2, Color.BLACK);

  
       //start button
        Text startbutton = new Text("start");
        startbutton.setFont(Font.font(customFont.getFamily(), 40));
        startbutton.setFill(Color.WHITE);
        startbutton.setLayoutX(115); startbutton.setLayoutY(430);
        startbutton.setEffect(dropShadow);
        root.getChildren().add(startbutton); 
        
        //start game title
        ImageView gameTitleView = new ImageView(new Image("The-Last-Of-Us-Logo-PNG.png"));
        gameTitleView.setFitWidth(130);
        gameTitleView.setFitHeight(190);
        gameTitleView.setLayoutX(100);
        gameTitleView.setLayoutY(200);
        root.getChildren().add(gameTitleView);

        //player start page
        playerBackgroundImageView.setFitWidth(945);
        playerBackgroundImageView.setFitHeight(600);
        playerBackgroundImageView.setPreserveRatio(true);
       
        
        //picking the character
        Text instructions = new Text("Pick Your Hero");
        instructions.setFont(Font.font(customFont.getFamily(), 40)); 
        instructions.setFill(Color.WHITE); 
        instructions.setLayoutX(340); instructions.setLayoutY(130); 
        instructions.setEffect(dropShadow); 
        playersLayout.getChildren().add(instructions);

        
        //displaying character details 
        characterDetails.setFont(Font.font(customFont.getFamily(), 18));
        characterDetails.setFill(Color.WHITE);
        characterDetails.setLayoutX(70);
        characterDetails.setLayoutY(480);
        characterDetails.setEffect(dropShadow);
        playersLayout.getChildren().add(characterDetails);
        

        //playing area 
        // Image playBackgroundImage = new Image("background.jpg");
        // ImageView playBackgroundImageView = new ImageView(playBackgroundImage);

        tempview.setFitWidth(350);
        tempview.setFitHeight(600);
        
        playLayout.setLeft(gridPane);
        playLayout.setRight(sidePane);
        
        createCharacterView(primaryStage,"riley.PNG", 3, 50, 140, 50, 260);
        createCharacterView(primaryStage,"tommy.PNG", 4, 90, 160, 650, 260);
        createCharacterView(primaryStage,"ellie.PNG", 1, 80, 140, 150, 260);
        createCharacterView(primaryStage,"joel.PNG", 0, 70, 180, 560, 280);
        createCharacterView(primaryStage,"david.PNG", 6, 90, 240, 750, 300);
        createCharacterView(primaryStage, "bill.PNG", 5, 120, 150, 210, 260);
        createCharacterView(primaryStage, "henry.PNG", 7, 50, 50, 355, 270);
        createCharacterView(primaryStage,"tess.PNG", 2, 130, 170, 370, 260);
        
        Text btnUP = createButton("UP", customFont, Color.WHITE, dropShadow, 80, 50);
        Text btnDOWN = createButton("DOWN", customFont, Color.WHITE, dropShadow, 70, 130);
        Text btnRIGHT = createButton("RIGHT", customFont, Color.WHITE, dropShadow, 115, 90);
        Text btnLEFT = createButton("LEFT", customFont, Color.WHITE, dropShadow, 30, 90);
        Text endTurn = createButton("End Turn", customFont, Color.WHITE, dropShadow, 225, 50);
        Text cure = createButton("Cure", customFont, Color.WHITE, dropShadow, 225, 90);
        Text attack = createButton("Attack", customFont, Color.WHITE, dropShadow, 225, 130);
        Text useSpecial = createButton("Use Special", customFont, Color.WHITE, dropShadow, 225, 170);

        sidePane.getChildren().addAll(btnUP, btnDOWN, btnRIGHT, btnLEFT, endTurn, cure, attack, useSpecial);
    	
    	endTurn.setOnMouseClicked(e ->{
				try {
					Game.endTurn();
				} catch (NotEnoughActionsException e1) {
					displayErrorAlert("You don't have enough actioins! End your turn.");
				} catch (InvalidTargetException e1) {
					displayErrorAlert("You must select a character!");
				}
		
			updateData(sidePane, pickedHero, characterDetails2);
    		updateGrid(gridPane, Game.map);
    		if(Game.checkGameOver())
    			showLoseScreen(primaryStage);
			
			prepareCharacters( sidePane ,Game.heroes, Game.availableHeroes);
    		
    	});
    	
    	useSpecial.setOnMouseClicked(e-> {
    				if(pickedHero instanceof Medic) {
    					if(pickedHeal == null) {
    						displayErrorAlert("You must select a character!");
    						return;
    					}
    					
    					pickedHero.setTarget(pickedHeal);

    				}
    					
    				
    	        	try {
						pickedHero.useSpecial();
						
					} catch (NoAvailableResourcesException e1) {
						displayErrorAlert("You don't have any supplies!");
					} catch (InvalidTargetException e1) {
						displayErrorAlert("You can't select this character.");
					}
    	        	updateData(sidePane, pickedHero, characterDetails2);
    	    		updateGrid(gridPane, Game.map);
    	    		prepareCharacters( sidePane , Game.heroes, Game.availableHeroes) ;
   	});
    	
    	
    	cure.setOnMouseClicked(e-> {
    		 if (pickedTarget != null) { // Check if a target is picked
     	            pickedHero.setTarget(pickedTarget);
     	                try {
							pickedHero.cure();
						} catch (NoAvailableResourcesException e1) {
							displayErrorAlert("You don't have any vaccines!");
						} catch (InvalidTargetException e1) {
							displayErrorAlert("You must select an adjacent character!");
						} catch (NotEnoughActionsException e1) {
							displayErrorAlert("You don't have enough actions!");
						}
//     	               characterDetails2.setText("");
     	            updateData(sidePane, pickedHero, characterDetails2);
     	    		updateGrid(gridPane, Game.map);
     	    		prepareCharacters( sidePane , Game.heroes, Game.availableHeroes) ;
     	    		if(Game.checkWin())
     	    			showWinScreen(primaryStage);
     	    	} else {
     	    	displayErrorAlert("Pick a zombie to cure!");

     	    }
    	});
    	
    	attack.setOnMouseClicked(e -> {
    	            pickedHero.setTarget(pickedTarget);
    	            try {
    	                pickedHero.attack();
    	            } catch (NotEnoughActionsException e1) {
    	            	displayErrorAlert("You don't have enough actions!");
    	            } catch (InvalidTargetException e1) {
    	            	displayErrorAlert("You must select an adjacent character!");
    	            }
    	            updateData(sidePane, pickedHero, characterDetails2);
    	    		updateGrid(gridPane, Game.map);
    	});

        
        
		
        
    	btnDOWN.setOnMouseClicked(e -> handleMovement(Direction.DOWN, sidePane, gridPane, pickedHero, characterDetails2, location, primaryStage));
    	btnLEFT.setOnMouseClicked(e -> handleMovement(Direction.LEFT, sidePane, gridPane, pickedHero, characterDetails2, location, primaryStage));
    	btnRIGHT.setOnMouseClicked(e -> handleMovement(Direction.RIGHT, sidePane, gridPane, pickedHero, characterDetails2, location, primaryStage));
    	btnUP.setOnMouseClicked(e -> handleMovement(Direction.UP, sidePane, gridPane, pickedHero, characterDetails2, location, primaryStage));
    	
        //starting the game 
        startbutton.setOnMouseClicked(e -> {
        	Scene playersScene = new Scene(playersLayout, 945, 600);
        	primaryStage.setScene(playersScene);
        });
        startbutton.setOnMouseEntered(event -> {
        	startbutton.setEffect(glowEffect);
        });
        startbutton.setOnMouseExited(event -> {
        	startbutton.setEffect(null);
        });

        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setTitle("The Last of Us - Legacy");
        gridPane.requestFocus();

        primaryStage.show();

    }
    
}
