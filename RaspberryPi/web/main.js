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
	formatTime : function(time) {
		if (time == 0) {
			return "Forever";
		}

		var minutes = parseInt(time / 60);
		var seconds = parseInt(time - (minutes * 60));

		var formated = minutes + ":";

		if (seconds < 10) {
			formated = formated + '0';
		}
		formated = formated + seconds;
		return formated;
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

		var activeStep = this.getActiveStep();

		if (data.configurationVersion != config.get("version")) {
			self.loadConfiguration();
			return;
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

			var heater = tank.get("heater")
			if (heater && heater.get("io") >= 0) {
				if (activeStep) {
					var controlPoint = activeStep.get("controlPoints").findByIo(heater.get("io"));
					if (controlPoint) {
						heater.duty = controlPoint.get("duty");
						heater.on = controlPoint.get("on");
						tank.set("heaterDuty", heater.duty);
						tank.set("heaterOn", heater.on);
					}
				}
			}

			tank.set("hasSensor", foundSensor);
		});
		brewLayout.get("pumps").each(function(pump) {
			if (activeStep) {
				var controlPoint = activeStep.get("controlPoints").findByIo(pump.get("io"));
				if (controlPoint) {
					pump.set("duty", controlPoint.get("duty") > 0);
					pump.set("on", controlPoint.get("on"));
				}

			}
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
			always : function() {
				// self.scheduleStatusUpdate();
			}
		});
	},

	getActiveStep : function() {
		var steps = this.get("steps");
		return steps.first();
	},
	getSelectedStep : function() {
		return this.getActiveStep();
	},
	updateStep : function(step) {
		this.retrieveStatus({
			"modifySteps" : JSON.stringify([ step.toJSON() ])
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

		this.renderSteps();
		this.renderMode();
		return this;
	}
});

BrewCtrl.showPopup = function(popupContent,event) {
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
		popupContent.completeAction = function(){
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
