<template>
  <div>
    <el-dialog
      title="文档借阅"
      :visible.sync="borrowVisible"
      @close="borrowVisible = false"
      width="90%"
      style="width: 100%"
      :close-on-click-modal="false"
      v-dialogDrag
    >
      <div>
        <BorrowStartUp
          :workflowObj="workflow"
          :showUploadFile="true"
          :workflowFileList="selectedItemList"
          @closedialog="closeDialog"
        ></BorrowStartUp>
      </div>
    </el-dialog>
    <el-dialog
      :title="$t('application.openShopingCart')"
      :visible.sync="shopingCartDialogVisible"
      @close="shopingCartDialogVisible = false"
      width="95%"
      style="width: 100%"
      custom-class="customWidth"
    >
      <!-- <ShopingCart
        ref="ShopingCart"
        @onSaved="onSaved"
        width="100%"
        v-bind:shopingCartForm="shopingCartForm"
      ></ShopingCart>-->

      <router-view
        @showOrHiden="showOrHiden"
        ref="ShowShopingCart"
      ></router-view>
      <!-- <div slot="footer" class="dialog-footer">
        <router-link  to="/borroworder"   >借阅</router-link>
        </div>       -->
      <!-- <router-link  to="/borroworder"></router-link> -->
    </el-dialog>
    <el-dialog
      title="文件列表"
      :visible.sync="itemDialogVisible"
      width="96%"
      @close="itemDialogVisible = false"
    >
      <InnerItemViewer
        ref="innerItemViewer"
        v-bind:id="currentId"
        v-bind:tableHeight="innerTableHeight"
      ></InnerItemViewer>
    </el-dialog>
    <el-dialog
      :title="$t('application.property')"
      :visible.sync="propertyVisible"
      @close="propertyVisible = false"
      width="90%"
    >
      <ShowPropertyReadOnly
        ref="ShowProperty"
        @onSaved="onSaved"
        width="100%"
        v-bind:itemId="selectedItemId"
        v-bind:folderId="currentFolder.id"
        v-bind:typeName="currentFolder.typeName"
        :showTypeName="true"
      ></ShowPropertyReadOnly>
      <div slot="footer" class="dialog-footer">
        <el-button @click="propertyVisible = false">{{
          $t("application.cancel")
        }}</el-button>
      </div>
    </el-dialog>
    <el-dialog
      title="选择需要展示的字段"
      :visible.sync="columnsInfo.dialogFormVisible"
      width="40%"
      center
      top="15vh"
      :close-on-click-modal="false"
    >
      <el-checkbox
        :indeterminate="columnsInfo.isIndeterminate"
        v-model="columnsInfo.checkAll"
        @change="handleCheckAllChange"
        >全选</el-checkbox
      >
      <el-checkbox-group v-model="showFields" @change="handleCheckedColsChange">
        <el-checkbox
          v-for="item in gridList"
          :label="item.attrName"
          :key="item.attrName"
          >{{ item.label }}</el-checkbox
        >
      </el-checkbox-group>
      <div slot="footer" class="dialog-footer">
        <el-button
          @click="columnsInfo.dialogFormVisible = false"
          size="medium"
          >{{ $t("application.cancel") }}</el-button
        >
        <el-button type="primary" @click="confirmShow" size="medium"
          >确定</el-button
        >
      </div>
    </el-dialog>

    <div :style="{ position: 'relative', height: asideHeight + 'px' }">
      <split-pane
        split="vertical"
        @resize="resize"
        min-percent="10"
        :default-percent="leftPercent"
      >
        <template slot="paneL">
          <el-breadcrumb style="padding-top: 10px; padding-bottom: 10px">
            <el-breadcrumb-item class="title16">
              <i class="el-icon-receiving"></i>
              &nbsp; {{ $t("route.companyDoc") }}
            </el-breadcrumb-item>
          </el-breadcrumb>

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
                v-model="inputValueNum"
                placeholder="请输入文件夹名称"
                @keyup.enter.native="search()"
              ></el-input>
              <el-button type="primary" @click="search()">{{
                $t("application.SearchData")
              }}</el-button>
            </el-header>

            <el-tree
              style="width: 100%"
              :props="defaultProps"
              :data="dataList"
              node-key="id"
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
                    v-model="inputkey"
                    :placeholder="
                      $t('message.pleaseInput') + $t('application.keyword')
                    "
                    @change="searchItem"
                    prefix-icon="el-icon-search"
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <template v-if="isFileAdmin">
                    <el-button
                      type="primary"
                      plain
                      size="medium"
                      icon="el-icon-bottom"
                      @click="obtainItem()"
                      >{{ $t("application.obtained") }}</el-button
                    >
                    <el-button
                      type="primary"
                      plain
                      size="medium"
                      icon="el-icon-document-delete"
                      @click="destroyItem()"
                      >{{ $t("application.destroy") }}</el-button
                    >
                  </template>
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    plain
                    size="medium"
                    icon="el-icon-folder-add"
                    @click="addToShopingCart()"
                    >添加到收藏</el-button
                  >
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    plain
                    size="medium"
                    icon="el-icon-right"
                    @click="getWorkFlow"
                    >发起借阅</el-button
                  >
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click.native="exportData">{{
                    $t("application.ExportExcel")
                  }}</el-button>
                </el-form-item>
                <el-form-item>
                  <AddCondition
                    v-model="AddConds"
                    :inputType="hiddenInput"
                    @change="searchItem"
                  ></AddCondition>
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
                    <el-row>
                      <el-table
                        :height="
                          ((layout.height - startHeight) * topPercent) / 100 -
                          topbarHeight
                        "
                        :data="itemDataList"
                        border
                        v-loading="tableLoading"
                        @selection-change="selectChange"
                        @row-click="refRowClick"
                        @sort-change="sortchange"
                        style="width: 100%"
                        @header-dragend="onHeaderDragend"
                        fit
                      >
                        <el-table-column
                          type="selection"
                          @selection-change="selectChange"
                          width="50"
                        ></el-table-column>
                        <el-table-column
                          :label="$t('field.indexNumber')"
                          width="70"
                        >
                          <template slot-scope="scope">
                            <span>{{
                              (currentPage - 1) * pageSize + scope.$index + 1
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column width="40">
                          <template slot-scope="scope">
                            <img
                              v-if="scope.row.TYPE_NAME == '图册'"
                              :src="'./static/img/drawing.gif'"
                              :title="scope.row.TYPE_NAME"
                              border="0"
                            />
                            <img
                              v-else-if="scope.row.C_ITEM_TYPE == '案卷'"
                              :src="'./static/img/box.gif'"
                              :title="scope.row.TYPE_NAME"
                              border="0"
                            />
                            <img
                              v-else-if="
                                scope.row.FORMAT_NAME == null ||
                                scope.row.FORMAT_NAME == ''
                              "
                              :src="'./static/img/format/f_undefined_16.gif'"
                              title="无电子文件"
                              border="0"
                            />
                            <img
                              v-else
                              :src="
                                './static/img/format/f_' +
                                scope.row.FORMAT_NAME +
                                '_16.gif'
                              "
                              :title="scope.row.FORMAT_NAME"
                              border="0"
                            />
                          </template> </el-table-column
                        >>
                        <div v-for="(citem, idx) in gridList" :key="idx">
                          <div v-if="citem.visibleType == 1">
                            <el-table-column
                              v-if="(citem.width + '').indexOf('%') > 0"
                              :label="citem.label"
                              :prop="citem.attrName"
                              :min-width="citem.width"
                              :sortable="citem.allowOrderby"
                            >
                              <template slot-scope="scope">
                                <div v-if="citem.attrName.indexOf('DATE') > 0">
                                  <span>{{
                                    dateFormat(scope.row[citem.attrName])
                                  }}</span>
                                </div>
                                <div v-else>
                                  <span>{{
                                    scope.row[citem.attrName]
                                  }}</span>
                                </div>
                              </template>
                            </el-table-column>
                            <el-table-column
                              v-else
                              :label="citem.label"
                              :prop="citem.attrName"
                              :width="citem.width"
                              :sortable="citem.allowOrderby"
                            >
                              <template slot-scope="scope">
                                <div v-if="citem.attrName.indexOf('DATE') > 0">
                                  <span>{{
                                    dateFormat(scope.row[citem.attrName])
                                  }}</span>
                                </div>
                                <div v-else>
                                  <span>{{
                                    scope.row[citem.attrName]
                                  }}</span>
                                </div>
                              </template>
                            </el-table-column>
                          </div>
                        </div>
                        <el-table-column align="left" width="140">
                          <template slot="header">
                            <el-button
                              icon="el-icon-s-grid"
                              size="small"
                              @click="dialogFormShow"
                              title="选择展示字段"
                            ></el-button>
                          </template>
                          <template slot-scope="scope">
                            <el-button
                              type="primary"
                              plain
                              size="small"
                              :title="$t('application.viewContent')"
                              icon="el-icon-picture-outline"
                              @click="showItemContent(scope.row)"
                            ></el-button>
                            <el-button
                              type="primary"
                              plain
                              size="small"
                              :title="$t('application.property')"
                              icon="el-icon-info"
                              @click="showItemProperty(scope.row)"
                            ></el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                      <el-pagination
                        background
                        @size-change="handleSizeChange"
                        @current-change="handleCurrentChange"
                        :current-page="currentPage"
                        :page-sizes="[10, 20, 50, 100, 200]"
                        :page-size="pageSize"
                        layout="total, sizes, prev, pager, next, jumper"
                        :total="itemCount"
                      ></el-pagination>
                    </el-row>
                  </template>
                  <template slot="paneR" v-if="isFile">
                    <el-row>
                      <el-col>
                        <DataGrid
                          ref="relevantFileDataGrid"
                          key="relevantFile"
                          v-bind="tables.relevantFileDataGrid"
                          v-bind:tableHeight="
                            ((layout.height - startHeight) *
                              (100 - topPercent)) /
                              100 -
                            bottomHeight
                          "
                          :isshowOption="true"
                          :isshowSelection="true"
                          :optionWidth = "2"
                          :isEditProperty="false"
                          showOptions="查看内容"
                          dataUrl="/dc/getDocuments"
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
  </div>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
import DataGrid from "@/components/DataGrid";
import AddCondition from "@/views/record/AddCondition";
import ShowPropertyReadOnly from "@/components/ShowPropertyReadOnly";
import InnerItemViewer from "./InnerItemViewer.vue";
import BorrwoForm from "@/components/form/Borrow";
import BorrowFile from "@/views/workflow/BorrowFile.vue";
import BorrowStartUp from "@/views/workflow/BorrowStartUp.vue";
import ExcelUtil from "@/utils/excel.js";
export default {
  components: {
    DataLayout: DataLayout,
    DataGrid: DataGrid,
    ShowPropertyReadOnly: ShowPropertyReadOnly,
    InnerItemViewer: InnerItemViewer,
    BorrwoForm: BorrwoForm,
    BorrowStartUp: BorrowStartUp,
    ExcelUtil: ExcelUtil,
    AddCondition: AddCondition,
  },
  data() {
    return {
      currentuser: {
        user: {},
        roles: [],
      },
      columnsInfo: {
        checkAll: true,
        dialogFormVisible: false,
        isIndeterminate: false,
      },

      leftPercent: 20,
      // 本地存储高度名称
      leftStorageName: "PreArchiveftHeight",
      topStorageName: "PreArchiveTopHeight",
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 55,
      // 底部除列表高度
      bottomHeight: 35,
      AddConds: "",
      innerTableHeight: window.innerHeight - 360,
      asideHeight: window.innerHeight - 80,
      treeHeight: window.innerHeight - 120,
      asideWidth: "100%",
      currentLanguage: "zh-cn",
      propertyVisible: false,
      borrowVisible: false,
      hiddenInput: "hidden",
      loading: false,
      tableLoading: false,
      currentFolder: [],
      isFileAdmin: false,
      showFields: [],
      pageSize: 20,
      dataList: [],
      gridList: [],
      itemDataList: [],
      itemDataListFull: [],
      showBox: false,
      orderBy: "",
      itemCount: 0,
      inputkey: "",
      currentPage: 1,
      selectedItemId: "",
      defaultProps: {
        children: "children",
        label: "name",
      },
      selectedItemList: [],
      itemDialogVisible: false,
      currentId: "",
      disable: true,
      exportAble: false,
      formLabelWidth: "100px",
      borrowData: [],
      dialogTitle: "借阅",
      borrowDialogVisible: false,
      shopingCartDialogVisible: false,
      defaultData: {
        gridView: "GeneralView",
      },
      borrowForm: {
        taskId: 0,
        result: "在线浏览",
        message: "",
      },

      tables: {
        relevantFileDataGrid: {
          gridViewName: "GeneralPre",
          condition: " NAME='irel_children' and IS_HIDDEN=0 ",
          folderId: "",
        },
      },

      workflow: {},
      gridViewTrans: "",
      idTrans: "",
      inputValueNum: "",
      radioValue: "案卷",
      isFile: true,
      isExpand: false,
    };
  },
  created() {
    this.topPercent = this.getStorageNumber(this.topStorageName, 60);
    this.leftPercent = this.getStorageNumber(this.leftStorageName, 20);
    var username = sessionStorage.getItem("access-userName");
    let _self = this;
    axios.post("/user/getGroupByUserName", username).then(function (response) {
      var groupList = response.data.data;
      groupList.forEach(function (val, index, arr) {
        if (val.name == "档案管理员") {
          _self.isFileAdmin = true;
        }
      });
    });
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
    _self.search();
  },
  methods: {
    changeRadio(val) {
      let _self = this;
      if (val == "文件") {
        _self.isFile = false;
        _self.topPercent = 99;
        _self.startHeight = 145;
      } else {
        _self.startHeight = 135;
        _self.isFile = true;
        _self.topPercent = 65;
        _self.$nextTick(() => {
          if (_self.$refs.relevantFileDataGrid) {
            _self.$refs.relevantFileDataGrid.itemDataList = [];
          }
        });
      }

      _self.loadGridData(_self.currentFolder);
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

    search() {
      let _self = this;
      if (_self.inputValueNum != "" && _self.inputValueNum != undefined) {
        var m = new Map();
        m.set("NAME", _self.inputValueNum);
        m.set("parentPath", "/档案库");
        axios
          .post("/admin/searchFolder", JSON.stringify(m))
          .then(function (response) {
            _self.dataList = response.data.data;
            _self.loadGridInfo(_self.defaultData);
            _self.loading = false;
          })
          .catch(function (error) {
            console.log(error);
            _self.loading = false;
          });
      } else {
        axios
          .post("/admin/getArchivesFolder", 0)
          .then(function (response) {
            _self.dataList = response.data.data;
            _self.loadGridInfo(_self.defaultData);
            _self.loading = false;
          })
          .catch(function (error) {
            console.log(error);
            _self.loading = false;
          });
      }
    },
    getWorkFlow() {
      let _self = this;

      var m = new Map();
      m.set("processDefinitionKey", "文档借阅流程");

      axios
        .post("/dc/getWorkflow", JSON.stringify(m))
        .then(function (response) {
          _self.workflow = response.data.data[0];
          console.log(_self.workflow);
          _self.borrowVisible = true;
        })
        .catch(function (error) {
          console.log(error);
        });
    },

    resize() {
      //console.log('resize')
      this.asideWidth = "100%";
    },
    // 加载表格样式
    initGridInfo() {
      let _self = this;
      _self.loading = true;
      var m = new Map();
      m.set("gridName", "GeneralGrid");
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
    rowClick(row) {
      this.currentId = row.ID;
      if (row.TYPE_NAME == "卷盒" || row.C_ITEM_TYPE == "案卷") {
        //this.itemDialogVisible = true;
        let _self = this;
        setTimeout(() => {
          _self.$refs.innerItemViewer.id = _self.currentId;
          _self.$refs.innerItemViewer.loadGridInfo();
          //_self.$refs.innerItemViewer.bindData();
        }, 100);
      }
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
    // 查看内容
    showItemContent(indata) {
      let condition = indata.ID;
      let href = this.$router.resolve({
        path: "/viewdoc",
        query: {
          id: condition,
          //token: sessionStorage.getItem('access-token')
        },
      });
      //console.log(href);
      window.open(href.href, "_blank");
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
            _self.inputkey = "";
            indata.extended = true;
            _self.loading = false;
          })
          .catch(function (error) {
            console.log(error);
            _self.loading = false;
          });
      }
      _self.loadGridInfo(indata);
      if (_self.showBox) {
        _self.loadAllGridData(indata);
      } else {
        _self.loadGridData(indata);
      }
    },
    showOrHiden(b) {
      this.shopingCartDialogVisible = b;
    },
    sortchange(column) {
      this.orderBy =
        column.column.property + column.column.order == "ascending"
          ? " ASC"
          : " DESC";
    },
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
    // 加载表格数据
    loadGridData(indata) {
      let _self = this;
      _self.tableLoading = true;
      var key = _self.inputkey;
      var m = new Map();
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
      if (_self.AddConds != "") {
        key +=
          " and C_ITEM_TYPE = " + _self.radioValue + " and " + _self.AddConds;
      }
      _self.gridViewTrans = indata.gridView;
      _self.idTrans = indata.id;
      m.set("gridName", indata.gridView);
      m.set("folderId", indata.id);
      m.set("condition", key);
      m.set("pageSize", _self.pageSize);
      m.set("pageIndex", _self.currentPage - 1);
      m.set("orderBy", "MODIFIED_DATE desc");
      console.log(m);
      axios
        .post("/exchange/doc/getExceptBoxDocuments", JSON.stringify(m))
        .then(function (response) {
          _self.itemDataList = response.data.data;
          _self.itemDataListFull = response.data.data;
          _self.itemCount = response.data.pager.total;
          //_self.tableHeight = window.innerHeight - 170;
          //console.log(JSON.stringify(response.data.datalist));
          _self.tableLoading = false;
        }).catch(function(error) {
        console.log(error);
        _self.tableLoading = false;
      });
    },
    //获取包含卷盒在内的所有信息
    loadAllGridData(indata) {
      let _self = this;
      _self.tableLoading = true;
      var key = _self.sqlStringFilter(_self.inputkey);
      var m = new Map();
      m.set("gridName", indata.gridView);
      m.set("folderId", indata.id);
      m.set("condition", key);
      m.set("pageSize", _self.pageSize);
      m.set("pageIndex", _self.currentPage - 1);
      m.set("orderBy", "MODIFIED_DATE desc");
      axios
        .post("/dc/getContainBoxDocuments", JSON.stringify(m))
        .then(function (response) {
          _self.itemDataList = response.data.data;
          _self.itemDataListFull = response.data.data;
          _self.itemCount = response.data.pager.total;
          //console.log(JSON.stringify(response.data.datalist));
          _self.tableLoading = false;
        }).catch(function(error) {
        console.log(error);
        _self.tableLoading = false;
      });
    },
    selectChange(selection) {
      this.selectedItemList = [];
      if (selection.length > 0) {
        for (var i = 0; i < selection.length; i++) {
          this.selectedItemList.push(selection[i]);
        }
      }
    },

    refRowClick: function (row) {
      this.parentID = row.ID;
      var typeChose = row.C_ITEM_TYPE;
      var condition1 =
        "SELECT CHILD_ID from ecm_relation where NAME='irel_children' and PARENT_ID ='" +
        row.ID +
        "'";
      var key1 = "ID IN (" + condition1 + ") AND IS_HIDDEN=0";
      this.$refs.relevantFileDataGrid.condition = key1;
      this.$refs.relevantFileDataGrid.gridViewName = "GeneralPre";
      this.$refs.relevantFileDataGrid.itemDataList = [];
      this.$refs.relevantFileDataGrid.loadGridData();
    },
    //展示勾选弹框
    dialogFormShow() {
      this.columnsInfo.dialogFormVisible = true;
    },
    //弹框中全选，展示所有属性
    handleCheckAllChange(val) {
      this.showFields = [];
      if (val) {
        this.gridList.forEach((element) => {
          this.showFields.push(element.attrName);
        });
      }
      this.columnsInfo.isIndeterminate = false;
    },
    //单个勾选属性
    handleCheckedColsChange(value) {
      let checkedCount = value.length;
      this.columnsInfo.checkAll = checkedCount === this.gridList.length;
      this.columnsInfo.isIndeterminate =
        checkedCount > 0 && checkedCount < this.gridList.length;
    },
    //确认按钮显示哪些属性
    confirmShow() {
      let _self = this;
      _self.gridList.forEach((element) => {
        element.visibleType = 2;
      });
      _self.showFields.forEach((element) => {
        let item = _self.getgriditem(element);
        if (item) {
          item.visibleType = 1;
        }
      });
      this.columnsInfo.dialogFormVisible = false;
    },
    getgriditem(attrName) {
      let _self = this;
      let ret = null;
      _self.gridList.forEach((element) => {
        if (element.attrName == attrName) {
          ret = element;
          return;
        }
      });
      return ret;
    },
    //搜索
    searchItem() {
      let _self = this;
      _self.loadGridInfo(_self.currentFolder);
      if (_self.showBox) {
        _self.loadAllGridData(_self.currentFolder);
      } else {
        _self.loadGridData(_self.currentFolder);
      }
    },

    closeDialog(val) {
      this.borrowVisible = val;
    },
    //借阅
    borrowItem() {
      let _self = this;
      var obtainItemId = [];
      if (this.selectedItemList.length > 0) {
        for (var i = 0; i < this.selectedItemList.length; i++) {
          obtainItemId.push(this.selectedItemList[i].ID);
        }
      }
      _self.borrowDialogVisible = true;
    },
    //导出Excel
    // exportExcel() {
    //   var url = "/dc/getExportExcel";
    //   var m = new Map();
    //   if (this.exportAble) {
    //     if (this.showBox) {
    //       m.set("showBox", true);
    //     } else {
    //       m.set("showBox", false);
    //     }
    //     m.set("gridName", this.currentFolder.gridView);
    //     m.set("lang", this.currentLanguage);
    //     m.set("folderId", this.currentFolder.id);
    //     m.set("orderBy", "MODIFIED_DATE desc");
    //     axios
    //       .post(url, JSON.stringify(m), {
    //         responseType: "blob",
    //       })
    //       .then((res) => {
    //         let fileName = res.headers["content-disposition"]
    //           .split(";")[1]
    //           .split("=")[1]
    //           .replace(/\"/g, "");
    //         let type = res.headers["content-type"];
    //         let blob = new Blob([res.data], { type: type });
    //         // IE
    //         if (window.navigator.msSaveBlob) {
    //           window.navigator.msSaveBlob(blob, fileName);
    //         } else {
    //           // console.log(3)
    //           var link = document.createElement("a");
    //           link.href = window.URL.createObjectURL(blob);
    //           link.download = fileName;
    //           link.click();
    //           //释放内存
    //           window.URL.revokeObjectURL(link.href);
    //         }
    //       });
    //   } else {
    //     this.$message({
    //       showClose: true,
    //       message: "请在文档目录下进行操作!",
    //       duration: 2000,
    //     });
    //   }
    // },
    //下架文档
    obtainItem() {
      let _self = this;
      var obtainItemId = [];
      if (this.selectedItemList.length > 0) {
        for (var i = 0; i < this.selectedItemList.length; i++) {
          obtainItemId.push(this.selectedItemList[i].ID);
        }
        axios
          .post("/dc/obtainDocument", JSON.stringify(obtainItemId))
          .then(function (response) {
            if (response.data.code) {
              if (_self.showBox) {
                _self.loadAllGridData(_self.currentFolder);
              } else {
                _self.loadGridData(_self.currentFolder);
              }
              _self.$message({
                showClose: true,
                message: response.data.msg,
                duration: 2000,
                type: "success",
              });
            } else {
              _self.$message({
                showClose: true,
                message: response.data.msg,
                duration: 2000,
                type: "warning",
              });
            }
          });
      } else {
        this.$message({
          showClose: true,
          message: "请勾选待下架文件!",
          duration: 2000,
        });
      }
    },
    //销毁文档
    destroyItem() {
      let _self = this;
      var deletItemId = [];
      if (this.selectedItemList.length > 0) {
        for (var i = 0; i < this.selectedItemList.length; i++) {
          deletItemId.push(this.selectedItemList[i].ID);
        }
        axios
          .post("/dc/destroyDocuments", JSON.stringify(deletItemId))
          .then(function (response) {
            if (response.data.code) {
              if (_self.showBox) {
                _self.loadAllGridData(_self.currentFolder);
              } else {
                _self.loadGridData(_self.currentFolder);
              }
              _self.$message({
                showClose: true,
                message: "销毁成功!",
                duration: 2000,
                type: "success",
              });
            } else {
              _self.$message({
                showClose: true,
                message: "销毁失败!",
                duration: 2000,
                type: "warning",
              });
            }
          });
      } else {
        this.$message({
          showClose: true,
          message: "请勾选待销毁文件!",
          duration: 2000,
        });
      }
    },
    //添加到收藏夹
    addToShopingCart() {
      let _self = this;
      var m = new Map();
      var addItemId = "";
      if (this.selectedItemList.length > 0) {
        var addItemId = [];
        if (this.selectedItemList.length > 0) {
          for (var i = 0; i < this.selectedItemList.length; i++) {
            addItemId.push(this.selectedItemList[i].ID);
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
    //添加到借阅单
    openShopingCart() {
      let _self = this;
      _self.shopingCartDialogVisible = true;
      _self.$router.push({
        path: "/borrow",
      });
      /*       if (
        _self.$refs.ShowShopingCart &&
        _self.$refs.ShowShopingCart.componentName == "shopingCart"
      ) {
        _self.$refs.ShowShopingCart.openShopingCart();
      } */
      // var arg = [];
      //   axios
      //     .post("/dc/openShopingCart", JSON.stringify(arg))
      //     .then(function(response) {
      //       if (response.data.code) {
      //         _self.shopingCartDialogVisible = true;
      //         // setTimeout(()=>{
      //           // _self.$refs.ShopingCart.dataList = response.data.data;
      //           _self.$router.push({
      //             path:'/ShopingCart',
      //              query: { tabledata: response.data.data }
      //           });
      //           if(_self.$refs.ShowShopingCart && _self.$refs.ShowShopingCart.componentName=="shopingCart"){
      //              _self.$refs.ShowShopingCart.openShopingCart();
      //           }
      //         // },10);

      //       } else {
      //         _self.$message({
      //           showClose: true,
      //           message: "打开失败!",
      //           duration: 2000,
      //           type: "warning"
      //         });
      //       }
      //     });
    },
    //查看属性
    showItemProperty(indata) {
      let _self = this;
      _self.selectedItemId = indata.ID;
      _self.propertyVisible = true;
      _self.itemDialogVisible = false;
      if (_self.$refs.ShowProperty) {
        _self.$refs.ShowProperty.myItemId = indata.ID;
        _self.$refs.ShowProperty.loadFormInfo();
      }
    },
    showFileBox() {
      if (this.showBox) {
        this.loadAllGridData(this.currentFolder);
      } else {
        this.loadGridData(this.currentFolder);
      }
    },
    //分页 页数改变
    handleSizeChange(val) {
      let _self = this;
      this.pageSize = val;
      localStorage.setItem("docPageSize", val);
      _self.loadGridInfo(this.currentFolder);
      if (_self.showBox) {
        _self.loadAllGridData(this.currentFolder);
      } else {
        _self.loadGridData(this.currentFolder);
      }
    },
    // 分页 当前页改变
    handleCurrentChange(val) {
      let _self = this;
      this.currentPage = val;
      _self.loadGridInfo(this.currentFolder);
      if (_self.showBox) {
        _self.loadAllGridData(this.currentFolder);
      } else {
        _self.loadGridData(this.currentFolder);
      }
    },
    // on save todo
    onSaved(val) {
      let _self = this;
      this.currentPage = val;
      _self.loadGridInfo(this.currentFolder);
      if (_self.showBox) {
        _self.loadAllGridData(this.currentFolder);
      } else {
        _self.loadGridData(this.currentFolder);
      }
    },

    exportData() {
      let _self = this;
      let params = {
        URL: "/file/exportFolderPath",
        gridName: _self.currentFolder.gridView,
        folderId: _self.currentFolder.id,
        orderBy: "MODIFIED_DATE desc",
        pageSize: _self.pageSize,
        pageIndex: _self.currentPage - 1,
        lang: "zh-cn",
      };
      console.log(params);
      ExcelUtil.export4Cnpe(params);
    },
  },
};
</script>
<style scoped>
.el-container {
  height: 100%;
}
/* .el-aside {
  height: 680px;
} */
</style>
<style>
</style>