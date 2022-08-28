package mnkgame;

import java.util.Random;

import javax.swing.plaf.FileChooserUI;

import mnkgame.MNKBoard;

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
	
		
		double score, maxEval = Integer.MIN_VALUE;
		int pos   = rand.nextInt(FC.length); 
		MNKCell result = FC[pos]; // random move
        int numCell = 0;

		for(MNKCell currentCell : FC) {
			
			// If time is running out, return the randomly selected  cell
            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)) {
				break;
				
			}else{
                B.markCell(currentCell.i, currentCell.j);	
                numCell = FC.length;
                if(numCell > 85)
					score = alphabeta(B, true, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
				else if((numCell > 50) && (numCell <= 85))
					score = alphabeta(B, true, 2, Integer.MIN_VALUE, Integer.MAX_VALUE);
				else if((numCell > 30) && (numCell <= 50))
					score = alphabeta(B, true, 3, Integer.MIN_VALUE, Integer.MAX_VALUE);
				else if((numCell > 20) && (numCell <= 30))
					score = alphabeta(B, true, 4, Integer.MIN_VALUE, Integer.MAX_VALUE);
				else if((numCell > 16) && (numCell <= 20))
					score = alphabeta(B, true, 5, Integer.MIN_VALUE, Integer.MAX_VALUE);	
				else
					score = alphabeta(B, true, 6, Integer.MIN_VALUE, Integer.MAX_VALUE);					
		
				
				if(score > maxEval){
					maxEval = score;
					result = currentCell;
				}
				
				B.unmarkCell();			

            


            }
        }
        return result;

    }

    public double alphabeta(MNKBoard board_, boolean myNode, int depth, double alpha, double beta){
		
		double eval;
		int iteratore = 0;

		MNKCell fc[] = board_.getFreeCells();
		
		if(depth <= 0 || board_.gameState != MNKGameState.OPEN || (System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(99.0/100.0)){
            //l'albero ha profondità 0, o se non siamo in gioco o se stiamo per terminare la scelta, valutiamo
			eval = evaluate(board_, depth, myNode);  
						
		}else if(myNode){
			eval = Integer.MAX_VALUE;
			
			for(MNKCell cell : fc){ //per ogni cella libera
				board_.markCell(cell.i, cell.j); //marchiamo la cella
				eval = Math.min(eval, alphabeta(board_, false, depth-1, alpha, beta));
				beta = Math.min(eval, beta);
				board_.unmarkCell(); //togliamo il mark
				if(beta <= alpha) //il sottoalbero relativo viene ignorato
					break;
			}
		}else{
			eval = Integer.MIN_VALUE;
			for(MNKCell cell : fc){ //per ogni cella libera
				board_.markCell(cell.i, cell.j);
				eval = Math.max(eval, alphabeta(board_, true, depth-1, alpha, beta)); //prendiamo il massimo questa volta
				beta = Math.max(eval, alpha);
				board_.unmarkCell();
				if(beta <= alpha) //il sottoalbero relativo viene ignorato
					break;
			}			
		}
		
		return eval;		
	}

	public double evaluate(MNKBoard board, int depth, boolean myTurn){
		int player;
		int score = 0; //score attuale
		if(board.gameState == myWin)
		{
			score = 10 + depth; // arriviamo alla vittoria più velocemente
		}
		else if(board.gameState == yourWin)
		{
			score = -10;
		}
		else{
			//da fare il controllo per righe, colonne e diagonali, cercando di rimanere nell'intervallo [-10, 10]
		}
		//controllo righe
		return score;
	}
	 

    
    public String playerName() {
        return "Mandus";
    }
}