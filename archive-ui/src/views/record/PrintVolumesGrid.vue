<template>
  <div>
        <div class="table">
           <div class="table-tr" style="height:30px;text-align: center;color:#000000;font-size:28px">  
              卷内目录
          </div> 
          <div class="table-tr">
                       <el-table
                        :height="tableHeight"
                        :data="innerDataList"
                        border="1"
                        style="width: 100%;border:1px solid #000000">
                        <el-table-column key="#1" :label="$t('field.indexNumber')"  width="70">
                                <template slot-scope="scope">
                                    <span>{{ scope.$index+1}}</span>
                                </template>
                        </el-table-column>
                        <el-table-column width="1">
                        </el-table-column>
                        <div v-for="(citem,idx) in gridList">
                          <div v-if="citem.visibleType==1">
                            <div v-if="(citem.width+'').indexOf('%')>0">
                                <el-table-column :label="citem.label" :prop="citem.attrName" :min-width="citem.width" :sortable="citem.allowOrderby">
                                <template slot-scope="scope">
                                  <div v-if="citem.attrName.indexOf('DATE')>0">
                                    <span>{{dateFormat(scope.row[citem.attrName])}}</span>
                                  </div>
                                  <div v-else>
                                    <span>{{scope.row[citem.attrName]}}</span>
                                  </div>
                                </template>
                              </el-table-column>
                            </div>
                            <div v-else>
                              <el-table-column :label="citem.label" :width="citem.width" :prop="citem.attrName" :sortable="citem.allowOrderby">
                                <template slot-scope="scope">
                                  <div v-if="citem.attrName.indexOf('DATE')>0">
                                    <span>{{dateFormat(scope.row[citem.attrName])}}</span>
                                  </div>
                                  <div v-else>
                                    <span>{{scope.row[citem.attrName]}}</span>
                                  </div>
                                </template>
                              </el-table-column>
                            </div>
                          </div>
                        </div>
                      </el-table>
          </div>
        </div>
  </div>
</template>

<script type="text/javascript">
import Print from '@/plugins/print'
import Vue from 'vue';
import QRCode from 'qrcodejs2'// 引入qrcode
Vue.use(Print)
export default {
   name: 'test',
    
  // name: "printPage",
  data() {
    return {
      innerDataList:[],
      currentLanguage: "zh-cn",
      gridList:[],
      volumeTitle:"",
    };
  },
  mounted() {
    
  },
  props: {
    archiveId: {type:[String,Number]},
    tableHeight:{type:Number},
    gridName:{type:String}
  },
  methods: {

    // 加载表格样式
    loadGridInfo(gridName) 
    {
      let _self = this;
      _self.loading = true;
      var m = new Map();
      m.set('gridName',gridName);
      m.set('lang',_self.currentLanguage);
      axios.post("/dc/getGridViewInfo",JSON.stringify(m))
        .then(function(response) {
          _self.gridList = response.data.data;
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    loadInnerFile(){
      let _self = this;
      var m = new Map();
      m.set('id',_self.archiveId);
      _self
      .axios.post("/dc/getAllDocuByRelationParentId",JSON.stringify(m))
      .then(function(response) {
        
        _self.innerDataList = response.data.data;
        
        _self.loading = false;
      })
      .catch(function(error) {
        console.log(error);
        _self.loading = false;
      });
    },
    loadArchiveData(id,gridName){
      let _self=this;
      _self.archiveId = id;
      _self.gridName = gridName;
      _self.loadGridInfo(_self.gridName); 
      _self.loadInnerFile();
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
border-top: 1px solid #000000; 
border-left: 1px solid #000000;
border-right: 1px solid #000000;
/* border:1px solid #212121; */
text-align: center;vertical-align: middle;}
.sub-table-td1 {display: table-cell; height: 100%;
/* border-left: 1px solid #212121; */
text-align: center;vertical-align: middle;}
</style>
