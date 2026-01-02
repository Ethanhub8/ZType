import tester.*;
import java.util.Random;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;

//Utils class for random word generation
class Utils {

  public String WordGen(String word, Random rand) {
    //library of possible letters to make a word from
    String characters = "abcdefghijklmnopqrstuvwxyz";

    //generate a letter and append it to the current word
    int index = rand.nextInt(characters.length()); 
    word += characters.charAt(index);

    //Randomly deciding to return a longer word while maintaining decent length
    if (((word.length() >= 6) && (index <= 12)) || (word.length() == 9)) {
      return word;
    }

    return WordGen(word, rand);
  }
}

//Represents a world state in the game
class ZTypeWorld extends World {
  ILoWord words;
  int tickCounter;
  int score;
  int level;
  Random random;

  //Random games constructor
  ZTypeWorld(ILoWord words, int tickCounter, int score, int level) {
    this.words = words;
    this.tickCounter = tickCounter;
    this.score = score;
    this.level = level;
    this.random = new Random();
  }

  //Non random games constructor
  ZTypeWorld(ILoWord words, int tickCounter, int score, int level, Random random) {
    this.words = words;
    this.tickCounter = tickCounter;
    this.score = score;
    this.level = level;
    this.random = random;
  }

  //makes a new worldscene
  public WorldScene makeScene() {
    return this.words.draw(new WorldScene(400, 600))
        .placeImageXY(new TextImage("Score: " + this.score, 20, Color.BLUE), 55, 20)
        .placeImageXY(new TextImage("Level: " + this.level, 20, Color.GREEN), 330, 20);
  }

  //Counter to add new words and to move them down along the game
  public World onTick() { 
    if (this.words.reachedBottom()) { 
      return new GameOverWorld(score); 
    } else if (this.tickCounter >= Math.max(50 - (this.level * 10), 10)) {
      ILoWord add = new ConsLoWord(
          new InactiveWord(new Utils().WordGen("", this.random),
              50 + new Random().nextInt(301), 100), this.words); 
      return new ZTypeWorld(add.moveDown(), 0, score, level); 
    } else { 
      return new ZTypeWorld(this.words.moveDown(), this.tickCounter + 1, score, level); 
    }
  }

  //tracks the user typing
  //FOR EXTRA CREDIT: when the space key is pressed, it restarts the game
  public World onKeyEvent(String key) {
    if(key.equals(" ")) {
      return new ZTypeWorld(new MtLoWord(), 0, 0, 1);
    }
    ILoWord newWords = this.words.actMatchingWord(key);
    ILoWord filteredWords = newWords.checkAndReduce(key);

    return new ZTypeWorld(filteredWords, this.tickCounter, 
        this.score + this.words.count(key) * 30, this.score / 100 + 1);
  }
}

//World Scene when the game is over
class GameOverWorld extends World {
  int score;

  GameOverWorld(int score) {
    this.score = score;
  }
  
  //End screen with level and score
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(400, 600);
    return scene.placeImageXY(new TextImage("Game Over", 50, Color.RED), 200, 300)
        .placeImageXY(new TextImage("Press Space to Play Again", 20, Color.RED), 200, 400)
        .placeImageXY(new TextImage("Your Score:" + this.score, 20, Color.RED), 200, 350);
  }

  //checking what the user presses and returns a world state
  //FOR EXTRA CREDIT: when the space key is pressed, it restarts the game
  public World onKeyEvent(String key) {
    if (key.equals(" ")) {
      return new ZTypeWorld(new MtLoWord(), 0, 0, 1);
    } else {
      return this;
    }
  } 
}

//Interface for a List of Words
interface ILoWord {
  //Draws the images of the list of words
  WorldScene draw(WorldScene scene);

  //Moves the list of words down
  ILoWord moveDown();

  //Checks if a word have reached a certain y level
  boolean reachedBottom();

  //Changes InactiveWord to ActiveWords with user key presses
  ILoWord actMatchingWord(String key);

  //Checks if the given string is the same as the first word 
  boolean checkSame(String key);

  //Reduces the letters of words depending on whether the key was pressed
  ILoWord checkAndReduce(String s);

  //Checks if the list of words have an active word
  boolean hasActiveWord();

  //Count how many words in the list are actively being typed or are in an "active" state 
  int count(String key);

}

//Empty List of Words
class MtLoWord implements ILoWord {
  
  //Produces the given scene
  public WorldScene draw(WorldScene scene) {
    return scene;
  }

  //Moves the list of words down
  public ILoWord moveDown() {
    return this;
  }

  //Checks if a word have reached a certain y level
  public boolean reachedBottom() {
    return false;
  }

  //Changes InactiveWord to ActiveWords with user key presses
  public ILoWord actMatchingWord(String key) {
    return new MtLoWord();
  }

  //Checks if the given string is the same as the first word 
  public boolean checkSame(String key) {
    return false;
  }

  //Reduces the letters of words depending on whether the key was pressed
  public ILoWord checkAndReduce(String s) {
    return new MtLoWord();
  }

  //Checks if the list of words have an active word
  public boolean hasActiveWord() {
    return false;
  }

  //Count how many words in the list are actively being typed or are in an "active" state 
  public int count(String key) {
    return 0;
  }
}

//Non-Empty List of Words
class ConsLoWord implements ILoWord {

  IWord first;
  ILoWord rest;

  ConsLoWord(IWord first, ILoWord rest) {
    this.first = first;
    this.rest = rest;
  }

  // Draw words recursively
  public WorldScene draw(WorldScene scene) {
    return this.rest.draw(this.first.draw(scene));
  }

  // Move all words down
  public ILoWord moveDown() {
    return new ConsLoWord(this.first.moveDown(), this.rest.moveDown());
  }

  //Checks whether words reached the bottom
  public boolean reachedBottom() {
    return this.first.reachedBottom() || this.rest.reachedBottom();
  }

  //Changes InactiveWord to ActiveWords with user key presses
  public ILoWord actMatchingWord(String key) {
    if (this.first.checkLetter(key) && !this.hasActiveWord()) {
      return new ConsLoWord(this.first.activateWord(), this.rest);
    } else {
      return new ConsLoWord(this.first, this.rest.actMatchingWord(key));
    }
  }

  //checks the user accuracy for key presses
  public boolean checkSame(String key) {
    return this.first.checkLetter(key); 
  }

  //reduces the letters of words depending on whether the key was pressed
  public ILoWord checkAndReduce(String s) {
    if (this.first.theSameAndActive(s)) {
      return this.rest;
    } else {
      return new ConsLoWord(this.first.checkAndReduce(s),
          this.rest.checkAndReduce(s));
    }
  }

  //checks if the list of words have an active word
  public boolean hasActiveWord() {
    if (this.first.isActive()) {
      return true;
    } else {
      return this.rest.hasActiveWord();
    }
  }

  //count how many words in the list are actively being typed or are in an "active" state
  public int count(String key) {
    if (this.first.theSameAndActive(key)) {
      return 1 + this.rest.count(key);
    } else {
      return this.rest.count(key);
    }
  }
}

interface IWord {

  //Produces an image of the IWord
  WorldScene draw(WorldScene scene);

  //Checks if the word is the same as the given word and if its active
  boolean theSameAndActive(String typedWord);

  //produces an ActiveWord
  IWord activateWord();

  //checks if a word has reached a certain y level
  boolean reachedBottom();

  //adds 5 to the IWords y level
  IWord moveDown();

  //checks if the text starts with the given string
  boolean checkLetter(String key);

  //checks if the word if it starts with the given string and if true, return a new ActiveWord
  IWord checkAndReduce(String s);

  //Checks if the word is active or not
  boolean isActive();

}

//Word Class
abstract class AWord implements IWord{
  String text;
  int x, y;

  AWord(String text, int x, int y) {
    this.text = text;
    this.x = x;
    this.y = y;
  }

  //checks if the text starts with the given string
  public boolean checkLetter(String key) {
    return this.text.startsWith(key);
  }

  // checks if the word has reached the y level 580
  public boolean reachedBottom() {
    return this.y == 580;
  }
}

//Inactive Word (falls down)
class InactiveWord extends AWord {

  InactiveWord(String text, int x, int y) {
    super(text, x, y);
  }

  //produces an image of the InactiveWord 
  public WorldScene draw(WorldScene scene) {
    return scene.placeImageXY(new TextImage(this.text, 20, Color.BLACK), this.x, this.y);
  }

  //adds 5 to the y level of the word
  public AWord moveDown() {
    return new InactiveWord(this.text, this.x, this.y + 5);
  }

  //returns an active word
  public AWord activateWord() {
    return new ActiveWord(this.text, this.x, this.y);
  }

  //Checks if the word is the same as the given word and if its active
  public boolean theSameAndActive(String typedWord) {
    return false;
  }

  //returns the InactiveWord
  public IWord checkAndReduce(String s) {
    return this;
  }

  //Checks if the word is active or not
  public boolean isActive() {
    return false;
  }
}

class ActiveWord extends AWord {
  ActiveWord(String text, int x, int y) {
    super(text, x, y);
  }

  //Produces an image of the ActiveWord
  public WorldScene draw(WorldScene scene) {
    return scene.placeImageXY(new TextImage(this.text, 20, Color.RED), this.x, this.y);
  }

  //adds 5 to the y level of the word
  public AWord moveDown() {
    return new ActiveWord(this.text, this.x, this.y + 5);
  }

  //returns an active word
  public AWord activateWord() {
    return this;
  }

  //Checks if the word is the same as the given word and if its active
  public boolean theSameAndActive(String typedWord) {
    return this.text.equals(typedWord);
  }

  //checks if the word if it starts with the given string and if true, return a new ActiveWord
  public IWord checkAndReduce(String s) {
    if (this.text.startsWith(s)) {
      return new ActiveWord(this.text.substring(1), this.x, this.y);
    } else {
      return this;
    }
  }

  //Checks if the word is active or not
  public boolean isActive() {
    return true;
  }
}

class ExamplesZType {

  //testing the creation of a new world
  boolean testBigBang(Tester t) {
    ZTypeWorld world = new ZTypeWorld(new MtLoWord(), 0, 0, 1);
    int worldWidth = 400;
    int worldHeight = 600;
    double tickRate = .1;
    return world.bigBang(worldWidth, worldHeight, tickRate);
  }
}
