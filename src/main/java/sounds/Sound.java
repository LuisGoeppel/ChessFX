package sounds;

/**
 * A class which allows the user to easily play Sounds.
 * Sounds should be imported as .wav files
 */
public interface Sound {

    /**
     * plays the given sound
     */
    void play();

    /**
     * stops the given sound from Playing
     */
    void stop();

    /**
     * loops the given sound infinitely
     */
    void playAndRepeat();

    /**
     * loops the given sound
     * @param nTimes how often the sound will be played
     */
    void playAndRepeat(int nTimes);

    /**
     * Sets the volume with which the sound is played
     * Values must be between 0 and 1
     * @param volume the volume with which the sound will be played
     */
    void setVolume(double volume);

    /**
     * returns the current volume of the sound
     * @return the current volume
     */
    double getVolume();
}
