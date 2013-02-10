package com.mohaine.brewcontroller.net.mock;

public class SubTank {

	private static final double WATT_SEC_IN_CALORIES = 0.239005736;

	private double tempK;
	private final double volumeMl;
	private TankCfg cfg;

	public SubTank(TankCfg cfg) {
		double tankVolume = cfg.sizeMl / cfg.stackCount;
		this.volumeMl = tankVolume;
		this.cfg = cfg;
		setStartTemp(cfg.ambient);
	}

	protected void calulateNewTemp(double calorie) {
		double energyInTank = tempK * (volumeMl * cfg.volumeEnergyDensity);
		energyInTank += calorie;

		double ambientLosses = (getTempInK(cfg.ambient) - tempK) * cfg.ambientLossRate;
		energyInTank += ambientLosses;

		tempK = (energyInTank / (volumeMl * cfg.volumeEnergyDensity));
		validateTemp(tempK);
	}

	public double getVolumeMl() {
		return volumeMl;
	}

	public double getTemp() {
		return getTempInC(tempK);
	}

	public void mixWith(SubTank tank2) {
		double tempDiff = tempK - tank2.tempK;

		double mixFactor = tempDiff < 0 ? cfg.mixFactorOtherHotter : cfg.mixFactorOtherColder;

		double crossTransfer = tempDiff * mixFactor;
		tempK -= crossTransfer;
		tank2.tempK += crossTransfer;
	}

	public void setStartTemp(double startTemp) {
		this.tempK = getTempInK(startTemp);
	}

	public void calulateNewTemp(double flowVolume, double tempIn, double powerWattSecPerHeatStack) {

		double energyOutOfSubTank = tempK * flowVolume;
		double energyInToSubTank = getTempInK(tempIn) * flowVolume;
		double heatingEnergy = powerWattSecPerHeatStack * WATT_SEC_IN_CALORIES;

		double power = +energyInToSubTank - energyOutOfSubTank + heatingEnergy;

		if (flowVolume > this.getVolumeMl()) {
			throw new RuntimeException("Flow To High " + flowVolume + " > " + this.getVolumeMl());
		}

		this.calulateNewTemp(power);
	}

	private double getTempInK(double tempInC) {
		double tempInK = tempInC + 274.15;
		validateTemp(tempInK);
		return tempInK;
	}

	private void validateTemp(double tempInK) {
		if (tempInK < 0) {
			throw new RuntimeException("Temp below zero");
		}
	}

	private double getTempInC(double tempInK) {
		validateTemp(tempInK);
		return tempInK - 274.15;
	}
}
