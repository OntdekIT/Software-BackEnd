package Ontdekstation013.ClimateChecker.features.admin;

import com.sun.jna.platform.win32.WinDef;

public class wsCodeRequest {
    private Long length;
    private Long duration;

    // Getter for length
    public Long getLength() {
        return length;
    }

    // Setter for length
    public void setLength(Long length) {
        this.length = length;
    }

    // Getter for duration
    public Long getDuration() {
        return duration;
    }

    // Setter for duration
    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
