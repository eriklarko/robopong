package se.purplescout.pong.competition.jvm;

import java.io.InputStream;

/**
 * Created by eriklark on 3/4/15.
 */
public interface NewJvmListener {

    void acceptOutputFromOtherJvm(ExecutionResult result);

    void otherJvmTimedOut();
}
