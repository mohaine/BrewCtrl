package com.mohaine.brewcontroller.net.mock;

public class MockTank {

	private final SubTank[] stack;

	private TankCfg cfg;

	private double startTemp;

	private String name;

	public MockTank(double startTemp, TankCfg cfg) {
		super();
		this.cfg = cfg;

		stack = new SubTank[cfg.stackCount];
		for (int i = 0; i < stack.length; i++) {
			stack[i] = new SubTank(cfg);
		}

		setStartTemp(startTemp);
	}

	public void runStack(double flowVolume, double powerInWattSecond, double inputTemp) {
		// System.out.println("flowVolume: " + flowVolume);
		// System.out.println("powerInWattSecond: " + powerInWattSecond);
		// System.out.println("inputTemp: " + inputTemp);

		double powerPerHeatStack = powerInWattSecond / stack.length;
		double lastTemp = inputTemp;
		for (int i = 0; i < stack.length; i++) {
			SubTank tank = stack[i];
			double startOfTickTemp = tank.getTemp();
			tank.calulateNewTemp(flowVolume, lastTemp, powerPerHeatStack);
			lastTemp = startOfTickTemp;
		}

		for (int i = 0; i < stack.length - 1; i++) {
			SubTank tank1 = stack[i];
			SubTank tank2 = stack[i + 1];
			tank1.mixWith(tank2);
		}
	}

	public double getExitTemp() {
		return stack[stack.length - 1].getTemp();
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append(" new Tank(");
		sb.append(startTemp);
		sb.append(",");
		sb.append(cfg.toString());

		sb.append(")");

		return sb.toString();
	}

	public boolean configSame(MockTank obj) {
		return cfg.equals(obj.cfg);
	}

	public void setStartTemp(double startTemp) {
		this.startTemp = startTemp;
		for (int i = 0; i < stack.length; i++) {
			stack[i].setStartTemp(startTemp);
		}
	}

	public void printDetails() {

		System.out.println("********** " + name + " *****************");
		System.out.println(" Size(ML)  : " + cfg.sizeMl);
		System.out.println(" Stack Size: " + cfg.stackCount);

	}

	public void printStacks() {
		for (int i = 0; i < stack.length; i++) {
			System.out.println("     : " + stack[i].getTemp());
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
