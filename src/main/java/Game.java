public class Game {
  static final int BLACK = 1; // Declare state of each square
  static final int WHITE = 2;
  static final int EMPTY = 0;
  static final int WIDTH = 10;
  static final int HEIGHT = 10;
  final int board[][] = new int[WIDTH][HEIGHT];

  /** Default constructor */
  public Game() {}

  /**
   * Creates a copy of the game
   *
   * @param another The game to be copied
   */
  public Game(Game another) {
    for (int i = 0; i < HEIGHT; i++) {
      for (int j = 0; j < WIDTH; j++) {
        this.board[i][j] = another.board[i][j];
      }
    }
  }

  /**
   * Decide if the move is legal
   *
   * @param r row in the game matrix
   * @param c column in the game matrix
   * @param color color of the player - Black or White
   * @param flip true if the player wants to flip the discs
   * @return true if the move is legal, else false
   */
  public boolean legalMove(int r, int c, int color, boolean flip) {
    // Initialize boolean legal as false
    boolean legal = false;

    // If the cell is empty, begin the search
    // If the cell is not empty there is no need to check anything
    // so the algorithm returns boolean legal as is
    if (board[r][c] == 0) {
      // Initialize variables
      int posX;
      int posY;
      boolean found;
      int current;

      // Searches in each direction
      // x and y describe a given direction in 9 directions
      // 0, 0 is redundant and will break in the first check
      for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
          // Variables to keep track of where the algorithm is and
          // whether it has found a valid move
          posX = c + x;
          posY = r + y;
          found = false;
          current = board[posY][posX];

          // Check the first cell in the direction specified by x and y
          // If the cell is empty, out of bounds or contains the same color
          // skip the rest of the algorithm to begin checking another direction
          if (current == -1 || current == 0 || current == color) {
            continue;
          }

          // Otherwise, check along that direction
          while (!found) {
            posX += x;
            posY += y;
            current = board[posY][posX];

            // If the algorithm finds another piece of the same color along a direction
            // end the loop to check a new direction, and set legal to true
            if (current == color) {
              found = true;
              legal = true;

              // If flip is true, reverse the directions and start flipping until
              // the algorithm reaches the original location
              if (flip) {
                posX -= x;
                posY -= y;
                current = board[posY][posX];

                while (current != 0) {
                  board[posY][posX] = color;
                  posX -= x;
                  posY -= y;
                  current = board[posY][posX];
                }
              }
            }
            // If the algorithm reaches an out of bounds area or an empty space
            // end the loop to check a new direction, but do not set legal to true yet
            else if (current == -1 || current == 0) {
              found = true;
            }
          }
        }
      }
    }

    return legal;
  }

  /**
   * A modification of legalMove() that uses a simple class to hold data about the potential move
   *
   * @param r Row in the game matrix
   * @param c Column in the game matrix
   * @param color Color of the player - Black or White
   * @param flip True if the player wants to flip the discs
   * @param point A table of Multipoints to determine the numeric value of a move
   * @return A move object that also indicates whether or not the move is legal
   */
  public Move pointMove(int r, int c, int color, boolean flip, int[][] point) {
    // Initialize a default Move object
    Move newMove = new Move();

    if (board[r][c] == 0) {
      int posX;
      int posY;
      boolean found;
      int current;
      int sum;

      for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
          posX = c + x;
          posY = r + y;
          found = false;
          current = board[posY][posX];
          sum = 0;

          if (current == -1 || current == 0 || current == color) {
            continue;
          } else {
            // First piece is an enemy so add to the point count
            sum += point[posY][posX];
          }

          while (!found) {
            posX += x;
            posY += y;
            current = board[posY][posX];

            if (current == color) {
              found = true;
              newMove.legal = true;
              newMove.x = c;
              newMove.y = r;
              newMove.points += point[c][r];

              if (flip) {
                posX -= x;
                posY -= y;
                current = board[posY][posX];

                while (current != 0) {
                  board[posY][posX] = color;
                  posX -= x;
                  posY -= y;
                  current = board[posY][posX];
                }
              }
            } else if (current == -1 || current == 0) {
              // The pieces in this direction won't be flipped so reset sum to 0
              sum = 0;
              found = true;
            } else {
              // Piece is an enemy so add to the point count
              sum += point[posY][posX];
            }
          }

          // Done checking this direction so add the sum to the Move object point co
          newMove.points += sum;
        }
      }
    }
    return newMove;
  }

  /** Prints out the board for debugging purposes. */
  public void printBoard() {
    for (int i = 1; i <= HEIGHT - 2; i++) {
      for (int j = 1; j <= WIDTH - 2; j++) {
        System.out.print("[" + board[i][j] + "]");
      }
      System.out.println();
    }
  }
}
