BrewCtrl.Models.ControlPoint = Backbone.Model.extend({
	defaults : function() {
		return {
			controlName : "",
			targetName : "",
			automaticControl : false,
			controlIo : 0,
			duty : 0,
			on : false,
			targetTemp : 0
		};
	},
});
BrewCtrl.Models.Step = Backbone.Model.extend({
	initialize : function() {
		this.set('controlPoints', new BrewCtrl.Collections.ControlPoints(this.get("controlPoints")));
	},
	defaults : function() {
		return {
			name : "New Step",
			time : 0,
			controlPoints : []
		};
	},
});
BrewCtrl.Models.StepList = Backbone.Model.extend({
	initialize : function() {
		this.set('steps', new BrewCtrl.Collections.Steps(this.get("steps")));
	},
	defaults : function() {
		return {
			name : "Step List",
			steps : []
		};
	},
});

BrewCtrl.Collections.ControlPoints = Backbone.Collection.extend({
	model : BrewCtrl.Models.ControlPoint,
	initialize : function() {
	},
	findByIo : function(io) {
		var cpFound;
		this.each(function(cp) {
			if (cp.get("controlIo") == io) {
				cpFound = cp;
			}
		});
		return cpFound;
	},
	findByAutomaticAndSensorAddress : function(address) {
		var cpFound;
		this.each(function(cp) {
			if (cp.get("automaticControl") && cp.get("tempSensorAddress") == address) {
				cpFound = cp;
			}
		});
		return cpFound;
	}
});
BrewCtrl.Collections.Steps = Backbone.Collection.extend({
	model : BrewCtrl.Models.Step,
	initialize : function() {
	}
});
BrewCtrl.Collections.StepLists = Backbone.Collection.extend({
	model : BrewCtrl.Models.StepList,
	initialize : function() {
	}
});

BrewCtrl.Views.Step = Backbone.View.extend({
	template : _.template($('#step-template').html()),
	tagName : "span",

	// The DOM events specific to an item.
	events : {
		"click .stepDelete" : "deleteStep",
		"click .stepTime" : "editTime",
		"click .select" : "selectStep"

	},
	initialize : function() {
		this.listenTo(this.model, 'change', this.render);
	},
	selectStep : function(event) {
		BrewCtrl.main.selectStep(this.model);
	},

	editTime : function(event) {
		var step = this.model;
		var popup = new BrewCtrl.Views.NumberEdit({

		});
		popup.increment = 60;
		popup.maxValue = 60 * 60 * 60;

		popup.applyChange = function() {
			BrewCtrl.main.updateStep(step);
		};
		popup.getValue = function() {
			return step.get("stepTime");
		};
		popup.getTextValue = function() {
			return BrewCtrl.formatTimeMinutes(this.getValue());
		};
		popup.setValue = function(newValue) {
			step.set("stepTime", newValue);
		};

		popup.quickClickValues = [];
		var createQuick = function(minutes) {
			var time = minutes * 60;
			return {
				text : BrewCtrl.formatTimeMinutes(time),
				click : function() {
					popup.updateValue(time);
					popup.applyChange();
				}
			};
		}

		popup.quickClickValues.push(createQuick(15));
		popup.quickClickValues.push(createQuick(30));
		popup.quickClickValues.push(createQuick(60));
		popup.quickClickValues.push(createQuick(90));
		popup.quickClickValues.push(createQuick(0));

		BrewCtrl.showPopup(popup, event);

	},

	render : function() {
		var display = this.template(this.model.toJSON());
		this.$el.html(display);

		if (this.model.get("selected")) {
			var stepElement = $(this.$el.find(".step")[0]);
			stepElement.addClass("selected");
		}

		return this;
	},
	deleteStep : function() {
		var self = this;
		BrewCtrl.confirm("Are you sure you want to delete Step \"" + self.model.get("name") + "\"?", function() {
			BrewCtrl.main.deleteStep(self.model);
		});
	}
});
