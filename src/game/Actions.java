package game;

import game.spaces.PropertySpace;

import java.util.ArrayList;
import java.util.Scanner;

public abstract class Actions {
    public static void nextTurn(Player player) {
        if (player.getJailTurnsRemaining() > 0) {
            player.reduceJailTurnsRemaining();
        }
        Game.nextPlayer();
    }

    public static void getPlayerMoney(Player player) { System.out.println("$" + player.getMoney()); }

    public static void addPlayerMoney(Player player, int amount) {
        player.receiveMoney(amount);
        System.out.println("added " + amount);
    }

    public static void removePlayerMoney(Player player, int amount) {
        if (player.spendMoney(amount)) {
            System.out.println("removed $" + amount);
            return;
        }
        System.out.println(player.getName() + " does not have that much money");
    }
    public static void buy(Player player) {
        Space space = Board.getSpace(player.getPosition());
        if (!(space instanceof PropertySpace)) {
            System.out.println("cannot buy " + space.getName());
            return;
        }
        player.purchaseProperty((PropertySpace) space);
    }
    public static void jail(Player player) {
        player.setInJail(true);
    }

    public static void jailExit(Player player, boolean pay) {
        if (pay) {
            player.spendMoney(50);
        }
        System.out.println(player + " is out of jail");
        player.setInJail(false);
    }

    public static void move(Player player, int amount) {
        if (player.isInJail() && player.getJailTurnsRemaining() == 0) {
            // TODO: check if a get out of jail free card is owned
            System.out.println("must exit jail");
            System.out.println("use get-out-of-jail-free card? (y/n) ");
            Scanner kb = new Scanner(System.in);
            String useCard = kb.next();
            kb.close();
            if (!useCard.equals("y")) {
                if (!player.spendMoney(50)) {
                    System.out.println(player + " does not have enough money to get out of jail");
                    player.notEnoughMoney(50);
                    player.spendMoney(50);
                }
            }
            player.setInJail(false);
            System.out.println(player + " is out of jail");
        }

        player.move(amount, true);
    }

    public static void house(Player player, String propertyName) {
        PropertySpace property = player.getOwnedPropertyByName(propertyName);

        if (property == null) {
            System.out.println(player + " does not own " + propertyName);
            return;
        }

        int setId = property.getSetId();
        if (setId == 8 || setId == 9) {
            System.out.println("cannot build houses on railroads and utilities");
            return;
        }

        if (property.getRentLevel() < 1) {
            System.out.println(player + " does not own all properties in the set");
            return;
        }

        ArrayList<PropertySpace> setProperties = player.getPropertiesOwnedOfSet(property.getSetId());
        for (PropertySpace setProperty : setProperties) {
            System.out.println(setProperty + " has " + (setProperty.getRentLevel() - 1) + " houses");
            boolean housesUpdated = false;
            do {
                System.out.print("change by: ");
                Scanner kb = new Scanner(System.in);
                int changeHouseAmount = kb.nextInt();

                if (changeHouseAmount == 0) {
                    break;
                }

                if (setProperty.getRentLevel() + changeHouseAmount > 6
                        || setProperty.getRentLevel() + changeHouseAmount < 1) {
                    System.out.println("cannot change houses by " + changeHouseAmount);
                    continue;
                }

                if (changeHouseAmount > Board.houses) {
                    System.out.println("The bank does not have that many houses left (" + Board.houses + ")");
                    continue;
                }

                Board.houses -= changeHouseAmount;
                if (setProperty.getRentLevel() + changeHouseAmount == 6) {
                    if (Board.hotels < 1) {
                        System.out.println("The bank does not have any hotels left");
                        continue;
                    }
                    Board.houses += 5;
                    Board.hotels--;
                }

                if (setProperty.getRentLevel() == 6 && changeHouseAmount < 0) {
                    Board.hotels++;
                    Board.houses -= 5;
                }

                System.out.println("the bank now has " + Board.houses + " houses");

                if (changeHouseAmount <= 0) {
                    player.receiveMoney(((setProperty.getSetId() / 2) + 1) * 25 * -changeHouseAmount);
                } else {
                    if (!player.spendMoney(((setProperty.getSetId() / 2) + 1) * 50 * changeHouseAmount)) {
                        System.out.println(player + " does not have enough money for " + changeHouseAmount + " houses");
                        continue;
                    }
                }

                setProperty.setRentLevel(setProperty.getRentLevel() + changeHouseAmount);
                System.out.println("rent on " + setProperty + " is now $" + setProperty.getRent());
                housesUpdated = true;

            } while (!housesUpdated);
        }
    }

    public static void moveTo(Player player, String spaceName) {
        Space space = Board.spaceSearch(spaceName);
        if (space != null && space.getId() >= 0) {
            player.moveTo(space.getId());
        }
    }
}
