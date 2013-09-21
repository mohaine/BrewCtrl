BrewCtrl.Models.Tank = Backbone.Model.extend({
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
	template : _.template(BrewCtrl.loadTemplate("tank.svg")),
	tagName : "span",

	// The DOM events specific to an item.
	events : {

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

BrewCtrl.Views.Pump = Backbone.View.extend({
	template : _.template(BrewCtrl.loadTemplate("pump.svg")),
	tagName : "span",

	// The DOM events specific to an item.
	events : {
		"click" : "toggleState",
	},
	toggleState : function() {
		var newDuty = this.model.get("duty") > 0 ? 0 : 100;
		this.model.set("duty", newDuty);
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