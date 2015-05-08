
/**
 * Табы
 * @param ui - {nav|opt.navRoot, content}
 * @param controllers - {key-str|initedController}
 * @param opt - extra config
 */
TabPanel = function(ui, controllers, opt){

	opt = Util.extend({
		root: "root",
		navRoot: "nav",
		navItemsQuery: ".nav li",
		contentRoot: "content"
	}, opt);

	var empty = Util.isEmpty(controllers);


	var self = this;
	var id = Util.generateUid();
	var log = LogFactory.getLog("TabPanel@"+id);

	var items = {};
	var menuItems = [];
	var curElem;
	
	function init(){

		if(empty){
			controllers = {};
		} else {
			$(opt.navItemsQuery, ui[opt.root]).each(function(i, elem){
				menuItems.push($(elem));
			});
		}


		
		$.each(controllers, function(key){
			if(!ui[key]){
				log.warn("can't find nav item by key: "+key);
				return;
			}
			initTabItem(ui[key], key);
		});
	}

	this.addItem = function(key, tabItem, controller){

		if(ui[key]){
			log.error("can't add alredy exists tab item by key: "+key);
			return;
		}

		//init model
		var data = {item: tabItem, controller: controller};
		items[key] = data;
		
		//init tab view
		ui[key] = tabItem;
		ui[opt.navRoot].append(tabItem);
		initTabItem(tabItem, key);
		menuItems.push(tabItem);
		
		//init content view
		if(! data.controller) return;
		var contentRoot = data.controller.getUI();
		contentRoot.hide();
		ui[opt.contentRoot].append(contentRoot);
	};

	function initTabItem(elem, key){
		elem.click(function(){
			self.selectItem(key);
		});
	}

	this.getItemUI = function(key){
		return ui[key];
	};

	this.getItemTab = function(key){
		var data = items[key];
		return data? data.item : null;
	}

	this.selectItem = function(key){

		if( ! ui[key]){
			log.warn("can't select unknown nav item by key: "+key);
			return;
		}

		var isNewData = false;
		var data = items[key];
		if( ! data){
			isNewData = true;
			data = {};
			data.item = ui[key];

			var controllerObj = controllers[key];
			if($.isFunction(controllerObj.getUI)){
				data.controller = controllerObj;
			} else {
				if( ! window[controllerObj]) throw new Error("TabPanel: can't find '"+controllerObj+"' class to create object");
				data.controller = new window[controllerObj]();
				data.controller.init();
			}
			items[key] = data;

		}

		if(data.item.hasClass("active")) return;
		if( ! data.controller) {
			return;
		}

		deselectItems();
		data.item.addClass("active");

		curElem = data.controller.getUI();
		if(isNewData) {
			curElem.detach();
			ui[opt.contentRoot].append(curElem);

			if(data.controller.onTabAttached) data.controller.onTabAttached();

		} else {
			curElem.show();
		}
		

	};

	this.getController = function(key){
		var data = items[key];
		return data ? data.controller : null;
	};

	function deselectItems(){
		$.each(menuItems, function(i, elem){
			elem.removeClass("active");
		});
		
		if(curElem) {
			curElem.hide();
		}
	}

	init();

};


TabPanelUtil = {

	createTabRootUI: function(rootClass, navExtraClass){
		var root = $("<div></div>");
		if(rootClass) root.addClass(rootClass);

		var nav = $("<ul bind='nav'></ul>");
		nav.addClass("nav nav-pills");
		if(navExtraClass) nav.addClass(navExtraClass);

		root.append(nav);
		root.append("<div bind='content'></div>");

		return root.createUI();
	},


	createTabItem: function(content, isHtml){

		var link = $("<a href='javascript:'></a>");

		if(isHtml) link.html(content);
		else link.text(content);

		var tabItem = $("<li></li>");
		tabItem.append(link);

		return tabItem;
	}

}