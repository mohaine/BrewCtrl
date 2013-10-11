var BrewCtrl = {
	Models : {},
	Collections : {},
	Views : {},
	Templates : {},
	loadTemplate : function(tmpl_name) {
		var tmpl_url = tmpl_name;
		var tmpl_string;
		$.ajax({
			url : tmpl_url,
			method : 'GET',
			async : false,
			success : function(data) {
				tmpl_string = data;
			}
		});
		return tmpl_string;
	},
	convertC2F : function(tempC) {
		return (9.0 / 5.0) * tempC + 32;
	},
	convertF2C : function(tempF) {
		return (5.0 / 9.0) * (tempF - 32);
	},
	round : function(value, places) {
		var factor = Math.pow(10, places);
		value = Math.round(value * factor);
		return value / factor;
	},
	parseTime : function(time) {
		var seconds = 0;
		var split = time.split(":");
		split = split.reverse();
		for ( var i = 0; i < split.length; i++) {
			var msLevel = parseInt(split[i], 10);
			msLevel *= Math.pow(60, i);
			seconds += msLevel;
		}
		return seconds;

	},
	formatTime : function(time) {
		if (time == 0) {
			return "\u221E";
		}

		var minutes = parseInt(time / 60);
		var seconds = parseInt(time - (minutes * 60));

		var formated = minutes + ":";

		if (seconds < 10) {
			formated = formated + '0';
		}
		formated = formated + seconds;
		return formated;
	},
	formatTimeMinutes : function(time) {
		if (time == 0) {
			return "\u221E";
		}

		var minutes = parseInt(time / 60);
		var seconds = parseInt(time - (minutes * 60));

		var formated = minutes

		return formated;
	},
	confirm : function(msg, work) {
		if (confirm(msg)) {
			work();
		}
	},
	alert : function(msg) {
		alert(msg);
	},
	alphaId : function() {
		var raw = [];
		for ( var i = 0; i < 8; i++) {
			raw[raw.length] = String.fromCharCode(Math.floor(Math.random() * 26 + 65));
		}
		return raw.join('');
	}

}

BrewCtrl.Models.Main = Backbone.Model.extend({
	initialize : function() {

	},
	defaults : function() {
		return {
			version : null,
			config : new BrewCtrl.Models.Config(),
			steps : new BrewCtrl.Collections.Steps(),
			mode : null
		};
	},

	loadConfiguration : function() {
		var self = this;
		$.ajax("cmd/configuration").success(function(data) {
			var config = new BrewCtrl.Models.Config(data);
			self.set("config", config);
			self.scheduleStatusUpdate();
		});
	},
	start : function() {
		var self = this;
		this.loadConfiguration();
	},

	applyStatus : function(data) {
		var self = this;
		var config = self.get("config");

		// console.log(data);

		var steps = new BrewCtrl.Collections.Steps(data.steps);
		self.set("steps", steps);
		self.set("mode", data.mode);

		if (data.configurationVersion != config.get("version")) {
			self.loadConfiguration();
			return;
		}

		self.updateSelectedFlag();
		var selectedStep = self.getSelectedStep();
		if (selectedStep) {
			self.updateLayoutForStep(selectedStep);
		} else {
			var activeStep = self.getActiveStep();
			self.selectStep(activeStep);
		}

		var brewLayout = config.get("brewLayout");
		brewLayout.get("tanks").each(function(tank) {
			var foundSensor = false;
			_.each(data.sensors, function(sensor) {
				var sensorAddress = tank.get("sensorAddress");
				if (sensor.address == sensorAddress) {
					tank.set("temperatureC", sensor.temperatureC);
					tank.set("reading", sensor.reading);
					foundSensor = true;
				}
			});
			tank.set("hasSensor", foundSensor);
		});

	},
	scheduleStatusUpdate : function() {
		var self = this;
		if (self.statusTimeout) {
			clearTimeout(self.statusTimeout);
			self.statusTimeout = 0;
		}
		self.statusTimeout = setTimeout(function() {
			self.retrieveStatus();
		}, 500);
	},
	retrieveStatus : function(dataToSend) {
		var self = this;

		if (self.statusTimeout) {
			clearTimeout(self.statusTimeout);
			self.statusTimeout = 0;
		}

		$.ajax({
			type : "POST",
			url : "cmd/status",
			data : dataToSend,
			success : function(responseData) {
				self.applyStatus(responseData);
			},
			fail : function(e) {
				console.log("scheduleStatusUpdate Error");
				console.log(e);
			},
			complete : function() {
				self.scheduleStatusUpdate();
			}
		});
	},
	getActiveStep : function() {
		var steps = this.get("steps");
		return steps.first();
	},
	selectStep : function(stepToSelect) {
		var self = this;

		self.selectedStepId = stepToSelect ? stepToSelect.get("id") : "";
		self.updateSelectedFlag();
		self.updateLayoutForStep(stepToSelect);
	},
	updateSelectedFlag : function(stepToSelect) {
		var self = this;
		var steps = this.get("steps");
		var stepToSelect = null;
		steps.each(function(step) {
			step.set("selected", false);
			if (self.selectedStepId == step.get("id")) {
				stepToSelect = step;
			}
		});
		if (stepToSelect) {
			stepToSelect.set("selected", true);
		}

	},
	findControlByName : function(name) {
		var control = null;
		var self = this;
		var config = self.get("config");
		var brewLayout = config.get("brewLayout");

		brewLayout.get("tanks").each(function(tank) {

			if (name == tank.get("name")) {
				control = tank;
			}

			var element = tank.get("heater");
			if (element) {
				if (name == element.get("name")) {
					control = element;
				}
			}
		});
		brewLayout.get("pumps").each(function(pump) {
			if (name == pump.get("name")) {
				control = pump;
			}
		});
		return control;
	},
	startStepList : function(stepList) {
		var self = this;
		var steps = stepList.get("steps");
		// Todo Resolve IOs if not set
		var apply = true;

		steps.each(function(step) {
			step.set("id", BrewCtrl.alphaId());

			if (step.get("stepTime") == null) {
				step.set("stepTime", BrewCtrl.parseTime(step.get("time")));
			}

			var controlPoints = step.get("controlPoints");
			controlPoints.each(function(controlPoint) {
				var control = self.findControlByName(controlPoint.get("controlName"));
				if (control != null) {

					if (controlPoint.get("controlIo") < 0) {
						controlPoint.set("controlIo", control.get("io"));
					}
					controlPoint.set("hasDuty", control.get("hasDuty"));

					var fullOnAmps = control.get("fullOnAmps");
					if (!fullOnAmps) {
						fullOnAmps = 0;
					}
					controlPoint.set("fullOnAmps", fullOnAmps);
					if (controlPoint.get("automaticControl") || controlPoint.get("targetName")) {
						controlPoint.set("automaticControl", true);
						var target = self.findControlByName(controlPoint.get("targetName"));
						if (target != null) {
							controlPoint.set("tempSensorAddress", target.get("sensorAddress"));
						} else {
							apply = false;
							BrewCtrl.alert("Failed to find target \"" + controlPoint.get("targetName") + "\"");
						}
					}
				} else {
					apply = false;
					BrewCtrl.alert("Failed to find control \"" + controlPoint.get("controlName") + "\"");
				}

			});

		});
		if (apply) {
			this.applySteps(steps);
		}
	},
	updateLayoutForStep : function(activeStep) {
		var config = this.get("config");
		var brewLayout = config.get("brewLayout");
		brewLayout.get("tanks").each(function(tank) {
			var heater = tank.get("heater")
			if (heater && heater.get("io") >= 0) {
				if (activeStep) {
					var controlPoint = activeStep.get("controlPoints").findByIo(heater.get("io"));
					if (controlPoint) {

						heater.set("duty", controlPoint.get("duty"));
						heater.set("on", controlPoint.get("on"));
						heater.set("automaticControl", controlPoint.get("automaticControl"));

						tank.set("heaterDuty", controlPoint.get("duty"));
						tank.set("heaterOn", controlPoint.get("on"));
						tank.set("targetTemp", controlPoint.get("targetTemp"));
						tank.set("automaticControl", controlPoint.get("automaticControl"));
					}
				}
			}
		});
		brewLayout.get("pumps").each(function(pump) {
			if (activeStep) {
				var controlPoint = activeStep.get("controlPoints").findByIo(pump.get("io"));
				if (controlPoint) {
					pump.set("duty", controlPoint.get("duty") > 0);
					pump.set("on", controlPoint.get("on"));
					pump.set("automaticControl", controlPoint.get("automaticControl"));

				}

			}
		});
	},
	getSelectedStep : function() {

		var steps = this.get("steps");
		var selected = null;
		steps.each(function(step) {
			if (step.get("selected")) {
				selected = step;
			}
		});
		return selected;
	},
	updateStep : function(step) {
		this.retrieveStatus({
			"modifySteps" : JSON.stringify([ step.toJSON() ])
		});
	},
	deleteStep : function(stepToDelete) {
		var newStepList = [];
		this.get("steps").each(function(step) {
			if (stepToDelete.get("id") != step.get("id")) {
				newStepList.push(step.toJSON());
			}
		});
		this.applySteps(newStepList);
	},
	applySteps : function(steps) {
		this.retrieveStatus({
			"steps" : JSON.stringify(steps)
		});
	},
	updateMode : function(mode) {
		this.retrieveStatus({
			"mode" : mode
		});
	}

});

BrewCtrl.Models.Config = Backbone.Model.extend({
	initialize : function() {
		this.set('brewLayout', new BrewCtrl.Models.Layout(this.get("brewLayout")));
		this.set('stepLists', new BrewCtrl.Collections.StepLists(this.get("stepLists")));
	},

	defaults : function() {
		return {
			version : null,
			brewLayout : {},
			stepLists : []
		};
	},
});

BrewCtrl.Models.Layout = Backbone.Model.extend({
	initialize : function() {
		this.set('tanks', new BrewCtrl.Collections.Tanks(this.get("tanks")));
		this.set('pumps', new BrewCtrl.Collections.Pumps(this.get("pumps")));
	},
	defaults : function() {
		return {
			tanks : [],
			pumps : []
		};
	},
});

BrewCtrl.Views.Mode = Backbone.View.extend({
	template : _.template($('#mode-template').html()),
	tagName : "span",

	// The DOM events specific to an item.
	events : {
		"click #modeOn" : "turnOn",
		"click #modeOff" : "turnOff",
		"click #modeHeatOff" : "turnHeatOff",
		"click #modeHold" : "turnHold"

	},
	turnOn : function() {
		BrewCtrl.main.updateMode("ON");
	},
	turnOff : function() {
		BrewCtrl.main.updateMode("OFF");
	},
	turnHold : function() {
		BrewCtrl.main.updateMode("HOLD");
	},
	turnHeatOff : function() {
		BrewCtrl.main.updateMode("HEAT_OFF");
	},
	render : function() {
		var display = this.template({
			mode : BrewCtrl.main.get("mode")
		});
		this.$el.html(display);
		return this;
	}
});

BrewCtrl.Views.Main = Backbone.View.extend({
	el : $("#brewctrl-main"),
	events : {
		"click #toggle-all" : "toggleAllComplete"

	},

	initialize : function() {
		var self = this;
		this.options.main.on("change:config", function() {
			self.render()
		});
		this.options.main.on("change:steps", function() {
			self.renderSteps()
		});
		this.options.main.on("change:mode", function() {
			self.renderMode()
		});
	},

	toggleAllComplete : function() {
	},

	renderSteps : function() {
		$("#brewctrl-steps").empty();
		this.options.main.get("steps").each(function(step) {
			var view = new BrewCtrl.Views.Step({
				model : step
			});
			$("#brewctrl-steps").append(view.render().el);
		});
	},
	renderMode : function() {
		$("#brewctrl-mode").empty();
		var view = new BrewCtrl.Views.Mode({});
		$("#brewctrl-mode").append(view.render().el);
	},

	render : function() {
		var config = this.options.main.get("config");
		var brewLayout = config.get("brewLayout");

		$("#brewctrl-tanks").empty();
		brewLayout.get("tanks").each(function(tank) {
			var view = new BrewCtrl.Views.Tank({
				model : tank
			});
			$("#brewctrl-tanks").append(view.render().el);
		});
		$("#brewctrl-pumps").empty();
		brewLayout.get("pumps").each(function(pump) {
			var view = new BrewCtrl.Views.Pump({
				model : pump
			});
			$("#brewctrl-pumps").append(view.render().el);
		});

		$("#brewctrl-steplists").empty();

		config.get("stepLists").each(function(stepList) {
			var view = new BrewCtrl.Views.StepList({
				model : stepList
			});
			$("#brewctrl-steplists").append(view.render().el);
		});

		this.renderSteps();
		this.renderMode();
		return this;
	}
});

BrewCtrl.Views.NumberEdit = Backbone.View.extend({
	template : _.template($('#duty-template').html()),
	tagName : "span",
	increment : 1,
	minValue : 0,
	maxValue : 100,
	quickClickValues : [],
	updateDisplay : function() {
		$(this.$el.find("#textValue")[0]).text(this.getTextValue());
	},
	mouseDown : function(increment) {
		var self = this;
		var count = 0;
		var timeoutFunction = function() {
			if (self.mouseDownTimeout) {
				clearTimeout(self.mouseDownTimeout);
				self.mouseDownTimeout = null;
			}

			self.updateValue(self.getValue() + (increment));
			count++;

			var delay = 300;
			if (count > 3) {
				delay = 200;
			} else if (count > 7) {
				delay = 100;
			}

			self.mouseDownTimeout = setTimeout(timeoutFunction, delay);
		};
		timeoutFunction();

	},
	mouseUp : function() {
		var self = this;
		if (self.mouseDownTimeout) {
			clearTimeout(self.mouseDownTimeout);
			self.mouseDownTimeout = null;
		}
		this.applyChange();
	},

	updateValue : function(newValue, skipUpdate) {

		if (newValue > this.maxValue) {
			newValue = this.maxValue;
		}
		if (newValue < this.minValue) {
			newValue = this.minValue;
		}

		if (newValue == this.getValue()) {
			return;
		}
		this.setValue(newValue);
		this.updateDisplay();
	},

	applyChange : function() {

	},
	getValue : function() {
		return 0;
	},
	getTextValue : function() {
		return this.getValue();
	},

	setValue : function() {
	},
	completeAction : function() {
	},
	render : function() {
		var self = this;

		if (self.quickClickValues) {
			for (key in self.quickClickValues) {
				var quickClick = self.quickClickValues[key];
				if (!quickClick.id) {
					quickClick.id = BrewCtrl.alphaId();
				}
			}
		}

		var display = this.template({
			quickClickValues : self.quickClickValues
		});
		this.$el.html(display);

		var stopMouseDown = function() {
			self.mouseUp();
		}

		var startUpFunction = function(event) {
			event.preventDefault();
			self.mouseDown(self.increment);
		}
		var startDownFunction = function(event) {
			event.preventDefault();
			self.mouseDown(-self.increment);
		}

		var upOne = $(this.$el.find("#upOne")[0]);
		upOne.on("touchstart", startUpFunction);
		upOne.on("touchend", stopMouseDown);
		upOne.mousedown(startUpFunction);
		upOne.mouseup(stopMouseDown);

		var downOne = $(this.$el.find("#downOne")[0]);
		downOne.on("touchstart", startDownFunction);
		downOne.on("touchend", stopMouseDown);
		downOne.mousedown(startDownFunction);
		downOne.mouseup(stopMouseDown);

		self.updateDisplay();

		for (key in self.quickClickValues) {
			var quickClick = self.quickClickValues[key];
			$(this.$el.find("#" + quickClick.id)[0]).click(quickClick.click);
		}
		return this;
	}
});

BrewCtrl.showPopup = function(popupContent, event) {
	var display = _.template($('#popup-template').html());
	var html = display({});
	var popupEl = $('<div/>').html(html)[0];
	var glass = $($(popupEl).children(".glass")[0]);

	var hidePopup = function() {
		popupEl.parentElement.removeChild(popupEl);
	};

	glass.click(hidePopup);

	var popup = $(popupEl).children(".popup")[0];

	popup.style.top = event.clientY + "px";
	popup.style.left = event.clientX + "px";

	var content = $($(popup).children(".content")[0]);
	content.empty();
	if (popupContent.completeAction) {
		var oldCompleteAction = popupContent.completeAction;
		popupContent.completeAction = function() {
			popupContent.completeAction = oldCompleteAction;
			popupContent.completeAction();
			hidePopup();
		}
	}
	content.append(popupContent.render().el);
	$("body").append(popupEl);
};

$(function() {
	BrewCtrl.main = new BrewCtrl.Models.Main();

	new BrewCtrl.Views.Main({
		main : BrewCtrl.main
	});
	BrewCtrl.main.start();

});