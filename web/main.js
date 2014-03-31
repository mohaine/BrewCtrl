var BrewCtrl = {
	autoUpdateStatus : true,
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

	convertC2Display : function(tempC) {
		return BrewCtrl.convertC2F(tempC);
	},

	convertC2F : function(tempC) {
		return (9.0 / 5.0) * tempC + 32;
	},

	convertDisplay2C : function(tempC) {
		return BrewCtrl.convertF2C(tempC);
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
		for (var i = 0; i < split.length; i++) {
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
		for (var i = 0; i < 8; i++) {
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
			sensors : new BrewCtrl.Collections.Sensors(),
			steps : new BrewCtrl.Collections.Steps(),
			mode : null
		};
	},

	loadConfiguration : function() {
		var self = this;
		$.ajax("cmd/configuration").success(function(data) {
			self.applyConfiguration(data);
		});
	},

	uploadConfiguration : function(cfgJson, onSuccess) {
		var self = this;
		$.ajax({
			type : "POST",
			url : "cmd/configuration",
			data : {
				"configuration" : cfgJson
			},
			success : function(responseData) {
				self.applyConfiguration(responseData);
				if (onSuccess) {
					onSuccess();
				}
			},
			error : function(e) {
				BrewCtrl.alert("Failed to upload file. : " + e.statusText);
			},

		});
	},

	applyConfiguration : function(data) {
		var self = this;
		var config = new BrewCtrl.Models.Config(data);
		self.set("config", config);
		console.log("config version: " + config.get('version'));
		self.scheduleStatusUpdate();
	},

	start : function() {
		var self = this;
		this.loadConfiguration();

		if (BrewCtrl.autoUpdateStatus) {
			self.checkStatusUpdate();
		}
	},
	checkStatusUpdate : function() {
		var self = this;
		if (self.statusTimeout) {
			clearTimeout(self.statusCheckTimeout);
			self.statusCheckTimeout = 0;
		}

		if (!self.lastStatusUpdateTime || new Date().getTime() - self.lastStatusUpdateTime > 5000) {
			if (!self.statusPopup) {
				var status = new BrewCtrl.Views.Status({});
				self.statusPopup = BrewCtrl.showPopup(status, {
					clientX : 2,
					clientY : 3
				}, function() {
					self.statusPopup = null;
				});
			}

		} else if (self.statusPopup) {
			self.statusPopup.hidePopup();
		}
		self.statusCheckTimeout = setTimeout(function() {
			self.checkStatusUpdate();
		}, 500);
	},
	applyStatus : function(data) {
		var self = this;
		var config = self.get("config");

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
		config.get("sensors").each(function(cfgSensor) {
			var foundSensor = false;
			_.each(data.sensors, function(sensor) {
				if (sensor.address == cfgSensor.get("address")) {
					cfgSensor.set("temperatureC", sensor.temperatureC);
					cfgSensor.set("reading", sensor.reading);
					foundSensor = true;
				}
			});
			cfgSensor.set("present", foundSensor);
		});
		self.lastStatusUpdateTime = new Date().getTime();

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
				if (BrewCtrl.autoUpdateStatus) {
					self.scheduleStatusUpdate();
				}
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

	listControls : function() {
		var controlPoints = [];
		var self = this;
		var config = self.get("config");
		var brewLayout = config.get("brewLayout");

		brewLayout.get("tanks").each(function(tank) {
			var element = tank.get("heater");
			if (element && element.get("io") > -1) {
				controlPoints.push(element);
			}
		});
		brewLayout.get("pumps").each(function(pump) {
			if (pump && pump.get("io") > -1) {
				controlPoints.push(pump);
			}
		});
		return controlPoints;
	},

	listSensorLocations : function() {
		var locations = [];
		var self = this;
		var config = self.get("config");
		var brewLayout = config.get("brewLayout");

		brewLayout.get("tanks").each(function(tank) {
			locations.push(tank);
		});
		return locations;
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

	initStep : function(step) {
		var self = this;
		step.set("id", BrewCtrl.alphaId());

		if (step.get("stepTime") == null) {
			if (step.get("time")) {
				step.set("stepTime", BrewCtrl.parseTime(step.get("time")));
			} else {
				step.set("stepTime", 0);
			}
		}

		var controlPoints = step.get("controlPoints");
		controlPoints.each(function(controlPoint) {
			var control = self.findControlByName(controlPoint.get("controlName"));
			if (control != null) {

				controlPoint.setupFromControl(control);

				if (controlPoint.get("automaticControl") || controlPoint.get("targetName")) {
					var target = self.findControlByName(controlPoint.get("targetName"));
					if (target != null) {
						controlPoint.set("automaticControl", true);
						controlPoint.set("tempSensorAddress", target.get("sensorAddress"));
					} else if (controlPoint.get("automaticControl")) {
						apply = false;
						BrewCtrl.alert("Failed to find target \"" + controlPoint.get("targetName") + "\"");
					}
				}
			} else {
				apply = false;
				BrewCtrl.alert("Failed to find control \"" + controlPoint.get("controlName") + "\"");
			}
		});
		// Add control points that are missing from step in as manual
		var controls = self.listControls();
		_.each(controls, function(control) {
			var found = false;
			controlPoints.each(function(controlPoint) {
				if (controlPoint.get("controlIo") == control.get("io")) {
					found = true;
				}
			});
			if (!found) {
				var manualCp = new BrewCtrl.Models.ControlPoint();
				manualCp.setupFromControl(control);
				controlPoints.add(manualCp);
			}
		});
	},
	startStepList : function(stepList) {
		var self = this;
		var steps = _.clone(stepList.get("steps"));

		// Todo Resolve IOs if not set
		var apply = true;

		steps.each(function(step) {
			self.initStep(step);
		});
		if (apply) {
			// console.log(JSON.stringify(steps));
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
	addStep : function() {
		var self = this;
		var newStepList = [];
		this.get("steps").each(function(step) {
			newStepList.push(step.toJSON());
		});
		var step = new BrewCtrl.Models.Step();
		self.initStep(step);
		newStepList.push(step.toJSON());
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
		this.set('sensors', new BrewCtrl.Collections.Sensors(this.get("sensors")));
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
	}
});
BrewCtrl.Views.Status = Backbone.View.extend({
	template : _.template($('#popup-status-loading').html()),
	tagName : "div",
	render : function() {
		var display = this.template({});
		this.$el.html(display);
		return this;
	}
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
	events : {},

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

	renderUploadConfig : function() {
		$("#brewctrl-config-upload").empty();
		var view = new BrewCtrl.Views.UploadConfiguration({});
		$("#brewctrl-config-upload").append(view.render().el);
	},

	render : function() {
		var config = this.options.main.get("config");
		var brewLayout = config.get("brewLayout");

		var self = this;

		$("#brewctrl-add-step").on("click", function() {
			console.log()
			self.options.main.addStep();
		});

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

		$("#brewctrl-sensors").empty();
		config.get("sensors").each(function(sensor) {
			var view = new BrewCtrl.Views.Sensor({
				model : sensor
			});
			$("#brewctrl-sensors").append(view.render().el);
		});

		this.renderSteps();
		this.renderMode();
		this.renderUploadConfig();
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

BrewCtrl.Views.UploadConfiguration = Backbone.View.extend({
	template : _.template($('#config-upload-template').html()),
	tagName : "span",
	events : {
		"click #uploadConfiguration" : "uploadConfiguration"
	},
	uploadConfiguration : function() {
		var form = document.getElementById("configurationToUpload");
		var files = form.files;

		if (files.length == 0) {
			BrewCtrl.alert("Please select a file.");
			return;
		}

		if (files.length > 1) {
			BrewCtrl.alert("Please select a single file.");
			return;
		}

		var reader = new FileReader();

		// Read in the image file as a data URL.
		reader.onloadend = function(evt) {
			if (evt.target.readyState == FileReader.DONE) {
				BrewCtrl.main.uploadConfiguration(evt.target.result, function() {
					$("#config-upload-form")[0].reset();
				});

			}
		};

		reader.readAsBinaryString(files[0]);

	},
	completeAction : function() {
	},
	render : function() {
		var self = this;

		var display = this.template({});
		this.$el.html(display);

		return this;
	}
});

BrewCtrl.showPopup = function(popupContent, event, onHide) {
	var display = _.template($('#popup-template').html());
	var html = display({});
	var popupEl = $('<div/>').html(html)[0];
	var glass = $($(popupEl).children(".glass")[0]);

	var hidePopup = function() {
		if (popupEl.parentElement) {
			popupEl.parentElement.removeChild(popupEl);
		}

		if (onHide) {
			onHide();
		}
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

	return {
		hidePopup : hidePopup
	};
};

$(function() {
	BrewCtrl.main = new BrewCtrl.Models.Main();

	new BrewCtrl.Views.Main({
		main : BrewCtrl.main
	});
	BrewCtrl.main.start();

});