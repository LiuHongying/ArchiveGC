<template>
  <div>
    <button @click="printPage" v-print="'#print'">打印</button>
    <el-container style="width:100%;height:540px;overflow:auto;">
      <div id='print' ref='print' style="height:100%;width:100%;">
         <div v-for="(item,idx) in archiveObjects" :key="'divk'+idx" :style="'width:'+divWidth+';padding:5px;'">
           <PreparationTableForm :ref="'printForm'+idx" :archiveId="item.ID"></PreparationTableForm>
           <div v-if="idx < archiveObjects.length-1" style="page-break-before:always;"></div>
         </div>
      </div>
     </el-container>
  </div>
</template>

<script type="text/javascript">
import Print from "@/plugins/print";
import PreparationTableForm from '@/views/record/PreparationTableForm.vue'
import Vue from "vue";
Vue.use(Print);
export default {
  name: "PrintPreparationTable",
  components: { 
     PreparationTableForm:PreparationTableForm
  },
  data() {
    return {
      archiveObjects: [],
      currentLanguage: "zh-cn",
    };
  },
  mounted() {
  
  },
  methods: {
    refreshArchiveObj(objs){
      let _self=this;
      this.archiveObjects = objs;
      if(objs){
         setTimeout(() => {
        for(var i=0;i<objs.length;i++){
          _self.$refs['printForm'+i][0].loadArchiveData(objs[i].ID);
        }},100);
      }
    },
    printPage(){
      this.$print(this.$refs.print)
    },
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1,
h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
/* .table-a table{border:1px solid #212121}  */
.v-auto-out .auto-in {
  position: absolute;
  top: 50%;
  border: 1px dashed #ddd;
  /* 这里有兼容性问题 */
  -webkit-transform: translateY(-50%);
  -ms-transform: translateY(-50%);
  -o-transform: translateY(-50%);
  transform: translateY(-50%);
}

.bg-purple-dark {
  background: #99a9bf;
}
.bg-purple {
  background: #d3dce6;
}
.bg-purple-light {
  background: #e5e9f2;
}
.grid-content {
  border-radius: 4px;
  min-height: 36px;
}
.row-bg {
  padding: 10px 0;
  background-color: #f9fafc;
}
/* 样式 */
.table,
.table * {
  margin: 0 auto;
  padding: 0;
  font-size: 14px;
  font-family: Arial, 宋体, Helvetica, sans-serif;
}
.table {
  display: table;
  width: 80%;
  border-collapse: collapse; /*border-bottom: 1px solid gray;*/
}

.table-tr {
  display: table-row;
  height: 30px;
}
.table-th {
  display: table-cell;
  font-weight: bold;
  height: 100%; /*border: 1px solid gray;*/
  text-align: center;
  vertical-align: middle;
}
.table-td {
  display: table-cell;
  height: 100%;
}

.sub-table {
  width: 100%;
  height: 100%;
  border-collapse: collapse;
  display: table;
}
.sub-table-tr {
  display: table-row;
  height: 100%;
}
.sub-table-td {
  display: table-cell;
  height: 100%;
  /* border-top: 1px solid gray; 
border-left: 1px solid gray;
border-right: 1px solid gray; */
  border-top: 1px solid #212121;
  border-left: 1px solid #212121;
  border-right: 1px solid #212121;
  /* border:1px solid #212121; */
  text-align: center;
  vertical-align: middle;
}
.sub-table-td1 {
  display: table-cell;
  height: 100%;
  /* border-left: 1px solid #212121; */
  text-align: center;
  vertical-align: middle;
}
.sub-table-tdNull {
  display: table-cell;
  height: 35px;
  /* border-left: 1px solid #212121; */
  text-align: center;
  border-right: 1px solid #212121;
  border-top: 1px solid #212121;
  vertical-align: middle;
}
</style>
