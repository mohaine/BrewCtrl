/*
    Copyright 2009-2011 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package com.mohaine.brewcontroller;

import java.util.List;

import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.layout.BrewLayout;

public interface Controller {

	public enum Mode {
		ON, HOLD, OFF
	}

	public HeaterStep getSelectedStep();

	public List<HeaterStep> getSteps();

	public Mode getMode();

	public void setBoilDuty(int duty);

	public void setMode(Mode mode);

	public void setSelectedStep(HeaterStep step);

	public void setSteps(List<HeaterStep> steps);

	public void nextStep();

	public BrewLayout getLayout();

}
