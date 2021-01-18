<template>
  <DataLayout>
    <template v-slot:header>
      <el-dialog
        title="退回备注"
        :visible.sync="pendNotVisible"
        @close="pendNotVisible = false"
        :append-to-body="true"
        width="60%"
        :close-on-click-modal="false"
        v-dialogDrag
      >
        <el-col :span="24">
          <el-form label-position="right" :model="pendForm" @submit.native.prevent>
            <el-input type="textarea" :rows="6" v-model="pendForm.rejectComment" autocomplete="off"></el-input>
          </el-form>
        </el-col>
        <div slot="footer" class="dialog-footer">
          <el-button @click="saveRejectComment" :loading="butt">{{$t('application.save')}}</el-button>
          <el-button @click="pendNotVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
      
      <el-dialog
        title="添加库位号"
        :visible.sync="propertyVisible"
        @close="propertyVisible = false"
        width="30%"
        :close-on-click-modal="false"
        v-dialogDrag
      >
        <el-form>
          <el-form-item>
            <el-input
              style="width: 90%"
              v-model="locationCoding"
              placeholder="请输入库位号"
            >
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updateLocationCoding()">{{
              $t("application.ok")
            }}</el-button>
            <el-button @click="propertyVisible = false">{{
              $t("application.cancel")
            }}</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>
      <el-form :inline="true" @submit.native.prevent>
        <el-form-item>
          <el-input
            style="width: 200px"
            v-model="inputValueNum"
            placeholder="请输入编码、题名或批次"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search()">{{
            $t("application.SearchData")
          }}</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="propertyVisible=true">添加库位号</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handOver()">入库</el-button>
        </el-form-item>
        <el-form-item>
        <el-button type="primary" @click="pendNot">退回</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click.native="exportData">{{
            $t("application.ExportExcel")
          }}</el-button>
        </el-form-item>
        <el-form-item>
          <AddCondition v-model="AdvCondition" :inputType="hiddenInput" @sendMsg="search()"></AddCondition>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <div
        :style="{
          position: 'relative',
          height: layout.height - startHeight + 'px',
        }"
      >
        <split-pane
          v-on:resize="onSplitResize"
          :min-percent="20"
          :default-percent="topPercent"
          split="horizontal"
        >
          <template slot="paneL">
            <DataGrid
              ref="mainDataGrid"
              v-bind="tables.main"
              v-bind:tableHeight="
                ((layout.height - startHeight) * topPercent) / 100 -
                topbarHeight
              "
              v-bind:isshowOption="true"
              v-bind:isshowSelection="true"
              :optionWidth="2"
              :isshowCustom="false"
              :isEditProperty="true"
              showOptions="查看内容"
              :isShowChangeList="false"
              @selectchange="onSelectChange"
              @rowclick="onDataGridRowClick"
            >
            </DataGrid>
          </template>
          <template slot="paneR">
            <DataGrid
              ref="subtabGrid"
              v-bind="tables.subtabGrid"
              v-bind:tableHeight="
                ((layout.height - startHeight) * (100 - topPercent)) / 100 -
                bottomHeight
              "
              v-bind:isshowOption="true"
              v-bind:isshowSelection="true"
              :isInitData="false"
              :optionWidth="2"
              :isshowCustom="false"
              :isEditProperty="false"
              showOptions="查看内容"
              :isShowChangeList="false"
              @selectchange="onSelectChange"
            ></DataGrid>
          </template>
        </split-pane>
      </div>
    </template>
  </DataLayout>
</template>
<script type="text/javascript">
import DataLayout from "@/components/ecm-data-layout";
import DataGrid from "@/components/DataGrid";
import ExcelUtil from "@/utils/excel.js";
import AddCondition from '@/views/record/AddCondition'
export default {
  name: "ArchiveHandOver",
  data() {
    return {
      isNotCnpe: true,
      batchDialogVisible: false, //导入对话框可见性
      // 本地存储高度名称
      topStorageName: "ArchiveHandOverTopHeight",
      // 非split pan 控制区域高度
      startHeight: 140,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 40,
      pendForm: {
        rejectComment: ""
      },
      tables: {
        main: {
          gridViewName: "ArchiveHandOverGrid",
          dataUrl: "/dc/getDocuments",
          condition: "",
        },
        subtabGrid: {
          gridViewName: "GeneralSubGrid",
          dataUrl: "/dc/getDocuments",
          condition: "",
        },
      },
      relation: {},
      inputValueNum: "",
      selectedItems: [],
      propertyVisible: false,
      locationCoding: "",
      pendNotVisible:false,
      AdvCondition:"",
      hiddenInput:"hidden",
    };
  },
  props: {},
  mounted() {
    _self.$refs.mainDataGrid.loadGridInfo();
    _self.$refs.mainDataGrid.loadGridData();

    setTimeout(() => {
      this.topPercent = this.getStorageNumber(this.topStorageName, 60);
    }, 300);
  },
  methods: {
    pendNot() {
      let _self = this;
      if (_self.selectedItems.length == 0) {
        _self.$message({
          showClose: true,
          message: _self.$t("message.PleaseSelectOneOrMoreData"),
          duration: 2000,
          type: "error"
        });
        return;
      }
      _self.pendNotVisible = true;
    },
    saveRejectComment() {
      let _self = this;
      if (_self.pendForm.rejectComment == "") {
        _self.$message({
          showClose: true,
          message: _self.$t("message.PleaseInputData"),
          duration: 2000,
          type: "error"
        });
        return;
      }
      let ids = new Array();
      _self.selectedItems.forEach(function(e) {
        ids.push(e.ID);
      });

      let m = new Map();
      m.set("ids", ids);
      m.set("comment", _self.pendForm.rejectComment);
      let formdata = new FormData();
      formdata.append("metaData", JSON.stringify(m));
      axios
        .post("/exchange/doc/Reject", formdata) ///dc/getSelectList
        .then(function(response) {
          if (response.data.code == 1) {
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveSuccess"),
              duration: 2000,
              type: "success"
            });
            _self.pendNotVisible = false;
            _self.$refs.mainDataGrid.loadGridData();
          } else {
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveFailured"),
              duration: 2000,
              type: "error"
            });
          }
        })
        .catch(function(error) {
          console.log(error);
          _self.$message({
            showClose: true,
            message: _self.$t("message.saveFailured"),
            duration: 2000,
            type: "error"
          });
        });
    },
    onSplitResize(topPercent) {
      // 顶部百分比*100
      this.topPercent = topPercent;
      this.setStorageNumber(this.topStorageName, topPercent);
      //console.log(JSON.stringify(topPercent))
    },

    onSelectChange(val) {
      this.selectedItems = val;
    },

    search() {
      let _self = this;
      let key = "STATUS = '待入库' and IS_CHILD = '0' ";
      if (_self.inputValueNum != "" && _self.inputValueNum != undefined) {
        key +=
          "and (CODING LIKE '%" +
          _self.inputValueNum +
          "%' OR TITLE LIKE '%" +
          _self.inputValueNum +
          "%' OR C_BATCH_CODING2 LIKE '%" +
          _self.inputValueNum +
          "%')";
      }
      if (_self.AdvCondition != "" && _self.AdvCondition != undefined) {
        key +=
          "and "+ _self.AdvCondition 
      }

      _self.$refs.mainDataGrid.condition = key;
      _self.$refs.mainDataGrid.currentPage = 1;
      _self.$refs.mainDataGrid.loadGridInfo();
      _self.$refs.mainDataGrid.loadGridData();
      _self.$refs.subtabGrid.itemDataList = [];
    },

    updateLocationCoding() {
      let _self = this;
      if (_self.selectedItems.length == 0) {
        let msg = "请勾选文档！";
        _self.$message({
          showClose: true,
          message: msg,
          duration: 5000,
          type: "warning",
        });
        return;
      }
      if (-self.locationCoding==null || _self.locationCoding.length == 0) {
        let msg = "请输入库位号！";
        _self.$message({
          showClose: true,
          message: msg,
          duration: 5000,
          type: "warning",
        });
        return;
      }


      var id = [];

      var i;
      for (i in _self.selectedItems) {
        id.push(_self.selectedItems[i]["ID"]);
      }
      let mp = new Map();
      mp.set("ids", id);
      mp.set("locationCoding", _self.locationCoding);
      axios
        .post("/record/createStorageNum", JSON.stringify(mp), {
          headers: {
            "Content-Type": "application/json;charset=UTF-8",
          },
        })
        .then(function (response) {
          
          let code = response.data.code;
          if(code == "1"){
             _self.$message({
              showClose: true,
              message: "操作成功！",
              duration: 2000,
              type: "success",
            });
            _self.search();
          }
        });

      _self.propertyVisible = false;
    },

    handOver() {
      let _self = this;
      if (_self.selectedItems.length == 0) {
        let msg = "请选择档案信息";
        _self.$message({
          showClose: true,
          message: msg,
          duration: 2000,
          type: "warning",
        });
        return;
      }

      var id = [];

      var i;
      for (i in _self.selectedItems) {
        id.push(_self.selectedItems[i]["ID"]);
      }

      let mp = new Map();
      mp.set("ids", id);

      axios
        .post("/record/handOverRecord", JSON.stringify(mp), {
          headers: {
            "Content-Type": "application/json;charset=UTF-8",
          },
        })
        .then(function (response) {
          _self.$refs.mainDataGrid.loadGridData();
          let code = response.data.code;
          var j;
          for (j in _self.selectedItems) {
            writeAudit(_self.selectedItems[j]["ID"]);
          }
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

    onDataGridRowClick: function (row) {
      this.parentID = row.ID;
      var condition1 =
        "SELECT CHILD_ID from ecm_relation where NAME='irel_children'and PARENT_ID ='" +
        row.ID +
        "'";
      var key1 = "ID IN (" + condition1 + ") ";
      this.$refs.subtabGrid.condition = key1;
      this.$refs.subtabGrid.gridViewName = "GeneralSubGrid";
      this.$refs.subtabGrid.loadGridInfo();
      this.$refs.subtabGrid.loadGridData();
    },

    exportData() {
      var fileDate = new Date();
      let fileDateStr =
        fileDate.getFullYear() +
        "" +
        fileDate.getMonth() +
        "" +
        fileDate.getDate();
      let params = {
        gridName: "ArchiveHandOverGrid",
        lang: "zh-cn",
        condition: this.$refs.mainDataGrid.condition,
        filename: "File_HandOver_" + fileDateStr + ".xlsx",
        sheetname: "移交入库",
      };
      ExcelUtil.export(params);
    },
  },
  components: {
    DataLayout: DataLayout,
    DataGrid: DataGrid,
    ExcelUtil: ExcelUtil,
    AddCondition:AddCondition,
  },
};
</script>
<style scoped>
</style>