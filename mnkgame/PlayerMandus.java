package mnkgame;

import java.util.Random;

import javax.swing.plaf.FileChooserUI;

import mnkgame.MNKBoard;

import java.lang.reflect.Array;
import java.util.Arrays;


/**
 * Software player only a bit smarter than random.
 * <p> It can detect a single-move win or loss. In all the other cases behaves randomly.
 * </p> 
 */
public class PlayerMandus implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;
    private long start;
	int	numOfCombinations = 0;

	int numCell;

    public void PlayerMandus() {
	}


	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand    = new Random(System.currentTimeMillis()); 
		B       = new MNKBoard(M,N,K);
		myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		TIMEOUT = timeout_in_secs;	
	}

public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		start = System.currentTimeMillis();
		numOfCombinations = 0;
		
		if(MC.length > 0) {
			MNKCell c = MC[MC.length-1]; // Recover the last move from MC
			B.markCell(c.i,c.j);         // Save the last move in the local MNKBoard
		}
		
		// If there is just one possible move, return immediately
		if(FC.length == 1)
			return FC[0];
		
		if(myWin == MNKGameState.WINP1 && MC.length==0) //se la prima mossa spetta al mio giocatore può essere effettuata randomicamente
		{
			MNKCell c = FC[rand.nextInt(FC.length)];
			B.markCell(c.i,c.j);
			return c;
		}

		for(MNKCell MW: FC){
			if(B.markCell(MW.i, MW.j) == myWin){
				return MW;
			}
			else{
				B.unmarkCell();
			}
		}
	
		
		double score, maxEval = Integer.MIN_VALUE;
		int pos   = rand.nextInt(FC.length); 
		MNKCell result = FC[pos]; // random move

		for(MNKCell currentCell : FC) {
			
			// If time is running out, return the randomly selected  cell
            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)) {
				break;
				
			}else{
                B.markCell(currentCell.i, currentCell.j);	
                numCell = FC.length;
                if(numCell > 85)
					score = alphabeta(B, true, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
				else if((numCell > 50) && (numCell <= 85))
					score = alphabeta(B, true, 2, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
				else if((numCell > 30) && (numCell <= 50))
					score = alphabeta(B, true, 3, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
				else if((numCell > 20) && (numCell <= 30))
					score = alphabeta(B, true, 4, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
				else if((numCell > 16) && (numCell <= 20))
					score = alphabeta(B, true, 5, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);	
				else
					score = alphabeta(B, true, 6, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);					
		
				
				if(score > maxEval){
					maxEval = score;
					result = currentCell;
				}
				
				B.unmarkCell();			

            


            }
        }
		B.markCell(result.i, result.j);		

        return result;

    }

    public double alphabeta(MNKBoard board_, boolean myNode, int depth, double alpha, double beta, int turns_to_win){
		
		double eval = 0;
		int iteratore = 0;
		int bestCol = 0;
		int bestRow;
		double score = 0;

		MNKCell freeCells[] = board_.getFreeCells();
		
		if(depth <= 0 || board_.gameState != MNKGameState.OPEN || (System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)){
			numOfCombinations++;
            //l'albero ha profondità 0, o se non siamo in gioco o se stiamo per terminare la scelta, valutiamo
			eval = evaluate(board_, depth, turns_to_win);  
						
		}else if(myNode){
			eval = Integer.MAX_VALUE;
			
			for(MNKCell cell : freeCells){
				board_.markCell(cell.i, cell.j);
				eval = Math.min(eval,alphabeta(board_, false, depth-1, alpha, beta, turns_to_win + 1));
				beta = Math.min(eval, beta);
				board_.unmarkCell();
				if(beta <= alpha)
					break;
			}
			
		}else{
			eval = Integer.MIN_VALUE;
			
			for(MNKCell cell : freeCells){
				board_.markCell(cell.i, cell.j);
				eval = Math.max(eval, alphabeta(board_, true, depth-1, alpha, beta, turns_to_win + 1));
				beta = Math.max(eval, alpha);
				board_.unmarkCell();
				if(beta <= alpha)
					break;
			}			
		}
		
		return eval;	
	}

	public double evaluate(MNKBoard board, int depth, int turns_to_win){
		System.out.println("turno: "+ turns_to_win);
		double score = 0; //score attuale
		int contaP1 = 0;
		int contaP2 = 0;
		boolean ctrl = true;
		System.out.println("Stato di gioco : " + board.gameState);
		System.out.println("Combinazione numero : " + numOfCombinations);
		if(board.gameState == myWin)
		{
			score = 100 - turns_to_win * 10; //45; // arriviamo alla vittoria più velocemente
			System.out.println("score my win: "+ score);
		}
		else if(board.gameState == yourWin)
		{
			score = -100 - turns_to_win * 10; //-45;
			System.out.println("score your win: "+ score);
		}
		else if(board.gameState == MNKGameState.DRAW)
		{
			score = 0;
			System.out.println("score draw: "+ score);
		}
		else{
			//da fare il controllo per righe, colonne e diagonali, cercando di rimanere nell'intervallo [-10, 10]
			
			// valuto righe
			for (int i = 0; i < board.M; i++)
			{
				MNKCellState[] combination = new MNKCellState[board.N];
				for (int j=0; j < board.N; j++)
				{
					combination[j] = board.B[i][j];
					System.out.println(Arrays.toString(combination));
				}
				score += calculateCombination(board, combination);
			}

			//colonne
			for (int j = 0; j < board.N; j++)
			{
				System.out.println("entro un attimo");
				MNKCellState[] combination = new MNKCellState[board.M];
				for (int i = 0; i < board.M; i++)
				{
					System.out.println("sto entrando anche qua");
					combination[i] = board.B[i][j];
					System.out.println(Arrays.toString(combination));
				}
				System.out.println("Combinazione : " + Arrays.toString(combination));
				score += calculateCombination(board, combination);
			}
			//diagonale
			for (int i = 0; i <= board.M - board.K; i++)
      		{
				for (int j = 0; j <= board.N - board.K; j++)
	  			{
					MNKCellState[] combination = new MNKCellState[board.M];
					int c = 0;
					int x = i;
					int y = j;
					while (c < board.K)
					{
						combination[c] = board.B[x++][y++];
						c++;
					}
					System.out.println("Combinazione diagonale : " + Arrays.toString(combination));
					score += calculateCombination(board, combination);
				}
      		}
			//anti diagonale
			for (int i = 0; i <= board.M - board.K; i++)
      		{
				for (int j = board.N - 1; j >= board.K - 1; j--)
	  			{
					System.out.println("j: "+ j);
					MNKCellState[] combination = new MNKCellState[board.M];
					int c = 0;
					int x = i;
					int y = j;
					while (c < board.K)
					{
						combination[c] = board.B[x++][y--];
						System.out.println("y: "+ y);
						c++;
					}
					System.out.println("Combinazione anti diagonale : " + Arrays.toString(combination));
					score += calculateCombination(board, combination);
				}
      		}

		}
		return score;
	}
	 
	public double calculateCombination(MNKBoard board, MNKCellState[] combination)
	{
		double score = 0.0;
		int[] occurrenciesP1 = new int[combination.length];
		int[] occurrenciesP2 = new int[combination.length];

		if(combination[0] == MNKCellState.P1)
		{
			occurrenciesP1[0] = 1;
			occurrenciesP2[0] = 0;
		}
		else if(combination[0] == MNKCellState.P2)
		{
			occurrenciesP1[0] = 0;
			occurrenciesP2[0] = 1;
		}
		else{
			occurrenciesP1[0] = 1;
			occurrenciesP2[0] = 1;
		}

		int maxP1 = occurrenciesP1[0];
		int maxP2 = occurrenciesP2[0];

		for(int i=1; i < combination.length;i++)
		{
			if(combination[i] == MNKCellState.P1 || combination[i] == MNKCellState.FREE)
			{
				occurrenciesP1[i] = occurrenciesP1[i-1] + 1;
				occurrenciesP2[i] = 0;
			}
			else if(combination[i] == MNKCellState.P2 || combination[i] == MNKCellState.FREE)
			{
				occurrenciesP2[i] = occurrenciesP2[i-1] + 1;
				occurrenciesP1[i] = 0;
			}

			if(occurrenciesP1[i] > maxP1)
			{
				maxP1 = occurrenciesP1[i];
			}
			if(occurrenciesP2[i] > maxP2)
			{
				maxP2 = occurrenciesP2[i];
			}
		}

		if(maxP1 >= board.K)
		{
			score += maxP1 - maxP2;
		}
		else if(maxP2 >= board.K)
		{
			score -= maxP2 - maxP1;
		}
		System.out.println(Arrays.toString(combination));
		System.out.println(Arrays.toString(occurrenciesP1) + " " + Arrays.toString(occurrenciesP2));
		System.out.println(maxP1 + " " + maxP2);
		System.out.println(score);
		return score;
	}
    
    public String playerName() {
        return "Mandus";
    }
}