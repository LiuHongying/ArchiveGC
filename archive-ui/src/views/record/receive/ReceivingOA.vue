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
              <el-form :inline="true" @submit.native.prevent>
                <el-form-item>
                  <el-input
                    style="width: 200px"
                    v-model="inputValueNumD"
                    placeholder='请输入编码或标题'
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" plain @click="searchD()">{{
                    $t("application.SearchData")
                  }}</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" plain>提交整编</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="success" plain @click="beforeRejectDE()">驳回</el-button>
                </el-form-item>
              </el-form>
            </el-row>
            <el-row>
              <el-col :span="24">
                <DataGrid
                  ref="DeliveryDataGrid"
                  key="Delivery"
                  v-bind="tables.Delivery"
                  :tableHeight="layout.height-210"
                  @selectchange="selectDEChange"
                  @rowclick="onDataGridRowClick"
                  :pageSize="500"
                >
                </DataGrid>
              </el-col>
            </el-row>
          </template>
          <template slot="paneR">
            <el-row>
              <el-form :inline="true" @submit.native.prevent>
                <el-form-item>
                  <el-input
                    style="width: 200px"
                    v-model="inputValueNum"
                    placeholder='请输入编码或标题'
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" plain @click="search()">{{
                    $t("application.SearchData")
                  }}</el-button>
                </el-form-item>
                <!-- 接收 -->
                <el-form-item>
                    <el-button type="primary" plain @click="submit()">{{$t('application.Receive')}}</el-button>
                </el-form-item>
                <!-- 驳回 -->
                <el-form-item>
                    <el-button type="warning" plain @click="beforeReject()">驳回</el-button>
                </el-form-item>
              </el-form>
            </el-row>
            <el-row style="background-color: white;">
              <el-col :span="24">
                <DataGrid
                  ref="Drawing"
                  key="Drawing"
                  v-bind="tables.Drawing"
                  :tableHeight="layout.height-180"
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
          condition: "TYPE_NAME='移交单' AND STATUS!='整编' AND FOLDER_ID in(SELECT ID from ecm_folder where FOLDER_PATH='/移交库/OA')",
        },
        //
        Drawing: {
          gridViewName: "ArrangeGridDQXZ",
          dataUrl: "/dc/getDocuments",
          condition: "TYPE_NAME='设计文件'",
          isInitData:false,
        },
      },
      selectedDCItems: [],//文档
      selectedDEItems:[],//移交单
      inputValueNum:"",
      inputValueNumD:"",//移交单查询
      rejectComment:"",
      rejectVisible:false,
    };
  },
  mounted() {
    setTimeout(() => {
      this.leftPercent = this.getStorageNumber(this.leftStorageName,20)
    }, 300);
    this.init();
  },
  methods: {
    init(){
      let _self = this;
      _self.condition = "SELECT ID from ecm_folder where FOLDER_PATH='/移交库/OA'"
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
      var condition1 =
        "SELECT CHILD_ID from ecm_relation where NAME='irel_children'and PARENT_ID ='" +row.ID +"'";
      var key1 = "ID IN (" + condition1 + ")";
      this.$refs.Drawing.condition = key1;
      this.$refs.Drawing.gridViewName = "DrawingGridT";
      this.$refs.Drawing.loadGridInfo();
      this.$refs.Drawing.loadGridData();
    },
    //文档模糊查询
    search() {
      let _self = this;
      _self.$refs.Drawing.condition ="TYPE_NAME='设计文件' AND CODING LIKE '%"+_self.inputValueNum+"%' OR TITLE LIKE '%"+_self.inputValueNum+"%'";
      _self.tables.main.condition = _self.$refs.Drawing.condition;
      _self.$refs.Drawing.currentPage = 1;
      _self.$refs.Drawing.loadGridInfo();
      _self.$refs.Drawing.loadGridData();
    },
    //移交单文档模糊查询
    searchD() {
      let _self = this;
      _self.$refs.DeliveryDataGrid.condition ="TYPE_NAME='移交单' AND CODING LIKE '%"+_self.inputValueNumD+"%' OR C_COMMENT LIKE '%"+_self.inputValueNumD+"%'";
      _self.tables.main.condition = _self.$refs.DeliveryDataGrid.condition;
      _self.$refs.DeliveryDataGrid.currentPage = 1;
      _self.$refs.DeliveryDataGrid.loadGridInfo();
      _self.$refs.DeliveryDataGrid.loadGridData();
    },
    submit(){
      let _self=this
      if(_self.selectedDCItems.length==0){
        _self.$message({
                showClose: true,
                message: _self.$t("message.pleaseSelectDC"),
                duration: 2000,
                type: 'warning' 
            });
            return
      }
      if(_self.selectedDCItems.C_ITEM_STATUS=="已接收"||_self.selectedDCItems.C_ITEM_STATUS=="驳回"){
        _self.$message({
          showClose: true,
          message:"文件不能接收",
          duration: 2000,
          type: 'warning' 
        });
        return
      }
      else{
        let _self = this;
        var m = [];
        let tab = selectedDCItems;
        
        var i;
        for (i in tab) {
            m.push(tab[i]["ID"]);
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
      }
      
    },
    selectDCChange(val) {
      this.selectedDCItems = val;
    },
    selectDEChange(val) {
      this.selectedDEItems = val;
    },
    beforeReject(){
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
        _self.rejectVisible=true
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
          _self.$refs.Drawing.loadGridInfo();
          _self.$refs.Drawing.loadGridData();  
          _self.$message({
              showClose: true,
              message: _self.$t("message.rollbackSuccess"),
              duration: 2000,
              type: 'success'
          }); 
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
  },
  props: {},
  components: {
    DataGrid: DataGrid,
    DataLayout: DataLayout,
    RejectButton:RejectButton,
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