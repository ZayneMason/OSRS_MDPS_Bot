package com.zayneiacplugs.zaynemdps;

import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.coords.LocalPoint;
import net.unethicalite.api.widgets.Prayers;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class MinimaxAlphaBeta {

    private static final int MAX_DEPTH = 3;

    @Inject
    private Client client;

    @Inject
    private State state;

    public MinimaxAlphaBeta(Client client, State state) {
        this.client = client;
        this.state = state;
    }

    public List<Action> getBestActions() {
        List<Action> bestActions = new ArrayList<>();
        MinimaxResult bestAttackAction = minimax(state, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        if (bestAttackAction.getAction() != null) {
            bestActions.add(bestAttackAction.getAction());
        }
        Action bestPrayerAction = getBestPrayerAction(state);
        if (bestPrayerAction != null) {
            bestActions.add(bestPrayerAction);
        }
        return bestActions;
    }

    private MinimaxResult minimax(State currentState, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || isTerminal(currentState)) {
            return new MinimaxResult(null, evaluateState(currentState));
        }

        List<Action> actions = getPossibleActions(currentState);

        if (maximizingPlayer) {
            MinimaxResult maxEval = new MinimaxResult(null, Integer.MIN_VALUE);
            for (Action action : actions) {
                State nextState = currentState.clone();
                applyAction(nextState, action);
                MinimaxResult eval = minimax(nextState, depth - 1, alpha, beta, false);
                if (eval.getScore() > maxEval.getScore()) {
                    maxEval = new MinimaxResult(action, eval.getScore());
                }
                alpha = Math.max(alpha, eval.getScore());
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            MinimaxResult minEval = new MinimaxResult(null, Integer.MAX_VALUE);
            for (Action action : actions) {
                State nextState = currentState.clone();
                applyAction(nextState, action);
                MinimaxResult eval = minimax(nextState, depth - 1, alpha, beta, true);
                if (eval.getScore() < minEval.getScore()) {
                    minEval = new MinimaxResult(action, eval.getScore());
                }
                beta = Math.min(beta, eval.getScore());
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    private boolean isTerminal(State state) {
        // Determine if the state is a terminal state (e.g., the game is over)
        return state.getPlayerHealth() <= 0 || state.getNpcs().isEmpty();
    }

    private int evaluateState(State state) {
        // Evaluate the state and return a score
        int score = state.getPlayerHealth() * 2; // Health is critical
        score += state.getPlayerPrayerPoints(); // Prayer is important for sustained combat
        score += state.getPlayerSpecialAttackEnergy(); // Special attacks can turn the tide
        score += state.getTotalHeals(); // Availability of healing items
        score -= state.getNpcs().size() * 10; // Fewer NPCs is better
        return score;
    }

    private List<Action> getPossibleActions(State state) {
        // Generate all possible actions from the current state
        List<Action> actions = new ArrayList<>();
        actions.add(new Action("Move", getSafestTile()));
        if (state.getTick() >= state.getNextAttackTick()) {
            actions.add(new Action("Attack", getTarget()));
        }
        actions.add(new Action("Heal"));
        return actions;
    }

    private void applyAction(State state, Action action) {
        // Apply the action to the state
        switch (action.getType()) {
            case "Attack":
                if (action.getTarget() != null) {
                    state.attack(action.getTarget());
                    state.setLastAttackTick(state.getTick());
                }
                break;
            case "Heal":
                state.heal();
                break;
            case "Move":
                if (action.getTargetTile() != null) {
                    state.move(action.getTargetTile());
                }
                break;
            case "Pray":
                if (action.getPrayer() != null) {
                    Prayers.toggle(action.getPrayer());
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown action type: " + action.getType());
        }
    }

    public EnhancedNPC getTarget() {
        EnhancedNPC target = null;
        int highestPriority = Integer.MIN_VALUE;
        for (EnhancedNPC npc : state.getNpcs()) {
            int priority = npc.getPriority();
            if (priority > highestPriority) {
                highestPriority = priority;
                target = npc;
            }
        }
        return target;
    }

    public LocalPoint getSafestTile() {
        List<TargetTile> safeTiles = state.getTileMap().getSafeTiles();
        if (safeTiles.isEmpty()) {
            return state.playerLocation;
        }

        // Example heuristic to choose the safest tile: closest to the current player location
        LocalPoint playerLocation = state.playerLocation;
        TargetTile safestTile = null;
        int minDistance = Integer.MAX_VALUE;

        for (TargetTile tile : safeTiles) {
            int distance = playerLocation.distanceTo(tile.localPoint);
            if (distance < minDistance) {
                minDistance = distance;
                safestTile = tile;
            }
        }

        return safestTile != null ? safestTile.localPoint : state.playerLocation;
    }

    public Action getBestPrayerAction(State state) {
        TargetTile playerTile = state.getTileMap().getTile(getSafestTile());
        if (playerTile != null) {
            for (AttackInfo attackInfo : playerTile.getAttackInfos()) {
                if (attackInfo.getTicksUntilAttack() <= 1) {
                    Prayer prayer = null;
                    switch (attackInfo.getAttackType()) {
                        case MAGE:
                            prayer = Prayer.PROTECT_FROM_MAGIC;
                            break;
                        case RANGE:
                            prayer = Prayer.PROTECT_FROM_MISSILES;
                            break;
                        case MELEE:
                            prayer = Prayer.PROTECT_FROM_MELEE;
                            break;
                        default:
                            break;
                    }
                    if (prayer != null) {
                        return new Action("Pray", prayer);
                    }
                }
            }
        }
        return null;
    }

    private static class MinimaxResult {
        private final Action action;
        private final int score;

        public MinimaxResult(Action action, int score) {
            this.action = action;
            this.score = score;
        }

        public Action getAction() {
            return action;
        }

        public int getScore() {
            return score;
        }
    }

    public static class Action {
        private final String type;
        private EnhancedNPC target;
        private LocalPoint targetTile;
        private Prayer prayer;

        public Action(String type) {
            this.type = type;
        }

        public Action(String type, EnhancedNPC target) {
            this.type = type;
            this.target = target;
        }

        public Action(String type, LocalPoint targetTile) {
            this.type = type;
            this.targetTile = targetTile;
        }

        public Action(String type, Prayer prayer) {
            this.type = type;
            this.prayer = prayer;
        }

        public String getType() {
            return type;
        }

        public EnhancedNPC getTarget() {
            return target;
        }

        public LocalPoint getTargetTile() {
            return targetTile;
        }

        public Prayer getPrayer() {
            return prayer;
        }

        public void setTarget(EnhancedNPC target) {
            this.target = target;
        }

        public void setTargetTile(LocalPoint targetTile) {
            this.targetTile = targetTile;
        }

        public void setPrayer(Prayer prayer) {
            this.prayer = prayer;
        }
    }
}
