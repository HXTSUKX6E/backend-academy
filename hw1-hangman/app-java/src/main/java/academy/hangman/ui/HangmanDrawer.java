package academy.hangman.ui;

public class HangmanDrawer {
    private static final int MAX_ATTEMPTS = 15;
    private static final int MIN_ATTEMPTS = 2;

    public void drawHangman(int wrongAttempts, int maxAttempts) {
        int allowedMaxAttempts = Math.min(Math.max(maxAttempts, MIN_ATTEMPTS), MAX_ATTEMPTS);
        drawUniversalGallows(wrongAttempts, allowedMaxAttempts, maxAttempts);
    }

    public void drawHangman(int wrongAttempts) {
        drawHangman(wrongAttempts, 6);
    }

    private void drawUniversalGallows(int wrongAttempts, int drawingAttempts, int displayAttempts) {
        switch (drawingAttempts) {
            case 4:
                draw4AttemptsGallows(wrongAttempts);
                break;
            case 6:
                draw6AttemptsGallows(wrongAttempts);
                break;
            case 8:
                draw8AttemptsGallows(wrongAttempts);
                break;
            default:
                // %
                drawPercentageGallows(wrongAttempts, drawingAttempts);
        }

        System.out.printf("Ошибок: %d/%d\n", wrongAttempts, displayAttempts);
    }

    private void draw4AttemptsGallows(int wrongAttempts) {
        String[] gallows = {
            "  ┌─────┐",
            "  │     │",
            "  │     " + (wrongAttempts >= 1 ? "O" : " "),
            "  │    " + getBodyAndArms4Attempts(wrongAttempts),
            "  │    " + getLegs4Attempts(wrongAttempts),
            "  │",
            "══╩══════"
        };

        for (String line : gallows) {
            System.out.println(line);
        }
    }

    private String getBodyAndArms4Attempts(int wrongAttempts) {
        return switch (wrongAttempts) {
            case 1 -> "  ";    // только голова
            case 2 -> "/|";    // тело + левая рука
            case 3 -> "/|\\";  // тело + обе руки
            case 4 -> "/|\\";  // тело + обе руки (остаются)
            default -> "  ";   // ничего
        };
    }

    private String getLegs4Attempts(int wrongAttempts) {
        return switch (wrongAttempts) {
            case 3 -> "/";     // левая нога
            case 4 -> "/ \\";  // обе ноги
            default -> "  ";   // нет ног
        };
    }

    private void draw6AttemptsGallows(int wrongAttempts) {
        String[] gallows = {
            "  ┌─────┐",
            "  │     │",
            "  │     " + (wrongAttempts >= 1 ? "O" : " "),
            "  │    " + getBodyAndArms6Attempts(wrongAttempts),
            "  │    " + getLegs6Attempts(wrongAttempts),
            "  │",
            "══╩══════"
        };

        for (String line : gallows) {
            System.out.println(line);
        }
    }

    private String getBodyAndArms6Attempts(int wrongAttempts) {
        return switch (wrongAttempts) {
            case 2 -> " |";    // только тело
            case 3 -> "/|";    // тело + левая рука
            case 4 -> "/|\\";  // тело + обе руки
            case 5 -> "/|\\";  // тело + обе руки (остаются)
            case 6 -> "/|\\";  // тело + обе руки (остаются)
            default -> "  ";
        };
    }

    private String getLegs6Attempts(int wrongAttempts) {
        return switch (wrongAttempts) {
            case 5 -> "/";     // левая нога
            case 6 -> "/ \\";  // обе ноги
            default -> "  ";
        };
    }

    private void draw8AttemptsGallows(int wrongAttempts) {
        String[] gallows = {
            "  ┌─────┐",
            "  │     │",
            "  │     " + (wrongAttempts >= 1 ? "O" : " "),
            "  │    " + getUpperBody8Attempts(wrongAttempts),
            "  │    " + getLowerBody8Attempts(wrongAttempts),
            "  │    " + getLegs8Attempts(wrongAttempts),
            "  │",
            "══╩══════"
        };

        for (String line : gallows) {
            System.out.println(line);
        }
    }

    private String getUpperBody8Attempts(int wrongAttempts) {
        return switch (wrongAttempts) {
            case 2 -> " |";    // тело
            case 3 -> "/|";    // тело + левая рука
            case 4 -> "/|\\";  // тело + обе руки
            case 5 -> "/|\\";  // тело + обе руки (остаются)
            case 6 -> "/|\\";  // тело + обе руки (остаются)
            case 7 -> "/|\\";  // тело + обе руки (остаются)
            case 8 -> "/|\\";  // тело + обе руки (остаются)
            default -> " ";
        };
    }

    private String getLowerBody8Attempts(int wrongAttempts) {
        return switch (wrongAttempts) {
            case 2, 3, 4, 5, 6, 7, 8 -> " |";    // тело продолжение
            default -> "  ";
        };
    }

    private String getLegs8Attempts(int wrongAttempts) {
        return switch (wrongAttempts) {
            case 5 -> "/";     // левая нога
            case 6 -> "/ ";    // левая нога (продолжение)
            case 7, 8 -> "/ \\";  // обе ноги
            default -> "  ";   // нет ног
        };
    }

    // Универсальная виселица процентная система
    private void drawPercentageGallows(int wrongAttempts, int maxAttempts) {
        double progress = maxAttempts > 0 ? (double) wrongAttempts / maxAttempts : 0;

        boolean showHead = progress >= 0.15;
        boolean showBody = progress >= 0.30;
        boolean showLeftArm = progress >= 0.45;
        boolean showRightArm = progress >= 0.60;
        boolean showLeftLeg = progress >= 0.75;
        boolean showRightLeg = progress >= 0.90;
        boolean showBothLegs = progress >= 1.0;

        if (maxAttempts <= 3) {
            showHead = wrongAttempts >= 1;
            showBody = wrongAttempts >= 1;
            showLeftArm = wrongAttempts >= 2;
            showRightArm = wrongAttempts >= 2;
            showLeftLeg = wrongAttempts >= 3;
            showRightLeg = wrongAttempts >= 3;
            showBothLegs = wrongAttempts >= maxAttempts;
        }

        String[] gallows = {
            "  ┌─────┐",
            "  │     │",
            "  │     " + (showHead ? "O" : " "),
            "  │    " + getArmsPercentage(showLeftArm, showRightArm, showBody),
            "  │     " + (showBody ? "│" : " "),
            "  │    " + getLegsPercentage(showLeftLeg, showRightLeg, showBothLegs),
            "  │",
            "══╩══════"
        };

        for (String line : gallows) {
            System.out.println(line);
        }
    }

    private String getArmsPercentage(boolean showLeftArm, boolean showRightArm, boolean showBody) {
        if (showLeftArm && showRightArm) {
            return showBody ? "/|\\" : "/ \\";
        } else if (showLeftArm) {
            return showBody ? "/| " : "/  ";
        } else if (showRightArm) {
            return showBody ? " |\\" : "  \\";
        } else {
            return showBody ? " | " : "   ";
        }
    }

    private String getLegsPercentage(boolean showLeftLeg, boolean showRightLeg, boolean showBothLegs) {
        if (showBothLegs) {
            return "/ \\";
        } else if (showLeftLeg && showRightLeg) {
            return "/ \\";
        } else if (showLeftLeg) {
            return "/  ";
        } else if (showRightLeg) {
            return "  \\";
        } else {
            return "   ";
        }
    }
}
