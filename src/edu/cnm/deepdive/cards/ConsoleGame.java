package edu.cnm.deepdive.cards;

import edu.cnm.deepdive.cards.Deck.InsufficientCardsException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * This class implements a console-mode, one-player (plus computer dealer)
 * Blackjack game. Only basic game play and betting actions are supported; in
 * particular, doubling down and splitting are not supported at all, and only a
 * limited form of insurance is supported.
 *
 * @author Nicholas Bennett &amp; Deep Dive Coding Java Cohort 4
 */
public class ConsoleGame {

  private static final int INITIAL_POT = 100;
  private static final int MAX_BET = 10;
  private static final Pattern NON_WHITE_SPACE = Pattern.compile("\\S+");
  private static final String RESOURCE_BUNDLE = "resources/console_game";
  private static final String BEGINNING_POT_PATTERN_KEY = "beginning_pot_pattern";
  private static final String TOP_CARD_PATTERN_KEY = "top_card_pattern";
  private static final String ENDING_POT_PATTERN_KEY = "ending_pot_pattern";
  private static final String BET_PLACE_PATTERN_KEY = "bet_place_pattern";
  private static final String BUY_INSURANCE_PATTERN_KEY= "buy_insurance_pattern";
  private static final String YES_INPUT_PATTERN_KEY = "yes_input_pattern";
  private static final String NO_INPUT_PATTERN_KEY = "no_input_pattern";
  private static final String YOUR_PLAY_PATTERN_KEY = "your_play_pattern";
  private static final String DEALERS_PLAY_PATTERN_KEY = "dealers_play_pattern";
  private static final String WON_PATTERN_KEY = "won_pattern";
  private static final String DEALER_HAND_PATTERN_KEY = "dealer_hand_pattern";
  private static final String LOST_PATTERN_KEY = "lost_pattern";
  private static final String PUSH_PATTERN_KEY = "push_pattern";

  private static ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);

  /**
   *
   * @param args
   */
  public static void main(String... args) {
    try (Scanner scanner = new Scanner(System.in)) {
      Random rng = new SecureRandom();
      Deck deck = new Deck();
      int pot = INITIAL_POT;
      for (boolean play = true; play; play &= pot > 0) {
        System.out.printf(bundle.getString(BEGINNING_POT_PATTERN_KEY), pot);
        int bet = getBet(scanner, pot);
        if (bet > 0) {
          deck.gather();
          deck.shuffle(rng);
          BlackjackHand dealer = new BlackjackDealerHand(deck);
          BlackjackHand player = new InteractiveBlackjackHand(deck, scanner);
          Card topCard = dealer.getHand()[1];
          System.out.printf(bundle.getString(TOP_CARD_PATTERN_KEY), topCard);
          if (!player.isBlackjack()
              || (topCard.getRank() != Rank.ACE)
              || !buyInsurance(scanner, player)) {
            pot += playHands(scanner, player, dealer, bet);
          }
        } else {
          play = false;
        }
      }
      System.out.printf(bundle.getString(ENDING_POT_PATTERN_KEY), pot);
    } catch (InsufficientCardsException e) {
      /*
      In this program, this exception should never occur. If it does, wrap it in
      and throw a RuntimeException, terminating the program.
      */
      throw new RuntimeException(e);
    }
  }

  private static int getBet(Scanner scanner, int pot) {
    int bet = -1;
    int maxBet = Math.min(10, MAX_BET);
    do {
      System.out.printf(bundle.getString(BET_PLACE_PATTERN_KEY), maxBet);
      while (!scanner.hasNext()) {}
      if (scanner.hasNextInt()) {
        int input = scanner.nextInt();
        if (input >= 0 && input <= maxBet) {
          bet = input;
        }
      }
      scanner.nextLine();
    } while (bet < 0);
    return bet;
  }

  private static boolean buyInsurance(Scanner scanner, BlackjackHand player) {
    Boolean insure = null;
    System.out.println(player);
    while (insure == null) {
      System.out.print(bundle.getString(BUY_INSURANCE_PATTERN_KEY));
      while (!scanner.hasNext(NON_WHITE_SPACE)) {}
      char input = scanner.next(NON_WHITE_SPACE).toLowerCase().charAt(0);
      if (input == bundle.getString(YES_INPUT_PATTERN_KEY).charAt(0)) {
        insure = true;
      } else if (input == bundle.getString(NO_INPUT_PATTERN_KEY).charAt(0)) {
        insure = false;
      }
      scanner.nextLine();
    }
    return insure;
  }

  private static int playHands(Scanner scanner, BlackjackHand player, BlackjackHand dealer, int bet)
      throws InsufficientCardsException {
    int gain = 0;
    System.out.printf(bundle.getString(YOUR_PLAY_PATTERN_KEY));
    player.play();
    System.out.printf(bundle.getString(DEALERS_PLAY_PATTERN_KEY));
    if (!player.isBusted()) {
      dealer.play();
    }
    System.out.printf(bundle.getString(DEALER_HAND_PATTERN_KEY), dealer);
    int comparison = player.compareTo(dealer);
    if (comparison > 0) {
      gain = player.isBlackjack() ? bet * 3 / 2 : bet;
      System.out.printf(bundle.getString(WON_PATTERN_KEY), gain);
    } else if (comparison < 0 || dealer.isBlackjack()) {
      gain = -bet;
      System.out.printf(bundle.getString(LOST_PATTERN_KEY), bet);
    } else {
      System.out.printf(bundle.getString(PUSH_PATTERN_KEY));
    }
    return gain;
  }

}
