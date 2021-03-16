<template>
  <DataLayout>
    <template v-slot:header>
      <!-- 新建档案销毁 -->
      <el-dialog
        title="档案销毁"
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
      <el-dialog title="选择文档" :visible.sync="propertyrela" width="80%" :close-on-click-modal="false"> 
        <selectDC @selectchange="selectedChooseDC" :ShowFileType="true" :conditionFile="conditionFile"></selectDC>
          <div slot="footer" class="dialog-footer">
        <el-button @click="propertyrela = false">{{
          $t("application.cancel")
        }}</el-button>
        <el-button  @click="newRelation()">确定</el-button>
      </div>
      </el-dialog>
      <el-dialog
      title="档案销毁流程"
      :visible.sync="flowVisible"
      @close="flowVisible = false"
      width="90%"
      style="width: 100%"
      :close-on-click-modal="false"
      v-dialogDrag
    >
      <div><DestoryStartUp :workflowObj="workflow" :showUploadFile="true" :parentId="parentID" :workflowFileList="files4Start" @close="flowVisible = false"></DestoryStartUp></div>
    </el-dialog>
      <el-form :inline="true" @submit.native.prevent>
        <el-form-item>
          <el-input
            style="width: 200px"
            v-model="inputValueNum"
            placeholder='请输入编码或标题'
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary"  plain @click="search()">{{
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
            <el-button type="primary"  plain @click="propertyVisible=true">新建</el-button>
        </el-form-item>
        <el-form-item v-if="showFlow">
            <el-button type="primary"  plain @click="getWorkFlow">发起流程</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <split-pane v-on:resize="onSplitResize" :min-percent='20' :default-percent='topPercent' split="horizontal">
          <template slot="paneL">
                <DataGrid
                  ref="ArchiveDestructionGrid"
                  key="Thematic"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="ArchiveDestructionGrid"
                  condition="TYPE_NAME='档案销毁单' and STATUS='新建' and IS_RELEASED=1 "
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isEditProperty="false"
                  :isInitData="false"
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
              <el-form :inline="true" @submit.native.prevent>
                <el-form-item>
                    <el-button type="primary"  plain @click="beforeAdd()">添加</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="warning"  plain @click="DelRelation" >移除</el-button>
                </el-form-item>
              </el-form>
              </el-col>
            </el-row>
            <DataGrid
              ref="DestructionFileGrid"
              key="DestructionFile"
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
import DataGrid from "@/components/DataGrid";
import DataLayout from "@/components/ecm-data-layout";
import DestoryStartUp from "@/views/workflow/DestoryStartUp.vue"
import selectDC from"@/components/controls/selectDC.vue"

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
      bottomHeight:80,
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
      value:"新建",
      flowVisible:false,
      workflow:{},
      selectedDCItems:[],
      parentID:"",
      files4Start:[],
      showFlow:true,
      comment:"",//原因说明
      propertyVisible:false,   //新建窗口
      propertyrela:false,
      selectedChooseDCItems:""
    };
  },
  mounted() {
    this.search();
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
      var key1 = "ID IN (" + condition1 + ") AND IS_RELEASED=1 and STATUS <> '作废'";
      this.$refs.DestructionFileGrid.condition = key1;
      this.$refs.DestructionFileGrid.gridViewName = "GeneralGrid";
      this.$refs.DestructionFileGrid.loadGridInfo();
      this.$refs.DestructionFileGrid.loadGridData();
    },
    //文档模糊查询
    search() {
      let _self = this;
      let key="TYPE_NAME='档案销毁单'";
      if(_self.inputValueNum!=''&&_self.inputValueNum!=undefined){
        key+="and (CODING LIKE '%"+_self.inputValueNum+"%' OR TITLE LIKE '%"+_self.inputValueNum+"%')";
      }
      if(_self.value!=''&&_self.value!=undefined){
        key+=" and STATUS = '"+_self.value+"'";
      }
      _self.$refs.ArchiveDestructionGrid.condition=key;
      _self.$refs.ArchiveDestructionGrid.currentPage = 1;
       _self.$refs.DestructionFileGrid.itemDataList=[];
      _self.$refs.ArchiveDestructionGrid.loadGridInfo();
      _self.$refs.ArchiveDestructionGrid.loadGridData();
    },
    selectedChooseDC(val) {
      this.selectedChooseDCItems = val;
    },
    selectThChange(val) {
      this.selectedThItems = val;
    },
    changeStatus:function(){
      this.search()
      if(this.value!='新建'){
       this.showFlow=false
      }else{
        this.showFlow=true
      }
    },
    getWorkFlow() {
      let _self = this;
      if(_self.value!='新建'){
        _self.$message({
            showClose: true,
            message:"只有新建状态的文件才能发起流程",
            duration: 2000,
            type: "warning",
          });
        return
      }
      if(_self.selectedThItems==''||_self.selectedThItems==undefined){
        _self.$message({
            showClose: true,
            message:"请选择一个档案销毁单",
            duration: 2000,
            type: "warning",
          });
        return
      }
      _self.files4Start = _self.$refs.DestructionFileGrid.itemDataList
      var m = new Map();
      m.set("processDefinitionKey", "档案销毁流程");

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
    // 保存档案销毁
    saveItem() {
      let _self = this;
      var m = new Map();
      m.set("TYPE_NAME", "档案销毁单");
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
            _self.search()
            _self.$refs.DestructionFileGrid.itemDataList=[];
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
    selectDCChange(val) {
      this.selectedDCItems = val;
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
          _self.$refs.DestructionFileGrid.loadGridData();
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
    beforeAdd(){
      let _self=this
      let ID=''
      if(_self.parentID==''||_self.parentID==undefined){
        _self.$message({
            showClose: true,
            message:"请选择一个档案销毁单",
            duration: 2000,
            type: "warning",
          });
        return
      }
      axios
        .get("/dc/checkDestructionFile")
        .then(function (response) {
          let code = response.data.code;
          ID=response.data.ID;
          if (code == 1) {
            if(ID!=''){
              _self.conditionFile=" and ID NOT IN ("+ID+")"
            }
            _self.propertyrela=true
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
          _self.$refs.DestructionFileGrid.loadGridData();
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
  },
  props: {},
  components: {
    DataGrid: DataGrid,
    DataLayout: DataLayout,
    DestoryStartUp:DestoryStartUp,
    selectDC:selectDC
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