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

BrewCtrl.Views.Tank = Backbone.View.extend({
	template : BrewCtrl.loadTemplate("tank.svg"),
	tagName : "span",

	// The DOM events specific to an item.
	events : {
		"click #heatElement" : "editDuty",
		"click #temperatures" : "editTargetTemp"
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
				popup.increment = BrewCtrl.convertDisplay2C(33);

				popup.applyChange = function() {
					BrewCtrl.main.updateStep(selectedStep);
				};
				popup.getValue = function() {
					return controlPoint.get("targetTemp");
				};
				popup.getTextValue = function() {
					return BrewCtrl.round(BrewCtrl.convertC2Display(this.getValue()), 0).toFixed(0) + "\xB0";
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
						popup.updateValue(BrewCtrl.convertDisplay2C(120));
						popup.applyChange();
					}

				}, {
					text : "140\xB0",
					click : function() {
						popup.updateValue(BrewCtrl.convertDisplay2C(140));
						popup.applyChange();
					}
				}, {
					text : "153\xB0",
					click : function() {
						popup.updateValue(BrewCtrl.convertDisplay2C(153));
						popup.applyChange();
					}
				}, {
					text : "165\xB0",
					click : function() {
						popup.updateValue(BrewCtrl.convertDisplay2C(165));
						popup.applyChange();
					}
				}, {
					text : "210\xB0",
					click : function() {
						popup.updateValue(BrewCtrl.convertDisplay2C(210));
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

				if (controlPoint.get("automaticControl")) {
					BrewCtrl.confirm("Do you want to stop automatic control?", function() {
						controlPoint.set("automaticControl", false);
						BrewCtrl.main.updateStep(selectedStep);
					});
					return;
				}

				var popup = new BrewCtrl.Views.NumberEdit({});

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
				
				if(this.model.get("sensorAddress") != ""){				
					popup.quickClickValues.push({
						text : "Auto",
						click : function() {
							controlPoint.set("automaticControl", true);
							popup.applyChange();
							popup.completeAction();
						}
					});
				}
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

		var temp = $($element.find("#temperatures")[0]);

		if (this.model.get("hasSensor")) {
			temp.attr("class", "tank " + (this.model.get("reading") ? "reading" : "notReading"));
			$($element.find("#tempatureText")[0]).text(BrewCtrl.round(BrewCtrl.convertC2Display(this.model.get("temperatureC")), 1).toFixed(1) + '\xB0');
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

					$($element.find("#targetTempText")[0]).text("(" + BrewCtrl.round(BrewCtrl.convertC2Display(controlPoint.get("targetTemp")), 0).toFixed(0) + '\xB0)');

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

			var className = "heatElement";
			if (heater.get("automaticControl")) {
				className += " automatic"
			}
			className += heater.get("on") ? " on" : " off"
			heatElement.attr("class", className);
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

				if (controlPoint.get("automaticControl")) {
					return;
				}

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

		var className = "pump";
		if (this.model.get("automaticControl")) {
			className += " automatic"
		}

		var actuallyOn = this.model.get("on") && this.model.get("duty") > 0;
		className += this.model.get("duty") > 0 ? " on" : " off"
		className += actuallyOn ? " actuallyOn" : " actuallyOff"

		$($(element).find("#pump")[0]).attr("class", className);

		this.$el.empty();
		this.$el.append(element);
		return this;
	},
});

BrewCtrl.Models.Sensor = Backbone.Model.extend({
	initialize : function() {
	},
	defaults : function() {
		return {
			address : "",
			location : "Unknown Sensor",
			name : "",
			temperatureC : 0,
			reading : false,
			present : false
		};
	},
});
BrewCtrl.Collections.Sensors = Backbone.Collection.extend({
	model : BrewCtrl.Models.Sensor,
	initialize : function() {
	}
});

BrewCtrl.Views.Sensor = Backbone.View.extend({
	template : _.template($('#sensor-template').html()),
	tagName : "span",

	// The DOM events specific to an item.
	events : {
		"change .name" : "updateName",
		"change .location" : "updateLocation",
	},

	initialize : function() {
		this.listenTo(this.model, 'change', this.render);
	},
	updateName : function() {
		var self = this;
		var newName = self.$el.find(".name").attr("value");
		if (newName != self.model.get("name")) {
			self.model.set("name", newName)
			BrewCtrl.main.uploadConfiguration(JSON.stringify(self.loadedCfg));
		}
	},
	updateLocation : function() {
		var self = this;
		var locationSelect = self.$el.find(".location")[0];
		var locationName = locationSelect.options[locationSelect.selectedIndex].value
		if (locationName != self.model.get("location")) {
			self.model.set("location", locationName)
			BrewCtrl.main.uploadConfiguration(JSON.stringify(self.loadedCfg));
		}
	},
	render : function() {
		var self = this;
		self.loadedCfg = BrewCtrl.main.get("config");
		var display = self.template(self.model.toJSON());
		self.$el.html(display);

		self.$el.find(".name").attr("value", self.model.get("name"));

		var locationSelect = self.$el.find(".location")[0];
		var locations = BrewCtrl.main.listSensorLocations();
		_.each(locations, function(location) {
			var option = new Option(location.get("name"), location.get("name"));
			option.selected = self.model.get("location") == location.get("name");
			locationSelect.options.add(option);
		});

		return this;
	},
});