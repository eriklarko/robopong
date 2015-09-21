package se.purplescout.pong.competition.jvm;

import java.util.Arrays;

/**
 * Created by eriklark on 3/4/15.
 */
class ClassThatGetsRunInOtherJvm_AndExitsNormally {

    public static void main(String[] args) throws InterruptedException {
        for (String arg : args) {
            System.out.println(arg);
        }
    }
}

class ClassThatGetsRunInOtherJvm_AndExitsAbnormally {

    public static void main(String[] args) throws InterruptedException {
        System.exit(1);
    }
}

class ClassThatGetsRunInOtherJvm_AndTimesOut {

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(2000);
    }
}

class ClassThatGetsRunInOtherJvm_AndThrowsError {

    public static void main(String[] args) throws InterruptedException {
        throw new Error();
    }
}
