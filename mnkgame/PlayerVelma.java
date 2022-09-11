package mnkgame;

import java.util.Random;

public class PlayerVelma implements MNKPlayer{
	private MNKBoard board;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;
    private long start;
    private int opponentRoundCounter;
    private int velmaRoundCounter;
    private double timeoutLimiter;

    @Override
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		board = new MNKBoard(M,N,K);
		myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		TIMEOUT = timeout_in_secs;
        opponentRoundCounter = 0;
        velmaRoundCounter = 0;
        timeoutLimiter = 0.98;
    }

    @Override
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        start = System.currentTimeMillis();
        int moveIndex = 0, bestCell = Integer.MIN_VALUE, cellValueAlphaBeta = Integer.MIN_VALUE;

        if(FC.length != board.M * board.N){
            opponentRoundCounter++; //incremento mosse fatte dall'avversario
        }
        
        if(MC.length > 0) {
            /*System.out.println("Recovering last move");*/
			MNKCell c = MC[MC.length - 1];     // Recover the last move from MC
			board.markCell(c.i,c.j);         // Save the last move in the local MNKBoard
		}
        
        /*System.out.println("Tavola reale: ");
        for(MNKCell c : MC){
            System.out.println(c);
        }
        MNKCell[] markedCells = board.getMarkedCells();
        System.out.println("Tavola copia: ");
        for(MNKCell c : markedCells){
            System.out.println(c);
        }*/

        /*if(MC.length == 0){ //non ci sono celle marcate
            System.out.println("Making the first move");
            board.markCell(0,0);
            velmaRoundCounter++; //Incremento mosse fatte da Velma
            return new MNKCell(0,0);
        }*/
        
        if(FC.length == 1){ //per vedere se c'è una mossa disponibile
            /*System.out.println("Selecting the last free cell");*/
            board.markCell(FC[0].i, FC[0].j);
            velmaRoundCounter++;
            return FC[0];
        }

        if(velmaRoundCounter >= board.K - 1){ //controllo se c'è una possibilità di vittoria in una mossa
            /*System.out.println("Checking possibility to win in one move");*/
            for (MNKCell possibleWin : FC) {
                if (board.markCell(possibleWin.i, possibleWin.j) == myWin) {
                    velmaRoundCounter++;
                    /*System.out.println("Check succesful");*/
                    return possibleWin;
                } else {
                    board.unmarkCell();
                }
            }
        }

        if(opponentRoundCounter >= board.K - 1){  //controllo per bloccare se possibile l'avversario
            /*System.out.println("Trying to stop the opponent next winning move");*/
            board.markCell(FC[0].i, FC[0].j);
            /*int i = 0;*/
            for(MNKCell examinedCell : FC){
                /*i++;
                System.out.println("Iterazione numero " + i);
                System.out.println("cella: " + examinedCell.i + "," + examinedCell.j + " / " + FC[0].i + "," + FC[0].j);
                System.out.println(examinedCell.i + " " + FC[0].i + " / " + examinedCell.j + " " + FC[0].j);*/
                if(examinedCell.i != FC[0].i || examinedCell.j != FC[0].j){
                    if(board.markCell(examinedCell.i, examinedCell.j) == yourWin){
                        board.unmarkCell();
                        board.unmarkCell();
                        board.markCell(examinedCell.i, examinedCell.j);
                        velmaRoundCounter++;
                        /*System.out.println("Stopped!");*/
                        return examinedCell; 
                    } else board.unmarkCell();
                }
            }
            board.unmarkCell();
            board.markCell(FC[1].i, FC[1].j);
            if(board.markCell(FC[0].i, FC[0].j) == yourWin){
                board.unmarkCell();
                board.unmarkCell();
                MNKCell returnCell = FC[0];
                board.markCell(returnCell.i, returnCell.j);
                velmaRoundCounter++;
                /*System.out.println("Stopped!");*/
                return returnCell;
            } else {
                board.unmarkCell();
                board.unmarkCell();
            }
        }

        for (int i = 0; i < board.getFreeCells().length && !((System.currentTimeMillis() - start) / 1000.0 > TIMEOUT * timeoutLimiter); i++) {
            
            board.markCell(FC[i].i, FC[i].j);
            cellValueAlphaBeta = AlphaBeta(board, false, getDepth(FC.length), Integer.MIN_VALUE, Integer.MAX_VALUE);
        /*if(i == board.getFreeCells().length - 1){System.out.println("Exiting alpha beta pruning");}*/
            board.unmarkCell();
            
            if (cellValueAlphaBeta > bestCell) {
                bestCell = cellValueAlphaBeta;
                moveIndex = i;
            }
        
        }
        
        board.markCell(FC[moveIndex].i, FC[moveIndex].j);
        velmaRoundCounter++;
        /*System.out.println("Marking the decided cell with alpha beta pruning");*/
        return FC[moveIndex];
    }

    @Override
    public String playerName() {
        return "Velma";
    }
    
    //algoritmo AlphaBeta Pruning
    private int AlphaBeta(MNKBoard board, boolean myTurn, int depth, int alpha, int beta){
        if ((System.currentTimeMillis() - start) / 1000.0 > TIMEOUT * timeoutLimiter) { //se ho sforato il limite del tempo di timeout
            return 0;
        }

        /*System.out.println("Siamo entrati nell'alphabeta " + counter + " volte");*/
        
        int eval = 0;

        if(depth == 0 || !board.gameState().equals(MNKGameState.OPEN)){
            return evaluate(board, myTurn);
        }
        else if(myTurn){
             eval = Integer.MAX_VALUE;
             MNKCell[] c = board.getFreeCells();
             for(MNKCell freeCell : c){
                board.markCell(freeCell.i, freeCell.j);
                eval = Math.min(eval, AlphaBeta(board, !myTurn, depth-1, alpha, beta));
                beta = Math.min(eval, beta);
                board.unmarkCell();
                if(beta <= alpha)  //taglio alpha
                  return eval;
            }
        return eval;
        }
        else{
            eval = Integer.MIN_VALUE;
             MNKCell[] c = board.getFreeCells();
             for(MNKCell freeCell : c){
                board.markCell(freeCell.i, freeCell.j);
                eval = Math.max(eval, AlphaBeta(board, !myTurn, depth-1, alpha, beta));
                alpha = Math.max(eval, alpha);
                board.unmarkCell();
                if(beta <= alpha)  //taglio beta
                  return eval;
            }
        return eval;    
        }
    }

    public int getDepth(int len){  //funzione che restituisce profondità da esaminare con Alpha Beta Pruning
        if((len >= 0) && (len <= 20)){
            return 9;
        } else if((len >= 21) && (len <= 25)){
            return 8;
        } else if((len >= 26) && (len <= 30)){
            return 7;
        } else if((len >= 31) && (len <= 50)){
            return 6;
        } else if((len >= 51) && (len <= 100)){
            return 5;
        } else if((len >= 101) && (len <= 300)){
            return 4;
        } else if((len >= 301) && (len <= 2000)){
            return 3;
        } else return 2;
    }

    public int evaluate( MNKBoard board, boolean myTurn){
      int playerSelector,
      currentPlayerEval = 0,
      opponentEval = 0,
      currentPlayerSequenceMultiplier = 0,
      opponentSequenceMultiplier = 0,
      x,
      y,
      M = board.M,
      N = board.N,
      K = board.K,
      totaleDiagonali = (M + N - (K * 2) + 1);

      if(myTurn){     //variabile per assegnare un valore posistivo al player corrente e uno negativo per l'avversario
          playerSelector = 1;
      }
      else{
          playerSelector = -1;
      }

      //controllo sulle righe
      for(int i=0; i<M; i++){    //i per le righe j per le colonne
        currentPlayerSequenceMultiplier = 1;
        opponentSequenceMultiplier = 1;
        for(int j=0; j<N; j++){

            if((board.cellState(i, j) == MNKCellState.P1 && myWin == MNKGameState.WINP1) || 
            (board.cellState(i, j) == MNKCellState.P2 && myWin == MNKGameState.WINP2)){
               currentPlayerEval += currentPlayerSequenceMultiplier;
               currentPlayerSequenceMultiplier += 10;
               opponentSequenceMultiplier = 1;
            }
            else if((board.cellState(i, j) == MNKCellState.P1 && myWin == MNKGameState.WINP2) || 
            (board.cellState(i, j) == MNKCellState.P2 && myWin == MNKGameState.WINP1)){
                opponentEval += opponentSequenceMultiplier;
               opponentSequenceMultiplier += 10;
               currentPlayerSequenceMultiplier = 1;
            }
        }
      }
      //controllo sulle colonne
      for(int j=0; j<N; j++){
        currentPlayerSequenceMultiplier = 1;
        opponentSequenceMultiplier = 1;
          for(int i=0; i<M; i++){
            if((board.cellState(i, j) == MNKCellState.P1 && myWin == MNKGameState.WINP1) || 
            (board.cellState(i, j) == MNKCellState.P2 && myWin == MNKGameState.WINP2)){
                currentPlayerEval += currentPlayerSequenceMultiplier;
                currentPlayerSequenceMultiplier += 10;
                opponentSequenceMultiplier = 1;
             }
             else if((board.cellState(i, j) == MNKCellState.P1 && myWin == MNKGameState.WINP2) || 
             (board.cellState(i, j) == MNKCellState.P2 && myWin == MNKGameState.WINP1)){
                 opponentEval += opponentSequenceMultiplier;
                opponentSequenceMultiplier += 10;
                currentPlayerSequenceMultiplier = 1;
             }
          }
      }

    //controllo sulle diagonali da sx a dx
    x = M - K;
    y = 0;
    
    for(int iterator = 0; iterator < totaleDiagonali; iterator++){
        if(x != 0){
            int xPrimo = x;
            int yPrimo = y;

            while(xPrimo < M){
                if((board.cellState(xPrimo, yPrimo) == MNKCellState.P1 && myWin == MNKGameState.WINP1) || 
                (board.cellState(xPrimo, yPrimo) == MNKCellState.P2 && myWin == MNKGameState.WINP2)){
               currentPlayerEval += currentPlayerSequenceMultiplier;
               currentPlayerSequenceMultiplier += 10;
               opponentSequenceMultiplier = 1;
            }
            else if((board.cellState(xPrimo, yPrimo) == MNKCellState.P1 && myWin == MNKGameState.WINP2) || 
            (board.cellState(xPrimo, yPrimo) == MNKCellState.P2 && myWin == MNKGameState.WINP1)){
                opponentEval += opponentSequenceMultiplier;
               opponentSequenceMultiplier += 10;
               currentPlayerSequenceMultiplier = 1;
            }
                xPrimo++;
                yPrimo++;
            }

            x--;
        } else {
            int xPrimo = x;
            int yPrimo = y;

            while(yPrimo < N){
                if((board.cellState(xPrimo, yPrimo) == MNKCellState.P1 && myWin == MNKGameState.WINP1) || 
                (board.cellState(xPrimo, yPrimo) == MNKCellState.P2 && myWin == MNKGameState.WINP2)){
                    currentPlayerEval += currentPlayerSequenceMultiplier;
                    currentPlayerSequenceMultiplier += 10;
                    opponentSequenceMultiplier = 1;
                 }
                 else if((board.cellState(xPrimo, yPrimo) == MNKCellState.P1 && myWin == MNKGameState.WINP2) || 
                 (board.cellState(xPrimo, yPrimo) == MNKCellState.P2 && myWin == MNKGameState.WINP1)){
                     opponentEval += opponentSequenceMultiplier;
                    opponentSequenceMultiplier += 10;
                    currentPlayerSequenceMultiplier = 1;
                 }

                xPrimo++;
                yPrimo++;
            }

            y++;
        }
    }

    //controllo sulle diagonali da dx a sx
    x = M - K;
    y = N - 1;
    
    for(int iterator = 0; iterator < totaleDiagonali; iterator++){
        if(x != 0){
            int xPrimo = x;
            int yPrimo = y;

            while(xPrimo < M){
                if((board.cellState(xPrimo, yPrimo) == MNKCellState.P1 && myWin == MNKGameState.WINP1) || 
                (board.cellState(xPrimo, yPrimo) == MNKCellState.P2 && myWin == MNKGameState.WINP2)){
                    currentPlayerEval += currentPlayerSequenceMultiplier;
                    currentPlayerSequenceMultiplier += 10;
                    opponentSequenceMultiplier = 1;
                 }
                 else if((board.cellState(xPrimo, yPrimo) == MNKCellState.P1 && myWin == MNKGameState.WINP2) || 
                 (board.cellState(xPrimo, yPrimo) == MNKCellState.P2 && myWin == MNKGameState.WINP1)){
                     opponentEval += opponentSequenceMultiplier;
                    opponentSequenceMultiplier += 10;
                    currentPlayerSequenceMultiplier = 1;
                 }

                xPrimo++;
                yPrimo--;
            }

            x--;
        } else {
            int xPrimo = x;
            int yPrimo = y;

            while(yPrimo >= 0){
                if((board.cellState(xPrimo, yPrimo) == MNKCellState.P1 && myWin == MNKGameState.WINP1) || 
                (board.cellState(xPrimo, yPrimo) == MNKCellState.P2 && myWin == MNKGameState.WINP2)){
                    currentPlayerEval += currentPlayerSequenceMultiplier;
                    currentPlayerSequenceMultiplier += 10;
                    opponentSequenceMultiplier = 1;
                 }
                 else if((board.cellState(xPrimo, yPrimo) == MNKCellState.P1 && myWin == MNKGameState.WINP2) || 
                 (board.cellState(xPrimo, yPrimo) == MNKCellState.P2 && myWin == MNKGameState.WINP1)){
                     opponentEval += opponentSequenceMultiplier;
                    opponentSequenceMultiplier += 10;
                    currentPlayerSequenceMultiplier = 1;
                 }

                xPrimo++;
                yPrimo--;
            }

            y--;
        }
    }
    
    return(playerSelector * (currentPlayerEval - opponentEval));
    }
}