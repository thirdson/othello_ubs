/*
 * White.java
 *
 * Version:
 *    $Id$
 *
 * Revisions:
 *    &Log$
 *
 */

import java.util.ArrayList;
public class White
{
	final static int pointTable[][] = new int[Game.WIDTH][Game.HEIGHT];

	// Assigned points to various places on the board, based on this configuration
	// of a quarter of the board
	// [50][-1][5][2]
	// [-1][-10][1][1]
	// [5][1][1][1]
	// [2][1][1][0]
	// Credits for values go to: http://www.site-constructor.com/othello/Present/BoardLocationValue.html

	final int CORNER = 50;
	final int DIAGONAL = -10;
	final int SECOND = -1;
	final int THIRD = 5;
	final int FOURTH = 2;
	final int COMMON = 1;
	final int STARTER = 0;

	// Weight values used to determine priorities when examining potential moves
	final double POSITIONWEIGHT = 5;
	final double MOBILITYWEIGHT = 15;
	final double ENDWEIGHT = 300;

	// Used to check during minimax algorith
	final double INFINITE = 100000000;

	// Used to determine how large search trees should be
	// Depth 3 actually performs worse, there is some kind of logic error in my algorithm
	// Depth 4 doesn't even return anything, I can't tell if it's because of the exponential growth or if there's a bug somewhere
	final int MAXDEPTH = 2;

	/**
	 *  Default constructor
	 */
	public White()
	{
		fillPoints();
	}

	/**
	 *  Manually fills in the points board, which is used for a simple point-based strategy
     *  Only supports 8 x 8 board
	 *
     *  @return   void
	 */
	public void fillPoints()
	{
		// Set values for the top left quadrant
		pointTable[1][1] = CORNER;
		pointTable[2][2] = DIAGONAL;
		pointTable[1][2] = pointTable[2][1] = SECOND;
		pointTable[1][3] = pointTable[3][1] = THIRD;
		pointTable[1][4] = pointTable[4][1] = FOURTH;
		pointTable[2][3] = pointTable[2][4] = pointTable[3][2] = pointTable[3][3] =
		pointTable[3][4] = pointTable[4][2] = pointTable[4][3] = COMMON;
		pointTable[4][4] = STARTER;

		// Duplicate values for top right quadrant
		for (int i = 5; i <= 8; i++)
		{
			for (int j = 1; j <= 4; j++)
			{
				pointTable[j][i] = pointTable[(j)][9-i];
			}
		}

		// Duplicate values for bottom two quadrants
		for (int i = 1; i <= 8; i++)
		{
			for (int j = 5; j <= 8; j++)
			{
				pointTable[j][i] = pointTable[9-j][(i)];
			}
		}

		// Set values for out of bounds cells
		for (int i = 0; i <= 9; i++)
		{
			pointTable[i][0] = pointTable[i][9] = pointTable[0][i] = pointTable[9][i] = STARTER;
		}
	}

    /**
     *  This method calls the appropriate strategy.
     *
     *  @param    game    the current state of the game
     *  @param    done    true if the player cannot move anywhere
     *  @param    color   the color (Black or White) of the player
     *
     *  @return   game    the resulting state of the game
     */
    public Game strategy(Game game, boolean done, int color) {

        return searchStrategy(game,done,color);
    }

    /**
     *  Take a turn using a random strategy.
     *
     *  @param    game    the current state of the game
     *  @param    done    true if the player cannot move anywhere
     *  @param    color   the color (Black or White) of the player
     *
     *  @return   game    the resulting state of the game
     */
    public Game randStrategy(Game game, boolean done, int color) {

        int row = (int)(Math.random()*(game.HEIGHT-2)) + 1;
        int column = (int)(Math.random()*(game.WIDTH-2)) + 1;

        while (!done && !game.legalMove(row,column,color,true)) {
            row = (int)(Math.random()*(game.HEIGHT-2)) + 1;
            column = (int)(Math.random()*(game.WIDTH-2)) + 1;
        }

        if (!done)
            game.board[row][column] = color;

        return game;
    }

    /**
     *  Take a turn using a point based strategy
     *
     *  @param    game    the current state of the game
     *  @param    done    true if the player cannot move anywhere
     *  @param    color   the color (Black or White) of the player
     *
     *  @return   game    the resulting state of the game
     */
    public Game pointStrategy(Game game, boolean done, int color)
	{
		if (!done)
		{
			Move bestMove = new Move();
			Move currentMove = new Move();

			// Look for every legal move
			for (int i = 1; i <= 8; i++)
			{
				for (int j = 1; j <= 8; j++)
				{
					// Store the current move being checked
					currentMove = game.pointMove(j, i, color, false, pointTable);

					// If it is legal
					if (currentMove.legal)
					{
						// If the Move being checked has a higher point score than
						// the best Move, or if the best Move has not been assigned
						if (currentMove.points > bestMove.points || !bestMove.legal)
						{
							bestMove = currentMove;
						}
					}
				}
			}

			if (bestMove.legal)
			{
				game.pointMove(bestMove.y, bestMove.x, color, true, pointTable);
				game.board[bestMove.y][bestMove.x] = color;
			}
		}

        return game;
    }

	/**
     *  Take a turn using a search based strategy that makes use of cell points
     *
     *  @param    game    the current state of the game
     *  @param    done    true if the player cannot move anywhere
     *  @param    color   the color (Black or White) of the player
     *
     *  @return   game    the resulting state of the game
     */
	public Game searchStrategy(Game game, boolean done, int color)
	{
		if (!done)
		{
			Node currentState = new Node(new Move(), game, color);
			currentState = buildTree(currentState, MAXDEPTH);
			//printTree(currentState);

			Move bestMove = minimax(currentState, color);
			//System.out.println("Best move is " + testMove.x + " " + testMove.y);

			if (bestMove.legal)
			{
				game.pointMove(bestMove.y, bestMove.x, color, true, pointTable);
				game.board[bestMove.y][bestMove.x] = color;
			}
		}

		return game;
	}

	/**
     *  Creates a tree to explore potential moves
     *
	 * 	@param    parent   The node to expand
     *  @param    depth    A counter to keep track of the depth of the tree.
	 *
	 *  @return   parent   The parent with any new children
     */
	public Node buildTree(Node parent, int depth)
	{
		// Check to see if there is still more to expand and if the game isn't done
		if (depth > 0 && parent.end == -1)
		{
			// Decrease the depth count
			depth--;

			// Determine the subsequent turn ahead of time
			int nextTurn;
			if (parent.turn == Game.BLACK)
				nextTurn = Game.WHITE;
			else
				nextTurn = Game.BLACK;

			// Searches for legal moves
			for (int i = 1; i <= 8; i++)
			{
				for (int j = 1; j <= 8; j++)
				{
					// Store the current move being checked
					Move currentMove = parent.state.pointMove(i, j, parent.turn, false, pointTable);

					// If it is legal
					if (currentMove.legal)
					{
						//if (parent.turn == Game.BLACK)
						//	System.out.println("BLACK (" + parent.turn + ") turn");
						//else
						//	System.out.println("WHITE (" + parent.turn + ") turn");
						//System.out.println(j + " " + i + " is a legal move");

						// Create a Game object that attempts the current move
						Game futureGame = new Game(parent.state);
						futureGame = makeMove(futureGame, false, parent.turn, currentMove);

						// Create a Node that holds the current move, the future game
						Node newNode = new Node(currentMove, futureGame, nextTurn);

						// The position value of the new node is the number of points gained by the move leading up to that node
						newNode.position = currentMove.points;
						// Check the number of potential moves of the future game
						newNode.mobility = mobilityCheck(futureGame, nextTurn);
						// Checked whether or not the future game has ended
						newNode.end =  endCheck(futureGame);

						parent.addChild(newNode);
						newNode.parent = parent;
					}
				}
			}

			// Check again to see if there is still more to expand
			if (depth > 0)
			{
				// Build a sub tree for each child
				for (Node n : parent.children)
				{
					n = buildTree(n, depth);
				}
			}

			// Calculate mobility while recursing backwards
			parent.mobility = parent.children.size();
		}
		return parent;
	}

	/**
	 *  Uses minimax algorithm to determine a move
     *
     *  @param    root    the root of the tree to be checked
	 *	@param	  color   the player who is making the decision
	 *
	 *  @return   best    the best course of action as indicated by minimax
	 */

	public Move minimax(Node root, int color)
	{
		double max = 0;
		Node bestNode = null;

		// Check every node in children
		for (Node n : root.children)
		{
			// Find the node with the best value
			double tempMin = minValue(n, color);
			if (bestNode == null)
			{
				max = tempMin;
				bestNode = n;
			}
			else if (tempMin > max)
			{
				max = tempMin;
				bestNode = n;
			}
		}

		return bestNode.last;
	}

	 /**
	 *  Calculates the min-value of a given state
     *
     *  @param    check   the specific Node to check
	 *	@param	  color   the player who is making the decision
	 *
	 *  @return   min     the min-value for tree
	 */

	public double minValue(Node check, int color)
	{
		double min = INFINITE;

		// Check to see if this Node is a leaf
		if (check.children.size() == 0)
		{
			// Check to see if the game stored in this Node has ended
			if (check.end == -1)
			{
				// Calculate the value based on position and mobility
				min = check.position * POSITIONWEIGHT - check.mobility * MOBILITYWEIGHT;

				//System.out.println("Node: " + check.last.x + " " + check.last.y + " scores " + min + " points");
			}
			else if (check.end == color)
			{
				// The player has won, return
				min = ENDWEIGHT;
				return min;
			}
			else if (check.end != color)
			{
				// The player has lost or it is a tie, return
				min = -ENDWEIGHT;
				return min;
			}
		}

		// Look for the node among the children with the least value
		for (Node n : check.children)
		{
			double tempMin = maxValue(n, color);
			if (tempMin < min)
			{
				min = tempMin;
			}
		}

		return min;
	}

	/**
	 *  Calculates the max-value of a given state
     *
     *  @param    check    the specific Node to check
	 *	@param	  color   the player who is making the decision
	 *
	 *  @return   max     the max-value for tree
	 */
	public double maxValue(Node check, int color)
	{
		double max = -INFINITE;

		// Check to see if this Node is a leaf
		if (check.children.size() == 0)
		{
			// Check to see if the game stored in this Node has ended
			if (check.end == -1)
			{
				// Calculate the value based on position and mobility
				max = -check.position * POSITIONWEIGHT + check.mobility * MOBILITYWEIGHT;

				//System.out.println("Node: " + check.last.x + " " + check.last.y + " scores " + max + " points");
			}
			else if (check.end == color)
			{
				// The player has won, return
				max = ENDWEIGHT;
				return max;
			}
			else if (check.end != color)
			{
				// The player has lost or it is a tie, return
				max = -ENDWEIGHT;
				return max;
			}
		}

		// Look for the node among the children with the least value
		for (Node n : check.children)
		{
			double tempMax = minValue(n, color);
			if (tempMax > max)
			{
				max = tempMax;
			}
		}

		return max;
	}

	/**
     *  Take a turn using a given game and a move
     *
     *  @param    game    the current state of the game
     *  @param    done    true if the player cannot move anywhere
     *  @param    color   the color (Black or White) of the player
     *
     *  @return   game    the resulting state of the game
     */
    public Game makeMove(Game game, boolean done, int color, Move move)
	{
		if (!done)
		{
			if (move.legal)
			{
				game.pointMove(move.y, move.x, color, true, pointTable);
				game.board[move.y][move.x] = color;
			}
		}

        return game;
    }

	/**
     *  Checks to see how many potential moves can be made from this game
     *
     *  @param    game    the current state of the game
     *  @param    color   the player whose turn it is
	 *
     *  @return   result  the number of moves the player specified by color can make
     */
    public int mobilityCheck(Game game, int color)
	{
		int result = 0;

		for (int i=1; i<game.HEIGHT-1; i++)
		{
            for (int j=1; j<game.WIDTH-1; j++)
			{
				if ((game.legalMove(i, j, color, false)))
				{
                    result++;
				}
			}
		}

        return result;
    }

	/**
     *  Checks to see if a given game is finished
     *
     *  @param    game    the current state of the game
     *
     *  @return   result  -1 if game is unfinished, 0 in case of a tie or either 1 or 2 depending on which player is the winner
     */
    public int endCheck(Game game)
	{
		int result = -1;
		int whiteSum = 0;
		int blackSum = 0;

		for (int i=1; i<game.HEIGHT-1; i++)
		{
            for (int j=1; j<game.WIDTH-1; j++)
			{
				if ((game.legalMove(i, j, Game.BLACK, false)) ||
                   (game.legalMove(i, j, Game.WHITE, false)))
				{
                    result = -1;
					return result;
				}

				if (game.board[i][j] == Game.BLACK)
				{
					blackSum++;
				}
				else if (game.board[i][j] == Game.WHITE)
				{
					whiteSum++;
				}
			}
		}

		if (blackSum > whiteSum)
		{
			result = Game.BLACK;
		}
		else if (whiteSum > blackSum)
		{
			result = Game.WHITE;
		}
		else
		{
			result = 0;
		}

        return result;
    }

	/**
	 *	Prints out the tree for debugging purposes.
	 */
	public void printTree(Node parent)
	{
		ArrayList<Node> q = new ArrayList<Node>();
		Node temp = parent;
		for (Node n : temp.children)
		{
			q.add(n);
		}

		System.out.println("Node: " + temp.last.x + " " + temp.last.y);

		while (!q.isEmpty())
		{
			temp = (Node)q.remove(0);
			System.out.println("Node: " + temp.last.x + " " + temp.last.y +
								" (Parent: " + temp.parent.last.x + " " + temp.parent.last.y + ")");
			System.out.println("Position: " + temp.position);
			System.out.println("Mobility: " + temp.mobility);
			System.out.println("End: " + temp.end);

			for (Node n : temp.children)
			{
				q.add(n);
			}
		}
	}

	/**
	 *  Prints out the current scores for debugging purposes.
	 */
	public void printScores()
	{
		for (int i = 0; i <= 9; i++)
		{
			for (int j = 0; j <= 9; j++)
			{
				System.out.print("[" + pointTable[i][j] + "]");
			}
			System.out.println();
		}
	}
}
