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

		for(MNKCell MW: FC){ // se con un passo posso vincere, faccio direttamente quel passo
			if(B.markCell(MW.i, MW.j) == myWin){
				return MW;
			}
			else{
				B.unmarkCell();
			}
		}

		for (MNKCell MM : FC){

			B.markCell(MM.i, MM.j);
			for(MNKCell YW : FC){
				if(YW.i != MM.i && YW.j != MM.j){
					if(B.markCell(YW.i, YW.j) == yourWin){
						B.unmarkCell();
						B.unmarkCell();
						B.markCell(YW.i, YW.j);
						return YW;
					}
					else{
						B.unmarkCell();
					}
				}
			}
			B.unmarkCell();
		
		}
	
		
		double score, maxEval = Integer.MIN_VALUE;
		MNKCell result = FC[rand.nextInt(FC.length)]; // preparo una mossa random, in modo che se va in timeout, viene scelta lei
		int depth = 0;

		for(MNKCell currentCell : FC) {
			
			// If time is running out, return the randomly selected  cell
            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)) {
				break;
				
			}else{
                B.markCell(currentCell.i, currentCell.j);	
				depth = selectDepth(FC.length);
                score = alphabeta(B, true, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);				
			
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

    public double alphabeta(MNKBoard board_, boolean turn, int depth, double alpha, double beta){
		
		double eval = 0;
		double score = 0;

		MNKCell freeCells[] = board_.getFreeCells();

		if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0))
		{
			return 0;
		}
		
		if(depth <= 0 || board_.gameState != MNKGameState.OPEN){
            //l'albero ha profondità 0, o se non siamo in gioco o se stiamo per terminare la scelta, valutiamo
			eval = evaluate(board_);  
						
		}else if(turn){
			eval = Integer.MIN_VALUE;
			
			for(MNKCell cell : freeCells){
				board_.markCell(cell.i, cell.j);
				eval = Math.max(eval,alphabeta(board_, false, depth-1, alpha, beta) - depth * 10);
				beta = Math.max(eval, beta);
				board_.unmarkCell();
				if(beta <= alpha)
					break;
			}
			
		}else{
			eval = Integer.MAX_VALUE;
			
			for(MNKCell cell : freeCells){
				board_.markCell(cell.i, cell.j);
				eval = Math.min(eval, alphabeta(board_, true, depth-1, alpha, beta) + depth * 10);
				beta = Math.min(eval, alpha);
				board_.unmarkCell();
				if(beta <= alpha)
					break;
			}			
		}
		return eval;	
	}

	public double evaluate(MNKBoard board){
		//System.out.println("turno: "+ turns_to_win);
		double score = 0; //score attuale
		//System.out.println("Stato di gioco : " + board.gameState);
		//System.out.println("Combinazione numero : " + numOfCombinations);
		if(board.gameState == myWin)
		{
			score = 1000; 
		}
		else if(board.gameState == yourWin)
		{
			score = -1000; 
		}
		else if(board.gameState == MNKGameState.DRAW)
		{
			score = 0;
		}
		else{			
			// valuto righe
			for (int i = 0; i < board.M; i++)
			{
				MNKCellState[] combination = new MNKCellState[board.N];
				for (int j=0; j < board.N; j++)
				{
					combination[j] = board.B[i][j];
				}
				score += calculateCombination(board, combination);
			}

			//colonne
			for (int j = 0; j < board.N; j++)
			{
				//System.out.println("entro un attimo");
				MNKCellState[] combination = new MNKCellState[board.M];
				for (int i = 0; i < board.M; i++)
				{
					//System.out.println("sto entrando anche qua");
					combination[i] = board.B[i][j];
					//System.out.println(Arrays.toString(combination));
				}
				//System.out.println("Combinazione : " + Arrays.toString(combination));
				score += calculateCombination(board, combination);
			}
			//diagonale
			for (int i = 0; i <= board.M - board.K; i++)
      		{
				for (int j = 0; j <= board.N - board.K; j++)
	  			{
					MNKCellState[] combination = new MNKCellState[board.K];
					int c = 0;
					int x = i;
					int y = j;
					while (c < board.K)
					{
						combination[c] = board.B[x++][y++];
						c++;
					}
					//System.out.println("Combinazione diagonale : " + Arrays.toString(combination));
					score += calculateCombination(board, combination);
				}
      		}
			//anti diagonale
			for (int i = 0; i <= board.M - board.K; i++)
      		{
				for (int j = board.N - 1; j >= board.K - 1; j--)
	  			{
					//System.out.println("j: "+ j);
					MNKCellState[] combination = new MNKCellState[board.K];
					int c = 0;
					int x = i;
					int y = j;
					while (c < board.K)
					{
						combination[c] = board.B[x++][y--];
						//System.out.println("y: "+ y);
						c++;
					}
					//System.out.println("Combinazione anti diagonale : " + Arrays.toString(combination));
					score += calculateCombination(board, combination);
				}
      		}

		}
		return score;
	}
	 
	public double calculateCombination(MNKBoard board, MNKCellState[] combination)
	{
		double score = 0.0;
		double[] occurrenciesP1 = new double[combination.length];
		double[] occurrenciesP2 = new double[combination.length];

		if(combination[0] == MNKCellState.P1)
		{
			occurrenciesP1[0] = 1.5;
			occurrenciesP2[0] = 0;
		}
		else if(combination[0] == MNKCellState.P2)
		{
			occurrenciesP1[0] = 0;
			occurrenciesP2[0] = 2.5;
		}
		else{
			occurrenciesP1[0] = 1;
			occurrenciesP2[0] = 1.5;
		}

		double maxP1 = occurrenciesP1[0];
		double maxP2 = occurrenciesP2[0];

		for(int i=1; i < combination.length;i++)
		{
			if(combination[i] == MNKCellState.P1 || combination[i] == MNKCellState.FREE)
			{
				if(combination[i] == MNKCellState.P1)
				{
					occurrenciesP1[i] = occurrenciesP1[i-1] + 1.5;
				}
				else{
					occurrenciesP1[i] = occurrenciesP1[i-1] + 1;
				}
				occurrenciesP2[i] = 0;
			}
			else if(combination[i] == MNKCellState.P2 || combination[i] == MNKCellState.FREE)
			{
				if(combination[i] == MNKCellState.P2)
				{
					occurrenciesP2[i] = occurrenciesP2[i-1] + 2.5;
				}
				else{
					occurrenciesP2[i] = occurrenciesP2[i-1] + 1.5;
				}
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

		score += (maxP1 - maxP2) * 10;
		return score;
	}

	public int selectDepth(int numCell)
	{
		if(numCell >= 0 && numCell <= 12)
		{
			return 6;
		}
		else if(numCell > 12 && numCell <= 25)
		{
			return 5;
		}
		else if(numCell > 25 && numCell <= 36)
		{
			return 4;
		}
		else if(numCell > 36 && numCell <= 49)
		{
			return 3;
		}
		else if(numCell > 49 && numCell <= 64)
		{
			return 2;
		}
		else
		{
			return 1;
		}
	}
    
    public String playerName() {
        return "Mandus";
    }
}