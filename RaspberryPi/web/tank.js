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
	updateDisplay : function() {
		$(this.$el.find("#textValue")[0]).text(this.getValue() + "%");
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
	setValue : function() {
	},
	completeAction : function() {
	},
	render : function() {
		var self = this;
		var quickClickValues = [ {
			id : "duty0",
			text : "0%",
			click : function() {
				self.updateValue(0);
				self.applyChange();
			}

		}, {
			id : "duty25",
			text : "25%",
			click : function() {
				self.updateValue(25);
				self.applyChange();
			}
		}, {
			id : "duty50",
			text : "50%",
			click : function() {
				self.updateValue(50);
				self.applyChange();
			}
		}, {
			id : "duty75",
			text : "75%",
			click : function() {
				self.updateValue(75);
				self.applyChange();
			}
		}, {
			id : "duty100",
			text : "100%",
			click : function() {
				self.updateValue(100);
				self.applyChange();
			}
		}

		];

		var display = this.template({
			quickClickValues : quickClickValues
		});
		this.$el.html(display);

		var upOne = $(this.$el.find("#upOne")[0]);
		upOne.mousedown(function(event) {
			event.preventDefault();
			self.mouseDown(self.increment);
		});
		upOne.mouseup(function() {
			self.mouseUp();
		});

		var downOne = $(this.$el.find("#downOne")[0]);
		downOne.mousedown(function() {
			self.mouseDown(-self.increment);
		});
		downOne.mouseup(function() {
			self.mouseUp();
		});

		self.updateDisplay();

		for (key in quickClickValues) {
			var quickClick = quickClickValues[key];
			$(this.$el.find("#" + quickClick.id)[0]).click(quickClick.click);

		}
		return this;
	}
});

BrewCtrl.Views.Tank = Backbone.View.extend({
	template : _.template(BrewCtrl.loadTemplate("tank.svg")),
	tagName : "span",

	// The DOM events specific to an item.
	events : {
		"click #heatElement" : "editDuty"
	},

	initialize : function() {
		this.listenTo(this.model, 'change', this.render);
		// this.listenTo(this.model, 'destroy', this.remove);
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
				popup.setValue = function(newValue) {
					if (newValue > 100) {
						return;
					}
					if (newValue < 0) {
						return;
					}
					controlPoint.set("duty", newValue);
				};

				BrewCtrl.showPopup(popup, event);

			}
		}

	},
	render : function() {
		var display = this.template(this.model.toJSON());
		this.$el.html(display);
		return this;
	},
});

BrewCtrl.Views.Pump = Backbone.View.extend({
	template : _.template(BrewCtrl.loadTemplate("pump.svg")),
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
		var display = this.template(this.model.toJSON());
		this.$el.html(display);
		return this;
	},
});