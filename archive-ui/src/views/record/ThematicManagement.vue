<template>
  <DataLayout>
    <template v-slot:header>
      <!-- 专题新建属性窗口 -->
      <el-dialog
        title="专题属性"
        :visible.sync="propertyVisible"
        @close="propertyVisible = false"
        width="90%"
        :close-on-click-modal="false"
        v-dialogDrag
        >
        <ShowProperty
          ref="ShowProperty"
          width="100%"
          folderPath=""
          v-bind:typeName="typeName"
        ></ShowProperty>
        <div slot="footer" class="dialog-footer">
            <el-button  v-on:click="saveItem" :loading="butt">{{$t('application.save')}}</el-button>
            <el-button @click="propertyVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
      <!-- 相关文件创建文档选择-->
      <el-dialog title="选择文档" :visible.sync="propertyrela" width="80%" :close-on-click-modal="false" :before-close="handleClose"> 
        <DataLayout>
          <template v-slot:header>
            <el-form>
            <el-form-item>
              <el-input style="width:200px" v-model="DCinputValueNum" placeholder="请输入编码或标题"></el-input>
              <el-button type="primary" @click="searchDC()">{{$t('application.SearchData')}}</el-button>
            </el-form-item> 
            </el-form>  
            </template>
          <template v-slot:main>  
            <DataGrid 
              ref="chooseDrawing"
              key="General"
              dataUrl="/dc/getDocuments"
              v-bind:tableHeight="360"
              condition="IS_RELEASED=1"
              v-bind:isshowOption="true" v-bind:isshowSelection ="false"
              gridViewName="GeneralGrid"
              :optionWidth = "2"
              :isshowCustom="false"
              :isEditProperty="true"
              showOptions="查看内容"
              :isShowChangeList="false">
              <template slot="customMoreOption" slot-scope="scope">
              <el-button type="primary" @click="DCChoose(scope.data.row)" size="mini">{{$t('application.select')}}</el-button>
              </template>
            </DataGrid>
          </template>
        </DataLayout>
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
          <el-date-picker
            v-model="startDate"
            type="date"
            :placeholder="$t('application.startDate')"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="endDate"
            type="date"
            align="right"
            :placeholder="$t('application.endDate')"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
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
            <el-button type="primary"  @click="propertyVisible=true">新建专题</el-button>
        </el-form-item>
        <el-form-item>
          <MountFile :selectedItem="selectedThItems" @refresh='search' :title="$t('application.ReplaceDoc')">更新内容</MountFile>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <split-pane v-on:resize="onSplitResize" :min-percent='20' :default-percent='topPercent' split="horizontal">
          <template slot="paneL">
                <DataGrid
                  ref="ThematicGrid"
                  key="Thematic"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="ThematicManagementGrid"
                  condition="TYPE_NAME='专题' and STATUS='新建'"
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
                    <el-button type="primary" @click="beforeCreat">添加文档</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="warning" @click="DelRelation" >移除文档</el-button>
                </el-form-item>
              </el-form>
              </el-col>
            </el-row>
            <DataGrid
              ref="Drawing"
              key="Drawing"
              dataUrl="/dc/getDocuments"
              v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
              v-bind:isshowOption="true" v-bind:isshowSelection ="true"
              gridViewName="GeneralGrid"
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
import MountFile from '@/components/MountFile.vue';

export default {
  name: "TC",
  data() {
    return {
      // 本地存储高度名称
      topStorageName: 'ReceivingDC4CnpeHeight',
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 120,
      selectedDCItems: [],//文档
      selectedThItems:[],//专题
      inputValueNum:"",
      inputValueNumD:"",//移交单查询
      rejectComment:"",
      rejectVisible:false,
      options: [
        {
          value: "新建",
          label: "新建",
        },
        {
          value: "已发布",
          label: "已发布",
        },
      ],
      value:"新建",
      startDate: "",
      endDate:"",
      ThematicTypes:[],
      propertyVisible:false,
      typeName:"专题",
      propertyrela:false,
      butt:false,
      DCinputValueNum:"",
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
        "SELECT CHILD_ID from ecm_relation where NAME='专题'and PARENT_ID ='" +row.ID +"'";
      var key1 = "ID IN (" + condition1 + ") AND IS_RELEASED=1 and STATUS <> '作废'";
      this.$refs.Drawing.condition = key1;
      this.$refs.Drawing.gridViewName = "GeneralGrid";
      this.$refs.Drawing.loadGridInfo();
      this.$refs.Drawing.loadGridData();
    },
    //文档模糊查询
    search() {
      let _self = this;
      let key="TYPE_NAME='专题'";
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
      _self.$refs.ThematicGrid.condition=key;
      _self.$refs.ThematicGrid.currentPage = 1;
      _self.$refs.ThematicGrid.loadGridInfo();
      _self.$refs.ThematicGrid.loadGridData();
    },
    selectDCChange(val) {
      this.selectedDCItems = val;
    },
    selectThChange(val) {
      this.selectedThItems = val;
    },
    changeStatus:function(){
      this.search()
    },
    // 保存专题
    saveItem() {
      let _self = this;
      if (!this.$refs.ShowProperty.validFormValue()) {
        return;
      }
      var m = new Map();
      var c;
      for (c in _self.$refs.ShowProperty.dataList) {
        let dataRows = _self.$refs.ShowProperty.dataList[c].ecmFormItems;
        var i;
        for (i in dataRows) {
          if (dataRows[i].attrName && dataRows[i].attrName != "") {
            if (
              dataRows[i].attrName != "FOLDER_ID" &&
              dataRows[i].attrName != "ID"
            ) {
              var val = dataRows[i].defaultValue;
              if (val && dataRows[i].isRepeat) {
                var temp = "";
                for (let j = 0, len = val.length; j < len; j++) {
                  temp = temp + val[j] + ";";
                }
                temp = temp.substring(0, temp.length - 1);
                val = temp;
              }
              m.set(dataRows[i].attrName, val);
            }
          }
        }
      }
      if (_self.$refs.ShowProperty.myItemId != "") {
        m.set("ID", _self.$refs.ShowProperty.myItemId);
      }
      m.set("TYPE_NAME", "专题");
      let formdata = new FormData();
      formdata.append("metaData", JSON.stringify(m));
      if(_self.$refs.ShowProperty.file!=""){
          formdata.append("uploadFile",_self.$refs.ShowProperty.file.raw);
      }
      if (_self.$refs.ShowProperty.myItemId == "") {
        axios
          .post("/Thematic/newThematic", formdata, {
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
              _self.$refs.ThematicGrid.loadGridData();
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
      }
    },
    searchDC(){
      let _self = this;
      let key="IS_RELEASED=1";
      if(_self.DCinputValueNum!=''&&_self.DCinputValueNum!=undefined){
        key+=" and (CODING LIKE '%"+_self.DCinputValueNum+"%' OR TITLE LIKE '%"+_self.DCinputValueNum+"%')";
      }
      _self.$refs.chooseDrawing.condition=key;
      _self.$refs.chooseDrawing.currentPage = 1;
      _self.$refs.chooseDrawing.loadGridInfo()
      _self.$refs.chooseDrawing.loadGridData()
    },
    DCChoose(row){
        let ID = row.ID
        let _self = this;
        var m = new Map();
        m.set("relationName","专题")
        m.set("parent_id",_self.parentID)
        m.set("child_id",ID)
        let formdata = new FormData();
        formdata.append("metaData", JSON.stringify(m));
        axios
          .post("/Thematic/newDcRelation", formdata, {
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
              _self.$refs.ThematicGrid.loadGridData();
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
    beforeCreat(){
      let _self=this;
      if(_self.parentID==''||_self.parentID==undefined){
        _self.$message({
            showClose: true,
            message:"请选择一个专题",
            duration: 2000,
            type: "warning",
          });
        return
      }
      _self.propertyrela=true
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
      axios.post("/Thematic/DelDcRelation",JSON.stringify(m),{
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
          _self.propertyVisible = false;
          _self.$refs.Drawing.loadGridData();
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
    }
  },
  props: {},
  components: {
    ShowProperty:ShowProperty,
    DataGrid: DataGrid,
    DataLayout: DataLayout,
    MountFile:MountFile,
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