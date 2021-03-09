<template>
  <div>
        <div class="table">
           <div class="table-tr" style="height:30px;text-align: center;color:#000000;font-size:28px">  
              卷内目录
          </div>

        <button  @click="exportExcel4Print(names)" class="no-print" >导出文件内清单</button>
          <div class="table-tr">
                       <el-table
                       ref='report-table'
                        :height="tableHeight"
                        :data="innerDataList"
                        border="1"
                        style="width: 100%;border:1px solid #000000" row-class-name="cellClassName">
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
import XLSX from 'xlsx'
import FileSaver from 'file-saver'
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
      condition:"",
      names:"文件清单"
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
    cellClassName({ row, rowIndex}) {
      return "rowclass";
    },
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
      m.set('condition',_self.condition)
      _self
      .axios.post("/dc/getAllDocusByRelationParentId",JSON.stringify(m))
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
      exportExcel4Print(excelName) {
       //excelName --设置导出的excel名称
       //report-table --对应的要导出的el-table的ref名称
      try {
        const $e = this.$refs['report-table'].$el;
        // 如果表格加了fixed属性，则导出的文件会生产两份一样的数据，所以可在这里判断一下
        let $table = $e.querySelector('.el-table__fixed');
        if (!$table) {
          $table = $e;
        }
        // 为了返回单元格原始字符串，设置{ raw: true }
        const wb = XLSX.utils.table_to_book($table, { raw: true });
        const wbout = XLSX.write(wb, { bookType: 'xlsx', bookSST: true, type: 'array' });
        FileSaver.saveAs(
          new Blob([wbout], { type: 'application/octet-stream' }),
          `${excelName}.xlsx`,
        );
      } catch (e) {
        if (typeof console !== 'undefined') console.error(e);
      }
 },
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.el-table td, .el-table th.is-leaf {
    border-bottom: 1px solid #000000 !important;
}
.rowclass{
   border-bottom: 1px solid #000000 !important;
}
</style>