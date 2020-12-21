<template>
  <div :style="{ position: 'relative', height: asideHeight + 'px' }">
    <split-pane
      split="vertical"
      @resize="resize"
      :min-percent="10"
      :default-percent="15"
    >
      <template slot="paneL">
        <el-container
          :style="{
            height: treeHeight + 'px',
            width: asideWidth,
            overflow: 'auto',
          }"
        >
          <el-tree
            :props="defaultProps"
            :data="dataList"
            node-key="id"
            style="width: 100%"
            :render-content="renderContent"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          ></el-tree>
        </el-container>
      </template>
      <template slot="paneR">
        <DataLayout>
          <template v-slot:header>
            <el-form :inline="true" @submit.native.prevent>
              <el-form-item>
                <el-input
                  style="width: 200px"
                  v-model="inputValueNum"
                  placeholder="请输入编码或标题"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="search()">{{
                  $t("application.SearchData")
                }}</el-button>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click.native="exportData">{{
                  $t("application.ExportExcel")
                }}</el-button>
              </el-form-item>
              <el-form-item>
                <AddCondition
                  @change="searchItem"
                  v-model="advCondition"
                  v-bind:typeName="typeName"
                  :inputValue="advCondition"
                  :inputType="hiddenInput"
                ></AddCondition>
              </el-form-item>
              <el-form-item>
                <ArchieveStorage
                  :selectRowData="selectedItems"
                  :reload-Grid="reload"
                ></ArchieveStorage>
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
                  <el-tabs v-model="tabs.activeNum">
                    <el-tab-pane label="案卷" name="relevantFile">
                      <DataGrid
                        ref="relevantFileDataGrid"
                        key="relevantFile"
                        v-bind="tables.relevantFileDataGrid"
                      >
                      </DataGrid>
                    </el-tab-pane>
                    <el-tab-pane label="文件" name="initFile">
                      <DataGrid
                        ref="GeneralFileGrid"
                        key="GeneralFile"
                        v-bind="tables.GeneralFileGrid"
                      >
                      </DataGrid>
                    </el-tab-pane>
                  </el-tabs>
                </template>
              </split-pane>
            </div>
          </template>
        </DataLayout>
      </template>
    </split-pane>
  </div>
</template>
<script type="text/javascript">
import DataLayout from "@/components/ecm-data-layout";
import DataGrid from "@/components/DataGrid";
import AddCondition from "@/views/record/AddCondition.vue";
import StartupComponent from "@/views/workflow/StartupComponent.vue";
import ArchieveStorage from "@/components/SubmitFolder.vue";
import ExcelUtil from "@/utils/excel.js";
export default {
  data() {
    return {
      inputValueNum: "",
      advCondition: "",
      hiddenInput: "hidden",

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
      bottomHeight: 80,

      defaultProps: {
        children: "children",
        label: "name",
      },

      dataList: [],
      gridList: [],
      itemDataList: [],
      itemDataListFull: [],

      tables: {
        main: {
          gridViewName: "GeneralPre",
          dataUrl: "/dc/getDocuments",
          condition: "",
        },
        relevantFileDataGrid: {
          gridViewName: "GeneralPre",
          dataUrl: "/dc/getDocuments",
          condition: "C_ITEM_TYPE='案卷'",
        },
        GeneralFileGrid: {
          gridViewName: "GeneralPre",
          dataUrl: "/dc/getDocuments",
          condition: "C_ITEM_TYPE='文件'",
        },
      },
      tabs: {
        activeNum: "",
      },
    };
  },

  mounted() {
    let _self = this;

    var psize = localStorage.getItem("docPageSize");
    if (psize) {
      _self.pageSize = parseInt(psize);
    }
    _self.currentLanguage = localStorage.getItem("localeLanguage") || "zh-cn";
    _self.loading = true;
    axios
      .post("/admin/getPreArchivesFolder", 0)
      .then(function (response) {
        _self.dataList = response.data.data;
        //console.log(JSON.stringify(_self.dataList));
        _self.loading = false;
        _self.$refs.mainDataGrid.loadGridInfo(_self.defaultData);
        _self.$refs.mainDataGrid.loadGridData(_self.defaultData);
      })
      .catch(function (error) {
        console.log(error);
        _self.loading = false;
      });

    setTimeout(() => {
      _self.topPercent = _self.getStorageNumber(_self.topStorageName, 60);
    }, 300);
  },

  methods: {
    // 加载表格样式
    loadGridInfo(indata) {
      let _self = this;
      _self.loading = true;
      var m = new Map();
      m.set("gridName", indata.gridView);
      m.set("lang", _self.currentLanguage);
      axios
        .post("/dc/getGridViewInfo", JSON.stringify(m))
        .then(function (response) {
          _self.showFields = [];
          _self.gridList = response.data.data;
          _self.gridList.forEach((element) => {
            if (element.visibleType == 1) {
              _self.showFields.push(element.attrName);
            }
          });
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    },

    loadGridData(indata) {
      let _self = this;
      _self.tableLoading = true;
      var key = _self.inputkey;
      var m = new Map();
      _self.gridViewTrans = indata.gridView;
      _self.idTrans = indata.id;
      m.set("gridName", indata.gridView);
      m.set("folderId", indata.id);
      m.set("condition", key);
      m.set("pageSize", _self.pageSize);
      m.set("pageIndex", (_self.currentPage - 1) * _self.pageSize);
      m.set("orderBy", "MODIFIED_DATE desc");
      axios
        .post("/dc/getExceptBoxDocuments", JSON.stringify(m))
        .then(function (response) {
          _self.itemDataList = response.data.data;
          _self.itemDataListFull = response.data.data;
          _self.itemCount = response.data.pager.total;
          _self.tableHeight = window.innerHeight - 170;
          _self.tableLoading = false;
        });
    },

    onSplitResize(topPercent) {
      // 顶部百分比*100
      this.topPercent = topPercent;
      this.setStorageNumber(this.topStorageName, topPercent);
    },

    onSelectChange(val) {
      this.selectedItems = val;
    },

    handleNodeClick(indata) {
      let _self = this;
      _self.disable = false;
      _self.exportAble = true;
      _self.currentFolder = indata;
      if (indata.extended == false) {
        _self.loading = true;
        axios
          .post("/admin/getFolder", indata.id)
          .then(function (response) {
            indata.children = response.data.data;
            //console.log(JSON.stringify(indata));
            indata.extended = true;
            _self.inputkey = "";
            _self.loading = false;
          })
          .catch(function (error) {
            console.log(error);
            _self.loading = false;
          });
      }
      _self.$refs.mainDataGrid.loadGridInfo(indata);
      _self.$refs.mainDataGrid.loadGridData(indata);
    },

    renderContent: function (h, { node, data, store }) {
      if (data.extended) {
        return (
          <span>
            <i class="el-icon-folder-opened"></i>
            <span style="padding-left: 4px;">{node.label}</span>
          </span>
        );
      } else {
        return (
          <span>
            <i class="el-icon-folder"></i>
            <span style="padding-left: 4px;">{node.label}</span>
          </span>
        );
      }
    },

    search() {
      let _self = this;
      let key = "STATUS = '预归档' ";
      if (_self.inputValueNum != "" && _self.inputValueNum != undefined) {
        key +=
          "and (CODING LIKE '%" +
          _self.inputValueNum +
          "%' OR TITLE LIKE '%" +
          _self.inputValueNum +
          "%')";
      }

      if (_self.advCondition != undefined && _self.advCondition.length > 0) {
        key += " and " + _self.advCondition;
      }

      _self.$refs.mainDataGrid.condition = key;
      _self.$refs.mainDataGrid.currentPage = 1;
      _self.$refs.mainDataGrid.loadGridInfo();
      _self.$refs.mainDataGrid.loadGridData();
    },

    searchItem() {
      this.search();
    },

    onDataGridRowClick: function (row) {
      this.parentID = row.ID;
      var typeChose = row.C_ITEM_TYPE;
      var condition1 =
        "SELECT CHILD_ID from ecm_relation where NAME='irel_children'and PARENT_ID ='" +
        row.ID +
        "'";
      var key1 = "ID IN (" + condition1 + ") ";
      this.$refs.relevantFileDataGrid.condition = key1;
      this.$refs.relevantFileDataGrid.gridViewName = "GeneralPre";
      if (typeChose == "案卷") {
        this.$refs.relevantFileDataGrid.loadGridInfo();
        this.$refs.relevantFileDataGrid.loadGridData();
      }
      if (typeChose == "文件") {
        this.$refs.GeneralFileGrid.loadGridInfo();
        this.$refs.GeneralFileGrid.loadGridData();
      }
    },

    reload() {
      this.loadGridData(this.currentFolder);
    },

    exportData() {
      let _self = this;
      let params = {
        URL: "/file/exportFolderPath",
        gridName: _self.currentFolder.gridView,
        folderId: _self.currentFolder.id,
        orderBy: "MODIFIED_DATE desc",
        pageSize: _self.pageSize,
        pageIndex: (_self.currentPage - 1) * _self.pageSize,
        lang: "zh-cn",
      };
      console.log(params);
      ExcelUtil.export4Cnpe(params);
    },
  },

  components: {
    DataLayout: DataLayout,
    DataGrid: DataGrid,
    AddCondition: AddCondition,
    StartupComponent: StartupComponent,
    ArchieveStorage: ArchieveStorage,
    ExcelUtil: ExcelUtil,
  },
};
</script>
<style scoped>
</style>