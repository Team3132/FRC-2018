package org.team3132.interfaces;

public interface CompressorInterface {
	/**
	 * Allows the compressor to run
	 */
	public void turnOn();
	
	/**
	 * Disallows the compressor to run
	 */
    public void turnOff();
    
    /**
     * @return true if the compressor is allowed to run
     */
    public boolean isOn();
}
