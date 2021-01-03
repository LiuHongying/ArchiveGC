<template>
  <DataLayout>
    <template v-slot:header>
      <!-- 创建附件 -->
      <el-dialog :title="$t('application.Import')" :visible.sync="importdialogVisible" width="70%" :close-on-click-modal="false">
          <el-form size="mini" :label-width="formLabelWidth" v-loading='uploading'>
              <div style="height:200px;overflow-y:scroll; overflow-x:scroll;">
              <el-upload
                  :limit="100"
                  :file-list="fileList"
                  action
                  :on-change="handleChange"
                  :auto-upload="false"
                  :multiple="true"
              >
                  <el-button slot="trigger" size="small" type="primary">{{$t('application.selectFile')}}</el-button>
              </el-upload>
              </div>
          </el-form>
          <div slot="footer" class="dialog-footer">
              <el-button @click="importdialogVisible = false">{{$t('application.cancel')}}</el-button>
              <el-button type="primary" @click="uploadData()">{{$t('application.start')+$t('application.Import')}}</el-button>
          </div>
      </el-dialog>
      <el-dialog
      :title="uploadType == 0 ? '更新主文件' : '更新格式副本'"
      :visible.sync="udialogVisible"
      v-loading="uploading"
    >
      <el-form label-position="right" label-width="120px">
        <el-form-item :label="$t('message.file')" :label-width="formLabelWidth">
          <el-upload
            :limit="1"
            :file-list="newFileList"
            action
            :on-change="handleFileChange"
            :auto-upload="false"
            :multiple="false"
          >
            <el-button slot="trigger" size="small" type="primary">{{
              $t("application.selectFile")
            }}</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="udialogVisible = false">{{
          $t("application.cancel")
        }}</el-button>
        <el-button type="primary" @click="updateNewFile()">{{
          $t("application.ok")
        }}</el-button>
      </div>
    </el-dialog>
      <el-dialog
      :title="$t('route.movepos')"
      :visible.sync="moveDialogVisible"
      @close="moveDialogVisible = false"
    >
      <el-form>
        <el-form-item
          :label="$t('route.currentid')"
          :label-width="formLabelWidth"
          >{{ getSelectedIds() }}</el-form-item
        >
        <el-form-item
          :label="$t('route.desfolderid')"
          :label-width="formLabelWidth"
        >
          <FolderSelector parentId="eeb9d742cdef4e42b43796b6ac56b5ac"
            v-model="targetFolderId"
            v-bind:inputValue="targetFolderId"
          ></FolderSelector>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleMoveItem()">{{
          $t("application.ok")
        }}</el-button>
        <el-button @click="moveDialogVisible = false">{{
          $t("application.cancel")
        }}</el-button>
      </div>
    </el-dialog>
    <el-dialog :title="$t('message.Batch')+' '+$t('application.Import')+$t('application.document')" :visible.sync="batchDialogVisible" width="80%" >
        <BatchImport ref="BatchImport" tmpPath="/系统配置/导入模板/文件导入模板" :deliveryId="currentFolder.id" relationName="FolderId"  
        @onImported="onBatchImported" width="100%" importUrl="/import/batchSystemImport"></BatchImport>
        <div slot="footer" class="dialog-footer">
          <el-button @click="batchDialogVisible=false" size="medium">{{$t('application.close')}}</el-button>
        </div>
      </el-dialog>

      <el-dialog title="新建文件" :visible.sync="createDocVisible"
      @close="createDocVisible=false"
      width="90%"
      style="width:100%"
       :close-on-click-modal="false"
        v-dialogDrag
      >
        <CreateCommonFile ref="createCommonFile" :typeName="typeName" 
        :folderId="currentFolder.id"
        @afterSave="afterSave()"></CreateCommonFile>
        <div slot="footer" class="dialog-footer">
          <el-button @click="saveDoc">保存</el-button>
          <el-button @click="createDocVisible = false">{{
            $t("application.cancel")
          }}</el-button>
      </div>
      </el-dialog>

    <el-dialog
      title="文档借阅"
      :visible.sync="borrowVisible"
      @close="borrowVisible = false"
      width="90%"
      style="width: 100%"
      :close-on-click-modal="false"
      v-dialogDrag
    >
      <div><BorrowStartUp :workflowObj="workflow" :showUploadFile="true" :workflowFileList="itemDataList"></BorrowStartUp></div>
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
      <ShowProperty
        ref="ShowProperty"
        @onSaved="onSaved"
        width="100%"
        v-bind:itemId="selectedItemId"
        v-bind:folderId="currentFolder.id"
        v-bind:typeName="currentFolder.typeName"
      ></ShowProperty>
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
    </template>

    <template v-slot:main="{layout}">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <split-pane v-on:resize="onSplitResize" :min-percent='15' :default-percent='topPercent' split="vertical">
          <template slot="paneL">
            <el-breadcrumb style="padding-top: 10px; padding-bottom: 10px">
              <el-breadcrumb-item class="title16">
                <i class="el-icon-receiving"></i>
                &nbsp; {{ $t("route.docStorage") }}
              </el-breadcrumb-item>
            </el-breadcrumb>

            <!-- <el-container
              :style="{
                height: treeHeight + 'px',
                width: asideWidth,
                overflow: 'auto',
              }"
            > -->
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
          <!-- </el-container> -->
          </template>
        <template slot="paneR">
          <el-row>
          <el-form :inline="true">
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
            <!-- <el-form-item>
              <el-button
                type="primary"
                plain
                size="medium"
                icon="el-icon-folder-add"
                @click="addToShopingCart()"
                >{{ $t("application.addToShopingCart") }}</el-button
              >
            </el-form-item> -->
            <el-form-item>
              <el-button type="primary" icon="el-icon-upload2" @click="batchDialogVisible=true">批量导入</el-button>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" plain size="medium" icon="el-icon-plus" @click="beforeCreateFile">新建文件</el-button>
            </el-form-item>
            <!-- <el-form-item>
              <el-button type="primary" plain size="medium" icon="el-icon-right" @click="getWorkFlow">发起流程</el-button>
            </el-form-item> -->
            <el-form-item>
              <el-button icon="el-icon-delete"
                  type="warning"
                  @click="
                    onDeleleItem(selectedItemList, [$refs.mainDataGrid])
                  "
                  >{{ $t("application.delete") }}</el-button
                >
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click.native="exportData" icon="el-icon-download">{{$t("application.ExportExcel")}}</el-button>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                icon="el-icon-top-right"
                @click="moveItem()"
                >{{ $t("application.move") }}</el-button>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                icon="el-icon-upload2"
                @click="showUpdateFile(0)"
                >{{ $t("application.update") }}</el-button
              >
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                icon="el-icon-upload2"
                @click="showUpdateFile(1)"
                >{{ $t("application.transcript") }}</el-button
              >
            </el-form-item>
            <el-form-item>
              <template v-if="isFileAdmin">
                <!-- `checked` 为 true显示卷宗 或 false不显示卷宗 -->
                <el-checkbox
                  v-model="showBox"
                  :disabled="disable"
                  @change="showFileBox"
                  >{{
                    $t("application.show") + $t("application.fileBox")
                  }}</el-checkbox
                >
              </template>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="beforeUploadFile('/dc/addAttachment')">添加附件</el-button>
            </el-form-item>
          </el-form>
          </el-row>
          <el-row :style="'height:'+layout.height-startHeight+80">
            <DataGrid
                ref="mainDataGrid"
                key="main"
                dataUrl="/dc/getDocuments"
                v-bind:tableHeight="(layout.height-startHeight-75)"
                v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                :gridViewName="gridViewName"
                :optionWidth = "2"
                :condition="condition"
                :isshowCustom="false"
                :isEditProperty="true"
                :folderId="currentFolder.id"
                showOptions="查看内容"
                :isShowChangeList="false"
                :isInitData="false"
                @rowclick="rowClick"
                @selectchange="selectChange"
            ></DataGrid>
          </el-row>
        </template>
        </split-pane>
      </div>
    </template>
  </DataLayout>
</template>
<script>
import ShowProperty from "@/components/ShowProperty";
import InnerItemViewer from "./InnerItemViewer.vue";
import BorrwoForm from "@/components/form/Borrow";
import StartupWorkflow from "@/views/workflow/BorrowStartUp.vue";
import BorrowFile from "@/views/workflow/BorrowFile.vue";
import BorrowStartUp from "@/views/workflow/BorrowStartUp.vue"
import ExcelUtil from "@/utils/excel.js";
import DataGrid from "@/components/DataGrid";
import DataLayout from '@/components/ecm-data-layout'
import CreateCommonFile from "@/views/npc/CreateCommonFile"
import BatchImport from "@/components/controls/ImportDocument";
import FolderSelector from "@/components/controls/FolderSelector";
export default {
  components: {
    ShowProperty: ShowProperty,
    InnerItemViewer: InnerItemViewer,
    BorrwoForm: BorrwoForm,
    StartupWorkflow: StartupWorkflow,
    BorrowStartUp: BorrowStartUp,
    ExcelUtil: ExcelUtil,
    DataGrid:DataGrid,
    DataLayout:DataLayout,
    CreateCommonFile:CreateCommonFile,
    BatchImport:BatchImport,
    FolderSelector:FolderSelector
  },
  data() {
    return {
      // 非split pan 控制区域高度
      startHeight: 130,
      // 顶部百分比*100
      topPercent: 15,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 120,
      buttLoading:false,
      currentuser: {
        user: {},
        roles: [],
      },
      columnsInfo: {
        checkAll: true,
        dialogFormVisible: false,
        isIndeterminate: false,
      },
      innerTableHeight: window.innerHeight - 360,
      tableHeight: window.innerHeight - 170,
      asideHeight: window.innerHeight - 100,
      treeHeight: window.innerHeight - 120,
      asideWidth: "100%",
      currentLanguage: "zh-cn",
      propertyVisible: false,
      borrowVisible: false,
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
      selectedItems:[],
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
        gridView: "GeneralGrid",
      },
      borrowForm: {
        taskId: 0,
        result: "在线浏览",
        message: "",
      },
      newFileList: [],
      fileList:[],
      workflow: {},
      gridViewTrans: "",
      idTrans: "",
      gridViewName:"CommonFileGrid",
      createDocVisible:false,
      typeName:"",
      batchDialogVisible:false,
      moveDialogVisible:false,
      targetFolderId:"",
      isMoveFolder: false,
      uploadFile: null,
      uploadType: 0, //0：主格式，1：格式副本
      udialogVisible: false,
      uploading: false,
      importdialogVisible:false,
      uploadUrl:"",
      condition:""
    };
  },
  created() {
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
    axios
      .post("/admin/getFoldersByPath", "/文件库")
      .then(function (response) {
        _self.dataList = response.data.data;
        console.log(JSON.stringify(_self.dataList));
        _self.loading = false;
      })
      .catch(function (error) {
        console.log(error);
        _self.loading = false;
      });
    _self.loadGridInfo(_self.defaultData);
  },
  methods: {
    getFormData() {
            let _self = this;
            let formdata = new FormData();
            var data = {};
            data["parentDocId"] = _self.selectedItems[0].ID;//_self.selectedInnerItems[0].ID;//_self.selectedFileId;
            data["relationName"]='附件';
            data["TYPE_NAME"]='附件';
            formdata.append("metaData", JSON.stringify(data));
            _self.fileList.forEach(function(file) {
                //console.log(file.name);
                formdata.append("uploadFile", file.raw, file.name);
            });
            return formdata;
            },
    //上传文件
    uploadData() {
        let _self = this;
        let formdata = _self.getFormData();
        console.log("UploadData getData");
        console.log(formdata);
        _self.uploading=true;
        _self
            .axios({
            headers: {
                "Content-Type": "application/json;charset=UTF-8"
            },
            datatype: "json",
            method: "post",
            data: formdata,
            url: _self.uploadUrl
            })
            .then(function(response) {
            _self.importdialogVisible = false;
            // _self.refreshData();
            _self.uploading=false;
            // _self.$message(_self.$t('application.Import')+_self.$t('message.success'));
            _self.$message({
                    showClose: true,
                    message: _self.$t('application.Import')+_self.$t('message.success'),
                    duration: 2000,
                    type: 'success'
                });
            })
            .catch(function(error) {
            _self.uploading=false;
            console.log(error);
            });
        },
    beforeUploadFile(uploadpath){
        let _self=this;
        if(_self.selectedItems==undefined||_self.selectedItems.length==0){
            // _self.$message('请选择一条文件数据');
            _self.$message({
                    showClose: true,
                    message: _self.$t('message.PleaseSelectOneFile'),
                    duration: 2000,
                    type: "warning"
                });
            return;
        }
        _self.uploadUrl=uploadpath;
        _self.fileList=[];
        _self.importdialogVisible=true;
            
      },
    handleFileChange(file, fileList) {
      this.uploadFile = file;
    },
    handleChange(file, fileList) {
        this.fileList = fileList;
    },
    updateNewFile() {
      let _self = this;
      if (_self.selectedItems && _self.selectedItems.length > 0) {
        if (_self.uploadType == 0) {
          _self.uploadPrimry();
        } else {
          _self.uploadRendition();
        }
      }
    },
    uploadPrimry() {
      let _self = this;
      _self.uploading = true;
      let formdata = new FormData();
      formdata.append("id", _self.selectedItems[0].ID);
      if (_self.uploadFile != "") {
        formdata.append("uploadFile", _self.uploadFile.raw);
      }
      axios
        .post("/dc/updatePrimaryContent", formdata, {
          "Content-Type": "multipart/form-data",
        })
        .then(function (response) {
          _self.udialogVisible = false;
          _self.loadGridData(_self.currentFolder);
          _self.$message("更新成功!");
          _self.uploading = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.uploading = false;
        });
    },
    uploadRendition() {
      let _self = this;
      _self.uploading = true;
      let formdata = new FormData();
      var data = {};
      data["ID"] = _self.selectedItems[0].ID; //_self.selectedInnerItems[0].ID;//_self.selectedFileId;
      formdata.append("metaData", JSON.stringify(data));

      if (_self.uploadFile != "") {
        formdata.append("uploadFile", _self.uploadFile.raw);
      }
      axios
        .post("/dc/addRendition", formdata, {
          "Content-Type": "multipart/form-data",
        })
        .then(function (response) {
          _self.udialogVisible = false;
          _self.$message("更新成功!");
          _self.uploading = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.uploading = false;
        });
    },
    showUpdateFile(indata) {
      if (this.selectedItems && this.selectedItems.length > 0) {
        this.uploadFile = [];
        this.newFileList=[];
        this.uploadType = indata;
        this.udialogVisible = true;
      }else{
        this.$message({
            showClose: true,
            message: "请选择一条要修改的数据",
            duration: 2000,
            type: "error",
          });
      }
    },
    moveItem() {
      this.isMoveFolder = false;
      if(!this.selectedItems&&this.selectedItems.length==0){
        this.$message({
            showClose: true,
            message: "请选择一条或多条要移动的数据",
            duration: 2000,
            type: "error",
          });
        return;
      }
      if (this.selectedItems && this.selectedItems.length > 0) {
        this.moveDialogVisible = true;
      }
    },
    getSelectedIds() {
      if (this.isMoveFolder) {
        return this.currentFolder.id;
      } else {
        var str = "";
        this.selectedItems.forEach(function (val) {
          str += val.ID + ";";
        });
        return str;
      }
    },
    handleMoveItem() {
      if (this.isMoveFolder) {
        this.handleMoveFolder();
      } else {
        this.handleMoveDocument();
      }
    },
    handleMoveFolder() {
      let _self = this;
      if (
        _self.targetFolderId == _self.currentFolder.id ||
        _self.targetFolderId == _self.currentFolder.parentId
      ) {
        _self.$message("目标文件夹不能更当前文件夹相同!");
        return;
      }
      var fld = _self.currentFolder;
      fld.parentId = _self.targetFolderId;
      axios
        .post("/admin/updateFolder", JSON.stringify(fld))
        .then(function (response) {
          _self.$message(_self.$t("message.saveSuccess"));
          _self.moveDialogVisible = false;
          _self.loading = false;
          _self.refreshFolderData();
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    handleMoveDocument() {
      let _self = this;
      _self.loading = true;
      var m = new Map();
      m.set("ids", _self.getSelectedIds());
      m.set("folderId", _self.targetFolderId);
      axios
        .post("/dc/moveDocument", JSON.stringify(m))
        .then(function (response) {
          if (response.data.code == 1) {
            _self.$message("目录移动成功。");
            _self.$refs.mainDataGrid.loadGridData();
          } else {
            _self.$message("目录移动失败。<br>" + response.data.message);
          }
          _self.moveDialogVisible = false;
          _self.loading = false;
          
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    },
    beforeCreateFile(){
      this.createDocVisible=true;
      this.$nextTick(()=>{
        this.$refs.createCommonFile.refreshInfo();
      })
    },

    saveDoc(){
      this.$refs.createCommonFile.saveOrStartup(false);
      
    },
    onBatchImported(){
      this.$refs.mainDataGrid.loadGridData();
    },
    afterSave(){
      this.createDocVisible = false;
      this.$refs.mainDataGrid.loadGridData();
      
    },
    getWorkFlow() {
      let _self = this;

      var m = new Map();
      m.set("processDefinitionKey", "文档借阅流程");

      axios
        .post("/dc/getWorkflow", JSON.stringify(m))
        .then(function (response) {
          _self.workflow = response.data.data[0];
          console.log(_self.workflow)
          _self.borrowVisible = true;
        })
        .catch(function (error) {
          console.log(error);
        });

      
    },

    onSplitResize(topPercent){
            // 顶部百分比*100
            this.topPercent = topPercent
            this.setStorageNumber(this.topStorageName, topPercent)
            //console.log(JSON.stringify(topPercent))
        },
    // 加载表格样式
    initGridInfo() {
      let _self = this;
      _self.loading = true;
      var m = new Map();
      m.set("gridName", "OfficialGrid");
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
      _self.gridViewName=indata.gridView;
      _self.typeName=indata.typeName;
      if (indata.extended == false) {
        _self.loading = true;
        axios
          .post("/admin/getFolder", indata.id)
          .then(function (response) {
            indata.children = response.data.data;
            //console.log(JSON.stringify(indata));
            indata.extended = true;
            _self.loading = false;
          })
          .catch(function (error) {
            console.log(error);
            _self.loading = false;
          });
      }
      this.$nextTick(()=>{
        _self.$refs.mainDataGrid.loadGridData();
      });
      // _self.loadGridInfo(indata);
      // if (_self.showBox) {
      //   _self.loadAllGridData(indata);
      // } else {
      //   _self.loadGridData(indata);
      // }
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
          //console.log(JSON.stringify(response.data.datalist));
          _self.tableLoading = false;
        });
    },
    //获取包含卷盒在内的所有信息
    loadAllGridData(indata) {
      let _self = this;
      var key = _self.inputkey;
      var m = new Map();
      m.set("gridName", indata.gridView);
      m.set("folderId", indata.id);
      m.set("condition", key);
      m.set("pageSize", _self.pageSize);
      m.set("pageIndex", (_self.currentPage - 1) * _self.pageSize);
      m.set("orderBy", "MODIFIED_DATE desc");
      axios
        .post("/dc/getContainBoxDocuments", JSON.stringify(m))
        .then(function (response) {
          _self.itemDataList = response.data.data;
          _self.itemDataListFull = response.data.data;
          _self.itemCount = response.data.pager.total;
          //console.log(JSON.stringify(response.data.datalist));
          _self.loading = false;
        });
    },
    selectChange(selection) {
      this.selectedItems=selection;
      this.selectedItemList = selection;
     
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
      let key = _self.inputkey;
      let c=" CODING like '%"+key+"%' or NAME like '%"+key+"%' or C_FROM_CODING like '%"+key+"%'";
      _self.condition=c;
      _self.$nextTick(()=>{
        _self.$refs.mainDataGrid.loadGridData();
      })
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
    //添加到借阅单
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
        pageIndex: (_self.currentPage - 1) * _self.pageSize,
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