<template>
  <DataLayout>
    <template v-slot:header>
    <!-- 新建档案鉴定 -->
      <el-dialog
        title="档案鉴定"
        :visible.sync="propertyVisible"
        @close="propertyVisible = false"
        width="50%"
        :close-on-click-modal="false"
        v-dialogDrag
        >
        <el-input
          type="textarea"
          :rows="2"
          placeholder="请输入原因说明"
          v-model="comment">
        </el-input>
        <div slot="footer" class="dialog-footer">
            <el-button  v-on:click="saveItem" :loading="butt">{{$t('application.save')}}</el-button>
            <el-button @click="propertyVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
      <!-- 相关文件创建文档选择-->
      <el-dialog title="选择文档" :visible.sync="propertyrela" width="80%" :close-on-click-modal="false" :before-close="handleClose()"> 
        <DataLayout>
          <template v-slot:header>
            <el-row>
              <el-col :span="24">
                <el-select v-model="ArchivalType" @change="onSelectChange">
                  <el-option label="所有类目" value="all"></el-option>
                  <el-option v-for="item in options" :key="item[valueField]" v-bind="item"></el-option>
                </el-select>
                <el-input style="width:200px" v-model="DCinputValueNum" placeholder="请输入编码或标题"></el-input>
                <el-button type="primary" @click="searchDC()">{{$t('application.SearchData')}}</el-button>
                <el-select v-model="DCtype" :placeholder="DCtype.value" v-on:change="changeType()">
                  <el-option
                    v-for="item in DCoptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.label"
                  ></el-option>
                </el-select>
                <el-button type="primary" @click="newRelation()">添加</el-button>
              </el-col>
            </el-row>  
            </template>
          <template v-slot:main>  
            <DataGrid 
              ref="chooseFile"
              key="General"
              v-bind:dataUrl="dataUrl"
              v-bind:tableHeight="360"
              v-bind:condition="condition"
              v-bind:isshowOption="true" v-bind:isshowSelection ="true"
              gridViewName="AppraisalFileGrid"
              :isInitData="false"
              :optionWidth = "2"
              :isshowCustom="false"
              :isEditProperty="true"
              showOptions="查看内容"
              :isShowChangeList="false"
              @selectchange="selectedChooseDC">
            </DataGrid>
          </template>
        </DataLayout>
      </el-dialog>
       <el-dialog
      title="档案鉴定流程"
      :visible.sync="flowVisible"
      @close="flowVisible = false"
      width="90%"
      style="width: 100%"
      :close-on-click-modal="false"
      v-dialogDrag
    >
      <div><AppraisalStartUp :workflowObj="workflow" :showUploadFile="true" :parentId="parentID" :workflowFileList="selectedThItems" @close="flowVisible = false"></AppraisalStartUp></div>
    </el-dialog>
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
        <el-form-item>
          <el-select v-model="value" placeholder="新建" v-on:change="changeStatus(val)">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.label"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" @click="propertyVisible=true">新建</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary"  @click="getWorkFlow">发起流程</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <split-pane v-on:resize="onSplitResize" :min-percent='20' :default-percent='topPercent' split="horizontal">
          <template slot="paneL">
                <DataGrid
                  ref="ArchiveAppraisal"
                  key="ArchiveAppraisal"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="ArchiveAppraisalGrid"
                  :isInitData="false"
                  condition="TYPE_NAME='档案鉴定单' and STATUS='新建'"
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isEditProperty="true"
                  showOptions="查看内容"
                  :isShowChangeList="false"
                  @selectchange="selectThChange"
                  @rowclick="onDataGridRowClick"
                >
                </DataGrid>
          </template>
          <template slot="paneR">
            <el-row>
              <el-col :span="24">
              <el-form :inline="true">
                <el-form-item>
                    <el-button type="primary" @click="beforeAdd()">添加</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="warning" @click="DelRelation" >移除</el-button>
                </el-form-item>
              </el-form>
              </el-col>
            </el-row>
            <DataGrid
              ref="AppraisalFile"
              key="AppraisalFile"
              dataUrl="/dc/getDocuments"
              v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
              v-bind:isshowOption="true" v-bind:isshowSelection ="true"
              gridViewName="AppraisalFileGrid"
              :isInitData="false"
              :optionWidth = "2"
              :isshowCustom="false"
              :isEditProperty="false"
              showOptions="查看内容"
              :isShowChangeList="false"
              @selectchange="selectDCChange"
            >
            </DataGrid>
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
import DataSelect from "@/components/ecm-data-select";
import AppraisalStartUp from "@/views/workflow/AppraisalStartUp.vue"

export default {
  name: "TC",
  data() {
    return {
      // 本地存储高度名称
      topStorageName: 'ReceivingDC4CnpeHeight',
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 55,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 80,
      selectedDCItems: [],//文档
      selectedThItems:[],//专题
      inputValueNum:"",
      options: [
        {
          value: "新建",
          label: "新建",
        },
        {
          value: "流程中",
          label: "流程中",
        },
        {
          value: "已完成",
          label: "已完成",
        },
      ],
      DCoptions: [
        {
          value: "all",
          label: "所有文档",
        },
        {
          value: "deline",
          label: "到期文档",
        },
      ],
      value:"新建",
      typeName:"专题",
      comment:"",
      propertyVisible:false,   //新建窗口
      DCinputValueNum:"",
      DCtype:"所有文档",
      propertyrela:false,
      ArchivalType:"all",
      condition:"C_INCLUDE_PAPER='是' and IS_RELEASED=1 AND IS_CHILD=0 AND IS_CURRENT=1",
      dataUrl:"/dc/getDocuments",
      conditionFile:"",
      selectedChooseDCItems:"",
      options:[],
      valueField:"",
      dataTextField:"",
      allType:"select NAME from ecm_document where TYPE_NAME='配置项' and SUB_TYPE='档案类目'",
      flowVisible:false,
      workflow:{},
      selectedDCItems:[],
      parentID:""
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
    this.search()
  },
  methods: {
    // 上下分屏事件
    onSplitResize(topPercent){
      // 顶部百分比*100
      this.topPercent = topPercent
      this.setStorageNumber(this.topStorageName, topPercent)
      //console.log(JSON.stringify(topPercent))
    },
    //单击行
    onDataGridRowClick: function (row) {
      this.parentID=row.ID
      var condition1 =
        "SELECT CHILD_ID from ecm_relation where NAME='irel_children'and PARENT_ID ='" +row.ID +"'";
      var key1 = "ID IN (" + condition1 + ") AND IS_RELEASED=1";
      this.$refs.AppraisalFile.condition = key1;
      this.$refs.AppraisalFile.loadGridInfo();
      this.$refs.AppraisalFile.loadGridData();
    },
    //文档模糊查询
    search() {
      let _self = this;
      let key="TYPE_NAME='档案鉴定单' and IS_RELEASED=1";
      if(_self.inputValueNum!=''&&_self.inputValueNum!=undefined){
        key+="and (CODING LIKE '%"+_self.inputValueNum+"%' OR TITLE LIKE '%"+_self.inputValueNum+"%')";
      }
      if(_self.value!=''&&_self.value!=undefined){
        key+=" and STATUS = '"+_self.value+"'";
      }
      if(_self.startDate!=''&&_self.startDate!=undefined){
          key+=" and CREATION_DATE > '"+_self.startDate+"'";
      }
      if(_self.endDate!=''&&_self.endDate!=undefined){
          key+=" and CREATION_DATE < '"+_self.endDate+"'";
      }
      _self.$refs.ArchiveAppraisal.condition=key;
      _self.$refs.ArchiveAppraisal.currentPage = 1;
      _self.$refs.ArchiveAppraisal.loadGridInfo();
      _self.$refs.ArchiveAppraisal.loadGridData();
    },
    selectDCChange(val) {
      this.selectedDCItems = val;
    },
    selectThChange(val) {
      this.selectedThItems = val;
    },
    selectedChooseDC(val) {
      this.selectedChooseDCItems = val;
    },
    changeStatus:function(){
      this.search()
    },
    changeType:function(){
      var nowDate = new Date().getTime();
      if(this.DCtype=="到期文档"){
        this.dataUrl="/dc/getDocuments4DC"
        this.condition="C_INCLUDE_PAPER='是' and IS_RELEASED=1"+this.conditionFile
      }else{
        this.dataUrl="/dc/getDocuments"
        this.condition="C_INCLUDE_PAPER='是' and IS_RELEASED=1 AND IS_CHILD=0 AND IS_CURRENT=1"+this.conditionFile
      }
      setTimeout(() => {
        this.searchDC()
      }, 10);
      
    },
    // 保存档案鉴定
    saveItem() {
      let _self = this;
      var m = new Map();
      m.set("TYPE_NAME", "档案鉴定单");
      m.set("C_COMMENT",_self.comment)
      let formdata = new FormData();
      formdata.append("metaData", JSON.stringify(m));
      axios
        .post("/dc/newArchiveAppraisal", formdata, {
          "Content-Type": "multipart/form-data",
        })
        .then(function (response) {
          let code = response.data.code;
          if (code == 1) {
            _self.$message({
              showClose: true,
              message: _self.$t("message.newSuccess"),
              duration: 2000,
              type: "success",
            });
            _self.propertyVisible = false;
            _self.$refs.ArchiveAppraisal.loadGridData();
            _self.$refs.AppraisalFile.itemDataList=[];
          } else {
            _self.$message({
              showClose: true,
              message: _self.$t("message.newFailured"),
              duration: 2000,
              type: "warning",
            });
          }
        })
        .catch(function (error) {
          _self.$message(_self.$t("message.newFailured"));
          console.log(error);
        });
    },
    //删除关系
    DelRelation(){
      let _self = this;
      if(_self.selectedDCItems==''||_self.selectedDCItems==undefined){
        _self.$message({
            showClose: true,
            message:"请选择一个文档",
            duration: 2000,
            type: "warning",
          });
        return
      }
      var m = [];
      let tab = _self.selectedDCItems;
      var i;
      for (i in tab) {
        m.push(tab[i]["ID"]);
      }
      axios.post("/dc/Archive/DelDcRelation",JSON.stringify(m),{
        headers: {
            "Content-Type": "application/json;charset=UTF-8"
        }
      })
      .then(function (response) {
        let code = response.data.code;
        if (code == 1) {
          _self.$message({
            showClose: true,
            message:"删除成功",
            duration: 2000,
            type: "success",
          });
          _self.$refs.AppraisalFile.loadGridData();
        } else {
          _self.$message({
            showClose: true,
            message:"删除失败",
            duration: 2000,
            type: "warning",
          });
        }
      })
      .catch(function (error) {
        _self.$message("删除失败");
        console.log(error);
      });
    },
    searchDC(){
      let _self = this;
      let key=_self.condition+_self.conditionFile;
      if(_self.DCinputValueNum!=''&&_self.DCinputValueNum!=undefined){
        key+=" and (CODING LIKE '%"+_self.DCinputValueNum+"%' OR TITLE LIKE '%"+_self.DCinputValueNum+"%')";
      }
      if(_self.ArchivalType!=''&&_self.ArchivalType!=undefined){
        if(_self.ArchivalType=='all'){
          key+=" and C_ARC_CLASSIC in ("+_self.allType+")"
        }else{
          key+=" and C_ARC_CLASSIC='"+_self.ArchivalType+"'";
        }
        
      }
      _self.$refs.chooseFile.condition=key;
      _self.$refs.chooseFile.currentPage = 1;
      _self.$refs.chooseFile.loadGridInfo()
      _self.$refs.chooseFile.loadGridData()
    },
    //档案类目下拉菜单
    onSelectChange(val) {
      this.ArchivalType = val;
      this.searchDC();
    },
    beforeAdd(){
      let _self=this
      let ID=''
      if(_self.parentID==''||_self.parentID==undefined){
        _self.$message({
            showClose: true,
            message:"请选择一个档案鉴定单",
            duration: 2000,
            type: "warning",
          });
        return
      }
      axios
        .get("/dc/checkArchiveFile")
        .then(function (response) {
          let code = response.data.code;
          ID=response.data.ID;
          if (code == 1) {
            if(ID!=''){
              _self.conditionFile=" and ID NOT IN ("+ID+")"
            }
            _self.propertyrela=true
            setTimeout(() => {
               _self.initGridViewOptions()
              _self.searchDC()
            }, 10);
           
          } else {
            console.log(error);
          }
        })
        
    },
    newRelation(){
      let _self = this;
      if(_self.selectedChooseDCItems==''||_self.selectedChooseDCItems==undefined){
        _self.$message({
            showClose: true,
            message:"请选择一个文档",
            duration: 2000,
            type: "warning",
          });
        return
      }
      var m = new Map();
      m.set("parentID",_self.parentID)
      let formdata = new FormData();
      formdata.append("metaData", JSON.stringify(m));
      var a = [];
      let tab=_self.selectedChooseDCItems;
      var i;
      for (i in tab) {a
        a.push(tab[i]["ID"]);
      }
      formdata.append("ID", JSON.stringify(a));
      axios.post("/dc/Archive/newDcRelation", formdata, {
        "Content-Type": "multipart/form-data",
      })
      .then(function (response) {
        let code = response.data.code;
        if (code == 1) {
          _self.$message({
            showClose: true,
            message:"添加成功",
            duration: 2000,
            type: "success",
          });
          _self.propertyrela = false;
          _self.$refs.AppraisalFile.loadGridData();
        } else {
          _self.$message({
            showClose: true,
            message:"添加失败",
            duration: 2000,
            type: "warning",
          });
        }
      })
      .catch(function (error) {
        _self.$message("添加失败");
        console.log(error);
      });
    },
    // 获取档案类目列表
    initGridViewOptions(){
      let queryObj={}
      let _self = this;
      queryObj.queryName = "档案类目";
      axios.post("/query/getquery", JSON.stringify(queryObj)).then(function(resp){
        _self.textField = _self.dataTextField.length>0?_self.dataTextField:resp.data.labelField
        _self.valueField = _self.dataTextField.length>0?_self.dataTextField:resp.data.valueField
        
         let getOptions = resp.data.data
        getOptions.forEach(function(item){
          _self.options.push({label:item[_self.textField],value:item[_self.valueField]})
         
        })
         console.log(_self.options);
      }).catch(function(error) {
        console.log(error);
      });
    },
    handleClose(){
      let _self = this;
    },
    getWorkFlow() {
      let _self = this;
      if(_self.selectThChange==''||_self.selectThChange==undefined){
        _self.$message({
            showClose: true,
            message:"请选择一个档案鉴定单",
            duration: 2000,
            type: "warning",
          });
        return
      }

      var m = new Map();
      m.set("processDefinitionKey", "档案鉴定流程");

      axios
        .post("/dc/getWorkflow", JSON.stringify(m))
        .then(function (response) {
          _self.workflow = response.data.data[0];
          console.log(_self.workflow)
          _self.parentID=_self.selectedThItems[0].ID
          _self.flowVisible = true;
        })
        .catch(function (error) {
          console.log(error);
        });

      
    },
  },
  props: {}, 
  components: {
    ShowProperty:ShowProperty,
    DataGrid: DataGrid,
    DataLayout: DataLayout,
    DataSelect: DataSelect,
    AppraisalStartUp:AppraisalStartUp
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