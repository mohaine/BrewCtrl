package com.mohaine.brewcontroller.net.mock;

public class TankCfg implements Cloneable {
	public final double ambientLossRate;
	public final double ambient;
	public final double volumeEnergyDensity;
	public final double sizeMl;
	public final int stackCount;

	public final double mixFactorOtherHotter;
	public final double mixFactorOtherColder;

	public TankCfg(double ambient, double ambientLossRate, double volumeEnergyDensity, double sizeMl, int stackCount) {
		this(ambient, ambientLossRate, volumeEnergyDensity, sizeMl, stackCount, 0.0, 0.0);
	}

	public TankCfg(double ambient, double ambientLossRate, double volumeEnergyDensity, double sizeMl, int stackCount, double mixFactorOtherHotter, double mixFactorOtherColder) {
		this.ambient = ambient;
		this.ambientLossRate = ambientLossRate;
		this.volumeEnergyDensity = volumeEnergyDensity;
		// this.wallVolume = wallVolume;
		// this.wallEnergyDensity = wallEnergyDensity;
		// this.wallTransferRate = wallTransferRate;

		this.sizeMl = sizeMl;
		this.stackCount = stackCount;
		this.mixFactorOtherHotter = mixFactorOtherHotter;
		this.mixFactorOtherColder = mixFactorOtherColder;

		// if (wallEnergyDensity <= 0) {
		// throw new RuntimeException("wallEnergyDensity can not be <= zero");
		// }
		if (volumeEnergyDensity <= 0) {
			throw new RuntimeException("volumeEnergyDensity can not be <= zero");
		}
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append(" new TankCfg(");
		sb.append(ambient);
		sb.append(",");
		sb.append(ambientLossRate);
		sb.append(",");
		sb.append(volumeEnergyDensity);
		// sb.append(",");
		// sb.append(wallVolume);
		// sb.append(",");
		// sb.append(wallEnergyDensity);
		// sb.append(",");
		// sb.append(wallTransferRate);
		sb.append(",");
		sb.append(sizeMl);
		sb.append(",");
		sb.append(stackCount);
		sb.append(",");
		sb.append(mixFactorOtherHotter);
		sb.append(",");
		sb.append(mixFactorOtherColder);
		sb.append(")");

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(ambient);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(ambientLossRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(sizeMl);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + stackCount;
		temp = Double.doubleToLongBits(volumeEnergyDensity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		// temp = Double.doubleToLongBits(wallEnergyDensity);
		// result = prime * result + (int) (temp ^ (temp >>> 32));
		// temp = Double.doubleToLongBits(wallTransferRate);
		// result = prime * result + (int) (temp ^ (temp >>> 32));
		// temp = Double.doubleToLongBits(wallVolume);
		// result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TankCfg other = (TankCfg) obj;
		if (Double.doubleToLongBits(ambient) != Double.doubleToLongBits(other.ambient))
			return false;
		if (Double.doubleToLongBits(ambientLossRate) != Double.doubleToLongBits(other.ambientLossRate))
			return false;
		if (Double.doubleToLongBits(sizeMl) != Double.doubleToLongBits(other.sizeMl))
			return false;
		if (stackCount != other.stackCount)
			return false;
		if (Double.doubleToLongBits(volumeEnergyDensity) != Double.doubleToLongBits(other.volumeEnergyDensity))
			return false;
		// if (Double.doubleToLongBits(wallEnergyDensity) !=
		// Double.doubleToLongBits(other.wallEnergyDensity))
		// return false;
		// if (Double.doubleToLongBits(wallTransferRate) !=
		// Double.doubleToLongBits(other.wallTransferRate))
		// return false;
		// if (Double.doubleToLongBits(wallVolume) !=
		// Double.doubleToLongBits(other.wallVolume))
		// return false;
		return true;
	}

	public TankCfg getClone() throws CloneNotSupportedException {
		return (TankCfg) clone();
	}

}
