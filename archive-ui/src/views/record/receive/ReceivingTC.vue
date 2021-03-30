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
    <el-dialog title="四性检查" :visible.sync="Check4Visible" width="80%" @close="Check4Visible=false">
      <div height="200">
        <Check4 :checkData="selectedDCItems" @close="Check4Visible = false"></Check4>
      </div>
    </el-dialog>
    
    <!-- 批量修改 -->
    <el-dialog :visible.sync="modifyVisible" style="width:80%">
        <el-row style="padding:15px">
        <span>选择修改字段</span>
        <el-select v-model="resChoice" @change="onModifyChange" >
          <div v-for="item in objectSrc" :key="item.id">
              <el-option :label="item.label" :value="item.id"></el-option>
          </div>
          </el-select>
        </el-row>
        <el-row style="padding:15px">
        <span>选择操作类型</span>
        <el-select @change="onChoiceChange" v-model="Choice" >
          <div v-for="items in modifyOption" :key="items">
              <el-option :label="items" :value="items"></el-option>
          </div>
          </el-select>
        </el-row>
        <el-row v-if="isMF" style="padding:15px">
          <span >输入部分替换内容</span>
          <el-input style="width:220px" v-model="MFinput"></el-input>
        </el-row>
        <el-row style="padding:15px">
          <span>输入修改内容</span>
          <el-input style="width:220px" v-model="ChoiceInput"></el-input>
        </el-row>
        <el-row style="padding:15px;padding-left:200px">
          <el-button @click="submitModify" type='primary' style="padding-left:200px">提交修改</el-button>
          <el-button @click="close">取消</el-button>
        </el-row>
    </el-dialog>
    
    <template v-slot:main="{ layout }">
      <div :style="{position:'relative',height: layout.height-startHeight+45+'px'}">
        <split-pane split="vertical" @resize="onHorizontalSplitResize" :min-percent='1' :default-percent='leftPercent'>
          <template slot="paneL">
            <el-row>
              <el-form :inline="true" @submit.native.prevent>
                <el-form-item>
                  <el-input
                    style="width: 160px"
                    v-model="inputValueProject"
                    placeholder='输入项目号或项目名'
                    @change="searchProject()"
                  ></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" plain @click="submitArrange()">提交整编</el-button>
                </el-form-item>
              </el-form>
            </el-row>
            <el-row>
              <el-col :span="24">
                <DataGrid
                  ref="DeliveryDataGrid"
                  key="Delivery"
                  v-bind="tables.Delivery"
                  :tableHeight="layout.height-165"
                  showOptions="查看内容"
                  :showLeftOpenButton="false"
                  @selectchange="selectDEChange"
                  @rowclick="onDataGridRowClick"
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
                    <el-button type="warning" plain @click="beforeRejectDC()">驳回</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" plain @click="beforeCheck()">四性检查</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="beforeModify()" plain >批量修改</el-button>
                </el-form-item>
              </el-form>
            </el-row>
            <el-row style="background:#FFFFFF;">
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
                  &&scope.data.row['STATUS']=='驳回')?{'background':'red'}:''">{{(scope.currentPage-1) * scope.pageSize+ scope.data.$index+1}}</span>
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
import Check4 from "@/views/record/Check4.vue"

export default {
  name: "TC",
  data() {
    return {
      isInnerModify:false,
      isModify:false,
      modifyVisible:false,
      isMF:false,
      MFinput:"",
      resChoice:'',
      selectedItems: [],
      modifyOption:[],
      ChoiceInput:'',
      Choice:'',
      hoiceTypeName:'',
      startHeight: 135,
      leftStorageName: 'ReceivingTCWidth',
      leftPercent: 26,
      tables: {
        //移交单
        Delivery: {
          gridViewName: "DeliveryGridTC",
          dataUrl: "/dc/getDocuments",
          condition: "TYPE_NAME='移交单'",
          isInitData:false,
          isshowCustom:false,
          isEditProperty:false,
          isShowChangeList:false,
          isshowOption:true,
          optionWidth:2,
          isshowSelection:true,
        },
        //设计文件
        Drawing: {
          gridViewName: "DrawingGrid",
          dataUrl: "/dc/getDocuments",
          isInitData:false,
          isshowCustom:false,
          isEditProperty:true,
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
      Check4Visible:false,
      currentDeliver:null,
      inputValueProject:""
    };
  },
  mounted() {
    setTimeout(() => {
      this.leftPercent = this.getStorageNumber(this.leftStorageName,20)
    }, 300);
    this.init()
  },
  methods: {
    init(){
      let _self = this;
      _self.currentDeliver = null;
      _self.condition = "SELECT ID from ecm_folder where FOLDER_PATH='/移交库/TC'"
      _self.$refs.DeliveryDataGrid.condition ="TYPE_NAME='移交单' AND FOLDER_ID IN("+_self.condition+") AND STATUS <>'整编'"
      _self.tables.Delivery.condition = _self.$refs.DeliveryDataGrid.condition;
      _self.$refs.DeliveryDataGrid.currentPage = 1;
      _self.$refs.DeliveryDataGrid.loadGridInfo();
      _self.$refs.DeliveryDataGrid.loadGridData();
    },
    searchProject(){
      let _self = this;
      _self.currentDeliver = null;
      _self.condition = "SELECT ID from ecm_folder where FOLDER_PATH='/移交库/TC'"
      let cond = "TYPE_NAME='移交单' AND FOLDER_ID IN("+_self.condition+") AND STATUS <>'整编'";
      if(_self.inputValueProject!=''){
        cond += " AND (C_PROJECT_NUM like '%"+_self.inputValueProject+"%'  OR C_PROJECT_NAME like '%"+_self.inputValueProject+"%')";
      }
       _self.$refs.DeliveryDataGrid.condition = cond;
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
      this.currentDeliver = row;
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
      _self.$refs.Drawing.condition ="(CODING LIKE '%"+_self.inputValueNum+"%' OR TITLE LIKE '%"+_self.inputValueNum+"%') and (" + _self.condition1 + ") ";
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
          if(tab[i]["STATUS"]=="驳回"){
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
          _self.onDataGridRowClick(_self.currentDeliver);
          // _self.$refs.Drawing.loadGridInfo();
          // _self.$refs.Drawing.loadGridData();
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
      if(val && val[0] && val[0].TYPE_NAME){
        this.ChoiceTypeName = val[0].TYPE_NAME
        this.getTypeNamesByMainList(this.ChoiceTypeName)
        this.selectedItems = val;
      }
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
          if(item.STATUS=='驳回'){
            _self.$message({
                showClose: true,
                message: "不能重复驳回",
                duration: 3000,
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
            message: _self.$t("message.operationFaild")+":"+ response.data.message,
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
          _self.init();
          _self.$message({
              showClose: true,
              message: "整编成功",
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

    writeAudit(docId){
      var m = new Map();
      m.set("docId",docId)
      m.set("actionName","接收")
      m.set("appName","portal")
      axios
        .post("/audit/addAudit", JSON.stringify(m))
        .then(function(response){
          
        })
    },
    close(){
      this.modifyVisible = false
    },
    beforeModify(){
      this.isModify = true
      this.isInnerModify = false
      if(this.selectedItems.length==0){
        this.$message("请选择至少一条文件进行修改！")
        return
      }
      this.modifyVisible = true
      this.getTypeNamesByMainList(this.ChoiceTypeName)
    },
    onModifyChange(id){
      let _self = this
      _self.objectSrc.forEach(item => {
        if(item.id==id) {        //找到对应字段了
        if (item.controlType=='ValueSelect'||item.controlType=='Date'||item.controlType=='Select'){
          _self.modifyOption=['全部替换']
          return
        }else if(item.controlType=='TextBox'||item.controlType=='TextArea'){
          _self.modifyOption=['加前缀','加后缀','全部替换','部分替换']
        }
      }                 
      })
    },
    onChoiceChange(){
      if(this.Choice=='部分替换'){
      this.isMF = true}
      if(this.Choice!='部分替换'){
        this.isMF = false
      }
    },
    submitModify(){
      let _self = this
          _self.$confirm(
      "确认要进行批量修改?",
      {
        confirmButtonText: _self.$t("application.ok"),
        cancelButtonText: _self.$t("application.cancel"),
        type: "warning"
      })
      .then(() => {
      let ids = []
      let attr=''
      if(this.isModify==true){
      for(let i=0;i<this.selectedItems.length;i++){
        ids.push(this.selectedItems[i].ID)
      }}
      if(this.isInnerModify==true){
        for(let i=0;i<this.selectedInnerItems.length;i++){
        ids.push(this.selectedInnerItems[i].ID)
      }
      }
      _self.objectSrc.forEach(item => {
        if(item.id==_self.resChoice) {        //找到对应字段了
        attr = item.attrName
        if(item.controlType=='Date'){
        _self.isDates = true
        }
        }                 
      })
      var m = new Map();
      m.set("ids",ids)
      m.set("modifyType",this.Choice)
      m.set("attr",attr)
      m.set("MFinput",_self.MFinput)
      if(this.isDates==true){
         var r=this.ChoiceInput.match(/^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2})$/); 
          if(r==null){
            this.$alert("请输入格式正确的日期\n\r日期格式：yyyy-mm-dd\n\r例    如：2021-01-01\n\r");
             return false;
          }
          var d=new Date(r[1],r[3]-1,r[4]);   
          var num = (d.getFullYear()==r[1]&&(d.getMonth()+1)==r[3]&&d.getDate()==r[4]);
          if(num==0){
           this.$alert("请输入格式正确的日期\n\r日期格式：yyyy-mm-dd\n\r例    如：2021-01-01\n\r");
          return
          }
          
      }
      m.set("modifyResult",this.ChoiceInput)
      let formdata = new FormData();
      formdata.append("metaData",JSON.stringify(m));
        axios
        .post("/exchange/doc/modifty", formdata)
        .then(function(response) {
        if(response.data.code==1){
          _self.$message("修改成功！")
          _self.modifyVisible=false
          _self.$refs.Drawing.loadGridData()
          _self.resChoice = ""
          _self.Choice = ""
          _self.MFinput = ""
          _self.isMF = false
          _self.ChoiceInput = ""
        }
        })
        .catch(function(error) {
          console.log(error);
        });
      })
    },
    getTypeNamesByMainList(keyName) {
      let _self = this;
      var m = new Map();
      let j = 0
      m.set('itemInfo',keyName);//ID 或类型
      m.set('formName',keyName);
      m.set('lang',_self.getLang());
      axios
        .post("/dc/getFormClassifications", JSON.stringify(m))
        .then(function(response) {
          let sourcedata = response.data.data
          _self.objectSrc=[]
          sourcedata.forEach(items =>{
            items.ecmFormItems.forEach( item => {
              _self.objectSrc.push(item)
            })
          })
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    beforeCheck(){
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
        _self.Check4Visible=true
      }
    }
  },
  props: {},
  components: {
    DataGrid: DataGrid,
    DataLayout: DataLayout,
    RejectButton:RejectButton,
    ShowProperty: ShowProperty,
    Check4:Check4,
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