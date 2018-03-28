package aim4.sim.results;

public class VehicleResult {
    private int vin;
    private String specType;
    private double startTime;
    private double finishTime;
    private double finalVelocity;
    private double maxVelocity;
    private double minVelocity;

    public VehicleResult(int vin, String specType, double startTime, double finishTime, double finalVelocity, double maxVelocity, double minVelocity) {
        this.vin = vin;
        this.specType = specType;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.finalVelocity = finalVelocity;
        this.maxVelocity = maxVelocity;
        this.minVelocity = minVelocity;
    }

    public int getVin() {
        return vin;
    }

    public String getSpecType() {
        return specType;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getFinishTime() { return finishTime; }

    public double getFinalVelocity() {
        return finalVelocity;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }

    public double getMinVelocity() {
        return minVelocity;
    }
}
