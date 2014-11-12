package se.purplescout.pong.server.autofight;

import java.util.List;

public interface FightRoundDoneListener {

    void fightRoundDone(List<AutoFight> fights);
}
