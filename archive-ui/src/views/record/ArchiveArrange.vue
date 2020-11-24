<template>
  <div>
    <el-dialog :visible.sync="printsVisible">
      <PrintPage ref="printPage" v-bind:archiveId="this.archiveId"></PrintPage>
    </el-dialog>
    <el-dialog :visible.sync="printVolumesVisible" width="80%">
      <PrintVolumes
        ref="printVolumes"
        v-bind:archiveId="this.archiveId"
        v-bind:currentFolderId="this.currentFolder.id"
      ></PrintVolumes>
    </el-dialog>

    <el-dialog
      width="80%"
      :title="dialogName+$t('application.property')"
      :visible.sync="propertyVisible"
      @close="propertyVisible = false"
    >
      <ShowProperty
        ref="ShowProperty"
        @onSaved="onSaved"
        width="560"
        :folderPath="folderPath"
        v-bind:itemId="selectedItemId"
        v-bind:folderId="currentFolder.id"
        v-bind:typeName="typeName"
      ></ShowProperty>
      <div slot="footer" class="dialog-footer">
        <el-button @click="saveItem">{{$t('application.save')}}</el-button>
        <el-button @click="propertyVisible = false">{{$t('application.cancel')}}</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="打印背脊"
      width="43%"
      :visible="printRidgeVisible"
      @close="printRidgeVisible=false"
    >
      <div style="height:900px;">
        <PrintRidge ref="printRidge"></PrintRidge>
      </div>
    </el-dialog>

    <el-dialog :title="$t('application.Import')" :visible.sync="importdialogVisible" width="70%">
      <el-form size="mini" :label-width="formLabelWidth">
        <div style="height:200px;overflow-y:scroll; overflow-x:scroll;">
          <el-upload
            :limit="100"
            :file-list="fileList"
            action
            :on-change="handleChange"
            :auto-upload="false"
            :multiple="false"
          >
            <el-button slot="trigger" size="small" type="primary">{{$t('application.selectFile')}}</el-button>
          </el-upload>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="importdialogVisible = false">{{$t('application.cancel')}}</el-button>
        <el-button
          type="primary"
          @click="uploadData(uploadID)"
        >{{$t('application.start')+$t('application.Import')}}</el-button>
      </div>
    </el-dialog>
    
    <el-dialog
      :title="folderAction"
      :visible.sync="folderDialogVisible"
      @close="folderDialogVisible = false"
    >
      <el-form :model="folderForm">
        <el-form-item :label="$t('field.name')" :label-width="formLabelWidth">
          <el-input v-model="folderForm.name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('field.description')" :label-width="formLabelWidth">
          <el-input v-model="folderForm.description" auto-complete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="saveFolder(folderForm)">{{$t('application.ok')}}</el-button>
        <el-button @click="folderDialogVisible = false">{{$t('application.cancel')}}</el-button>
      </div>
    </el-dialog>
    <div :style="{position:'relative',height: asideHeight+'px'}">
      <split-pane split="vertical" @resize="resize" min-percent="10" :default-percent="15">
        <template slot="paneL">
          <el-container :style="{height:asideHeight+'px',width:asideWidt,overflow:'auto'}">
            <el-tree
              style="width:100%"
              :props="defaultProps"
              :data="dataList"
              node-key="id"
              :render-content="renderContent"
              :default-expand-all="isExpand"
              highlight-current
              @node-click="handleNodeClick"
            ></el-tree>
          </el-container>
        </template>
        <template slot="paneR">
          <el-row>
            <el-col :span="3" class="topbar-input">
              <el-input
                v-model="inputkey"
                :placeholder="$t('message.pleaseInput')+$t('application.keyword')"
                @change="searchItem"
                prefix-icon="el-icon-search"
              ></el-input>
            </el-col>
            <el-col :span="4" style="padding-top:8px;">
              <el-radio style="margin-right:5px;" v-model="radio" label="案卷" @change="changeRadio">案卷</el-radio>
              <el-radio style="margin-left:5px;" v-model="radio" label="文件" @change="changeRadio">文件</el-radio>
            </el-col>
            <el-col :span="17" class="topbar-button">
              <el-col :span="4">
                <TypeSelectComment @afterSelecteType="newArchiveItem"></TypeSelectComment>
              </el-col>
              
              <!-- <el-button
                type="primary"
                plain
                size="small"
                icon="el-icon-edit"
                @click="newArchiveItem('卷盒')"
              >{{$t('application.newArchive')}}</el-button> -->
              <!-- <el-button
                type="primary"
                plain
                size="small"
                icon="el-icon-edit"
                @click="newArchiveItem('文件')"
              >{{$t('application.newVolume')}}</el-button> -->
              <el-col :span="4">
              <el-button
                type="primary"
                plain
                size="small"
                icon="el-icon-delete"
                @click="onDeleleArchiveItem()"
              >{{$t('application.delete')+$t('application.document')}}</el-button>
              </el-col>
              <el-col :span="4">
              <el-button
                type="primary"
                plain
                :loading="getNumLoading"
                size="small"
                icon="el-icon-s-order"
                @click="takeNumbers"
              >{{$t('application.takeNumbers')}}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</el-button>
              </el-col>
              <el-col :span="4">
              <el-button
                type="primary"
                plain
                :loading="getInfoLoading"
                size="small"
                icon="el-icon-notebook-2"
                @click="fetchInformation"
              >{{$t('application.fetchInformation')}}</el-button>
              </el-col>
              <el-col :span="4">
              <el-button
                type="primary"
                plain
                size="small"
                icon="el-icon-printer"
                @click="beforePrintRidge(selectRow,'printRidgeGrid','打印背脊')"
              >打印&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</el-button>
              </el-col>
              <el-col :span="4">
              <el-button
                type="primary"
                plain
                :loading="releaseLoading"
                size="small"
                icon="el-icon-sell"
                @click="putInStorage"
              >{{$t('application.warehousing')}}</el-button>
            </el-col>
            </el-col>
          </el-row>
          <el-row>
            <DataGrid
              ref="mainDataGrid"
              key="main"
              dataUrl="/dc/getInnerFolderDocuments"
              :isInitData="false"
              v-bind:tableHeight="rightTableHeight"
              :isshowOption="true"
              :isshowSelection="true"
              :condition="mainParam.condition"
              :folderId="mainParam.folderId"
              gridViewName="ArrangeGrid"
              @rowclick="beforeShowInnerFile"
              @selectchange="selectChange"
            ></DataGrid>
            <div>
              <span style="float:left;text-align:left;">卷内文件列表</span>
              <!-- <el-button type="primary" plain size="small" title="自动组卷"  @click="autoPaper()">自动组卷</el-button> -->
              <!-- <el-button type="primary" plain size="small"  @click="childrenTypeSelectVisible=true">{{$t('application.createDocument')}}</el-button>
                      <el-button type="primary" plain size="small" :title="$t('application.addReuseFile')"  @click="reuseVisible=true">{{$t('application.addReuseFile')}}</el-button>
                      
                      <el-button type="primary" plain size="small" title="删除"  @click="onDeleleFileItem()">{{$t('application.delete')}}</el-button>
                      <el-button type="primary" plain size="small" title="挂载文件"  @click="importdialogVisible=true;uploadUrl='/dc/mountFile'">挂载文件</el-button>
              <el-button type="primary" plain size="small" :title="$t('application.viewRedition')"  @click="importdialogVisible=true;uploadUrl='/dc/addRendition'">格式副本</el-button>-->
              <el-button type="primary" plain size="small" @click="beforeCreateFile(selectRow)">著录</el-button>
              <el-button
                type="primary"
                plain
                size="small"
                title="挂载文件"
                @click="beforeMount(innerSelectedOne);uploadUrl='/dc/mountFile'"
              >挂载文件</el-button>
              <el-button
                type="primary"
                plain
                size="small"
                :title="$t('application.viewRedition')"
                @click="beforeMount(innerSelectedOne);uploadUrl='/dc/addRendition'"
              >格式副本</el-button>

              <el-button type="primary" plain size="small" title="上移" @click="onMoveUp()">上移</el-button>
              <el-button type="primary" plain size="small" title="下移" @click="onMoveDown()">下移</el-button>
              <DataGrid
                ref="leftDataGrid"
                key="left"
                @rowclick="selectOneFile"
                dataUrl="/dc/getDocuByRelationParentId"
                gridViewName='ArrangeInnerGrid'
                condition="and a.NAME='irel_children'"
                v-bind:tableHeight="rightTableHeight"
                :isshowOption="true"
                :isshowSelection="false"
                @selectchange="selectInnerChange"
              ></DataGrid>
            </div>
            
            
          </el-row>
        </template>
      </split-pane>
    </div>
  </div>
</template>

<script type="text/javascript">
import ShowProperty from "@/components/ShowProperty";
import DataGrid from "@/components/DataGrid";
import DataGridleft from "@/components/DataGrid";
import TypeSelectComment from "@/views/record/TypeSelectComment.vue";
//import Prints from '@/components/record/Print'

import "url-search-params-polyfill";

import PrintPage from "@/views/record/PrintPage";
import PrintVolumes from "@/views/record/PrintVolumes";
import PrintRidge from "@/views/record/PrintRidge";
export default {
  name: "FolderClassification",
  components: {
    ShowProperty: ShowProperty,
    TypeSelectComment:TypeSelectComment,
    // PDFViewer: PDFViewer,
    DataGrid: DataGrid,
    PrintPage: PrintPage,
    PrintVolumes: PrintVolumes,
    PrintRidge: PrintRidge
    //Prints:Prints
  },
  data() {
    return {
      isExpand: false,
      rightTableHeight: (window.innerHeight - 232) / 2,
      asideHeight: window.innerHeight - 85,
      treeHight: window.innerHeight - 125,
      asideWidth: "100%",
      currentLanguage: this.getLang(),
      printsVisible: false,
      printVolumesVisible: false,
      archiveId: "",
      dataList: [],
      showFields: [],
      innerDataList: [],
      innerDataListFull: [],
      outerDataList: [],
      outerDataListFull: [],
      childrenTypes: [],
      radio: "案卷",
      innerSelectedOne: [],
      currentFolder: [],
      dataListFull: "",
      uploadID: "",
      inputkey: "",
      loading: false,
      makeBoxLoading: false,
      getNumLoading: false,
      getInfoLoading: false,
      releaseLoading: false,
      autoArchiveLoading: false,
      dialogName: "",
      pageSize: 20,
      itemCount: 0,
      innerCount: 0,
      outerCount: 0,
      outerCurrentPage: 1,
      outerPageSize: 20,
      innerCurrentPage: 1,
      innerPageSize: 20,
      selectedItemId: 0,
      currentPage: 1,
      dialogVisible: false,
      propertyVisible: false,
      showButton: true,
      selectedItems: [],
      selectedOutItems: [],
      selectedInnerItems: [],
      selectedChildrenType: "",
      selectRow: [],
      importdialogVisible: false,
      selectedFileId: "",
      fileList: [],
      childrenTypes: [],
      childrenTypeSelectVisible: false,
      tableHeight: window.innerHeight - 428,
      folderAction: "",
      folderDialogVisible: false,
      imageArray: [""],
      imageViewVisible: false,
      imageViewer: Object,
      currentType: "",
      orderBy: "",
      printRidgeVisible: false,
      folderForm: {
        id: 0,
        name: "",
        description: "",
        parentId: 0,
        typeName: "Folder",
        gridView: "",
        aclName: ""
      },
      formLabelWidth: "120px",
      defaultProps: {
        children: "children",
        label: "name"
      },
      innerFileParam:{
        folderId:"",
        condition:""
      },
      mainParam:{
        folderId:"",
        condition:""
        
      },
      typeName:"",
      folderPath:"",
      parentId:"",
      folderId:""
    };
  },
  
  created() {
    let _self = this;
    var psize = localStorage.getItem("docPageSize");
    if (psize) {
      _self.pageSize = parseInt(psize);
    }
    _self.currentLanguage = localStorage.getItem("localeLanguage") || "zh-cn";
    _self.loading = true;
    _self
      .axios({
        headers: {
          "Content-Type": "application/json;charset=UTF-8"
        },
        method: "post",
        data: "ArchiveCollatedID",
        url: "/folder/getArchiveFolderByConfige"
      })
      .then(function(response) {
        _self.dataList = response.data.data;

        _self.handleNodeClick(_self.dataList[0]);
        _self.isExpand = true;
        //console.log(JSON.stringify(_self.dataList));
        _self.loading = false;
      })
      .catch(function(error) {
        console.log(error);
        _self.loading = false;
      });
  },
  methods: {
    
    
    resize() {
      //console.log('resize')
      this.asideWidth = "100%";
    },
   
    //著录文件
    beforeCreateFile(row){
      let _self=this;
      
      if(_self.selectRow.ID==undefined){
        // _self.$message('请选择一条图册或卷盒数据！')
        
         _self.$message({
                showClose: true,
                message: '请选择一条案卷数据！',
                duration: 2000,
                type: "warning"
              });
        return;
      }
      _self.parentId=row.ID;
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: row.TYPE_NAME,
          url: "/dc/getArchiveFileConfig"
        })
        .then(function(response) {
          let code=response.data.code;
          if(code=='1'){
            let data=response.data.data;
            let fileType=data[0].C_TO;
            _self.newArchiveFileItem(fileType,row);
          }else{
            _self.$message({
                showClose: true,
                message: response.data.msg,
                duration: 5000,
                type: "error"
              });
          }
        })
        .catch(function(error) {
          // _self.$message("添加失败！");
          _self.$message({
                showClose: true,
                message: '添加失败！',
                duration: 5000,
                type: "error"
              });
          console.log(error);
        });
      

    },
    ///打印背脊
    beforePrintRidge(selectedRow, gridName, vtitle) {
      let _self = this;
      if (selectedRow.ID == undefined) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择一条数据进行打印",
          duration: 2000,
          type: "warning"
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
    ///上架
    putInStorage() {
      let _self = this;
      if (_self.radio != "卷盒") {
        // _self.$message('请选择卷盒数据！');
        _self.$message({
          showClose: true,
          message: "请选择卷盒数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      if (_self.selectedItems.length == 0) {
        //  _self.$message('请选择一条或多条卷盒数据！');
        _self.$message({
          showClose: true,
          message: "请选择一条卷盒数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }

      let tab = _self.selectedItems;
      let m = [];
      let i;
      for (i in tab) {
        if (tab[i]["CODING"] == undefined || tab[i]["CODING"] == "") {
          _self.$message(
            "所选卷盒中有未取号的数据，请先对其进行取号并提取信息！"
          );
          return;
        }
        m.push(tab[i]["ID"]);
      }
      _self.releaseLoading = true;
      // m.push(_self.selectRow.ID);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(m),
          url: "/dc/putInStorage"
        })
        .then(function(response) {
          _self.loadGridData(_self.currentFolder);
          _self.innerDataList = [];
          _self.releaseLoading = false;
          // _self.showInnerFile(null);
          // _self.$message(_self.$t("message.warehousingSuccess"));
          _self.$message({
            showClose: true,
            message: _self.$t("message.warehousingSuccess"),
            duration: 2000,
            type: "success"
          });
        })
        .catch(function(error) {
          // _self.$message(_self.$t("message.warehousingFail"));
          _self.releaseLoading = false;
          _self.$message({
            showClose: true,
            message: _self.$t("message.warehousingFail"),
            duration: 5000,
            type: "error"
          });
          console.log(error);
        });
    },
    selectOneFile(row) {
      let _self = this;
      if (row != null) {
        _self.innerSelectedOne = row;
        _self.selectedFileId = row.ID;
      }
    },
    changeRadio(val) {
      let _self = this;
      _self.loadGridData(_self.currentFolder);
      
    },
    //上移
    onMoveUp() {
      let _self = this;
      if (_self.innerSelectedOne.ID == undefined) {
        //  _self.$message("请选择一条数据！");
        _self.$message({
          showClose: true,
          message: "请选择一条数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      var m = new Map();
      m.set("parentId", _self.archiveId);
      m.set("childId", _self.innerSelectedOne.ID);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(m),
          url: "/dc/moveUp"
        })
        .then(function(response) {
          let code = response.data.code;
          //console.log(JSON.stringify(response));
          if (code == 1) {
            _self.showInnerFile(null);
          } else {
            //  _self.$message( response.data.message);
            _self.$message({
              showClose: true,
              message: response.data.message,
              duration: 5000,
              type: "error"
            });
          }
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    //挂载
    beforeMount(selrow) {
      let _self = this;
      _self.fileList = [];
      if (selrow.ID == undefined) {
        //  _self.$message("请选择一条数据！");
        _self.$message({
          showClose: true,
          message: "请选择一条数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.uploadID = selrow.ID;
      _self.importdialogVisible = true;
    },
    getFormData(selId) {
      let _self = this;
      let formdata = new FormData();
      var data = {};
      data["ID"] = selId;
      formdata.append("metaData", JSON.stringify(data));
      _self.fileList.forEach(function(file) {
        //console.log(file.name);
        formdata.append("uploadFile", file.raw, file.name);
      });
      return formdata;
    },
    //上传文件
    uploadData(selId) {
      let _self = this;
      let formdata = _self.getFormData(selId);
      //console.log("UploadData getData");
      //console.log(formdata);
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
          _self.showInnerFile(null);
          // _self.$message(_self.$t('application.Import')+_self.$t('message.success'));
          _self.$message({
            showClose: true,
            message:
              _self.$t("application.Import") + _self.$t("message.success"),
            duration: 2000,
            type: "success"
          });
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    //下移
    onMoveDown() {
      let _self = this;
      if (_self.innerSelectedOne.ID == undefined) {
        //  _self.$message("请选择一条数据！");
        _self.$message({
          showClose: true,
          message: "请选择一条数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      var m = new Map();
      m.set("parentId", _self.archiveId);
      m.set("childId", _self.innerSelectedOne.ID);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(m),
          url: "/dc/moveDown"
        })
        .then(function(response) {
          let code = response.data.code;
          //console.log(JSON.stringify(response));
          if (code == 1) {
            _self.showInnerFile(null);
          } else {
            //  _self.$message( response.data.message);
            _self.$message({
              showClose: true,
              message: response.data.message,
              duration: 2000,
              type: "warning"
            });
          }
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    handleChange(file, fileList) {
      this.fileList = fileList;
    },
    

    
   
    getTypeNamesByMainList(keyName) {
      let _self = this;
      axios
        .post("/dc/getOneParameterValue", keyName)
        .then(function(response) {
          _self.childrenTypes = response.data.data;
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    beforeShowInnerFile(row) {
      this.innerCurrentPage = 1;
      this.innerSelectedOne = [];
      this.showInnerFile(row);
    },
    showInnerFile(row) {
      let _self = this;
      if (row != null) {
        _self.selectRow = row;
      }
      
    },
    renderContent: function(h, { node, data, store }) {
      //console.log(data);
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
    showNewWindow(id) {
      let condition = this.id;
      let href = this.$router.resolve({
        name: "docviewer",
        query: {
          id: condition,
          token: this.getToken()
        }
      });
      console.log(href);
      window.open(href.href, "_blank");
    },
    getgriditem(attrName) {
      let _self = this;
      let ret = null;
      _self.gridList.forEach(element => {
        if (element.attrName == attrName) {
          ret = element;
          return;
        }
      });
      return ret;
    },
    handleCheckChange(data, checked, indeterminate) {
      data.visibleType = checked ? 1 : 0;
    },
    inited(viewer) {
      this.imageViewer = viewer;
    },
    onImageClick() {
      this.imageViewVisible = false;
    },

    // 表格行选择
    selectChange(val) {
      // console.log(JSON.stringify(val));
      this.selectedItems = val;
    },
    // 表格行选择
    selectOutChange(val) {
      // console.log(JSON.stringify(val));
      this.selectedOutItems = val;
    },
    selectInnerChange(val) {
      this.selectedInnerItems = val;
    },
    
    // 加载表格数据
    loadGridData(indata) {
      let _self = this;

      var key = _self.inputkey;
      
      if (key != "") {
        key = " (coding like '%" + key + "%' or title like '%" + key + "%') ";
        if (_self.radio == "案卷") {
          key= key + " and C_ITEM_TYPE='案卷' ";
        } else {
          key= key + " and C_ITEM_TYPE='文件' ";
        }
      } else {
        if (_self.radio == "案卷") {
          key=key+ " C_ITEM_TYPE='案卷' ";
        } else {
          key=key+" C_ITEM_TYPE='文件' ";
        }
      }
      _self.mainParam.condition=key;
      _self.mainParam.folderId=indata.id
      _self.$nextTick(()=>{
        _self.$refs.mainDataGrid.loadGridData();
      });
      
      
    },
    
    // 文件夹节点点击事件
    handleNodeClick(indata) {
      let _self = this;
      _self.selectRow = [];
      _self.selectedFileId = "";

      _self.currentFolder = indata;
      //console.log(JSON.stringify(indata));
      // 没有加载，逐级加载
      if (indata.extended == false) {
        _self.loading = true;
        _self
          .axios({
            headers: {
              "Content-Type": "application/json;charset=UTF-8"
            },
            method: "post",
            data: indata.id,
            url: "/folder/getArchiveFolderByConfige" //"/admin/getFolder"
          })
          .then(function(response) {
            indata.children = response.data.data;
            //console.log(JSON.stringify(indata));
            indata.extended = true;
            _self.loading = false;
          })
          .catch(function(error) {
            console.log(error);
            _self.loading = false;
          });
      }
      // _self.loadGridInfo(indata);
      _self.loadGridData(indata);
    },
    
    newArchiveItem(typeName) {
      let _self = this;
      if (_self.currentFolder.id) {
        _self.selectedItemId = "";
        _self.typeName=typeName;
        _self.propertyVisible = true;
        setTimeout(() => {
          if (_self.$refs.ShowProperty) {
            _self.$refs.ShowProperty.myItemId = "";
            _self.dialogName = typeName;
            _self.$refs.ShowProperty.myTypeName = typeName;
            _self.$refs.ShowProperty.myFolderId = _self.currentFolder.id;
            _self.$refs.ShowProperty.loadFormInfo();
          }
        }, 10);
      } else {
        // _self.$message(_self.$t("message.pleaseSelectFolder"));
        _self.$message({
          showClose: true,
          message: _self.$t("message.pleaseSelectFolder"),
          duration: 2000,
          type: "warning"
        });
      }
    },
    newArchiveFileItem(typeName, selectedRow) {
      let _self = this;
      if (selectedRow.ID) {
        _self.selectedItemId = "";
        _self.propertyVisible = true;
        setTimeout(() => {
          if (_self.$refs.ShowProperty) {
            _self.$refs.ShowProperty.myItemId = "";
            _self.dialogName = typeName;
            _self.$refs.ShowProperty.myTypeName = typeName;
            _self.typeName = typeName;
            _self.$refs.ShowProperty.parentDocId = selectedRow.ID;
            _self.$refs.ShowProperty.folderId = _self.currentFolder.id;
            // _self.$refs.ShowProperty.myFolderId = _self.selectTransferRow.id;
            _self.$refs.ShowProperty.loadFormInfo();
          }
        }, 10);
      } else {
        // _self.$message(_self.$t("message.pleaseSelectFolder"));
        _self.$message({
          showClose: true,
          message: _self.$t("message.pleaseSelectFolder"),
          duration: 2000,
          type: "warning"
        });
      }
    },
    // 新建文档
    newItem() {
      let _self = this;
      if (_self.currentFolder.id) {
        _self.selectedItemId = "";
        _self.propertyVisible = true;
        if (_self.$refs.ShowProperty) {
          _self.$refs.ShowProperty.myItemId = "";
          _self.dialogName = _self.currentFolder.description;
          _self.$refs.ShowProperty.myTypeName = _self.currentFolder.description;
          _self.$refs.ShowProperty.myFolderId = _self.currentFolder.id;
          _self.$refs.ShowProperty.loadFormInfo();
        }
      } else {
        // _self.$message(_self.$t("message.pleaseSelectFolder"));
        _self.$message({
          showClose: true,
          message: _self.$t("message.pleaseSelectFolder"),
          duration: 2000,
          type: "warning"
        });
      }
    },
    // 保存文档
    saveItem() {
      if (!this.$refs.ShowProperty.validFormValue()) {
        return;
      }
      let _self = this;
      var m = new Map();
      let dataRows = this.$refs.ShowProperty.dataList;
      var c;
        for(c in _self.$refs.ShowProperty.dataList)
        {
            let dataRows = _self.$refs.ShowProperty.dataList[c].ecmFormItems;
            var i;
            for (i in dataRows) {
            if(dataRows[i].attrName && dataRows[i].attrName !='')
            {
                if(dataRows[i].attrName !='FOLDER_ID'&&dataRows[i].attrName !='ID')
                {
                var val = dataRows[i].defaultValue;
                if(val && dataRows[i].isRepeat){
                    var temp = "";
                // console.log(val);
                    for(let j=0,len=val.length;j<len;j++){
                    temp = temp + val[j]+";";
                    //console.log(temp);
                    }
                    temp = temp.substring(0,temp.length-1);
                    val = temp;
                    console.log(val);
                }
                m.set(dataRows[i].attrName, val);
                }
            }
            }
        }
      if (_self.$refs.ShowProperty.myItemId != "") {
        m.set("ID", _self.$refs.ShowProperty.myItemId);
      }
      if (_self.$refs.ShowProperty.myTypeName != "") {
        m.set("TYPE_NAME", _self.typeName);
        m.set("folderPath", _self.folderPath);
        m.set("transferId", _self.parentId);
        m.set("folderId",_self.currentFolder.id);
      }
      let formdata = new FormData();
      formdata.append("metaData", JSON.stringify(m));

      if (_self.$refs.ShowProperty.file != "") {
        //console.log(_self.file);
        formdata.append("uploadFile", _self.$refs.ShowProperty.file.raw);
      }
      // console.log(JSON.stringify(m));
      if (_self.$refs.ShowProperty.myItemId == "") {
        _self
          .axios({
            headers: {
              "Content-Type": "multipart/form-data"
              // x-www-form-urlencoded'
              //"Content-Type": "application/json;charset=UTF-8"
            },
            method: "post",
            data: formdata,
            url: "/dc/newArchiveOrDocument"
          })
          .then(function(response) {
            let code = response.data.code;
            //console.log(JSON.stringify(response));
            if (code == 1) {
              // _self.$message(_self.$t('message.newSuccess'));
              _self.$message({
                showClose: true,
                message: _self.$t('message.newSuccess'),
                duration: 2000,
                type: "success"
              });
              _self.propertyVisible = false;
              _self.loadGridData(null);
              _self.showInnerFile(null);
              
              
            } else {
              // _self.$message(_self.$t('message.newFailured'));
              _self.$message({
                showClose: true,
                message: _self.$t('message.newFailured'),
                duration: 2000,
                type: "warning"
              });
            }
          })
          .catch(function(error) {
            // _self.$message(_self.$t('message.newFailured'));
            _self.$message({
                showClose: true,
                message: _self.$t('message.newFailured'),
                duration: 5000,
                type: "error"
              });
            console.log(error);
          });
      } else {
        _self
          .axios({
            headers: {
              "Content-Type": "application/json;charset=UTF-8"
            },
            method: "post",
            data: JSON.stringify(m),
            url: "/dc/saveDocument"
          })
          .then(function(response) {
            let code = response.data.code;
            //console.log(JSON.stringify(response));
            if (code == 1) {
              _self.$emit("onSaved", "update");
            } else {
              // _self.$message(_self.$t('message.saveFailured'));
              _self.$message({
                showClose: true,
                message: _self.$t('message.saveFailured'),
                duration: 5000,
                type: "error"
              });
            }
          })
          .catch(function(error) {
            // _self.$message(_self.$t('message.saveFailured'));
            _self.$message({
                showClose: true,
                message: _self.$t('message.saveFailured'),
                duration: 5000,
                type: "error"
              });
            console.log(error);
          });
      }
    },
    // 保存结果事件
    onSaved(indata) {
      let _self = this;
      if (indata == "update") {
        // this.$message(this.$t("message.saveSuccess"));

        _self.$message({
          showClose: true,
          message: _self.$t("message.saveSuccess"),
          duration: 2000,
          type: "success"
        });
      } else {
        // this.$message("新建成功!");
        _self.$message({
          showClose: true,
          message: _self.$t("message.operationSuccess"),
          duration: 2000,
          type: "success"
        });
      }
      _self.propertyVisible = false;
      _self.loadGridData(_self.currentFolder);
    },
    

    
    // 删除文档事件
    onDeleleArchiveItem() {
      let _self = this;
      if (_self.selectRow.ID == undefined) {
        // _self.$message("请选择一条卷盒或图册数据！");
        _self.$message({
          showClose: true,
          message: "请选择一条卷盒或图册数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      this.$confirm(
        _self.$t("message.deleteInfo"),
        _self.$t("application.info"),
        {
          confirmButtonText: _self.$t("application.ok"),
          cancelButtonText: _self.$t("application.cancel"),
          type: "warning"
        }
      )
        .then(() => {
          _self.deleleInnerItem();
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除'
          // });
        });
    },
    // 删除文档
    deleleInnerItem() {
      let _self = this;
      
      var m = [];
      m.push(_self.selectRow.ID);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(m), //_self.selectRow.ID,//JSON.stringify(m),
          url: "/dc/delDocumentAndRelation"
        })
        .then(function(response) {
          _self.loadGridData(_self.currentFolder);

          _self.showInnerFile(null);
          // _self.$message(_self.$t("message.deleteSuccess"));
          _self.$message({
            showClose: true,
            message: _self.$t("message.deleteSuccess"),
            duration: 2000,
            type: "success"
          });
        })
        .catch(function(error) {
          // _self.$message(_self.$t("message.deleteFailured"));
          _self.$message({
            showClose: true,
            message: _self.$t("message.deleteFailured"),
            duration: 5000,
            type: "error"
          });
          console.log(error);
        });
    },

    

    // 保存文件夹
    saveFolder(indata) {
      let _self = this;
      if (_self.folderAction == _self.$t("application.newFolder")) {
        _self.newFolder(indata);
      } else {
        _self
          .axios({
            headers: {
              "Content-Type": "application/json;charset=UTF-8"
            },
            datatype: "json",
            method: "post",
            data: JSON.stringify(indata),
            url: "/admin/updateFolder"
          })
          .then(function(response) {
            // _self.$message(_self.$t("message.saveSuccess"));
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveSuccess"),
              duration: 2000,
              type: "success"
            });
            _self.folderDialogVisible = false;
          })
          .catch(function(error) {
            console.log(error);
          });
      }
    },
    //查询文档
    searchItem() {
      this.loadGridData(this.currentFolder);
      //  this. loadPageInfo(this.currentFolder);
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
body,
html {
  height: 100%;
  margin: 0px;
  padding: 0px;
  overflow: hidden;
}
.left,
.right {
  width: 47.5%;
}
.middle {
  width: 5%;
}
.left,
.middle,
.right {
  /* width:200px; */
  /* height:100px; */
  /* background-color:rgb(34,124,134); */
  float: left;
  height: 100%;
}

/* *{
        margin: 0;
        padding: 0;
      }            
             .middle{
                 position: absolute;
                 left: 50.3%;
                 right: 41%;
                 background-color: red;
                 word-break: break-word;
                 height: 20%;
             }
             .left{
               position:relative;
                 width: 45%;
                 height: 200px;
                 float: left;
                background-color: blue;
             }
            .right{
              position:relative;
                 width: 45%;
                 height: 200px;
                 float: right;
                 background-color: yellow;
            } */
h1,
h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
.el-select-dropdown.is-multiple .el-select-dropdown__item.selected::after {
  position: absolute;
  right: 2px;
  font-family: element-icons;
  content: "\E6DA";
  font-size: 12px;
  font-weight: 700;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
