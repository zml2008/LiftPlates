package ninja.leaping.liftplates;

/**
 * The result from a {@link Lift}'s motion.
 */
public class MoveResult {
    public static enum Type {
        CONTINUE(false),
        DELAY(true),
        STOP(true),
        BLOCK(false);

        private final boolean override;

        private Type(boolean override) {
            this.override = override;
        }

        public boolean isOverrideable() {
            return override;
        }
    }

    private final Type type;
    private final int amount;

    public MoveResult(Type type) {
        this(type, 0);
    }

    public MoveResult(Type type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public Type getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}
