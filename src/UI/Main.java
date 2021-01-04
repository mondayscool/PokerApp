package UI;

import Logic.Card;
import Logic.Deck;
import Logic.Game;
import Logic.Player;

import java.util.Arrays;

public class Main {

    private static int street;
    private static MainWindow gameWindow;
    private static Deck deck;
    private static Card[] board;
    private static Player[] players;
    private static int[] bets;
    private static boolean[] playerHasActed;
    private static boolean[] playersInHand;
    private static boolean[] playersSittingOut;
    private static int pot;
    private static int lastRaiseSize;
    private static int startingStackSize;
    private static int sb;
    private static int bb;
    private static int minBet;

    public static void main(String argsp[]) {
        if(argsp != null && argsp.length > 0){
            if(argsp[0].equals("-ui")){
                beginUI();
            }else if(argsp[0].equals("-client")){
                beginClient();
            }else if(argsp[0].equals("-server")){
                beginServer();
            }else{
                beginUI();
            }
        }
    }

    public static void beginServer(){
        System.out.println("Server, baby!");
    }

    public static void beginClient(){
        System.out.println("Client, baby!");
    }

    public static void beginUI(){
        deck = new Deck();
        board = new Card[5];
        street = 0;
        sb = 500;
        bb = 1000;
        pot = 0;
        startingStackSize = 100000;
        lastRaiseSize = bb;
        minBet = bb;

        //players = Game.createPlayers(5, deck, startingStackSize);
        players = new Player[6];
        players[0] = new Player(0, deck, startingStackSize, "Reid");
        players[1] = new Player(1, deck, startingStackSize, "Tyler");
        players[2] = new Player(2, deck, startingStackSize);
        players[3] = new Player(3, deck, startingStackSize);
        players[4] = new Player(4, deck, startingStackSize, "Dan");
        players[5] = new Player(5, deck, startingStackSize, "Cody");

        bets = new int[players.length];
        playerHasActed = new boolean[players.length];
        playersInHand = new boolean[players.length];
        playersSittingOut = new boolean[players.length];

        startGame();
    }

    public static void addToPot(int betSize) {
        pot += betSize;
    }

    public static int getPot() {
        return pot;
    }

    public static int getStartingStackSize() {
        return startingStackSize;
    }

    private static void nextStreet() {
        //i'd like to do something better with this method maybe
        Arrays.fill(playerHasActed, false);
        Game.resetBets(players, bets);
        street++;
        if (street == 1) {
            Game.flop(players, board, deck, playersInHand, gameWindow);
        } else if (street == 2) {
            Game.turn(players, board, deck, playersInHand, gameWindow);
        } else if (street == 3) {
            Game.river(players, board, deck, playersInHand, gameWindow);
        } else if (street >= 4) {
            Game.getWinners(players, board, playersInHand, pot);
            endHand();
        }
    }

    public static void callButtonAction() {
        players[Game.getCurrentActionIndex()].call(bets, playerHasActed);
        if (Game.checkFolds(players, playersInHand)) {
            endHand();
        } else if (Game.checkBettingRoundCompleted(players, bets, playersInHand, playerHasActed)) {
            nextStreet();
        } else {
            Game.updateCurrentAction(players, playersInHand);
        }

        Game.printPlayers(players, bets, playersInHand);

        gameWindow.updateButtons(players, bets);
    }

    public static void foldButtonAction() {
        int actionIndex = Game.getCurrentActionIndex();
        players[actionIndex].fold(bets, playersInHand);
        gameWindow.getTable().foldPlayer(actionIndex);

        gameWindow.updatePlayer(players);
        if (Game.checkFolds(players, playersInHand)) {
            endHand();
        } else if (Game.checkBettingRoundCompleted(players, bets, playersInHand, playerHasActed)) {
            nextStreet();
        } else {
            Game.updateCurrentAction(players, playersInHand);
        }

        Game.printPlayers(players, bets, playersInHand);

        gameWindow.updateButtons(players, bets);
    }

    public static void raiseButtonAction() {
        int holder = Game.getHighestBet(bets);
        int betValue = Game.getBetValue(bets, gameTable, lastRaiseSize, players, minBet, street);
        lastRaiseSize = betValue - holder;

        players[Game.getCurrentActionIndex()].raise(betValue, bets, playerHasActed);
        if (Game.checkFolds(players, playersInHand)) {
            endHand();
        } else if (Game.checkBettingRoundCompleted(players, bets, playersInHand, playerHasActed)) {
            nextStreet();
        } else {
            Game.updateCurrentAction(players, playersInHand);
        }

        Game.printPlayers(players, bets, playersInHand);

        gameWindow.updateButtons(players, bets);
    }

    public static void checkButtonAction() {
        players[Game.getCurrentActionIndex()].check(playerHasActed);
        if (Game.checkFolds(players, playersInHand)) {
            endHand();
        } else if (Game.checkBettingRoundCompleted(players, bets, playersInHand, playerHasActed)) {
            nextStreet();
        } else {
            Game.updateCurrentAction(players, playersInHand);
        }

        Game.printPlayers(players, bets, playersInHand);

        gameWindow.updateButtons(players, bets);
    }

    public static void resetButtonAction() {
        resetHand();
        System.out.println("Reset Pressed");
    }

    public static void testButtonAction(){

    }

    public static void startGame() {
        Arrays.fill(board, null);
        Arrays.fill(playersInHand, true);
        Game.resetBets(players, bets);

        Game.pickRandomDealer(players);

        Game.dealHands(players);

        Game.setStartingActionIndex(players, playersInHand, street);
        players[Game.getSmallBlindIndex()].postBlind(sb, bets);
        players[Game.getBigBlindIndex()].postBlind(bb, bets);

        Game.printPlayers(players, bets, playersInHand);

        gameWindow = new MainWindow(players);
        gameWindow.updateButtons(players, bets);

        gameWindow.getTable().createPlayerCards(true);
    }

    public static void endHand() {
        if (Game.checkFolds(players, playersInHand)) {
            for (int i = 0; i < playersInHand.length; i++) {
                if (playersInHand[i]) {
                    players[i].win(pot);
                    break;
                }
            }
        }

        resetHand();
    }

    public static void resetHand() {
        Game.printHands(players, playersInHand);
        Game.printBoard(board);

        pot = 0;
        street = 0;
        deck.shuffle();

        Arrays.fill(playerHasActed, false);
        Arrays.fill(playersInHand, true);
        Arrays.fill(board, null);

        Game.resetFolds(players);
        Game.resetBets(players, bets);
        Game.nextDealer(players);
        Game.setStartingActionIndex(players, playersInHand, street);
        Game.dealHands(players);

        players[Game.getSmallBlindIndex()].postBlind(sb, bets);
        players[Game.getBigBlindIndex()].postBlind(bb, bets);
        lastRaiseSize = bb;
        gameWindow.updateButtons(players, bets);

        gameWindow.getTable().deletePlayerCards();

        gameWindow.getTable().createPlayerCards(true);
    }
}