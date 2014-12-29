package se.purplescout.pong.competition.lan.client;

enum SimulationSpeed {
    SLOW(60), NORMAL(25), FAST(5);

    private final int delay;
    SimulationSpeed(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }
}
