BrewCtrl.Models.Tank = Backbone.Model.extend({
	initialize : function() {
		this.set('heater', new BrewCtrl.Models.Heater(this.get("heater")));
	},
	defaults : function() {
		return {
			name : "Unknown",
			temperatureC : 0.0,
			reading : false,
			hasSensor : false,
			heater : null,
			duty : 0
		};
	},
});

BrewCtrl.Collections.Tanks = Backbone.Collection.extend({
	model : BrewCtrl.Models.Tank,
	initialize : function() {
	}
});

BrewCtrl.Models.Heater = Backbone.Model.extend({
	defaults : function() {
		return {
			on : false,
			duty : 0,
			io : -1
		};
	},
});

BrewCtrl.Models.Pump = Backbone.Model.extend({
	defaults : function() {
		return {
			name : "Unknown",
			on : false,
			duty : 0,
			io : -1
		};
	},
});

BrewCtrl.Collections.Pumps = Backbone.Collection.extend({
	model : BrewCtrl.Models.Pump,
	initialize : function() {
	}
});

BrewCtrl.Views.NumberEdit = Backbone.View.extend({
	template : _.template($('#duty-template').html()),
	tagName : "span",
	increment : 1,
	quickClickValues : [],
	updateDisplay : function() {
		$(this.$el.find("#textValue")[0]).text(this.getTextValue());
	},
	mouseDown : function(direction) {
		var self = this;
		var count = 0;
		var timeoutFunction = function() {
			if (self.mouseDownTimeout) {
				clearTimeout(self.mouseDownTimeout);
				self.mouseDownTimeout = null;
			}
			self.updateValue(self.getValue() + (self.increment * direction));
			count++;

			var delay = 300;
			if (count > 3) {
				delay = 150;
			} else if (count > 7) {
				delay = 50;
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

	updateValue : function(newDuty, skipUpdate) {
		this.setValue(newDuty);
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

BrewCtrl.Views.Tank = Backbone.View.extend({
	template : BrewCtrl.loadTemplate("tank.svg"),
	tagName : "span",

	// The DOM events specific to an item.
	events : {
		"click #heatElement" : "editDuty",
		"click #tempatures" : "editTargetTemp"
	},

	initialize : function() {
		this.listenTo(this.model, 'change', this.render);
		// this.listenTo(this.model, 'destroy', this.remove);
	},

	editTargetTemp : function(event) {
		var selectedStep = BrewCtrl.main.getSelectedStep();
		if (selectedStep) {
			var sensorAddress = this.model.get("sensorAddress");
			var controlPoint = selectedStep.get("controlPoints").findByAutomaticAndSensorAddress(sensorAddress);
			if (controlPoint) {
				var popup = new BrewCtrl.Views.NumberEdit({

				});

				// Increment by 1 degree f
				popup.increment = BrewCtrl.convertF2C(33);

				popup.applyChange = function() {
					BrewCtrl.main.updateStep(selectedStep);
				};
				popup.getValue = function() {
					return controlPoint.get("targetTemp");
				};
				popup.getTextValue = function() {
					return BrewCtrl.round(BrewCtrl.convertC2F(this.getValue()), 0).toFixed(0) + "\xB0";
				}

				popup.setValue = function(newValue) {
					if (newValue > 120) {
						return;
					}
					if (newValue < 0) {
						return;
					}
					controlPoint.set("targetTemp", newValue);
				};
				popup.quickClickValues = [ {
					text : "120\xB0",
					click : function() {
						popup.updateValue(BrewCtrl.convertF2C(120));
						popup.applyChange();
					}

				}, {
					text : "140\xB0",
					click : function() {
						popup.updateValue(BrewCtrl.convertF2C(140));
						popup.applyChange();
					}
				}, {
					text : "153\xB0",
					click : function() {
						popup.updateValue(BrewCtrl.convertF2C(153));
						popup.applyChange();
					}
				}, {
					text : "165\xB0",
					click : function() {
						popup.updateValue(BrewCtrl.convertF2C(165));
						popup.applyChange();
					}
				}, {
					text : "210\xB0",
					click : function() {
						popup.updateValue(BrewCtrl.convertF2C(210));
						popup.applyChange();
					}

				}

				];
				BrewCtrl.showPopup(popup, event);

			}
		}
	},

	editDuty : function(event) {

		var selectedStep = BrewCtrl.main.getSelectedStep();
		if (selectedStep) {
			var controlPoint = selectedStep.get("controlPoints").findByIo(this.model.get("heater").get("io"));
			if (controlPoint) {
				var popup = new BrewCtrl.Views.NumberEdit({

				});

				popup.applyChange = function() {
					BrewCtrl.main.updateStep(selectedStep);
				};
				popup.getValue = function() {
					return controlPoint.get("duty");
				};
				popup.getTextValue = function() {
					return this.getValue() + "%";
				};
				popup.setValue = function(newValue) {
					if (newValue > 100) {
						return;
					}
					if (newValue < 0) {
						return;
					}
					controlPoint.set("duty", newValue);
				};
				popup.quickClickValues = [ {
					text : "0%",
					click : function() {
						popup.updateValue(0);
						popup.applyChange();
					}

				}, {
					text : "25%",
					click : function() {
						popup.updateValue(25);
						popup.applyChange();
					}
				}, {
					text : "50%",
					click : function() {
						popup.updateValue(50);
						popup.applyChange();
					}
				}, {
					text : "75%",
					click : function() {
						popup.updateValue(75);
						popup.applyChange();
					}
				}, {
					text : "100%",
					click : function() {
						popup.updateValue(100);
						popup.applyChange();
					}
				}

				];
				BrewCtrl.showPopup(popup, event);

			}
		}

	},
	render : function() {
		var self = this;
		var element = document.createElement('span');
		element.innerHTML = this.template;

		var $element = $(element);
		$($element.find("#tankNameText")[0]).text(this.model.get("name"));

		var temp = $($element.find("#tempature")[0]);
		if (this.model.get("hasSensor")) {
			temp.attr("class", "tank " + (this.model.get("reading") ? "reading" : "notReading"));
			$($element.find("#tempatureText")[0]).text(BrewCtrl.round(BrewCtrl.convertC2F(this.model.get("temperatureC")), 1).toFixed(1) + '\xB0');

		} else {
			temp.remove();
		}

		var targetTemp = $($element.find("#targetTemp")[0]);

		var showTarget = false;
		if (this.model.get("hasSensor")) {

			var selectedStep = BrewCtrl.main.getSelectedStep();
			if (selectedStep) {
				var sensorAddress = this.model.get("sensorAddress");
				var controlPoint = selectedStep.get("controlPoints").findByAutomaticAndSensorAddress(sensorAddress);
				if (controlPoint) {

					$($element.find("#targetTempText")[0]).text("(" + BrewCtrl.round(BrewCtrl.convertC2F(controlPoint.get("targetTemp")), 0).toFixed(0) + '\xB0)');
					$($element.find("#temperatures")[0]).click(function(event) {
						self.editTargetTemp(event);
					});

					showTarget = true;
				}
			}

		}
		if (!showTarget) {
			targetTemp.remove();
		}

		var heater = this.model.get("heater")
		var heatElement = $($element.find("#heatElement")[0]);
		if (heater && heater.get("io") >= 0) {
			heatElement.attr("class", "heatElement " + (heater.get("on") ? "on" : "off"));
			$($element.find("#heatElementText")[0]).text(heater.get("duty") + '%');
		} else {
			heatElement.remove();
		}

		this.$el.empty();
		this.$el.append(element);
		return this;
	},
});

BrewCtrl.Views.Pump = Backbone.View.extend({
	template : BrewCtrl.loadTemplate("pump.svg"),
	tagName : "span",

	// The DOM events specific to an item.
	events : {
		"click" : "toggleState",
	},
	toggleState : function() {
		var newDuty = this.model.get("duty") > 0 ? 0 : 100;
		// this.model.set("duty", newDuty);
		var selectedStep = BrewCtrl.main.getSelectedStep();
		if (selectedStep) {
			var controlPoint = selectedStep.get("controlPoints").findByIo(this.model.get("io"));
			if (controlPoint) {
				controlPoint.set("duty", newDuty);
				BrewCtrl.main.updateStep(selectedStep);
			}
		}
	},

	initialize : function() {
		this.listenTo(this.model, 'change', this.render);
		// this.listenTo(this.model, 'destroy', this.remove);
	},
	render : function() {
		var element = document.createElement('span');
		element.innerHTML = this.template;

		$($(element).find("#pumpNameText")[0]).text(this.model.get("name"));

		$($(element).find("#pump")[0]).attr("class", "pump " + (this.model.get("duty") > 0 ? "on" : "off"));

		this.$el.empty();
		this.$el.append(element);
		return this;
	},
});