package com.codecool.klondike;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.*;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private void shuffleDeck() {
        Collections.shuffle(deck);
    }


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK && card.equals(stockPile.getTopCard())) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        int cardIndex = activePile.getCardIndex(card);
        Pile.PileType pileType = activePile.getPileType();
        
        if (pileType == Pile.PileType.STOCK) {
            return;
        }
        if (pileType == Pile.PileType.DISCARD && !card.equals(card.getContainingPile().getTopCard())) {
            return;
        }
        if (pileType == Pile.PileType.TABLEAU && card.isFaceDown()) {
            return;
        }
        if (activePile.getPileType() == Pile.PileType.FOUNDATION && !card.equals(card.getContainingPile().getTopCard())){
            return;
        }
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        draggedCards.add(card);
        int topCardIndex = activePile.getTopCardIndex();
        if (pileType == Pile.PileType.TABLEAU && cardIndex != topCardIndex) {
            for (int i = cardIndex + 1; i < activePile.numOfCards(); i++) {
                draggedCards.add(activePile.getCard(i));
            }
        }

        for (Card draggedCard : draggedCards) {
            draggedCard.getDropShadow().setRadius(20);
            draggedCard.getDropShadow().setOffsetX(10);
            draggedCard.getDropShadow().setOffsetY(10);

            draggedCard.toFront();
            draggedCard.setTranslateX(offsetX);
            draggedCard.setTranslateY(offsetY);
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        List<Pile> validDropPiles = FXCollections.observableArrayList();
        validDropPiles.addAll(tableauPiles);
        validDropPiles.addAll(foundationPiles);
        Pile pile = getValidIntersectingPile(card, validDropPiles);
        //TODO
        if (pile != null) {
            handleValidMove(card, pile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
    };

    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        shuffleDeck();
        initPiles();
        dealCards();
        flipTopTableauCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        for (int i = discardPile.numOfCards() - 1; i >= 0 ; i--) {
            discardPile.getCards().get(i).flip();
            discardPile.getCards().get(i).moveToPile(stockPile);
        }
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getPileType().equals(Pile.PileType.FOUNDATION) && draggedCards.size() == 1) {
            Card topCard = destPile.getTopCard();

            if (topCard == null && card.getRank() == 1){
                return true;
            }
            else if (topCard == null){
                return false;
            }
            else if (topCard.getSuit() == card.getSuit() && topCard.getRank() + 1 == card.getRank()){
                return true;
            }
        }
        else if (destPile.getPileType().equals(Pile.PileType.TABLEAU)) {
            Card topCard = destPile.getTopCard();

            // if there's no top card and only KING
            if (topCard == null && card.getRank() == 13){
                return true;
            }
            else if (topCard == null) {
                return false;
            }
            // if diff color AND rank is +1
            else if (Card.isOppositeColor(card, topCard) && topCard.getRank() == (card.getRank() + 1) ){
                return true;
            }
        }
        return false;
    }
    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        autoFlipTableauTops(card);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }

    private void autoFlipTableauTops (Card card) {
        Pile containingPile = card.getContainingPile();
        Pile.PileType containingType = containingPile.getPileType();
        if (containingType == Pile.PileType.TABLEAU) {
            // If we are moving only one card, lets check if there is any above it already flipped and if yes, lets not flip anything
            if (draggedCards.size() == 1) {
                ObservableList<Card> cards = containingPile.getCards();
                for (int i=cards.size(); i>0; i--) {
                    try {
                        if (!cards.get(cards.size()-2).isFaceDown()) {
                            break;
                        } else if (cards.get(i-1).isFaceDown()) {
                            cards.get(i-1).flip();
                            break;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        ;
                    }
                }
                // If we move all the cards flipped, lets make sure that the first one above them gets flipped
            } else {
                int cardsToDig = draggedCards.size() + 1;
                Card theNewTop = containingPile.getTopXCard(cardsToDig);
                try {
                    theNewTop.flip();
                } catch (NullPointerException e) {
                    ;
                }
            }
        }
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        int cardsToAdd = 1;
        for (Pile pile : tableauPiles) {
            for (int i = 0; i < cardsToAdd; i++) {
                Card card = deckIterator.next();
                pile.addCard(card);
                card.setContainingPile(pile);
                addMouseEventHandlers(card);
                getChildren().add(card);
            }
            cardsToAdd++;
        }
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            card.setContainingPile(stockPile);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public void flipTopCard(Pile pile) {
        if (!pile.isEmpty()) {
            Card topCard = pile.getTopCard();
            topCard.flip();
        }
    }


    public void flipTopTableauCards() {
        for (Pile pile : tableauPiles) {
            if (!pile.isEmpty()) {
                flipTopCard(pile);
            }
        }
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
