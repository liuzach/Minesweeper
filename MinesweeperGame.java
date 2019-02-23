import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class MinesweeperGame extends World {
  ArrayList<ArrayList<Tile>> tiles;
  int rows;
  int cols;
  int mines;
  int tileSize;
  Random rand;

  // Default constructor with default tileSize and random
  MinesweeperGame(int rows, int cols, int mines) {
    this.tiles = new ArrayList<ArrayList<Tile>>();
    this.rows = rows;
    this.cols = cols;
    this.mines = mines;
    this.tileSize = 20;
    this.rand = new Random();
    this.generateBoard();
  }

  // Constructs the game with a given tileSize
  MinesweeperGame(int rows, int cols, int mines, int tileSize) {
    this.tiles = new ArrayList<ArrayList<Tile>>();
    this.rows = rows;
    this.cols = cols;
    this.mines = mines;
    this.tileSize = tileSize;
    this.rand = new Random();
    this.generateBoard();
  }

  // Constructs the game with a given random
  MinesweeperGame(int rows, int cols, int mines, Random rand) {
    this.tiles = new ArrayList<ArrayList<Tile>>();
    this.rows = rows;
    this.cols = cols;
    this.mines = mines;
    this.tileSize = 20;
    this.rand = rand;
    this.generateBoard();
  }

  // Generates the board, mines, and links between neighboring tiles
  void generateBoard() {
    for (int i = 0; i < rows; i++) {
      this.tiles.add(new ArrayList<Tile>());
      for (int j = 0; j < cols; j++) {
        this.tiles.get(i).add(new Tile(this.tileSize, this.tileSize));
      }
    }
    generateMines();
    linkTiles();
  }

  // Generates the mines
  void generateMines() {
    int count = 0;
    while (count < mines) {
      int row = this.rand.nextInt(this.rows);
      int col = this.rand.nextInt(this.cols);
      if (!(getTile(row, col).isMine())) {
        count++;
        getTile(row, col).addMine();
      }
    }
  }

  // Generates links between neighboring tiles
  void linkTiles() {

    for (int row = 0; row < tiles.size(); row++) {
      for (int col = 0; col < tiles.get(row).size(); col++) {
        if (this.rows - row > 1) { // not the bottom row
          getTile(row, col).addNeighbor(tiles.get(row + 1).get(col));
          // add the tile below
        }
        if (this.cols - col > 1) { // not the rightmost column
          getTile(row, col).addNeighbor(tiles.get(row).get(col + 1));
          // add the tile to the right
        }
        if (row > 0) { // not the top row
          getTile(row, col).addNeighbor(tiles.get(row - 1).get(col));
          // add the tile above
        }
        if (col > 0) { // not the leftmost column
          getTile(row, col).addNeighbor(tiles.get(row).get(col - 1));
          // add the tile to the left
        }
        if ((row > 0) && (col > 0)) { // not the top row or leftmost column
          getTile(row, col).addNeighbor(tiles.get(row - 1).get(col - 1));
          // add the tile up and to the left
        }
        if ((this.rows - row > 1) && (col > 0)) { // not the bottom row or leftmost column
          getTile(row, col).addNeighbor(tiles.get(row + 1).get(col - 1));
          // add the tile down and to the left
        }
        if ((row > 0) && (this.cols - col > 1)) { // not the top row or rightmost column
          getTile(row, col).addNeighbor(tiles.get(row - 1).get(col + 1));
          // add the tile up and to the right
        }
        if ((this.rows - row > 1) && (this.cols - col > 1)) {
          // not the bottom row or rightmost column
          getTile(row, col).addNeighbor(tiles.get(row + 1).get(col + 1));
          // add the tile down and to the right
        }

      }
    }
  }

  // Draws the game
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(this.cols * this.tileSize, this.rows * this.tileSize);
    for (int row = 0; row < this.tiles.size(); row++) {
      for (int col = 0; col < this.tiles.get(row).size(); col++) {
        ws.placeImageXY(getTile(row, col).draw(), col * this.tileSize, row * this.tileSize);
      }
    }
    return ws;
  }

  // Handles mouse clicks
  public void onMouseClicked(Posn pos, String buttonName) {
    for (int row = 0; row < this.tiles.size(); row++) {
      for (int col = 0; col < this.tiles.get(row).size(); col++) {
        if ((pos.x > col * this.tileSize) && (pos.x < (col + 1) * this.tileSize)
            && (pos.y > row * this.tileSize) && (pos.y < (row + 1) * this.tileSize)) {
          if (buttonName.equals("LeftButton")) {
            if (getTile(row, col).isMine()) {
              this.endOfWorld("Gameover");
            }
            else {
              getTile(row, col).reveal();
            }
          }
          else if (buttonName.equals("RightButton")) {
            if (!getTile(row, col).isRevealed) {
              getTile(row, col).flag();
            }
          }
        }
      }
    }
  }

  // Returns the tile at the given row and column
  Tile getTile(int row, int col) {
    return this.tiles.get(row).get(col);
  }

  public void onTick() {
    boolean win = true;
    for (int row = 0; row < this.tiles.size(); row++) {
      for (int col = 0; col < this.tiles.get(row).size(); col++) {
        if(!getTile(row, col).isMine() && !getTile(row, col).isRevealed) {
          win = false;
        }
      }
    }
    if (win) {
      this.endOfWorld("You win!");
    }
  }
  public WorldScene lastScene(String s) {
    int x = this.cols * this.tileSize;
    int y = this.rows * this.tileSize;
    Color c;
    WorldScene ws = new WorldScene(x, y);
    if (s.equals("You win!")) {
      c = Color.GREEN;
    }
    else {
      c = Color.RED;
    }
    ws.placeImageXY(new TextImage(s, c), x / 2, y / 2);
    return ws;
  }
}

class Tile {
  int width;
  int height;
  boolean isMine;
  boolean isRevealed;
  boolean isFlagged;
  ArrayList<Tile> neighbors;

  Tile(int width, int height) {
    this.width = width;
    this.height = height;
    this.isMine = false;
    this.isRevealed = false;
    this.isFlagged = false;
    this.neighbors = new ArrayList<Tile>();
  }

  // Returns whether this tile is a mine
  boolean isMine() {
    return this.isMine;
  }

  // Adds a mine to this tile
  void addMine() {
    this.isMine = true;
  }

  // Adds the given tile as a neighbor of this tile
  void addNeighbor(Tile t) {
    this.neighbors.add(t);
  }

  // Counts the number of neighboring mines
  int countMines() {
    int count = 0;
    for (Tile t : this.neighbors) {
      if (t.isMine) {
        count++;
      }
    }
    return count;
  }

  // Draws this tile, changing depending on whether it is a mine
  WorldImage draw() {
    Color unrevealed = Color.decode("#b3c6ff");
    Color revealed = Color.decode("#A0A0A0");
    RectangleImage outline = new RectangleImage(this.width, this.height, "outline", Color.BLACK);
/*
    if (this.isMine) {
      RectangleImage fill = new RectangleImage(this.width, this.height, "solid", unrevealed);
      OverlayImage rect = new OverlayImage(outline, fill);
      return new OverlayImage(new CircleImage((int) (this.width * 0.4), "solid", Color.RED), rect)
          .movePinhole(-(this.width / 2), -(this.height / 2));
    }
    
    else */if (this.isRevealed) {
      RectangleImage fill = new RectangleImage(this.width, this.height, "solid", revealed);
      OverlayImage rect = new OverlayImage(outline, fill);
      if (this.countMines() > 0) {
        return new OverlayImage(this.drawNumber(), rect).movePinhole(-(this.width / 2),
            -(this.height / 2));
      }
      else {
        return rect.movePinhole(-(this.width / 2), -(this.height / 2));
      }
      
    }
    
    else if (this.isFlagged) {
      EquilateralTriangleImage flag = new EquilateralTriangleImage(this.width * 0.3, OutlineMode.SOLID, Color.ORANGE);
      RectangleImage fill = new RectangleImage(this.width, this.height, "solid", unrevealed);
      OverlayImage rect = new OverlayImage(outline, fill);
      return new OverlayImage(flag, rect).movePinhole(-(this.width / 2),
          -(this.height / 2));
    }
    else {
      RectangleImage fill = new RectangleImage(this.width, this.height, "solid", unrevealed);
      OverlayImage rect = new OverlayImage(outline, fill);
      return rect.movePinhole(-(this.width / 2), -(this.height / 2));
    }
  }

  TextImage drawNumber() {
    Color c;
    if (this.countMines() == 1) {
      c = Color.BLUE;
    }
    else if (this.countMines() == 2) {
      c = Color.GREEN;
    }
    else if (this.countMines() == 3) {
      c = Color.RED;
    }
    else if (this.countMines() == 4) {
      c = Color.decode("#333399");
    }
    else if (this.countMines() == 5) {
      c = Color.decode("#990033");
    }
    else if (this.countMines() == 6) {
      c = Color.BLACK;
    }
    else {
      c = Color.GRAY;
    }
    return new TextImage(Integer.toString(this.countMines()), (int) (this.width * 0.8), c);
  }

  void reveal() {
    this.isRevealed = true;
    if (this.countMines() == 0) {
      for (Tile t : this.neighbors) {
        if (!t.isMine() && !t.isRevealed) {
          t.reveal();
        }
      }
    }
  }
  
  void flag() {
    this.isFlagged = !this.isFlagged;
  }
}

class ExampleMinesweeperGame {
  int rows;
  int cols;
  int mines;
  MinesweeperGame g;

  void initTestConditions() {
    this.rows = 10;
    this.cols = 10;
    this.mines = 2;
    this.g = new MinesweeperGame(rows, cols, mines);

  }

  void testIsMine(Tester t) {
    initTestConditions();
    t.checkExpect(g.tiles.get(0).get(0).isMine(), false);
    t.checkExpect(g.tiles.get(0).get(1).isMine(), true);
  }

  void testNeighbors(Tester t) {
    initTestConditions();
    t.checkExpect(g.tiles.get(0).get(0).neighbors, new ArrayList<Tile>(
        Arrays.asList(g.tiles.get(1).get(0), g.tiles.get(0).get(1), g.tiles.get(1).get(1))));
    t.checkExpect(g.tiles.get(1).get(1).neighbors,
        new ArrayList<Tile>(Arrays.asList(g.tiles.get(2).get(1), g.tiles.get(1).get(2),
            g.tiles.get(0).get(1), g.tiles.get(1).get(0), g.tiles.get(0).get(0),
            g.tiles.get(2).get(0), g.tiles.get(0).get(2), g.tiles.get(2).get(2))));
  }

  void testCountMines(Tester t) {
    initTestConditions();
    t.checkExpect(g.tiles.get(0).get(0).countMines(), 2);
    t.checkExpect(g.tiles.get(0).get(3).countMines(), 0);
    t.checkExpect(g.tiles.get(4).get(6).countMines(), 4);
  }

  void testDraw(Tester t) {
    initTestConditions();
    t.checkExpect(g.tiles.get(0).get(0).draw(),
        new OverlayImage(new RectangleImage(20, 20, "outline", Color.BLACK),
            new RectangleImage(20, 20, "solid", Color.LIGHT_GRAY)).movePinhole(-10, -10));
    t.checkExpect(g.tiles.get(0).get(1).draw(),
        new OverlayImage(new CircleImage(8, "solid", Color.RED),
            new OverlayImage(new RectangleImage(20, 20, "outline", Color.BLACK),
                new RectangleImage(20, 20, "solid", Color.LIGHT_GRAY))).movePinhole(-10, -10));

  }

  void testGame(Tester t) {
    initTestConditions();
    g.bigBang(g.cols * g.tileSize + 1, g.rows * g.tileSize + 1, 0.1);
  }

}