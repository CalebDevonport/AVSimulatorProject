package aim4.vehicle.rim;

public interface ResultsEnabledVehicle {
    double getFinishTime();

    double getDelay();

    double getFinalVelocity();

    double getMaxVelocity();

    double getMinVelocity();

    double getFinalXPos();

    double getFinalYPos();

    double getStartTime();

    void setFinishTime(double finishTime);

    void setDelay(double delay);

    void setFinalVelocity(double finalVelocity);

    void setMaxVelocity(double maxVelocity);

    void setMinVelocity(double minVelocity);

    void setFinalXPos(double xPos);

    void setFinalYPos(double yPos);

    void setStartTime(double startTime);
}
