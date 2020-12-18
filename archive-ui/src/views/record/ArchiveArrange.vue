<template>
  <DataLayout>
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
    <el-dialog :visible.sync="PreparationTablePrintVisible" width="80%"
    @close="PreparationTablePrintVisible=false">
      <PreparationTablePrint
        ref="PreparationTablePrint"
        v-bind:archiveId="this.archiveId"
        v-bind:currentFolderId="this.currentFolder.id"
      ></PreparationTablePrint>
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
    <el-dialog
      title="打印条码"
      width="43%"
      :visible="printBarCodeVisible"
      @close="printBarCodeVisible=false"
    >
      <div style="height:900px;">
        <PrintBarCode ref="printBarCode" :archiveObjects="selectedItems" :isBarCode="true"></PrintBarCode>
      </div>
    </el-dialog>

    <el-dialog
      title="打印pdf417条码"
      width="43%"
      :visible="printPdf417Visible"
      @close="printPdf417Visible=false"
    >
      <div style="height:900px;">
        <PrintPdf417 ref="printPdf417" :archiveObjects="selectedItems" :isBarCode="true"></PrintPdf417>
      </div>
    </el-dialog>

    <el-dialog
      title="打印档号"
      width="43%"
      :visible="printArchiveCodeVisible"
      @close="printArchiveCodeVisible=false"
    >
      <div style="height:900px;">
        <PrintArchiveCode ref="printArchiveCode" :archiveObjects="selectedItems" :isBarCode="true"></PrintArchiveCode>
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

    <el-dialog title="取批次号"
    :visible.sync="pieceNumVisible"
    @close="pieceNumVisible = false"
    >
      <el-form label-width="80px">
        <el-col :span="12">
          <el-form-item label="批次号">
            <el-input v-model="pieceNum" auto-complete="off"></el-input>
          </el-form-item>
          
        </el-col>
        <el-col :span="3">
          <div style="margin-top:6px">
            <el-button type="primary" @click="takePiecesNumber">取批次号</el-button>
          </div>
          
        </el-col>
        
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="savePiecesNumber">{{$t('application.ok')}}</el-button>
        <el-button @click="pieceNumVisible = false">{{$t('application.cancel')}}</el-button>
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
    <template v-slot:main="{layout}">
      <div :style="{position:'relative',height: layout.height-startHeight+45+'px'}">
        <split-pane split="vertical" @resize="onHorizontalSplitResize" :min-percent='1' :default-percent='leftPercent'>
          <template slot="paneL">
            <el-container :style="{height:asideHeight+'px',width:asideWidth,overflow:'auto'}">
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
            <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
              <split-pane v-on:resize="onSplitResize" :min-percent='20' :default-percent='topPercent' split="horizontal">
                <template slot="paneL">
                  <el-row>
                    <el-col :span="3" class="topbar-input">
                      <el-input
                        v-model="inputkey"
                        :placeholder="$t('message.pleaseInput')+$t('application.keyword')"
                        @change="searchItem"
                        prefix-icon="el-icon-search"
                      ></el-input>
                    </el-col>
                    <el-col :span="3" class="topbar-input">
                      <el-select v-model="archiveStatus" placeholder="请选择状态" @change="searchItem">
                        <el-option label="全部" value=""></el-option>
                        <el-option label="整编" value="整编"></el-option>
                        <el-option label="已整编" value="已整编"></el-option>
                        <el-option label="已质检" value="已质检"></el-option>
                      </el-select>
                    </el-col>
                    <el-col :span="4" style="padding-top:8px;">
                      <el-radio style="margin-right:5px;" v-model="radio" label="案卷" @change="changeRadio">案卷</el-radio>
                      <el-radio style="margin-left:5px;" v-model="radio" label="文件" @change="changeRadio">文件</el-radio>
                    </el-col>
                    <el-col :span="14" class="topbar-button">
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
                      <el-col :span="17">
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-delete"
                        @click="logicallyDel(selectedItems,function(){
                          let _self=this;
                          if(_self.$refs.leftDataGrid){
                              _self.$refs.leftDataGrid.itemDataList = [];
                            }
                          _self.loadGridData(_self.currentFolder);
                        })"
                        :title="$t('application.delete')+$t('application.document')"
                      ><!--{{$t('application.delete')+$t('application.document')}}--></el-button> 
                      
                      <el-button
                        type="primary"
                        plain
                        :loading="getNumLoading"
                        size="small"
                        icon="el-icon-s-order"
                        @click="takeNumbers"
                        :title="$t('application.takeNumbers')"
                      ></el-button>
                      
                      <el-button
                        type="primary"
                        plain
                        :loading="getInfoLoading"
                        size="small"
                        icon="el-icon-notebook-2"
                        @click="fetchInformation"
                        :title="$t('application.fetchInformation')"
                      ></el-button>
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        title="挂载文件"
                        icon="el-icon-upload2"
                        @click="beforeMount(selectedItems);uploadUrl='/dc/mountFile'"
                      ></el-button>
                      <!-- <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-printer"
                        @click="beforePrintRidge(selectRow,'printRidgeGrid','打印背脊')"
                        title="打印"
                      ></el-button> -->
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-printer"
                        @click="beforePrintBarCode(selectedItems,'打印条码')"
                        title="打印条码"
                      ></el-button>
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-printer"
                        @click="beforePrintArchiveCode(selectedItems,'打印档号')"
                        title="打印档号"
                      ></el-button>
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-printer"
                        @click="beforePrintPdf417(selectedItems,'打印档号')"
                        title="打印pdf417"
                      ></el-button>
                        <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-printer"
                        @click="arrangeComplete('已整编')"
                        title="整编完成"
                      ></el-button>
                    
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-printer"
                        @click="arrangeComplete('已质检')"
                        title="质检完成"
                      ></el-button>
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-printer"
                        @click="beforePrint(selectRow,'ArrangeInnerGridPrint','卷内目录')"
                        title="打印卷内目录"
                      ></el-button>
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-printer"
                        @click="beforePrintPreparationTable(selectRow,'备考表')"
                        title="打印备考表"
                      ></el-button>
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-help"
                        @click="pieceNumVisible=true"
                        title="生成批次号"
                      ></el-button>
                      <el-button
                        type="primary"
                        plain
                        :loading="releaseLoading"
                        size="small"
                        icon="el-icon-sell"
                        @click="moveToPreFilling"
                        title="提交预归档库"
                      ></el-button>
                      <el-button
                        type="primary"
                        plain
                        :loading="releaseLoading"
                        size="small"
                        icon="el-icon-sell"
                        @click="penddingStorage"
                        title="提交入库"
                      ></el-button>
                    </el-col>
                    </el-col>
                  </el-row>
                  <el-row>
                    <el-col :span="24">
                      <DataGrid
                        ref="mainDataGrid"
                        key="main"
                        dataUrl="/dc/getInnerFolderDocuments"
                        :isInitData="false"
                        v-bind:tableHeight="(layout.height-startHeight)*(topPercent-15)/100-topbarHeight"
                        :isshowOption="true"
                        :isshowSelection="true"
                        :condition="mainParam.condition"
                        :folderId="mainParam.folderId"
                        gridViewName="ArrangeGrid"
                        @rowclick="beforeShowInnerFile"
                        @selectchange="selectChange"
                      ></DataGrid>
                    </el-col>
                  </el-row>
                </template>
                <template slot="paneR" v-if="isFile">
                  <el-row>
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
                      @click="beforeMount(selectedInnerItems);uploadUrl='/dc/mountFile'"
                    >挂载文件</el-button>
                    <el-button
                      type="primary"
                      plain
                      size="small"
                      :title="$t('application.viewRedition')"
                      @click="beforeMount(selectedInnerItems);uploadUrl='/dc/addRendition'"
                    >格式副本</el-button>

                    <el-button type="primary" plain size="small" title="上移" @click="onMoveUp()">上移</el-button>
                    <el-button type="primary" plain size="small" title="下移" @click="onMoveDown()">下移</el-button>
                  </el-row>
                  <el-row>
                    <el-col :span="24">
                      <DataGrid
                        ref="leftDataGrid"
                        key="left"
                        @rowclick="selectOneFile"
                        dataUrl="/dc/getDocuByRelationParentId"
                        gridViewName='ArrangeInnerGrid'
                        condition="and a.NAME='irel_children' and b.IS_HIDDEN=0"
                        :parentId="parentId"
                        v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent-15)/100-bottomHeight"
                        :isshowOption="true"
                        :isshowSelection="true"
                        @selectchange="selectInnerChange"
                      ></DataGrid>
                    </el-col>
                  </el-row>
                </template>
              </split-pane>
            </div>
          </template>
        </split-pane>
      </div>
    </template>
  </DataLayout>
</template>

<script type="text/javascript">
import ShowProperty from "@/components/ShowProperty";
import DataGrid from "@/components/DataGrid";
import DataGridleft from "@/components/DataGrid";
import TypeSelectComment from "@/views/record/TypeSelectComment.vue";
//import Prints from '@/components/record/Print'
import DataLayout from '@/components/ecm-data-layout'

import "url-search-params-polyfill";

import PrintPage from "@/views/record/PrintPage";
// import PrintVolumes from "@/views/record/PrintVolumes";
import PrintVolumes from "@/views/record/PrintVolumes4Archive";
import PrintRidge from "@/views/record/PrintRidge";
import PreparationTablePrint from "@/views/record/PreparationTablePrint.vue"
import PrintBarCode from "@/views/record/PrintBarCode.vue"
import PrintArchiveCode from "@/views/record/PrintArchiveCode.vue"
import PrintPdf417 from "@/views/record/PrintPdf417.vue"
export default {
  name: "ArchiveArrange",
  components: {
    ShowProperty: ShowProperty,
    TypeSelectComment:TypeSelectComment,
    // PDFViewer: PDFViewer,
    DataGrid: DataGrid,
    PrintPage: PrintPage,
    PrintVolumes: PrintVolumes,
    PrintRidge: PrintRidge,
    PreparationTablePrint:PreparationTablePrint,
    PrintBarCode:PrintBarCode,
    PrintArchiveCode:PrintArchiveCode,
    //Prints:Prints
    DataLayout:DataLayout,
    PrintPdf417:PrintPdf417

  },
  data() {
    return {
      leftStorageName: 'ProjectViewerWidth',
      leftPercent: 20,

      // 本地存储高度名称
      topStorageName: 'ProjectViewerHeight',
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 35,
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
      folderId:"",
      archiveStatus:"",
      extendMap:{},
      pieceNum:"",//批次号
      pieceNumVisible:false,//是否显示取批次号dialog
      PreparationTablePrintVisible:false,
      printBarCodeVisible:false,
      printArchiveCodeVisible:false,
      printPdf417Visible:false,
      isFile:true,
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
    let m=new Map();
    m.set("folderConfig","ArchiveCollatedID");
    m.set("condition"," and((C_ITEM_TYPE='文件' or C_ITEM_TYPE='案卷') and IS_HIDDEN=0 and IS_CHILD=0) ");
    _self
      .axios({
        headers: {
          "Content-Type": "application/json;charset=UTF-8"
        },
        method: "post",
        data: JSON.stringify(m),
        url: "/folder/getFolderByConfigeGC"
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
      setTimeout(() => {
        this.topPercent = this.getStorageNumber(this.topStorageName,60)
        this.leftPercent = this.getStorageNumber(this.leftStorageName,20)
      }, 300);
  },
  methods: {
    // 水平分屏事件
    onHorizontalSplitResize(leftPercent){
      // 左边百分比*100
      this.leftPercent = leftPercent
      this.setStorageNumber(this.leftStorageName, leftPercent)
        
    },
    // 上下分屏事件
    onSplitResize(topPercent){
      // 顶部百分比*100
      this.topPercent = topPercent
      this.setStorageNumber(this.topStorageName, topPercent)
      //console.log(JSON.stringify(topPercent))
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
    ///打印档号
    beforePrintArchiveCode(selectedRows,vtitle) {
      let _self = this;
      if (selectedRows == undefined||selectedRows.length==0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择一条数据进行打印",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.printArchiveCodeVisible = true;

      setTimeout(() => {
        // _self.$refs.printBarCode.archiveObjects=selectedRows;
        _self.$refs.printArchiveCode.refresh(selectedRows, 1);
      }, 10);

    },

    beforePrintPdf417(selectedRows){
      let _self = this;
      if (selectedRows == undefined||selectedRows.length==0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择一条数据进行打印",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.printPdf417Visible = true;
      setTimeout(() => {
        _self.$refs.printPdf417.refresh(selectedRows, 1);
      }, 10);
    },
    ///打印条码
    beforePrintBarCode(selectedRows,vtitle) {
      let _self = this;
      if (selectedRows == undefined||selectedRows.length==0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择一条数据进行打印",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.printBarCodeVisible = true;

      setTimeout(() => {
        // _self.$refs.printBarCode.archiveObjects=selectedRows;
        _self.$refs.printBarCode.refreshBarCode(selectedRows, 1);
      }, 10);

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

    beforePrintPreparationTable(selectedRow,vtitle){
      
      let _self=this;
      if(selectedRow.ID==undefined){
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
                showClose: true,
                message: '请选择一条数据进行打印!',
                duration: 2000,
                type: "warning"
              });
        return;
      }
      _self.PreparationTablePrintVisible = true;

      setTimeout(()=>{
        _self.$refs.PreparationTablePrint.getArchiveObj(selectedRow.ID,
        vtitle); 
      },10);

    },

    beforePrint(selectedRow,gridName,vtitle){
      debugger
      let _self=this;
      if(selectedRow.ID==undefined){
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
                showClose: true,
                message: '请选择一条数据进行打印!',
                duration: 2000,
                type: "warning"
              });
        return;
      }
      _self.printVolumesVisible = true;

      setTimeout(()=>{
        _self.$refs.printVolumes.dialogQrcodeVisible = false
        _self.$refs.printVolumes.getArchiveObj(selectedRow.ID,
        gridName,
        vtitle); 
      },10);

      _self.printGridName=gridName;
      _self.printObjId=selectedRow.ID;
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
      if(val=='文件'){
        _self.isFile=false;
        _self.topPercent=95;
        // _self.$refs.mainDataGrid.tableHeight=(window.innerHeight-_self.startHeight);
      }else{
        _self.isFile=true;
        _self.topPercent=65;
        _self.$nextTick(()=>{
          if(_self.$refs.leftDataGrid){
             _self.$refs.leftDataGrid.itemDataList = [];
          }
        });
        
      }
      
      _self.loadGridData(_self.currentFolder);
      
    },
    //上移
    onMoveUp() {
      let _self = this;
      // if (_self.innerSelectedOne.ID == undefined) {
        if(_self.selectedInnerItems.length!=1){
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
      m.set("parentId", _self.selectRow.ID);
      m.set("childId", _self.selectedInnerItems[0].ID);
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
            _self.showInnerFile(_self.selectRow);
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
      if (selrow.length!=1||selrow[0].ID == undefined) {
        //  _self.$message("请选择一条数据！");
        _self.$message({
          showClose: true,
          message: "请选择一条数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.uploadID = selrow[0].ID;
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
          _self.showInnerFile(_self.selectRow);
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
      // if (_self.innerSelectedOne.ID == undefined) {
        if(_self.selectedInnerItems.length!=1){
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
      m.set("parentId", _self.selectRow.ID);
      m.set("childId", _self.selectedInnerItems[0].ID);
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
            _self.showInnerFile(_self.selectRow);
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
      this.innerSelectedOne = [];
      this.showInnerFile(row);
    },
    showInnerFile(row) {
      let _self = this;
      if (row != null) {
        _self.selectRow = row;
      }
      _self.parentId=row.ID;
      _self.$nextTick(()=>{
        if(_self.$refs.leftDataGrid){
             _self.$refs.leftDataGrid.itemDataList = [];
          }
      });
      
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
          key= key + " and C_ITEM_TYPE='案卷' and IS_HIDDEN=0 ";
        } else {
          key= key + " and C_ITEM_TYPE='文件' and IS_CHILD=0 and IS_HIDDEN=0 ";
        }
        
      } else {
        if (_self.radio == "案卷") {
          key=key+ " C_ITEM_TYPE='案卷' and IS_HIDDEN=0 ";
        } else {
          key=key+" C_ITEM_TYPE='文件' and IS_CHILD=0 and IS_HIDDEN=0";
        }
       
      }
      if(_self.archiveStatus!=''){
          key=key+" and status='"+_self.archiveStatus+"'";
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
        let mp=new Map();
        mp.set("folderId",indata.id);
        mp.set("condition"," and(IS_HIDDEN=0 and IS_CHILD=0)");
        _self
          .axios({
            headers: {
              "Content-Type": "application/json;charset=UTF-8"
            },
            method: "post",
            data: JSON.stringify(mp),
            url: "/folder/getArchiveFolderByConfigeArchiveGc" //"/admin/getFolder"
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
            _self.extendMap=null;
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
            _self.extendMap=new Map();
            _self.extendMap.set("IS_CHILD","1");
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
        m.set("STATUS","整编");
      }
      if(_self.extendMap){
        _self.extendMap.forEach(function(e,b,i){
            m.set(b,e);
          });
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
              if(_self.extendMap){
                _self.showInnerFile(_self.selectRow);
               
              }else{
                 if(_self.$refs.leftDataGrid){
                    _self.$refs.leftDataGrid.itemDataList = [];
                  }
                 _self.loadGridData(_self.currentFolder);
              }
              
              
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
    },
    
    penddingStorage(){
      let _self=this;
      if (_self.selectedItems.length == 0) {
        //  _self.$message("请选择一条卷盒数据！");
        _self.$message({
          showClose: true,
          message: "请选择一条或多条数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      let p=new Array();
      _self.selectedItems.forEach(e=>{
        p.push(e.ID);
      });
      _self
          .axios({
            headers: {
              "Content-Type": "application/json;charset=UTF-8"
            },
            datatype: "json",
            method: "post",
            data: JSON.stringify(p),
            url: "/dc/penddingStorage"
          })
          .then(function(response) {
            // _self.$message(_self.$t("message.saveSuccess"));
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveSuccess"),
              duration: 2000,
              type: "success"
            });
            if(_self.$refs.leftDataGrid){
              _self.$refs.leftDataGrid.itemDataList = [];
            }
            _self.searchItem();
          })
          .catch(function(error) {
            console.log(error);
          });
    },
    moveToPreFilling(){
      let _self=this;
      if (_self.selectedItems.length == 0) {
        //  _self.$message("请选择一条卷盒数据！");
        _self.$message({
          showClose: true,
          message: "请选择一条或多条数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      let p=new Array();
      _self.selectedItems.forEach(e=>{
        p.push(e.ID);
      });
      _self
          .axios({
            headers: {
              "Content-Type": "application/json;charset=UTF-8"
            },
            datatype: "json",
            method: "post",
            data: JSON.stringify(p),
            url: "/dc/moveToPreFiling"
          })
          .then(function(response) {
            // _self.$message(_self.$t("message.saveSuccess"));
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveSuccess"),
              duration: 2000,
              type: "success"
            });
            if(_self.$refs.leftDataGrid){
              _self.$refs.leftDataGrid.itemDataList = [];
            }
            _self.searchItem();
          })
          .catch(function(error) {
            console.log(error);
          });
    },
    arrangeComplete(statusVal){
      let _self=this;
      let p=new Array();
      _self.selectedItems.forEach(e=>{
        let m=new Map();
        m.set("ID",e.ID);
        m.set("STATUS",statusVal);
        p.push(m);
      });
      _self.updateData(p,function(){
        if(_self.$refs.leftDataGrid){
             _self.$refs.leftDataGrid.itemDataList = [];
          }
        _self.searchItem();
      });
    },
    fetchInformation() {
      let _self = this;
      // if(_self.radio=='卷盒'&&_self.selectRow.ID==undefined){
      //   //  _self.$message("请选择一条卷盒数据！");
      //    _self.$message({
      //           showClose: true,
      //           message: "请选择一条卷盒数据！",
      //           duration: 2000,
      //           type: "warning"
      //         });
      //     return;
      // }

      if (_self.selectedItems.length == 0) {
        //  _self.$message("请选择一条卷盒数据！");
        _self.$message({
          showClose: true,
          message: "请选择一条或多条数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }

      let tab = _self.selectedItems;
      let m = [];
      let p = [];
      let i;
      for (i in tab) {
        if (
          (tab[i]["CODING"] == undefined || tab[i]["CODING"] == "")) {
          _self.$message("所数据中中有未取号的数据，请先对其进行取号！");
          return;
        }
        // if(tab[i]["SUB_TYPE"]=="盒"){
        //   p.push(tab[i]["ID"]);
        // }
        m.push(tab[i]["ID"]);
      }
      _self.getInfoLoading = true;
      let pm = new Map();
      pm.set("configName", "ValidataHasArchiveCodeGC");
      let pids = m.join(",");
      pm.set("parentId", "'" + pids + "'");
      // pm.set('parentId',m);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(pm),
          url: "/dc/getObjectsByConfigClauseNoPage"
        })
        .then(function(response) {
          let sdata = response.data.data;
          if (sdata.length > 0) {
            _self.$message({
              showClose: true,
              message: "卷内文件" + sdata[0].CODING + "未取号！",
              duration: 2000,
              type: "warning"
            });
            _self.getInfoLoading = false;
            // _self.$message("盒内文件"+sdata[0].CODING+"未取号！");
            return;
          } else {
            _self
              .axios({
                headers: {
                  "Content-Type": "application/json;charset=UTF-8"
                },
                method: "post",
                data: JSON.stringify(m),
                url: "/dc/fetchInformationGc"
              })
              .then(function(response) {
                if (response.data.code == "1") {
                  _self.loadGridData(_self.currentFolder);
                  if(_self.$refs.leftDataGrid){
                      _self.$refs.leftDataGrid.itemDataList = [];
                    }
                  // _self.showInnerFile(null);
                  // _self.$message(_self.$t("message.fetchInformationSuccess"));
                  _self.$message({
                    showClose: true,
                    message: _self.$t("message.fetchInformationSuccess"),
                    duration: 2000,
                    type: "success"
                  });
                } else {
                  // _self.$message(response.data.message);
                  _self.$message({
                    showClose: true,
                    message: response.data.message,
                    duration: 5000,
                    type: "error"
                  });
                }
                _self.getInfoLoading = false;
              })
              .catch(function(error) {
                // _self.$message(_self.$t("message.fetchInformationFailed"));
                _self.$message({
                  showClose: true,
                  message: _self.$t("message.fetchInformationFailed"),
                  duration: 5000,
                  type: "error"
                });
                _self.getInfoLoading = false;
                console.log(error);
              });
          }
        })
        .catch(function(error) {
          _self.getInfoLoading = false;
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
      _self.updateData(p,function(){
        if(_self.$refs.leftDataGrid){
             _self.$refs.leftDataGrid.itemDataList = [];
          }
        _self.searchItem();
      });
      
    },
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

    takeNumbers() {
      let _self = this;
      if (_self.selectedItems.length == 0) {
        //  _self.$message("请选择一条或多条卷盒数据！");
        _self.$message({
          showClose: true,
          message: "请选择一条或多条卷盒数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      let tab = _self.selectedItems;
      let m = [];
      let i;
      for (i in tab) {
        // if (tab[i]["CODING"] != undefined && tab[i]["CODING"] !== "") {
        //   _self.$message("数据" + tab[i]["CODING"] + "已取过号不需要再取号!");
        //   return;
        // }
        m.push(tab[i]["ID"]);
      }
      _self.getNumLoading = true;
      // let m=[];
      // m.push(_self.selectRow.ID);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(m), //JSON.stringify(m),
          url: "/dc/takeNumbersArchiveGc"
        })
        .then(function(response) {
          _self.getNumLoading = false;
          _self.loadGridData(_self.currentFolder);
          // _self.showInnerFile(null);
          if(_self.$refs.leftDataGrid){
             _self.$refs.leftDataGrid.itemDataList = [];
          }
         
          let code = response.data.code;
          if (code == 1) {
            //  _self.$message("取号成功！");
            _self.$message({
              showClose: true,
              message: "取号成功！",
              duration: 2000,
              type: "success"
            });
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
