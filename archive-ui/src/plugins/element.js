import Vue from 'vue'
import Element from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import i18n from '@/assets/locales'

Vue.use(Element,{
	i18n:(key,value)=>i18n.t(key,value)
})
Vue.prototype.onHeaderDragend = function(newWidth,oldWidth,column,event){
	if(oldWidth<120&&newWidth<oldWidth){
	  column.width = oldWidth;
	  return true;
	}
	if(newWidth<120){
	  column.width = 120;
	  return false;
	}
	column.width = newWidth;
	return true;
  }