$(function(){

var BrewCtrl = {
    Models: {},
    Collections: {},
    Views: {},
    Templates:{},
loadTemplate: function (tmpl_name) {
    
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
}

}




BrewCtrl.Models.Tank = Backbone.Model.extend({
    defaults: function() {
      return {
	name:"Unknown",
	temperatureF: 0.0,
	duty: 0
      };
    },
  });

BrewCtrl.Collections.Tanks = Backbone.Collection.extend({
    model: BrewCtrl.Models.Tank,
    initialize: function(){
        console.log("Tanks initialize")
    }
});

BrewCtrl.Models.Config = Backbone.Model.extend({
    initialize: function() {
        this.set('brewLayout',  new BrewCtrl.Models.Layout(this.get("brewLayout")));
    },

    defaults: function() {
      return {
	version:null,
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
    template: _.template( BrewCtrl.loadTemplate("tank.svg") ),
    tagName:  "span",

    // The DOM events specific to an item.
    events: {
      "click .toggle"   : "toggleDone",
      "dblclick .view"  : "edit",
      "click a.destroy" : "clear",
      "keypress .edit"  : "updateOnEnter",
      "blur .edit"      : "close"
    },  
    
    initialize: function() {
    //  this.listenTo(this.model, 'change', this.render);
     // this.listenTo(this.model, 'destroy', this.remove);
    },
    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
	return this;
    },
});

 BrewCtrl.Views.Main = Backbone.View.extend({
    el: $("#brewctrl-main"),
    // Delegated events for creating new items, and clearing completed ones.
    events: {
      "click #toggle-all": "toggleAllComplete"
    },

    initialize: function() {
      this.listenTo(this.options.config, 'change', this.render);	
      this.footer = this.$('footer');
      this.main = $('#main');
    },

    toggleAllComplete: function () {

  
    },

    render: function() {
     var brewLayout = this.options.config.get("brewLayout");
      console.log("Tanks");
      console.log( brewLayout.get("tanks"));
   	 brewLayout.get("tanks").each(function(tank){


	      var view = new  BrewCtrl.Views.Tank({model: tank});
	      this.$("#todo-list").append(view.render().el);
	   

		console.log("Tank: " + tank.get("name"));
	});

      console.log("ABCD");
    }
});



$.ajax("cmd/configuration").success(function (data) {
	var config = new BrewCtrl.Models.Config(data);
	console.log(config );

	new BrewCtrl.Views.Main({config:config}).render();
});


});
