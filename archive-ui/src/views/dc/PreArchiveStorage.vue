<template>
  <div :style="{ position: 'relative', height: asideHeight + 'px' }">
    <split-pane
      split="vertical"
      @resize="resize"
      :min-percent="1"
      :default-percent="leftPercent"
    >
      <template slot="paneL">
        <el-container
          :style="{
            height: treeHeight + 'px',
            width: asideWidth,
            overflow: 'auto',
          }"
        >
          <el-header>
            <el-input
              style="width: 150px"
              v-model="inputFolder"
              placeholder="请输入文件夹名称"
              @keyup.enter.native="searchFolder()"
            ></el-input>
            <el-button type="primary" @click="searchFolder()">{{
              $t("application.SearchData")
            }}</el-button>
          </el-header>
          <el-tree
            ref="ecmFolderTree"
            :props="defaultProps"
            :data="dataList"
            node-key="id"
            style="width: 100%"
            :render-content="renderContent"
            :default-expand-all="isExpand"
            @node-click="handleNodeClick"
          ></el-tree>
        </el-container>
      </template>
      <template slot="paneR">
        <DataLayout>
          <template v-slot:header>
            <el-dialog
              title=""
              width="50%"
              :visible="printPdf417Visible"
              @close="printPdf417Visible = false"
            >
              <div style="height: 600px">
                <PrintPdf417
                  ref="printPdf417"
                  :archiveObjects="selectedItems"
                  :isBarCode="true"
                ></PrintPdf417>
              </div>
            </el-dialog>
                <el-dialog title="取批次号"
    :visible.sync="pieceNumVisible"
    @close="pieceNumVisible = false"
    width="640px"
    >
    <el-row>
      <el-form label-width="80px">
        <el-col :span="12">
          <el-form-item label="批次号">
            <el-input v-model="pieceNum" auto-complete="off"></el-input>
          </el-form-item>
          
        </el-col>
        <el-col :span="12">
          <div style="margin-top:6px">
            <el-button type="primary" @click="takePiecesNumber">取批次号</el-button>
          </div>
          
        </el-col>
        
      </el-form>
    </el-row>
      <div slot="footer" class="dialog-footer" style="text-align:center;">
        <el-button type="primary" @click="savePiecesNumber">{{$t('application.ok')}}</el-button>
        <el-button @click="pieceNumVisible = false">{{$t('application.cancel')}}</el-button>
      </div>
    </el-dialog>
            <el-dialog :visible.sync="printVolumesVisible" width="80%">
              <PrintVolumes
                ref="printVolumes"
                v-bind:archiveId="archiveId"
                v-bind:currentFolderId="currentFolder.id"
              ></PrintVolumes>
            </el-dialog>
            <el-dialog
              title="打印条码"
              width="43%"
              :visible="printBarCodeVisible"
              @close="printBarCodeVisible = false"
            >
              <div style="height: 600px">
                <PrintBarCode
                  ref="printBarCode"
                  :archiveObjects="selectedItems"
                  :isBarCode="true"
                ></PrintBarCode>
              </div>
            </el-dialog>
            <el-dialog
              title="打印档号"
              width="43%"
              :visible="printArchiveCodeVisible"
              @close="printArchiveCodeVisible = false"
            >
              <div style="height: 600px">
                <PrintArchiveCode
                  ref="printArchiveCode"
                  :archiveObjects="selectedItems"
                  :isBarCode="true"
                ></PrintArchiveCode>
              </div>
            </el-dialog>
            <el-dialog
              :visible.sync="PreparationTablePrintVisible"
              width="80%"
              @close="PreparationTablePrintVisible = false"
            >
              <PreparationTablePrint
                ref="PreparationTablePrint"
                v-bind:archiveId="archiveId"
                v-bind:currentFolderId="currentFolder.id"
              ></PreparationTablePrint>
            </el-dialog>
            <el-dialog :visible.sync="PrintCoverpageVisible" width="80%">
              <PrintCoverpage ref="PrintCoverpage"></PrintCoverpage>
            </el-dialog>
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
                <el-radio
                  style="margin-right: 5px"
                  v-model="radioValue"
                  label="案卷"
                  @change="changeRadio"
                  >案卷</el-radio
                >
                <el-radio
                  style="margin-left: 5px"
                  v-model="radioValue"
                  label="文件"
                  @change="changeRadio"
                  >文件</el-radio
                >
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  plain
                  size="medium"
                  icon="el-icon-folder-add"
                  @click="addToShopingCart()"
                  >收藏</el-button
                >
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click.native="exportData">{{
                  $t("application.ExportExcel")
                }}</el-button>
              </el-form-item>
              <el-form-item>
                <el-dropdown
                  class="avatar-container right-menu-item"
                  trigger="click"
                >
                  <div class="avatar-wrapper">
                    <i class="el-icon-printer"></i>
                    <span>打印</span>
                  </div>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item divided>
                      <span
                        @click="beforePrintPdf417(selectedItems)"
                        style="display: block"
                      >
                        <i class="el-icon-printer"></i>
                        打印条码
                      </span>
                    </el-dropdown-item>
                    <el-dropdown-item divided>
                      <span
                        @click="
                          beforePrintArchiveCode(selectedItems, '打印档号')
                        "
                        style="display: block"
                      >
                        <i class="el-icon-printer"></i>
                        打印档号
                      </span>
                    </el-dropdown-item>

                    <el-dropdown-item divided>
                      <span
                        @click="beforePrintCoverpage(selectedItems)"
                        style="display: block"
                      >
                        <i class="el-icon-printer"></i>
                        打印封面
                      </span>
                    </el-dropdown-item>
                    <el-dropdown-item divided>
                      <span
                        @click="
                          beforePrintInnerDoc(
                            selectedItems,
                            'ArrangeInnerGridPrint'
                          )
                        "
                        style="display: block"
                      >
                        <i class="el-icon-printer"></i>
                        打印卷内目录
                      </span>
                    </el-dropdown-item>
                    <el-dropdown-item divided>
                      <span
                        @click="beforePrintPreparationTable(selectedItems)"
                        style="display: block"
                      >
                        <i class="el-icon-printer"></i>
                        打印备考表
                      </span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
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
                <el-button
                  type="primary"
                  plain
                  :loading="releaseLoading"
                  size="small"
                  icon="el-icon-right"
                  @click="penddingStorage"
                  title="提交入库"
                  >提交入库</el-button>
              </el-form-item>
              <el-form-item>
                <el-button
                    type="primary"
                    plain
                    size="small"
                    icon="el-icon-folder-add"
                    @click="pieceNumVisible=true"
                    title="生成批次号"
                  >生成批次号</el-button>
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
                    :folderId="tables.main.folderId"
                    :isEditProperty="true"
                    showOptions="查看内容"
                    :isShowChangeList="true"
                    :isshowCustom="true"
                    :optionWidth = "3"
                    @selectchange="onSelectChange"
                    @rowclick="onDataGridRowClick"
                    dataUrl="/dc/getDocuments"
                    :showBatchCheck="true"
                    :sortBackData="true"
                  >
                  </DataGrid>
                </template>
                <template slot="paneR" v-if="isFile">
                  <el-row>
                    <el-col>
                      <DataGrid
                        ref="relevantFileDataGrid"
                        key="relevantFile"
                        v-bind="tables.relevantFileDataGrid"
                        v-bind:tableHeight="
                          ((layout.height - startHeight) * (100 - topPercent)) /
                            100 -
                          bottomHeight
                        "
                        v-bind:isshowOption="true"
                        :isShowChangeList="true"
                        :isshowCustom="true"
                        :optionWidth = "3"
                        :isEditProperty="true"
                        dataUrl="/dc/getDocuments"
                        :showBatchCheck="true"
                      >
                      </DataGrid>
                    </el-col>
                  </el-row>
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
import ExcelUtil from "@/utils/excel.js";
import PreparationTablePrint from "@/views/record/PreparationTablePrint.vue";
import PrintBarCode from "@/views/record/PrintBarCode.vue";
import PrintArchiveCode from "@/views/record/PrintArchiveCode.vue";
import PrintCoverpage from "@/views/record/PrintCoverpage.vue";
import PrintPdf417 from "@/views/record/PrintPdf417.vue";
import PrintVolumes from "@/views/record/PrintVolumes4Archive";
export default {
  data() {
    return {
      inputValueNum: "",
      advCondition: "",
      hiddenInput: "hidden",

      isNotCnpe: true,
      batchDialogVisible: false, //导入对话框可见性

      leftPercent: 20,
      // 本地存储高度名称
      leftStorageName: "PreArchiveftHeight",
      topStorageName: "PreArchiveTopHeight",
      // 非split pan 控制区域高度
      startHeight: 125,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 45,
      // 底部除列表高度
      bottomHeight: 35,

      rightTableHeight: (window.innerHeight - 150) / 2,
      asideHeight: window.innerHeight - 80,
      treeHight: window.innerHeight - 135,
      asideWidth: "100%",

      defaultProps: {
        children: "children",
        label: "name",
      },

      radioValue: "案卷",
      isFile: true,
      isExpand: false,
      releaseLoading: false,
      PreparationTablePrintVisible: false,
      printBarCodeVisible: false,
      PrintCoverpageVisible: false,
      printArchiveCodeVisible: false,
      printPdf417Visible: false,
      printVolumesVisible: false,
      pieceNumVisible:false,
      pieceNum:"",//批次号
      dataList: [],
      itemDataList: [],
      itemDataListFull: [],
      selectedItems: [],
      selectRow: [],
      selectedFileId: "",
      archiveId: "",
      currentPage: 1,
      pageSize: 20,
      judgement: "",
      currentFolder: [],
      currentTreeNode:null,
      tables: {
        main: {
          gridViewName: "GeneralPre",
          condition: " STATUS = '预归档' ",
          folderId: "",
        },
        relevantFileDataGrid: {
          gridViewName: "ArrangeInnerGrid",
          condition: " NAME='irel_children' and IS_HIDDEN=0 ",
          folderId: "",
        },
      },
      tabs: {
        activeNum: "",
      },
      inputFolder: "",
      typeName: "",
    };
  },

  created() {
    this.topPercent = this.getStorageNumber(this.topStorageName, 60);
    this.leftPercent = this.getStorageNumber(this.leftStorageName, 20);
  },

  mounted() {
    let _self = this;

    var psize = localStorage.getItem("docPageSize");
    if (psize) {
      _self.pageSize = parseInt(psize);
    }
    _self.currentLanguage = localStorage.getItem("localeLanguage") || "zh-cn";
    _self.loading = true;
    _self.topPercent = 65;
    _self.$refs.mainDataGrid.loadArchiveInfo("科技与信息","GeneralPre");
    _self.$refs.relevantFileDataGrid.cachePrefix = "PreArchive";
    _self.$refs.relevantFileDataGrid.gridviewInfo.gridviewName = "ArrangeInnerGrid";
    _self.$refs.relevantFileDataGrid.loadArchiveInfo("科技与信息","ArrangeInnerGrid");
    if (_self.inputFolder != "" && _self.inputFolder != undefined) {
      var m = new Map();
      m.set("NAME", _self.inputFolder);
      m.set("parentPath", "/预归档库");
      axios
        .post("/admin/searchFolder", JSON.stringify(m))
        .then(function (response) {
          _self.dataList = response.data.data;
          
          _self.$refs.mainDataGrid.loadCustomGridInfo("GeneralPre");
          _self.$refs.relevantFileDataGrid.loadCustomGridInfo("ArrangeInnerGrid");
          _self.loadGridInfo(_self.defaultData);
          _self.isExpand = true;
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    } else {
      axios
        .post("/admin/getPreArchivesFolder", 0)
        .then(function (response) {
          _self.dataList = response.data.data;
          _self.$refs.mainDataGrid.loadCustomGridInfo("GeneralPre");
          _self.$refs.relevantFileDataGrid.loadCustomGridInfo("ArrangeInnerGrid");
          _self.handleNodeClick(_self.dataList[0]);
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    }
  },

  methods: {
      takePiecesNumber(){
      let _self=this;
      let m=new Map();
      m.set('TYPE_NAME','批次号');
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(m), //JSON.stringify(m),
          url: "/dc/takeNumbersByPolicy"
        })
        .then(function(response) {
          
          let code = response.data.code;
          if (code == 1) {
            //  _self.$message("取号成功！");
            _self.$message({
              showClose: true,
              message: "取号成功！",
              duration: 2000,
              type: "success"
            });
            _self.pieceNum=response.data.data;
           
          } else {
            // _self.$message(response.data.message);
            _self.$message({
              showClose: true,
              message: response.data.message,
              duration: 2000,
              type: "warning"
            });
          }
        })
        .catch(function(error) {
          _self.getNumLoading = false;
          // _self.$message(_self.$t("message.takeNumberFaild"));
          _self.$message({
            showClose: true,
            message: _self.$t("message.takeNumberFaild"),
            duration: 5000,
            type: "error"
          });
          console.log(error);
        });
    },
        savePiecesNumber(){
      let _self=this;
      if(_self.selectedItems==undefined||_self.selectedItems.length==0){
         _self.$message({
              showClose: true,
              message: _self.$t('message.PleaseSelectOneOrMoreData'),
              duration: 2000,
              type: "error"
            });
            return;
      }
      let p=new Array();
      _self.selectedItems.forEach(e=>{
        let m=new Map();
        m.set("ID",e.ID);
        m.set("C_BATCH_CODING2",_self.pieceNum);
        p.push(m);
      });
      console.log(p)
      _self.updateData(p,function(){
        if(_self.$refs.relevantFileDataGrid){
             _self.$refs.relevantFileDataGrid.cleanData();
          }
          _self.pieceNumVisible = false;
        _self.searchItem();
      });
      
    },
    beforePrintArchiveCode(selectedRows, vtitle) {
      let _self = this;
      if (selectedRows == undefined || selectedRows.length == 0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请至少选择一条数据进行打印",
          duration: 2000,
          type: "warning",
        });
        return;
      }
      _self.printArchiveCodeVisible = true;

      setTimeout(() => {
        // _self.$refs.printBarCode.archiveObjects=selectedRows;
        _self.$refs.printArchiveCode.refresh(selectedRows, 1);
      }, 10);
    },

    beforePrintPdf417(selectedRows) {
      let _self = this;
      if (selectedRows == undefined || selectedRows.length == 0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择至少一条数据进行打印",
          duration: 2000,
          type: "warning",
        });
        return;
      }
      _self.printPdf417Visible = true;
      setTimeout(() => {
        _self.$refs.printPdf417.loadData(selectedRows);
      }, 10);
    },
    beforePrintBarCode(selectedRows, vtitle) {
      let _self = this;
      if (selectedRows == undefined || selectedRows.length == 0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择一条数据进行打印",
          duration: 2000,
          type: "warning",
        });
        return;
      }
      _self.printBarCodeVisible = true;

      setTimeout(() => {
        // _self.$refs.printBarCode.archiveObjects=selectedRows;
        _self.$refs.printBarCode.refreshBarCode(selectedRows, 1);
      }, 10);
    },
    beforePrintRidge(selectedRow, gridName, vtitle) {
      let _self = this;
      if (selectedRow.ID == undefined) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择一条数据进行打印",
          duration: 2000,
          type: "warning",
        });
        return;
      }
      _self.printRidgeVisible = true;

      setTimeout(() => {
        _self.$refs.printRidge.dialogQrcodeVisible = false;
        _self.$refs.printRidge.getArchiveObj(selectedRow.ID, gridName, vtitle);
      }, 10);

      _self.printGridName = gridName;
      _self.printObjId = selectedRow.ID;
    },
    beforePrintPreparationTable(selectedRows) {
      let _self = this;
      if (selectedRows == undefined || selectedRows.length == 0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择至少一个案卷进行打印",
          duration: 2000,
          type: "warning",
        });
        return;
      }
      _self.PreparationTablePrintVisible = true;

      setTimeout(() => {
         _self.$refs.PreparationTablePrint.isBusinessArchive = true;
        _self.$refs.PreparationTablePrint.refreshArchiveObj(selectedRows);
      }, 100);
    },
    beforePrintCoverpage(selectedRows) {
      let _self = this;
      if (selectedRows == undefined || selectedRows.length == 0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择至少一个案卷进行打印",
          duration: 2000,
          type: "warning",
        });
        return;
      }
      _self.PrintCoverpageVisible = true;

      setTimeout(() => {
        _self.$refs.PrintCoverpage.refreshArchiveObj(selectedRows);
      }, 100);
    },

    beforePrintInnerDoc(selectedRows, gridName) {
      let _self = this;
      if (selectedRows == undefined || selectedRows.length == 0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择至少一个案卷进行打印",
          duration: 2000,
          type: "warning",
        });
        return;
      }
      _self.printVolumesVisible = true;
      setTimeout(() => {
        _self.axios
          .post("/dc/getPrintArchiveGrid", selectedRows[0].TYPE_NAME)
          .then(function (response) {
            if (response.data.code == "1") {
              let printGridName = response.data.data.attributes.C_TO;
              _self.$refs.printVolumes.dialogQrcodeVisible = false;
              _self.$refs.printVolumes.refreshDataGrid(
                selectedRows,
                printGridName
              );
            } else {
              _self.$refs.printVolumes.dialogQrcodeVisible = false;
              _self.$refs.printVolumes.refreshDataGrid(selectedRows, gridName);
            }
          })
          .catch(function (error) {
            _self.$message({
              showClose: true,
              message: "操作失败",
              duration: 5000,
              type: "error",
            });
            console.log(error);
          });
      }, 100);
    },

    searchFolder() {
      let _self = this;
      if (_self.inputFolder != "" && _self.inputFolder != undefined) {
        var m = new Map();
        m.set("NAME", _self.inputFolder);
        m.set("parentPath", "/预归档库");
        axios
          .post("/admin/searchFolder", JSON.stringify(m))
          .then(function (response) {
            _self.dataList = response.data.data;
            _self.isExpand = true;
            _self.loading = false;
          })
          .catch(function (error) {
            console.log(error);
            _self.loading = false;
          });
      } else {
        axios
          .post("/admin/getPreArchivesFolder", 0)
          .then(function (response) {
            _self.dataList = response.data.data;
            _self.loading = false;
          })
          .catch(function (error) {
            console.log(error);
            _self.loading = false;
          });
      }
    },

    loadGridData(indata) {
      let _self = this;

      var key = _self.inputkey;

      if (key != "") {
        key = " (coding like '%" + key + "%' or title like '%" + key + "%') ";
        if (_self.radioValue == "案卷") {
          key = key + " and C_ITEM_TYPE='案卷' ";
        } else {
          key = key + " and C_ITEM_TYPE='文件' ";
        }
      } else {
        if (_self.radioValue == "案卷") {
          key = key + " C_ITEM_TYPE='案卷' ";
        } else {
          key = key + " C_ITEM_TYPE='文件' ";
        }
      }

      _self.tables.main.condition = key;
      _self.tables.main.folderId = indata.id;
      _self.$nextTick(() => {
        _self.$refs.mainDataGrid.loadGridData();
        _self.$refs.relevantFileDataGrid.cleanData();
      });
    },

    resize(leftPercent) {
      // 左边百分比*100
      this.leftPercent = leftPercent;
      this.setStorageNumber(this.leftStorageName, leftPercent);
    },

    onSplitResize(topPercent) {
      // 顶部百分比*100
      this.topPercent = topPercent;
      this.setStorageNumber(this.topStorageName, topPercent);
    },

    onSelectChange(val) {
      this.selectedItems = val;
    },

    handleNodeClick(indata,node,current) {
      let _self = this;
      _self.advCondition = "";
      _self.selectRow = [];
      _self.selectedFileId = "";
      // let gridView = indata.gridView
      // let archiveType = "科技与信息"
      // _self.$refs.mainDataGrid.loadArchiveInfo(archiveType,gridView)
      // _self.$refs.relevantFileDataGrid.loadArchiveInfo(archiveType,gridView)
      
      _self.currentFolder = indata;
      _self.loading = true;
      axios
        .post("/admin/getFolder", indata.id)
        .then(function (response) {
          indata.children = response.data.data;
          indata.extended = true;
          _self.inputkey = "";
          _self.loading = false;
          _self.loadGridData(indata);
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
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
      let key =
        " FOLDER_ID='" +
        _self.currentFolder.id +
        "' AND C_ITEM_TYPE = '" +
        _self.radioValue +
        "' AND IS_CHILD=0  AND IS_HIDDEN=0 ";

      if (_self.inputValueNum != "" && _self.inputValueNum != undefined) {
        key +=
          "and (CODING LIKE '%" +
          _self.inputValueNum +
          "%' OR TITLE LIKE '%" +
          _self.inputValueNum +
          "%')";
      }

      if (_self.advCondition != undefined && _self.advCondition.length > 0) {
        key += " and (" + _self.advCondition+")";
      }

      _self.$refs.mainDataGrid.condition += key;
      _self.$refs.mainDataGrid.currentPage = 1;
      _self.$refs.mainDataGrid.loadGridData();
    },

    searchItem() {
      this.search();
    },

    onDataGridRowClick: function (row) {
      this.parentID = row.ID;
      var typeChose = row.C_ITEM_TYPE;
      var condition1 =
        "SELECT CHILD_ID from ecm_relation where NAME='irel_children' and PARENT_ID ='" +
        row.ID +
        "'";
      var key1 = "ID IN (" + condition1 + ") AND IS_HIDDEN=0";
      this.$refs.relevantFileDataGrid.condition = key1;
      this.$refs.relevantFileDataGrid.gridViewName = "GeneralPre";
      this.$refs.relevantFileDataGrid.cleanData();
      this.$refs.relevantFileDataGrid.loadGridData();
    },

    reloadGrid() {
      this.loadGridData(this.currentFolder);
    },

    changeRadio(val) {
      let _self = this;
      if (val == "文件") {
        _self.isFile = false;
        _self.topPercent = 99;
        // _self.$refs.mainDataGrid.tableHeight=(window.innerHeight-_self.startHeight);
      } else {
        _self.isFile = true;
        _self.topPercent = 65;
        _self.$nextTick(() => {
          if (_self.$refs.relevantFileDataGrid) {
            _self.$refs.relevantFileDataGrid.cleanData();
          }
        });
      }

      _self.loadGridData(_self.currentFolder);
    },

    exportData() {
      let _self = this;
      let params = {
        URL: "/file/exportFolderPath",
        gridName: _self.currentFolder.gridView,
        folderId: _self.currentFolder.id,
        orderBy: "MODIFIED_DATE desc",
        condition: _self.$refs.mainDataGrid.condition,
        pageSize: _self.pageSize,
        pageIndex: (_self.currentPage - 1) * _self.pageSize,
        lang: "zh-cn",
      };
      console.log(params);
      ExcelUtil.export4Cnpe(params);
    },

    //添加到收藏夹
    addToShopingCart() {
      let _self = this;
      var m = new Map();
      var addItemId = "";
      if (this.selectedItems.length > 0) {
        var addItemId = [];
        if (this.selectedItems.length > 0) {
          for (var i = 0; i < this.selectedItems.length; i++) {
            addItemId.push(this.selectedItems[i].ID);
          }
        }

        axios
          .post("/dc/addToShopingCart", JSON.stringify(addItemId))
          .then(function (response) {
            if (response.data.code) {
              if (_self.showBox) {
                _self.loadAllGridData(_self.currentFolder);
              } else {
                _self.loadGridData(_self.currentFolder);
              }
              _self.$message({
                showClose: true,
                message: _self.$t("message.AddSuccess"),
                duration: 2000,
                type: "success",
              });
            } else {
              _self.$message({
                showClose: true,
                message: "添加失败!",
                duration: 2000,
                type: "warning",
              });
            }
          });
      } else {
        this.$message({
          showClose: true,
          message: "请勾选待添加文件!",
          duration: 2000,
        });
      }
    },

    penddingStorage() {
      let _self = this;
      if (_self.selectedItems.length == 0) {
        _self.$message({
          showClose: true,
          message: _self.$t("message.pleaseSelectDC"),
          duration: 2000,
          type: "warning",
        });
        return;
      }
      _self.$confirm(
        "是否要提交入库？",
      {
        confirmButtonText: _self.$t("application.ok"),
        cancelButtonText: _self.$t("application.cancel"),
        type: "warning"
      }
    )
      .then(() => {
      var m = [];
      let error =""
      let tab = _self.selectedItems;
      var i;
      let j = 1
      for (i in tab) {
        if(tab[i].C_ARCHIVE_CODING==null||tab[i].C_ARCHIVE_CODING==''){
          error +=j+'.'+tab[i].TITLE+'<br/>'
          j++
        }
        if(tab[i].C_ARCHIVE_CODING!=null&&tab[i].C_ARCHIVE_CODING!='')
        m.push(tab[i]["ID"]);
      }
      if(error.length!=0){
        error += '上述文件的文档号为空，已跳过'
        _self.$message({
        dangerouslyUseHTMLString: true,
        message: error,
        type: 'warning'
        })
      }
      if(m.length==0){
        return
      }
      axios
        .post("/record/archiveStorage", JSON.stringify(m), {
          headers: {
            "Content-Type": "application/json;charset=UTF-8",
          },
        })
        .then(function (response) {
          if (response.data.code == 1) {
            _self.$message({
              showClose: true,
              message: _self.$t("message.operationSuccess"),
              duration: 2000,
              type: "success",
            });
            _self.$refs.mainDataGrid.loadGridData();
          } else {
            _self.$message({
              showClose: true,
              message: _self.$t("message.operationFaild"),
              duration: 5000,
              type: "error",
            });
          }
        })
        .catch(function (error) {
          _self.$message({
            showClose: true,
            message: _self.$t("message.operationFaild"),
            duration: 5000,
            type: "error",
          });
          console.log(error);
        });
          });
    },
  },

  components: {
    DataLayout: DataLayout,
    DataGrid: DataGrid,
    AddCondition: AddCondition,
    StartupComponent: StartupComponent,
    ExcelUtil: ExcelUtil,
    PreparationTablePrint: PreparationTablePrint,
    PrintBarCode: PrintBarCode,
    PrintArchiveCode: PrintArchiveCode,
    PrintPdf417: PrintPdf417,
    PrintCoverpage: PrintCoverpage,
    PrintVolumes: PrintVolumes,
  },
};
</script>
<style scoped>
</style>