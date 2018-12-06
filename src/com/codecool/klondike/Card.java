package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private Suits suit;
    private Ranks rank;

    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;


    public Card(Suits suit, Ranks rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public int getSuit() {
        return suit.ordinal() + 1;
    }

    public String getSuitName() {
        String suitName = suit.toString().toLowerCase();
        return suitName;
    }

    public int getRank() {
        return rank.ordinal() + 1;
    }

    public String getRankName() {
        return rank.toString().toLowerCase();
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        int suit = this.suit.ordinal() + 1;
        int rank = this.rank.ordinal() + 1;
        return "S" + suit + "R" + rank;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + suit.toString().toLowerCase() + " " + rank.toString().toLowerCase();
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        boolean isOpposite = (card1.isRed() && card2.isBlack() ||
                              card2.isRed() && card1.isBlack());

        return isOpposite;
    }

    private boolean isRed() {
        boolean isRed = this.getSuitName().equals("hearts") || this.getSuitName().equals("diamonds");
        return isRed;
    }

    private boolean isBlack() {
        boolean isBlack = this.getSuitName().equals("spades") || this.getSuitName().equals("clubs");
        return isBlack;
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (Suits suit: Suits.values()) {
            for (Ranks rank: Ranks.values()) {
                result.add(new Card(suit, rank, true));
            }
        }
        return result;
    }

    public static void loadCardImages(Image theCardBack) {
        cardBackImage = theCardBack;
        String suitName;
        for (Suits suit: Suits.values()) {
            suitName = suit.toString().toLowerCase();
            int suitNr = suit.ordinal() + 1;
            for (Ranks rank: Ranks.values()) {
                int rankName = rank.ordinal() + 1;
                String cardName = suitName + rankName;
                String cardId = "S" + suitNr + "R" + rankName;
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));
            }
        }
    }

    public enum Ranks {
        ACE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING
    }

    public enum Suits {
        HEARTS,
        DIAMONDS,
        SPADES,
        CLUBS
    }

    public static Image getCardBackImage(int theme) {
        cardBackImage = new Image("card_images/card_back" + theme + ".png");
        return cardBackImage;
    }

}
