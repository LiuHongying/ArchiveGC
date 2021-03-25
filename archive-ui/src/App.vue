<template>
  <div id="app" v-title :data-title="$t('application.name')">
    <transition name="moveToTop">
      <router-view v-if="isRouterAlive"></router-view>
    </transition>
  </div>
</template>

<script>
import "vue-transition.css";
export default {
  name: "app",
  provide(){
		return {
			reload: this.reload
		}
	},
	data(){
		return {
			isRouterAlive:true
		}
	},
	methods:{
		reload(){
			this.isRouterAlive=false
			this.$nextTick(()=>this.isRouterAlive=true)
		}
	},
};
</script>

<style>
.el-table--striped .el-table__body tr.el-table__row--striped td {
  background: rgb(233, 233, 233);
}
.el-table--striped .el-table__body tr.el-table__row--striped.current-row td,
.el-table__body tr.current-row > td,
.el-table__body tr.hover-row.current-row > td,
.el-table__body tr.hover-row.el-table__row--striped.current-row > td,
.el-table__body tr.hover-row.el-table__row--striped > td,
.el-table__body tr.hover-row > td {
  background-color: #ebe6de !important;
}
.el-date-editor.el-input,
.el-date-editor.el-input__inner {
  width: 140px !important;
}
.el-table .cell,
.el-table th div {
  padding-right: 5px !important;
}
.el-table thead {
  color: #2c2c2c !important;
}
.el-date-editor.el-input__inner {
  padding-left: 25px !important;
}
.el-table {
  margin-left: 0px !important;
}
.el-table td {
  padding: 1px 0 !important;
}
.el-table th {
  padding: 1px 0 !important;
  background-color: #eef1f6 !important;
}
.el-form-item {
  margin-left: 5px !important;
  margin-top: 1px !important;
  margin-bottom: 1px !important;
}
.el-input__inner {
  height: 30px !important;
  border-radius: 4px !important;
  padding-left: 10px;
  vertical-align: middle  !important;
  top:0px !important;
  padding-top: 0px !important;
  /* line-height: 0  !important; */
}
.el-input__prefix {
    top: 2px !important;
}

.el-i
.el-dialog__body {
  padding: 5px 20px !important;
}
.el-dialog__header {
  padding: 10px 10px !important;
}
.el-dialog__footer {
  padding: 10px 10px !important;
  text-align: center!important;
}
.el-tree-node:focus > .el-tree-node__content {
  background-color: #92c6fd;
  font-weight: bold;
  color: #000000 !important;
}
.el-tree>.el-tree-node {
  display: inline-block;
  min-width: 100%;
}
.el-main {
  padding: 2px !important;
}
.el-header {
  padding: 5 10px !important;
  box-sizing: border-box;
}
.el-tabs__header {
  padding: 0;
  position: relative;
  margin: 0 0 2px !important;
}
.el-card {
    border: 1px solid #EBEEF5!important;
    margin: 20px 3px 0 3px;
    
}
.el-card__header {
  padding: 8px 5px !important;
  border-bottom: 1px solid #ebeef5;
  box-sizing: border-box;
}
.el-card__body {
  padding: 1px 12px 1px 12px !important;
}
.el-collapse {
  border-top: 0 solid #ebeef5 !important; 
}
.el-collapse-item__header {
  width: 100% !important;
  height: 40px !important;
  line-height: 40px !important;
  /* background-color: #ebeef5 !important; */
  /* border-bottom: 1px solid #ccc !important; */
  border-bottom: 0 solid rgb(161, 161, 161) !important;
  background-color: rgb(241, 241, 241) !important;
  font-size: 14px !important;
  font-weight: 600 !important;
}
.el-collapse-item__arrow{
  line-height: normal !important;
}

.el-collapse-item__wrap {
  border-bottom: 0px solid #EBEEF5!important;
}
.el-button {
  height: 30px !important;
  padding: 2px 12px !important;
  border-radius: 4px !important;
  vertical-align: middle  !important;
  line-height: 0  !important;
}
.gutter{
  display:block;
}

.el-table--border td, .el-table--border th, .el-table__body-wrapper .el-table--border.is-scrolling-left~.el-table__fixed {
    border-right: 1px solid #dcdcdc !important;
}

.el-button-ecm{
    height: 30px !important;
  padding: 2px 3px !important;
  border-radius: 4px !important;
  vertical-align: middle  !important;
  line-height: 0  !important;
  float:left;

}

.el-select-dropdown__item{
  width:90% !important;
}

.redlabel .el-form-item__label{
  color: #ff0000 ;
}
.el-form--inline .el-form-item {
    margin-right: 5px;
}
/* .el-message{
  background-color: #67c23acc !important;
}
.el-message .el-icon-info {
    color: #fff!important;
} */
</style>
