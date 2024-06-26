package AdventureModel;

import java.io.*;
import java.util.*;

/**
 * Class AdventureGame.  Handles all the necessary tasks to run the Adventure game.
 */
public class AdventureGame implements Serializable {
    private final String directoryName; //An attribute to store the Introductory text of the game.
    private String helpText; //A variable to store the Help text of the game. This text is displayed when the user types "HELP" command.
    private final HashMap<Integer, Room> rooms; //A list of all the rooms in the game.
    private HashMap<String,String> synonyms = new HashMap<>(); //A HashMap to store synonyms of commands.
    private final String[] actionWords = {"MOVE", "A", "B", "C", "D", "QUIT"}; //List of action words (other than motions) that exist in all games.
    public Player player; //The Player of the game.

    /**
     * Adventure Game Constructor
     * __________________________
     * Initializes attributes
     *
     * @param name the name of the adventure
     */
    public AdventureGame(String name){
        this.synonyms = new HashMap<>();
        this.rooms = new HashMap<>();
        this.directoryName = "Games/" + name; //all games files are in the Games directory!
        try {
            setUpGame();
        } catch (IOException e) {
            throw new RuntimeException("An Error Occurred: " + e.getMessage());
        }
    }

    /**
     * Save the current state of the game to a file
     * 
     * @param file pointer to file to write to
     */
    public void saveModel(File file) {
        try {
            FileOutputStream outfile = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(outfile);
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * setUpGame
     * __________________________
     *
     * @throws IOException in the case of a file I/O error
     */
    public void setUpGame() throws IOException {

        String directoryName = this.directoryName;
        AdventureLoader loader = new AdventureLoader(this, directoryName);
        loader.loadGame();

        // set up the player's current location
        this.player = new Player(this.rooms.get(1));
    }

    /**
     * tokenize
     * __________________________
     *
     * @param input string from the command line
     * @return a string array of tokens that represents the command.
     */
    public String[] tokenize(String input){

        input = input.toUpperCase();
        String[] commandArray = input.split(" ");

        int i = 0;
        while (i < commandArray.length) {
            if(this.synonyms.containsKey(commandArray[i])){
                commandArray[i] = this.synonyms.get(commandArray[i]);
            }
            i++;
        }
        return commandArray;

    }

    /**
     * movePlayer
     * __________________________
     * Moves the player in the given direction, if possible.
     * Return false if the player wins or dies as a result of the move.
     *
     * @param direction the move command
     * @return false, if move results in death or a win (and game is over).  Else, true.
     */
    public boolean movePlayer(String direction) {

        direction = direction.toUpperCase();
        PassageTable motionTable = this.player.getCurrentRoom().getMotionTable(); //where can we move?
        if (!motionTable.optionExists(direction)) return true; //no move

        ArrayList<Passage> possibilities = new ArrayList<>();
        for (Passage entry : motionTable.getDirection()) {
            if (entry.getDirection().equals(direction)) { //this is the right direction
                possibilities.add(entry); // are there possibilities?
            }
        }

        //try the blocked passages first
        Passage chosen = null;
        for (Passage entry : possibilities) {
            System.out.println(entry.getIsBlocked());
            System.out.println(entry.getKeyName());

            if (chosen == null && entry.getIsBlocked()) {
                if (this.player.getPoints() >= Integer.parseInt(entry.getKeyName())) {  // If user has 21 or more points they can proceed
                    chosen = entry; //we can make it through, given our stuff
                    break;
                }
            } else { chosen = entry; } //the passage is unlocked
        }

        if (chosen == null) return true; //doh, we just can't move.

        int roomNumber = chosen.getDestinationRoom();
        Room room = this.rooms.get(roomNumber);
        this.player.setCurrentRoom(room);

        return !this.player.getCurrentRoom().getMotionTable().getDirection().get(0).getDirection().equals("FORCED");
    }

    /**
     * interpretAction
     * interpret the user's action.
     * If user inputs an option of A, B, C, or D with an associated question,
     * verify if that is correct if so award them the required amount of points:
     * 3 points if easy, 5 points if hard
     * If they get a question wrong, then deduct the same amount of points
     *
     * * @param command String representation of the command.
     */
    public String interpretAction(String command){

        String[] inputArray = tokenize(command); //look up synonyms

        PassageTable motionTable = this.player.getCurrentRoom().getMotionTable(); //where can we move?

        if (motionTable.optionExists(inputArray[0])) {
            if (!movePlayer(inputArray[0])) {
                if (this.player.getCurrentRoom().getMotionTable().getDirection().get(0).getDestinationRoom() == 0)
                    return "GAME OVER";
                else return "FORCED";
            } //something is up here! We are dead or we won.
            return null;
        }

        else if (Arrays.asList(this.actionWords).contains(inputArray[0])) {

            if(inputArray[0].equals("QUIT")) {
                return "GAME OVER";
            } //time to stop!

            // If in the 5th or 2nd room and choose either easy or hard question
            else if (this.getPlayer().getCurrentRoom().getRoomNumber() == 2 || this.getPlayer().getCurrentRoom().getRoomNumber() == 5) {
                if (inputArray[0].equals("B")) {  // correct
                    if (this.getPlayer().getCurrentRoom().currentQuestion.getType().equals("EASY")) {
                        this.getPlayer().addPoints(3);  // Award 3 points if EASY
                    }
                    else {
                        this.getPlayer().addPoints(5);  // Award 5 points if HARD
                    }
                    return "CORRECT!";
                }
                else {  // incorrect
                    if (this.getPlayer().getCurrentRoom().currentQuestion.getType().equals("EASY")) {
                        this.getPlayer().removePoints(3);  // Take 3 points if EASY
                    }
                    else {
                        this.getPlayer().removePoints(5);  // Take 5 points if HARD
                    }

                    return "INCORRECT\n" + "The correct answer is: " + this.getPlayer().getCurrentRoom().currentQuestion.getAnswer();
                }
            }

            // If in the 6th or 3rd room and choose either easy or hard question
            else if (this.getPlayer().getCurrentRoom().getRoomNumber() == 3 || this.getPlayer().getCurrentRoom().getRoomNumber() == 6) {
                if (inputArray[0].equals("D")) {  // correct
                    if (this.getPlayer().getCurrentRoom().currentQuestion.getType().equals("EASY")) {
                        this.getPlayer().addPoints(3);  // Award 3 points if EASY
                    }
                    else {
                        this.getPlayer().addPoints(5);  // Award 5 points if HARD
                    }
                    return "CORRECT!";
                }

                else {  // Incorrect
                    if (this.getPlayer().getCurrentRoom().currentQuestion.getType().equals("EASY")) {
                        this.getPlayer().removePoints(3);  // Take 3 points if EASY
                    }
                    else {
                        this.getPlayer().removePoints(5);  // Take 5 points if HARD
                    }

                    return "INCORRECT\n" + "The correct answer is: " + this.getPlayer().getCurrentRoom().currentQuestion.getAnswer();
                }
            }

            // If in the 4th room and choose easy or hard question
            else if (this.getPlayer().getCurrentRoom().getRoomNumber() == 4) {
                if (inputArray[0].equals("A")) {  // If correct
                    if (this.getPlayer().getCurrentRoom().currentQuestion.getType().equals("EASY")) {
                        this.getPlayer().addPoints(3);  // Award 3 points if EASY
                    }
                    else {
                        this.getPlayer().addPoints(5);  // Award 5 points if HARD
                    }
                    return "CORRECT!";
                }

                else { // Incorrect
                    if (this.getPlayer().getCurrentRoom().currentQuestion.getType().equals("EASY")) {
                        this.getPlayer().removePoints(3);  // Take 3 points if EASY
                    }
                    else {
                        this.getPlayer().removePoints(5);  // Take 5 points if HARD
                    }

                    return "INCORRECT\n" + "The correct answer is: " + this.getPlayer().getCurrentRoom().currentQuestion.getAnswer();
                }
            }

        }

        return "INVALID COMMAND.";
    }

    /**
     * getDirectoryName
     * __________________________
     * Getter method for directory 
     * @return directoryName
     */
    public String getDirectoryName() {
        return this.directoryName;
    }

    /**
     * getInstructions
     * __________________________
     * Getter method for instructions 
     * @return helpText
     */
    public String getInstructions() {
        return helpText;
    }

    /**
     * getPlayer
     * __________________________
     * Getter method for Player 
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * getRooms
     * __________________________
     * Getter method for rooms 
     * @return map of key value pairs (integer to room)
     */
    public HashMap<Integer, Room> getRooms() {
        return this.rooms;
    }

    /**
     * getSynonyms
     * __________________________
     * Getter method for synonyms 
     * @return map of key value pairs (synonym to command)
     */
    public HashMap<String, String> getSynonyms() {
        return this.synonyms;
    }

    /**
     * setHelpText
     * __________________________
     * Setter method for helpText
     * @param help which is text to set
     */
    public void setHelpText(String help) {
        this.helpText = help;
    }


}
