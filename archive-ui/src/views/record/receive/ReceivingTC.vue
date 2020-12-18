<template>
  <DataLayout>
    <el-dialog :title="$t('application.Rejected')" :visible.sync="rejectVisible" width="50%" @close="rejectVisible=false">
      <el-input
        type="textarea"
        :rows="5"
        :placeholder="$t('message.pleaseInput')+$t('application.Rejected')+' '+$t('message.reason')"
        v-model="rejectComment">
      </el-input>
      <el-button @click="rejectSub()">{{$t('application.ok')}}</el-button>
    </el-dialog>
    <template v-slot:main="{ layout }">
      <div :style="{position:'relative',height: layout.height-startHeight+45+'px'}">
        <split-pane split="vertical" @resize="onHorizontalSplitResize" :min-percent='1' :default-percent='leftPercent'>
          <template slot="paneL">
            <el-row>
              <el-form :inline="true">
                <el-form-item>
                    <el-button type="primary" @click="submitArrange()">提交整编</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="warning" @click="beforeRejectDE()">驳回</el-button>
                </el-form-item>
              </el-form>
            </el-row>
            <el-row>
              <el-col :span="24">
                <DataGrid
                  ref="DeliveryDataGrid"
                  key="Delivery"
                  v-bind="tables.Delivery"
                  :tableHeight="layout.height-136"
                  showOptions="查看内容"
                  v-bind:isshowPage="false"
                  @selectchange="selectDEChange"
                  @rowclick="onDataGridRowClick"
                >
                </DataGrid>
              </el-col>
            </el-row>
          </template>
          <template slot="paneR">
            <el-row>
              <el-form :inline="true">
                <el-form-item>
                  <el-input
                    style="width: 200px"
                    v-model="inputValueNum"
                    placeholder='请输入编码或标题'
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="search()">{{
                    $t("application.SearchData")
                  }}</el-button>
                </el-form-item>
                <!-- 接收 -->
                <el-form-item>
                    <el-button type="success" @click="submit()">{{$t('application.Receive')}}</el-button>
                </el-form-item>
                <!-- 驳回 -->
                <el-form-item>
                    <el-button type="warning" @click="beforeRejectDC()">驳回</el-button>
                </el-form-item>
              </el-form>
            </el-row>
            <el-row>
              <el-col :span="24">
                <DataGrid
                  ref="Drawing"
                  key="Drawing"
                  v-bind="tables.Drawing"
                  :tableHeight="layout.height-166"
                  showOptions="查看内容"
                  @selectchange="selectDCChange"
                >
                <template slot="sequee" slot-scope="scope">
                  <span :style="(scope.data.row['STATUS']!=null
                  &&scope.data.row['STATUS']=='已驳回')?{'background':'red'}:''">{{(scope.currentPage-1) * scope.pageSize+ scope.data.$index+1}}</span>
                </template>
                </DataGrid>
              </el-col>
            </el-row>
          </template>
        </split-pane>
      </div>
    </template>
  </DataLayout>
</template>
<script type="text/javascript">
import ShowProperty from "@/components/ShowProperty";
import DataGrid from "@/components/DataGrid";
import DataLayout from "@/components/ecm-data-layout";
import RejectButton from "@/components/RejectButton";

export default {
  name: "TC",
  data() {
    return {
      startHeight: 135,
      leftStorageName: 'ReceivingTCWidth',
      leftPercent: 20,
      tables: {
        //移交单
        Delivery: {
          gridViewName: "DeliveryGrid",
          dataUrl: "/dc/getDocuments",
          condition: "TYPE_NAME='移交单'",
          isInitData:false,
          isshowCustom:false,
          isEditProperty:false,
          isShowChangeList:false,
          isshowOption:true,
          isshowSelection:true,
        },
        //设计文件
        Drawing: {
          gridViewName: "DrawingGrid",
          dataUrl: "/dc/getDocuments",
          condition: "TYPE_NAME='设计文件'",
          isInitData:false,
          isshowCustom:false,
          isEditProperty:false,
          isShowChangeList:false,
          isshowOption:true,
          isshowSelection:true,
        },
      },
      selectedDCItems: [],//文档
      selectedDEItems:[],//移交单
      inputValueNum:"",
      rejectComment:"",
      rejectVisible:false,
      condition:"",
      condition1:"",
    };
  },
  mounted() {
    if (!this.validataPermission()) {
      //跳转至权限提醒页
      let _self = this;
      _self.$nextTick(() => {
        _self.$router.push({ path: "/NoPermission" });
      });
    }
    setTimeout(() => {
      this.leftPercent = this.getStorageNumber(this.leftStorageName,20)
    }, 300);
    this.init()
  },
  methods: {
    init(){
      let _self = this;
      _self.condition = "SELECT ID from ecm_folder where FOLDER_PATH='/移交库/TC'"
      _self.$refs.DeliveryDataGrid.condition ="TYPE_NAME='移交单' AND FOLDER_ID IN("+_self.condition+") AND STATUS <>'整编'"
      _self.tables.Delivery.condition = _self.$refs.DeliveryDataGrid.condition;
      _self.$refs.DeliveryDataGrid.currentPage = 1;
      _self.$refs.DeliveryDataGrid.loadGridInfo();
      _self.$refs.DeliveryDataGrid.loadGridData();
    },
    // 水平分屏事件
    onHorizontalSplitResize(leftPercent){
      // 左边百分比*100
      this.leftPercent = leftPercent
      this.setStorageNumber(this.leftStorageName, leftPercent)
    },
    //单击行
    onDataGridRowClick: function (row) {
      let key1 =
        "SELECT CHILD_ID from ecm_relation where NAME='irel_children'and PARENT_ID ='" +row.ID +"'";
      this.condition1 = "ID IN (" + key1 + ")";
      this.$refs.Drawing.condition = this.condition1;
      this.$refs.Drawing.gridViewName = "DrawingGrid";
      this.$refs.Drawing.loadGridInfo();
      this.$refs.Drawing.loadGridData();
    },
    //文档模糊查询
    search() {
      let _self = this;
      _self.$refs.Drawing.condition ="TYPE_NAME='设计文件' AND (CODING LIKE '%"+_self.inputValueNum+"%' OR TITLE LIKE '%"+_self.inputValueNum+"%') and (" + _self.condition1 + ") ";
      _self.tables.Drawing.condition = _self.$refs.Drawing.condition;
      _self.$refs.Drawing.currentPage = 1;
      _self.$refs.Drawing.loadGridInfo();
      _self.$refs.Drawing.loadGridData();
    },
    //文档接收
    submit(){
      let _self=this
      if(_self.selectedDCItems.length==0){
        _self.$message({
                showClose: true,
                message:  "请选择一个文档",
                duration: 2000,
                type: 'warning' 
            });
            return
      }
      var m = [];
      let tab = _self.selectedDCItems;
      
      var i;
      for (i in tab) {
          if(tab[i]["STATUS"]=="已驳回"){
             _self.$message({
            showClose: true,
            message:"文件"+tab[i]["CODING"]+"为驳回文件不能接收",
            duration: 2000,
            type: 'warning' 
          });
            return
          }
          else if(tab[i]["STATUS"]=="已接收"){
             _self.$message({
            showClose: true,
            message:"文件"+tab[i]["CODING"]+"已接收",
            duration: 2000,
            type: 'warning' 
          });
            return
          }
          else{
              m.push(tab[i]["ID"]);
          }
      }
      let mp=new Map();
      mp.set("ids",m);
      axios.post("/TC/Receivingdc",JSON.stringify(mp),{
          headers: {
              "Content-Type": "application/json;charset=UTF-8"
          }
      })
      .then(function(response){
        if(response.data.code==1){
          _self.$refs.Drawing.loadGridInfo();
          _self.$refs.Drawing.loadGridData();
          _self.$message({
              showClose: true,
              message: "接收成功",
              duration: 2000,
              type: 'success'
          });
        }else{
              
          _self.$message({
              showClose: true,
              message: response.data.message,//_self.$t("message.operationFaild"),
              duration: 5000,
              type: 'error'
          });
        }
          
      })
      .catch(function(error) {
        _self.$message({
          showClose: true,
          message: _self.$t("message.operationFaild"),
          duration: 5000,
          type: 'error'
        });
        console.log(error);
      });
    },
    selectDCChange(val) {
      this.selectedDCItems = val;
    },
    selectDEChange(val) {
      this.selectedDEItems = val;
    },
    beforeRejectDC(){
      let _self = this;
      if(_self.selectedDCItems.length==0){
        _self.$message({
                showClose: true,
                message: "请选择一个文档",
                duration: 2000,
                type: 'warning' 
            });
            return
      }
      else{
        _self.selectedDCItems.forEach(function(item){
          if(item.STATUS=='已驳回'){
            _self.$message({
                showClose: true,
                message: "不能重复驳回",
                duration: 2000,
                type: 'warning' 
            });
            return
          }else{
            _self.rejectVisible=true
          }
        })
        
      }
    },
    beforeRejectDE(){
      let _self = this;
      if(_self.selectedDCItems.length==0){
        _self.$message({
                showClose: true,
                message:"请选择一条移交单",
                duration: 2000,
                type: 'warning' 
            });
            return
      }
      else{
        _self.rejectVisible=true
      }
    },
    rejectSub(){
      let _self = this;
      var m = [];
      let tab = _self.selectedDCItems;
      var i;
      for (i in tab) {
        m.push(tab[i]["ID"]);
      }
      let mp=new Map();
      mp.set("ids",m);
      mp.set("rejectCommon",_self.rejectComment);
      axios.post("/TC/Rejectdc",JSON.stringify(mp),{
        headers: {
            "Content-Type": "application/json;charset=UTF-8"
        }
      })
      .then(function(response) {
        if(response.data.code==1){
          _self.$refs.Drawing.condition=_self.condition1
          _self.$refs.Drawing.loadGridInfo();
          _self.$refs.Drawing.loadGridData();  
          _self.$message({
              showClose: true,
              message: _self.$t("message.rollbackSuccess"),
              duration: 2000,
              type: 'success'
          });
          _self.rejectVisible=false 
        }else{
          _self.$message({
            showClose: true,
            message: _self.$t("message.operationFaild"),
            duration: 5000,
            type: 'error'
          });
        }
      })
      .catch(function(error) {
        _self.$message({
          showClose: true,
          message: _self.$t("message.operationFaild"),
          duration: 5000,
          type: 'error'
        });
        console.log(error);
      });
    },
    //提交整编
    submitArrange(){
      let _self=this
      if(_self.selectedDEItems.length==0){
        _self.$message({
                showClose: true,
                message:  "请选择一个移交单",
                duration: 2000,
                type: 'warning' 
            });
            return
      }
      var m = [];
      let tab = _self.selectedDEItems;
      var i;
      for (i in tab) {
        m.push(tab[i]["ID"]);
      }
      let mp=new Map();
      mp.set("ids",m);
      axios.post("/TC/Arrangedc",JSON.stringify(mp),{
          headers: {
              "Content-Type": "application/json;charset=UTF-8"
          }
      })
      .then(function(response){
        if(response.data.code==1){
          _self.init()
          _self.$message({
              showClose: true,
              message: "整编成功",
              duration: 2000,
              type: 'success'
          });
          _self.$refs.Drawing.itemDataList=[];
          var j;
          for (j in tab) {
            writeAudit(tab[j]["ID"]);
          }
        }else{
              
          _self.$message({
              showClose: true,
              message: response.data.message,//_self.$t("message.operationFaild"),
              duration: 5000,
              type: 'error'
          });
        }
          
      })
      .catch(function(error) {
        _self.$message({
          showClose: true,
          message: _self.$t("message.operationFaild"),
          duration: 5000,
          type: 'error'
        });
        console.log(error);
      });
      
    },

    writeAudit(docId){
      var m = new Map();
      m.set("docId",docId)
      m.set("actionName","ecm_read")
      m.set("appName","portal")
      axios
        .post("/audit/addAudit", JSON.stringify(m))
        .then(function(response){
          
        })
    },
  },
  props: {},
  components: {
    DataGrid: DataGrid,
    DataLayout: DataLayout,
    RejectButton:RejectButton,
    ShowProperty: ShowProperty,
  },
};
</script>
<style scoped>
.el-form-item {
  margin-bottom: 0px;
}
.el-table td,
.el-table th {
  text-align: center !important;
}
</style>