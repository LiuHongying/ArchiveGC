<template>
  <div>
      <el-select v-if="isBusiness" @change="onChoiceChange" v-model="Choice" >
      <el-option label="通用打印" value="通用打印"></el-option>
      <el-option label="商务文件正本" value="商务文件正本"></el-option>
      <el-option label="商务文件副本" value="商务文件副本"></el-option>
      </el-select>
    <button @click="printPage" v-print="'#print'">打印</button>
    <!-- <button @click="exportdata()" >导出所有EXCEL</button> -->
    <button @click="exportExcel()" >导出所有EXCEL</button>
     <el-container style="width:100%;height:540px;overflow:auto;">
      <div id='print' ref='print' style="height:100%;width:100%;">
         <div v-for="(item,idx) in archiveObjects" :key="'divk'+idx" :style="'width:'+divWidth+';padding:5px;'">
           <PrintVolumesGrid :ref="'innerGrid'+idx" :gridName="gridName" :archiveId="item.ID"></PrintVolumesGrid>
           <div v-if="idx < archiveObjects.length-1" style="page-break-before:always;"></div>
         </div>
      </div>
     </el-container>
  </div>
</template>

<script type="text/javascript">
import PrintVolumesGrid from '@/views/record/PrintVolumesGrid.vue'
import Print from '@/plugins/print'
import Vue from 'vue';
import ExcelUtil from "@/utils/excel.js";
Vue.use(Print)
export default {
   name: 'printArchiveInnerFile',
   components: { 
     PrintVolumesGrid:PrintVolumesGrid,
     ExcelUtil:ExcelUtil
   },
  // name: "printPage",
  data() {
    return {
      currentLanguage: "zh-cn",
      isBusiness:false,
      Choice:"通用打印",
      condition:"and IS_HIDDEN=0",
      selectedRows:[],
      condition4Export:"",
      ids:[],
      gridViewName:"ArrangeInnerGridPrint"
    };
  },
  mounted() {
    
  },
  props: {
    tableHeight:{type:Number},
    gridName:{type:String},
    archiveObjects:{type:Array,default:() => []},
  },
  methods: {
    exportExcel(){
      if(this.ids.length!=0){
        let id4Export=""      //准备进行导出工作，先把ids数组转换成字符串
        for(let i = 0;i<this.ids.length;i++){
          id4Export += "'"
          id4Export += this.ids[i]
          id4Export +="'"
          if(i!=this.ids.length-1){
            id4Export += ","
          }
        }
      if(this.Choice=="商务文件副本"){
        this.condition4Export=" C_TYPE1='副本' and is_hidden=0 and id in (select CHILD_ID AS ID from ecm_relation where PARENT_ID IN ("+id4Export+") )"
      }
      if(this.Choice=="通用打印"){
      this.condition4Export=" id in (select CHILD_ID AS ID from ecm_relation where PARENT_ID IN ("+id4Export+") ) and is_hidden=0"
      }
      if(this.Choice=="商务文件正本"){
        this.condition4Export="C_TYPE1='正本' and is_hidden=0 and id in (select CHILD_ID AS ID from ecm_relation where PARENT_ID IN ("+id4Export+") )"
      }
      }

      let dataUrl = "/exchange/doc/export";
      let params = {
        gridName: this.gridViewName,
        lang: "zh-cn",
        condition: this.condition4Export,
        filename:
          "导出文件列表总和_" + new Date().Format("yyyy-MM-dd hh:mm:ss") + ".xlsx",
        sheetname: "Result",
      };
      //console.log(params)
      ExcelUtil.export(params);
    },
    onChoiceChange(){
      let _self = this
      if(this.Choice=='商务文件正本'){
        this.condition = "and C_TYPE1='正本' and is_hidden=0"
        this.gridViewName = "BusinessFilePrintGrid"
        //this.$refs.PrintVolumesGrid.condition = "and C_TYPE1='正本'"
        this.refreshDataGrid(this.selectedRows,"BusinessFilePrintGrid")
      }
      if(this.Choice=='商务文件副本'){
        this.condition = "and C_TYPE1='副本' and is_hidden=0"
        this.gridViewName = "BusinessFilePrintGrid4Content"
        // this.$refs.PrintVolumesGrid.condition = "and C_TYPE1='副本'"
        this.refreshDataGrid(this.selectedRows,"BusinessFilePrintGrid4Content")
      }
      if(this.Choice=='通用打印'){
      this.condition = "and is_hidden=0"
      this.gridViewName = "ArrangeInnerGridPrint"
      //this.$refs.PrintVolumesGrid.condition = ''
      this.refreshDataGrid(this.selectedRows,"ArrangeInnerGridPrint")
      }
    },
    refreshDataGrid(objs,gridName){
      let _self=this;
      this.archiveObjects = objs;
      if(objs){
         setTimeout(() => {
        for(var i=0;i<objs.length;i++){
          _self.ids[i] = objs[i].ID
          console.log(_self.ids)
          _self.$refs['innerGrid'+i][0].condition = _self.condition
          _self.$refs['innerGrid'+i][0].loadArchiveData(objs[i].ID,gridName);
        }},100);
      }
    },
    printPage(){
      this.$print(this.$refs.print)
    },
  }
}
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
  .table, .table * {margin: 0 auto; padding: 0;font-size: 14px;
  font-family: Arial, 宋体, Helvetica, sans-serif;}   
.table {display: table; width: 80%; border-collapse: collapse;/*border-bottom: 1px solid gray;*/}   
  
.table-tr {display: table-row; height: 30px;}   
.table-th {display: table-cell;font-weight: bold;height: 100%;/*border: 1px solid gray;*/text-align: center;vertical-align: middle;}   
.table-td {display: table-cell; height: 100%;}   
  
.sub-table {width: 100%;height: 100%;display: table;}   
.sub-table-tr {display: table-row; height: 100%;}   
.sub-table-td {display: table-cell; height: 100%;
/* border-top: 1px solid gray; 
border-left: 1px solid gray;
border-right: 1px solid gray; */
border-top: 1px solid #020202; 
border-left: 1px solid #020202;
border-right: 1px solid #020202;
/* border:1px solid #212121; */
text-align: center;vertical-align: middle;}
.sub-table-td1 {display: table-cell; height: 100%;
/* border-left: 1px solid #212121; */
text-align: center;vertical-align: middle;}
</style>
