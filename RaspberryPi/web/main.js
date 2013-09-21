
var BrewCtrl = {
    Models: {},
    Collections: {},
    Views: {},
    Templates: {},
    loadTemplate: function(tmpl_name) {
      var tmpl_url = tmpl_name;
      var tmpl_string;
      $.ajax({
        url: tmpl_url,
        method: 'GET',
        async: false,
        success: function(data) {
          tmpl_string = data;
        }
      });
      return tmpl_string;
    },
    convertC2F: function(tempC) {
	return (9.0 / 5.0) * tempC + 32;
    },
    convertF2C: function(tempF) {
	return (5.0 / 9.0) * (tempF - 32);
    },
    round: function(value, places) {
	var factor = Math.pow(10,places);
	value =  Math.round(value * factor);
	return  value/factor;
    }    
  }



$(function() {


  BrewCtrl.Models.Tank = Backbone.Model.extend({
    defaults: function() {
      return {
        name: "Unknown",
        temperatureF: 0.0,
        temperatureC: 0.0,
        reading: false,
        duty: 0
      };
    },
  });

  BrewCtrl.Collections.Tanks = Backbone.Collection.extend({
    model: BrewCtrl.Models.Tank,
    initialize: function() {
    }
  });

  BrewCtrl.Models.Main = Backbone.Model.extend({
   initialize: function() {

    },
   loadConfiguration: function() {



	var self = this;
	$.ajax("cmd/configuration").success(function(data) {
		var config = new BrewCtrl.Models.Config(data);
		self.set("config",config);    
		self.scheduleStatusUpdate();


	});
    },
    start: function() {
	var self = this;
	this.loadConfiguration();
    },



    applyStatus: function(data){
	var self = this;
	var config = self.get("config");
	
console.log("HERE");



	if(data.configurationVersion!= config.get("version")){
		self.loadConfiguration();
		return;
        }

	_.each(data.sensors,function(sensor){

		var brewLayout = config.get("brewLayout");
	        brewLayout.get("tanks").each(function(tank) {		
			var tankSensor = tank.get("sensor");
			if(tankSensor && tankSensor.address == sensor.address){
			     tank.set("temperatureF",  BrewCtrl.convertC2F(sensor.temperatureC));	
			     tank.set("temperatureC",  sensor.temperatureC);
			     tank.set("reading",   sensor.reading);
			}
		});
	});	
    },
    scheduleStatusUpdate: function(){
	var self = this;
	if(self.statusTimeout){
		clearTimeout(self.statusTimeout);
		self.statusTimeout = 0;
	}
	self.statusTimeout = setTimeout(function(){
		$.ajax("cmd/status").success(function(data) {
			self.applyStatus(data);
		}).fail(function(e) {
			console.log("scheduleStatusUpdate Error");
			console.log(e);
		}).always(function() {
			self.scheduleStatusUpdate();  
		});;
	},500);
    },
 
  

    defaults: function() {
      return {
        version: null,
        config: new BrewCtrl.Models.Config()
      };
    }
  });


  BrewCtrl.Models.Config = Backbone.Model.extend({
    initialize: function() {
      this.set('brewLayout', new BrewCtrl.Models.Layout(this.get("brewLayout")));
    },

    defaults: function() {
      return {
        version: null,
        brewLayout: {}
      };
    },
  });

  BrewCtrl.Models.Layout = Backbone.Model.extend({
    initialize: function() {
      this.set('tanks', new BrewCtrl.Collections.Tanks(this.get("tanks")));
    },
    defaults: function() {
      return {
        tanks: []
      };
    },
  });



  BrewCtrl.Views.Tank = Backbone.View.extend({
    template: _.template(BrewCtrl.loadTemplate("tank.svg")),
    tagName: "span",

    // The DOM events specific to an item.
    events: {
      "click .toggle": "toggleDone",
      "dblclick .view": "edit",
      "click a.destroy": "clear",
      "keypress .edit": "updateOnEnter",
      "blur .edit": "close"
    },

    initialize: function() {
      this.listenTo(this.model, 'change', this.render);
      // this.listenTo(this.model, 'destroy', this.remove);
    },
    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      return this;
    },
  });

  BrewCtrl.Views.Main = Backbone.View.extend({
    el: $("#brewctrl-main"),
    events: {
      "click #toggle-all": "toggleAllComplete"
    },

    initialize: function() {
      var self = this;
      this.options.main.on("change:config",function(){self.render()});
    },

    toggleAllComplete: function() {
    },

    render: function() {
      this.$("#brewctrl-main").empty();
      var config = this.options.main.get("config");
      var brewLayout = config.get("brewLayout");
      brewLayout.get("tanks").each(function(tank) {
	var view = new BrewCtrl.Views.Tank({
          model: tank
        });
        this.$("#brewctrl-main").append(view.render().el);
      });
      return this;
    }
  });

  var main = new BrewCtrl.Models.Main();
  
  new BrewCtrl.Views.Main({
      main: main
  });
  main.start();
  

 
});
