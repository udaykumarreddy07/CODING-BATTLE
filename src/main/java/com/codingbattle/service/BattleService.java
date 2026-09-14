package com.codingbattle.service;

import com.codingbattle.dao.BattleDAO;
import com.codingbattle.dao.ProblemDAO;
import com.codingbattle.dao.ScoreDAO;
import com.codingbattle.model.Battle;
import com.codingbattle.model.BattleStatus;
import com.codingbattle.model.Problem;
import com.codingbattle.model.Score;

import java.util.List;

/**
 * Service Layer for Battle lifecycle and contest operations.
 */
public class BattleService {

    private final BattleDAO battleDAO;
    private final ProblemDAO problemDAO;
    private final ScoreDAO scoreDAO;

    public BattleService() {
        this.battleDAO = new BattleDAO();
        this.problemDAO = new ProblemDAO();
        this.scoreDAO = new ScoreDAO();
    }

    public List<Battle> getAllBattles() {
        return battleDAO.getAllBattles();
    }

    public Battle getBattle(int battleId) {
        return battleDAO.getBattleById(battleId);
    }

    public List<Problem> getBattleProblems(int battleId) {
        return problemDAO.getProblemsByBattleId(battleId);
    }

    public int createBattle(Battle battle, List<Integer> problemIds) {
        return battleDAO.addBattle(battle, problemIds);
    }

    public boolean updateStatus(int battleId, BattleStatus status) {
        return battleDAO.updateBattleStatus(battleId, status);
    }

    public boolean removeBattle(int battleId) {
        return battleDAO.deleteBattle(battleId);
    }

    public List<Score> getBattleLeaderboard(int battleId) {
        return scoreDAO.getScoresByBattleId(battleId);
    }
}
