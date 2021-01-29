<template>
  <DataLayout>

      <el-dialog :visible.sync="modifyVisible" style="width:80%">
        <el-row style="padding:15px">
        <span>选择修改字段</span>
        <el-select v-model="resChoice" @change="onModifyChange" >
          <div v-for="item in objectSrc">
              <el-option :label="item.label" :value="item.id"></el-option>
          </div>
          </el-select>
        </el-row>
        <el-row style="padding:15px">
        <span>选择操作类型</span>
        <el-select @change="onChoiceChange" v-model="Choice" >
          <div v-for="items in modifyOption">
              <el-option :label="items" :value="items"></el-option>
          </div>
          </el-select>
        </el-row>
        <el-row v-if="isMF" style="padding:15px">
          <span >输入部分替换内容</span>
          <el-input style="width:220px" v-model="MFinput"></el-input>
        </el-row>
        <el-row style="padding:15px">
          <span>输入修改内容</span>
          <el-input style="width:220px" v-model="ChoiceInput"></el-input>
        </el-row>
        <el-row style="padding:15px;padding-left:200px">
          <el-button @click="submitModify" type='primary' style="padding-left:200px">提交修改</el-button>
          <el-button @click="close">取消</el-button>
        </el-row>

    </el-dialog>

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
    <el-dialog :visible.sync="PrintCoverpageVisible" width="80%">
      <PrintCoverpage ref="PrintCoverpage"></PrintCoverpage>
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
      width="96%"
      :title="dialogName+$t('application.property')"
      :visible.sync="propertyVisible"
      @close="propertyVisible = false"
      :close-on-click-modal="false"
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
      <div style="height:600px;">
        <PrintRidge ref="printRidge"></PrintRidge>
      </div>
    </el-dialog>
    <el-dialog
      title="打印条码"
      width="43%"
      :visible="printBarCodeVisible"
      @close="printBarCodeVisible=false"
    >
      <div style="height:600px;">
        <PrintBarCode ref="printBarCode" :archiveObjects="selectedItems" :isBarCode="true"></PrintBarCode>
      </div>
    </el-dialog>

    <el-dialog
      title=""
      width="50%"
      :visible="printPdf417Visible"
      @close="printPdf417Visible=false"
    >
      <div style="height:600px;">
        <PrintPdf417 ref="printPdf417" :archiveObjects="selectedItems" :isBarCode="true"></PrintPdf417>
      </div>
    </el-dialog>

    <el-dialog
      title="属性批量更新"
      width="50%"
      :visible="batchUpdateVisible"
      @close="batchUpdateVisible=false"
    >
      <BatchUpdate></BatchUpdate>
    </el-dialog>

    <el-dialog
      title="打印档号"
      width="43%"
      :visible="printArchiveCodeVisible"
      @close="printArchiveCodeVisible=false"
    >
      <div style="height:600px;">
        <PrintArchiveCode ref="printArchiveCode" :archiveObjects="selectedItems" :isBarCode="true"></PrintArchiveCode>
      </div>
    </el-dialog>
    <el-dialog :title="$t('application.Import')" :visible.sync="importdialogVisible" width="70%">
      <BatchFileMount ref="BatchFileMount" @afterMountFile="afterMountFile"></BatchFileMount>
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
    <el-dialog :title="$t('message.Batch')+' '+$t('application.Import')+$t('application.document')" :visible.sync="batchDialogVisible" width="80%" >
        <BatchImport ref="BatchImport" tmpPath="/系统配置/导入模板" :deliveryId="currentFolder.id" relationName="FolderId"  
        @onImported="onBatchImported" width="100%" importUrl="/import/batchImportFolder"></BatchImport>
        <div slot="footer" class="dialog-footer">
          <el-button @click="batchDialogVisible=false" size="medium">{{$t('application.close')}}</el-button>
         </div>
      </el-dialog>
    <template v-slot:main="{layout}">
      <div :style="{position:'relative',height: layout.height-startHeight+45+'px'}">
        <split-pane split="vertical" @resize="onHorizontalSplitResize" :min-percent='10' :default-percent='leftPercent'>
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
              <split-pane v-on:resize="onSplitResize" :default-percent='topPercent' split="horizontal">
                <template slot="paneL">
                  <el-form inline="true">
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
                      <div style="padding-top:5px;">
                        <el-radio style="margin-right:5px;" v-model="radio" label="案卷" @change="changeRadio">案卷</el-radio>
                        <el-radio style="margin-left:5px;" v-model="radio" label="文件" @change="changeRadio">文件</el-radio>
                      </div>
                    </el-col>
                    <el-col :span="18" style="padding-left:10px;">
                      <el-form-item>
                        <TypeSelectComment ref="TypeSelectComment" @afterSelecteType="newArchiveItem"></TypeSelectComment>
                      </el-form-item>
                      <el-form-item>
                        <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-copy-document"
                        @click="fileAttrsCopy(1)">复制著录</el-button>
                        <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-upload2"
                        @onImported="onBatchImported"
                        @click="batchDialogVisible=true"
                        title="批量导入"
                      >批量导入</el-button>
                      </el-form-item>
                      <el-form-item>
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        title="挂载文件"
                        icon="el-icon-upload2"
                        @click="beforeMount(selectedItems,true);"
                      >挂载文件</el-button>
                      </el-form-item>
                      <!-- <el-form-item>
                      <el-button
                        type="warning"
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
                      >{{$t('application.delete')}}</el-button> 
                      </el-form-item> -->
                      <el-form-item>
                      <el-button
                        type="primary"
                        plain
                        :loading="getNumLoading"
                        size="small"
                        icon="el-icon-s-order"
                        @click="takeNumbers"
                        title="文档取号"
                      >文档取号</el-button>
                      </el-form-item>
                      <el-form-item>
                      <el-button
                        type="primary"
                        plain
                        :loading="getInfoLoading"
                        size="small"
                        icon="el-icon-notebook-2"
                        @click="fetchInformation"
                        :title="$t('application.fetchInformation')"
                      >{{$t('application.fetchInformation')}}</el-button>
                      </el-form-item>
                       <!--
                      <el-form-item>
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-printer"
                        @click="beforePrintRidge(selectRow,'printRidgeGrid','打印背脊')"
                        title="打印"
                      ></el-button> 
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
                      >打印档号</el-button>
                     
                      </el-form-item>
                       -->
                      <el-form-item>
                        <el-dropdown class="avatar-container right-menu-item" trigger="click">
                          <div class="avatar-wrapper">
                            <i class="el-icon-printer"></i>
                            <span>打印</span>
                          </div>
                          <el-dropdown-menu slot="dropdown">
                            <el-dropdown-item divided>
                              <span @click="beforePrintPdf417(selectedItems)" style="display:block;">
                                <i class="el-icon-printer"></i>
                                打印条码
                              </span>
                            </el-dropdown-item>
                            <el-dropdown-item divided>
                              <span @click="beforePrintArchiveCode(selectedItems,'打印档号')" style="display:block;">
                                <i class="el-icon-printer"></i>
                                打印档号
                              </span>
                            </el-dropdown-item>
                             
                            <el-dropdown-item divided>
                              <span @click="beforePrintCoverpage(selectedItems)" style="display:block;">
                                <i class="el-icon-printer"></i>
                                打印封面
                              </span>
                            </el-dropdown-item>
                            <el-dropdown-item divided>
                              <span @click="beforePrintInnerDoc(selectedItems,'ArrangeInnerGridPrint')" style="display:block;">
                                <i class="el-icon-printer"></i>
                                打印卷内目录
                              </span>
                            </el-dropdown-item>
                            <el-dropdown-item divided>
                              <span @click="beforePrintPreparationTable(selectedItems)" style="display:block;">
                                <i class="el-icon-printer"></i>
                                打印备考表
                              </span>
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </el-dropdown>
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
                      <el-form-item>
                        <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-right"
                        @click="arrangeComplete('已整编')"
                        title="完成整编"
                      >完成整编</el-button>
                    </el-form-item>
                      <el-form-item>
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="el-icon-check"
                        @click="arrangeComplete('已质检')"
                        title="完成质检"
                      >完成质检</el-button>
                      </el-form-item>
                      <el-form-item v-if="currentFolder && currentFolder.folderPath && currentFolder.folderPath.indexOf('科技与信息')>0">
                      <el-button 
                        type="primary"
                        plain
                        :loading="releaseLoading"
                        size="small"
                        icon="el-icon-right"
                        @click="moveToPreFilling"
                        title="提交预归档库"
                      >提交预归档库</el-button>
                      </el-form-item>
                      <el-form-item>
                        <el-button
                          type="primary"
                          plain
                          :loading="releaseLoading"
                          size="small"
                          icon="el-icon-right"
                          @click="checkDC"
                          title="提交入库"
                        >提交入库</el-button>
                      </el-form-item>
                      <!-- <el-form-item>
                        <el-button
                          type="primary"
                          size="small"
                          plain
                          @click="beforeModify()"
                        >修改</el-button>
                       </el-form-item>
                      <el-form-item>
                        <el-button
                          type="primary"
                          size="small"
                          plain
                          @click="batchUpdateVisible=true"
                        >更新</el-button>
                      </el-form-item>
                      <el-form-item>
                        <el-button type="primary"
                        plain
                        size="small"
                        @click.native="exportData">{{$t("application.ExportExcel")}}</el-button>
                      </el-form-item> -->
                      <el-form-item>
                      <AddCondition v-model="AddConds" :inputType="hiddenInput" @change="searchItem"></AddCondition>
                      </el-form-item>
                      <el-form-item>
                        <el-dropdown class="avatar-container right-menu-item" trigger="click">
                          <div class="avatar-wrapper">
                            <i class="el-icon-caret-bottom"></i>
                            <span>更多</span>
                          </div>
                          <el-dropdown-menu slot="dropdown">
                            <el-dropdown-item divided>
                              <el-button @click="beforeModify()" style="display:block;width:117px" type="primary" plain size="small"> 
                                <i class="el-icon-s-tools"></i>
                                批量修改
                              </el-button>
                            </el-dropdown-item>
                            <el-dropdown-item divided>
                              <el-button @click="batchUpdateVisible=true" style="display:block;width:117px" type="primary" plain size="small">
                                <i class="el-icon-upload"></i>
                                批量更新
                              </el-button>
                            </el-dropdown-item>
                             
                            <el-dropdown-item divided>
                              <el-button @click="exportData()" style="display:block;width:117px" type="primary" plain size="small"> 
                                <i class="el-icon-download"></i>
                                导出EXCEL
                              </el-button>
                            </el-dropdown-item>
                            <el-dropdown-item divided>
                              <el-button @click="SearchBusinessDC()" style="display:block;width:117px" type="primary" plain size="small"> 
                                <i class="el-icon-search"></i>
                                商务文件查询
                              </el-button>
                            </el-dropdown-item>
                          
                      <el-dropdown-item divided>
                        <AddToArchive :folderId="currentFolder.id" :archiveObjects="selectedItems" @savesuccess='searchItem' ></AddToArchive>
                      </el-dropdown-item>
                      <el-dropdown-item divided>
                          <el-button  @click="logicallyDel(selectedItems,function(){
                          let _self=this;
                          if(_self.$refs.leftDataGrid){
                              _self.$refs.leftDataGrid.itemDataList = [];
                            }
                          _self.loadGridData(_self.currentFolder);
                        })" style="display:block; width:117px" type="warning" plain size="small" >
                        <i class="el-icon-document-delete" ></i>
                        删 除
                          </el-button>
                          </el-dropdown-item>
                          </el-dropdown-menu>
                        </el-dropdown>
                      </el-form-item>                      
                    </el-col>
                  </el-row>
                  </el-form>
                  <el-row>
                    <el-col :span="24">
                      <DataGrid
                        ref="mainDataGrid"
                        key="main"
                        dataUrl="/dc/getInnerFolderDocuments"
                        :isInitData="false"
                        v-bind:tableHeight="(layout.height-startHeight)*(topPercent)/100-topbarHeight"
                        :isshowOption="true"
                        :isshowSelection="true"
                        :condition="mainParam.condition"
                        :folderId="mainParam.folderId"
                        showOptions="查看内容"
                        :isShowChangeList="true"
                        :isshowCustom="true"
                        :optionWidth = "3"
                        @rowclick="beforeShowInnerFile"
                        @selectchange="selectChange"
                        @changeGridName="changeDataGridName"
                        :showBatchCheck="true"
                      ></DataGrid>
                    </el-col>
                  </el-row>
                  
                </template>
                <template slot="paneR" v-if="isFile">
                  <el-row>
                    <el-form inline="true">
                      <el-form-item>
                    <span>卷内文件列表</span>
                      </el-form-item>
                      <el-form-item>
                    <el-button type="primary" plain size="small" @click="beforeCreateFile(selectRow)">著录</el-button>
                    </el-form-item>
                      <el-form-item>
                    <el-button
                    type="primary"
                    plain
                    size="small"
                    @click="fileAttrsCopy(2)">复制著录</el-button>
                    </el-form-item>
                      <el-form-item>
                    <el-button
                      type="primary"
                      plain
                      size="small"
                      title="挂载文件"
                      @click="beforeMount(selectedInnerItems,false);"
                    >挂载文件</el-button>
                    </el-form-item>
                      <el-form-item>
                    <el-button type="primary" plain size="small" title="上移" @click="onMoveUp()">上移</el-button>
                    </el-form-item>
                      <el-form-item>
                    <el-button type="primary" plain size="small" title="下移" @click="onMoveDown()">下移</el-button>
                    </el-form-item>
                      <el-form-item>
                    <el-button
                          type="primary"
                          size="small"
                          plain
                          @click="beforeInnerModify()"
                    >修改</el-button>
                    <el-button type="warning" plain size="small" title="删除"  @click="logicallyDel(selectedInnerItems,function(){
                          let _self=this;
                          _self.showInnerFile(_selft.selectedRow);
                        })">{{$t('application.delete')}}</el-button>
                    </el-form-item>
                      <el-form-item>
                      <AddCondition ref="childAddCondition" v-model="childAddConds" :inputType="hiddenInput" :showFileType= false :typeName='childTypeName' @change="searchChildItem"></AddCondition>
                    </el-form-item>
                    </el-form>
                  </el-row>
                  <el-row>
                    <el-col :span="24">
                      <DataGrid
                        ref="leftDataGrid"
                        key="left"
                        @rowclick="selectOneFile"
                        :dataUrl="leftParam.childUrl"
                        gridViewName='ArrangeInnerGrid'
                        :condition="leftParam.childCondition"
                        :parentId="parentId"
                        v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
                        :isshowOption="true"
                        :isshowSelection="true"
                        :folderId="mainParam.folderId"
                        showOptions="查看内容"
                        :isShowChangeList="true"
                        :isshowCustom="true"
                        :optionWidth = "3"
                        @selectchange="selectInnerChange"
                        :showBatchCheck="true"
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
import AddCondition from '@/views/record/AddCondition'
import PrintPage from "@/views/record/PrintPage";
// import PrintVolumes from "@/views/record/PrintVolumes";
import PrintVolumes from "@/views/record/PrintVolumes4Archive";
import PrintRidge from "@/views/record/PrintRidge";
import PreparationTablePrint from "@/views/record/PreparationTablePrint.vue"
import PrintBarCode from "@/views/record/PrintBarCode.vue"
import PrintArchiveCode from "@/views/record/PrintArchiveCode.vue"
import PrintCoverpage from "@/views/record/PrintCoverpage.vue"
import PrintPdf417 from "@/views/record/PrintPdf417.vue"
import BatchImport from "@/components/controls/ImportDocument";
import ExcelUtil from "@/utils/excel.js";
import BatchUpdate from "@/views/record/BatchUpdate.vue" 
import BatchFileMount from "@/views/record/BatchFileMount.vue" 
import AddToArchive from "@/views/record/AddToArchive.vue"

export default {
  name: "ArchiveArrange",
  components: {
    ShowProperty: ShowProperty,
    TypeSelectComment:TypeSelectComment,
    BatchFileMount: BatchFileMount,
    DataGrid: DataGrid,
    PrintPage: PrintPage,
    PrintVolumes: PrintVolumes,
    PrintRidge: PrintRidge,
    PreparationTablePrint:PreparationTablePrint,
    PrintBarCode:PrintBarCode,
    PrintArchiveCode:PrintArchiveCode,
    BatchUpdate:BatchUpdate,
    DataLayout:DataLayout,
    PrintPdf417:PrintPdf417,
    BatchImport:BatchImport,
    AddCondition:AddCondition,
    PrintCoverpage:PrintCoverpage,
    AddToArchive:AddToArchive
  },
  data() {
    return {
      leftStorageName: 'ArchiveArrangeWidth',
      leftPercent: 20,

      // 本地存储高度名称
      topStorageName: 'ArchiveArrangeHeight',
      // 非split pan 控制区域高度
      startHeight: 125,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 125,
      // 底部除列表高度
      bottomHeight: 35,
      isExpand: false,
      rightTableHeight: (window.innerHeight - 150) / 2,
      asideHeight: window.innerHeight - 95,
      treeHight: window.innerHeight - 135,
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
      batchUpdateVisible: false,
      mountParentDoc:true,
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
      archiveStatus:"整编",
      extendMap:{},
      pieceNum:"",//批次号
      pieceNumVisible:false,//是否显示取批次号dialog
      PreparationTablePrintVisible:false,
      printBarCodeVisible:false,
      PrintCoverpageVisible:false,
      printArchiveCodeVisible:false,
      printPdf417Visible:false,
      isFile:true,
      batchDialogVisible: false,
      res:[],
      resChoice:'',
      ChoiceTypeName:'',
      modifyVisible:false,
      objectSrc:[],
      modifyOption:[],
      Choice:'',
      ChoiceInput:'',
      isAdd:false,
      AddComment:'',
      isDates:false,
      newChildDoc: false,
      hiddenInput:"hidden",
      AddConds:'',
      volumeInArchiveGridName:"",
      isInnerModify:false,
      isModify:false,
      isMF:false,
      MFinput:"",
      childAddConds:'',
      childTypeName:'所有',
      imageViewVisible:false,
      leftParam:{
        childUrl:'/dc/getDocuByRelationParentId',
        childCondition:"and a.NAME='irel_children' and b.IS_HIDDEN=0",
      },
      loadInfo:false,
      volumeInArchiveGridName:"",
      innerGridName:""
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
    m.set("condition","  and IS_HIDDEN=0 and IS_CHILD=0 ");
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
      }, 100);
  },
  methods: {
     SearchBusinessDC() {
      let href = this.$router.resolve({
        path: "/record/selectbusinessDC",
      });
      window.open(href.href, "_blank");
    },
    onChoiceChange(){
      if(this.Choice=='部分替换'){
      this.isMF = true}
      if(this.Choice!='部分替换'){
        this.isMF = false
      }
    },
    close(){
      this.modifyVisible = false
    },
    fileAttrsCopy(copyType){
      let _self = this;
      if(_self.currentFolder.id==undefined){
        _self.$message({
                showClose: true,
                message: "请在文件夹下进行操作",
                duration: 2000,
                type: "warning"
              });
        return;
      }
      var flag = 0;
      if(_self.selectRow.ID==undefined&&_self.innerSelectedOne.ID==undefined){
        flag = 1;
      }
      //主文件选了，子文件没选
      if(_self.selectRow.ID!=undefined&&_self.innerSelectedOne.ID==undefined){
        //主文件选的是案卷
        if(_self.selectRow.C_ITEM_TYPE == '案卷'){
          if(copyType == 1){
            //flag=2,案卷复制案卷
            flag = 2;
          }else{
            //flag=3,按照原来的方法，子文件参考父的属性，进行复制
            flag = 3;
          }
        }
        //主文件勾选的是普通文件
        else{
          if(copyType == 1){
            //flag=4,主文件复制主文件
            flag = 4;
          }else{
            //flag=5,文件不可以著录子文件！
            flag = 5;
          }
        }
      }
      if(_self.selectRow.ID!=undefined&&_self.innerSelectedOne.ID!=undefined){
        if(copyType == 1){
            //flag=6,主文件复制主文件
            flag = 6;
          }else{
            //flag=7,子文件复制子文件！
            flag = 7;
          }
      }
      switch(flag){
        case 1 :
          _self.$refs.TypeSelectComment.showdialog();
          break;
        case 2 :
          _self.copyFile(_self.selectRow);
          //console.log("案卷复制案卷");
          //todo 案卷复制案卷
          break;
        case 3 :
          _self.beforeCreateFile(_self.selectRow);
          console.log("按照原来的方法，子文件参考父的属性，进行复制");
          //todo 按照原来的方法，子文件参考父的属性，进行复制
          break;
        case 4 :
          _self.copyFile(_self.selectRow);
          console.log("主文件复制主文件");
          //todo 主文件复制主文件
          break;
        case 5 :
          _self.$message({
                showClose: true,
                message: '非案卷文件不可以著录子文件！',
                duration: 2000,
                type: "warning"
              });
          console.log("按照原来的方法，不能著录文件！！");
          //todo 按照原来的方法，反正文件不能著录文件！！
          break;
        case 6 :
          _self.copyFile(_self.selectRow);
          console.log('案卷复制案卷,主文件复制主文件')
          //todo 案卷复制案卷,主文件复制主文件
          break;
        case 7 :
          _self.beforeCreateLevel1File(_self.innerSelectedOne,_self.selectRow.ID);
          console.log('子文件复制子文件')
          //todo 子文件复制子文件
          break;
        default:
          console.log('(*￣︶￣)')
      }
    },
    changeDataGridName(val){
      this.innerGridName = val;
    },
    exportData() {
      let _self = this;
      var gridViewName = "";
      if(_self.innerGridName!=""){
        gridViewName = _self.innerGridName;
      }else{
        gridViewName = _self.$refs.mainDataGrid.gridViewName;
      }
      let params = {
        URL: "/file/exportFolderPath",
        gridName: gridViewName,
        folderId: _self.currentFolder.id,
        orderBy: "MODIFIED_DATE desc",
        condition: _self.$refs.mainDataGrid.condition,
        pageSize: _self.pageSize*10,
        pageIndex: _self.currentPage - 1,
        lang: "zh-cn",
      };
      ExcelUtil.export4Cnpe(params);
    },
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
    copyArchiveItem(typeName,copyInfo) {
      let _self = this;
      _self.newChildDoc = false;
      if (_self.currentFolder.id) {
        _self.selectedItemId = "";
        _self.typeName=typeName;
        _self.propertyVisible = true;
        setTimeout(() => {
          if (_self.$refs.ShowProperty) {
            _self.$refs.ShowProperty.myItemId = "";
            _self.dialogName = typeName;
            _self.extendMap=null;
            _self.$refs.ShowProperty.parentDocId = "";
            _self.$refs.ShowProperty.myTypeName = typeName;
            _self.$refs.ShowProperty.myFolderId = _self.currentFolder.id;
		   if(copyInfo){
			  let mp = new Map();
			  for (const key in copyInfo) {
				  mp.set(key, key);
			  }
			  _self.$refs.ShowProperty.setMainSubRelation(mp);
			  _self.$refs.ShowProperty.setMainObject(copyInfo);
      }
            if(_self.loadInfo){
              _self.$refs.ShowProperty.loadFormInfo();
            }else{
              _self.loadInfo = true;
            }

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
    //复制著录方法
    beforeCreateLevel1File(row,parentId){
      let _self=this;
      if(_self.selectRow.ID==undefined){
         _self.$message({
                showClose: true,
                message: '请选择一条主文件！',
                duration: 2000,
                type: "warning"
              });
        return;
      }
      _self.parentId = parentId
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: {id:row.ID},
          url: "/dc/getDocConfig"
        }).then(function(response){
          let code=response.data.code;
          if(code=='1'){
            let fileType = row.TYPE_NAME
            _self.newArchiveFileItem(fileType,row,response.data.copyInfo);
          }else{
            _self.$message({
                showClose: true,
                message: '添加失败！',
                duration: 5000,
                type: "error"
              });
          }
        }).catch(function(error) {
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
    copyFile(row){
      let _self=this;
      if(_self.selectRow.ID==undefined){
         _self.$message({
                showClose: true,
                message: '请选择一条主文件！',
                duration: 2000,
                type: "warning"
              });
        return;
      }
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: {id:row.ID},
          url: "/dc/getDocConfig"
        }).then(function(response){
          let code=response.data.code;
          if(code=='1'){
            let fileType = row.TYPE_NAME
            _self.copyArchiveItem(fileType,response.data.copyInfo);
          }else{
            _self.$message({
                showClose: true,
                message: '添加失败！',
                duration: 5000,
                type: "error"
              });
          }
        }).catch(function(error) {
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
          data: row.ID,
          url: "/dc/getArchiveFileConfig"
        })
        .then(function(response) {
          let code=response.data.code;
          let fileType
          if(code=='1'){
            let data=response.data.data;
            data.forEach(item => {
              if(item.C_TO!=row.TYPE_NAME) {
                fileType=item.C_TO;
              }                 
            })
            _self.newChildDoc = true;
            _self.newArchiveFileItem(fileType,row, response.data.copyInfo);
            //writeAudit(_self.parentId);
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

    ///打印档号
    beforePrintArchiveCode(selectedRows,vtitle) {
      let _self = this;
      if (selectedRows == undefined||selectedRows.length==0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请至少选择一条数据进行打印",
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
    //打印二维码
    beforePrintPdf417(selectedRows){
      let _self = this;
      if (selectedRows == undefined||selectedRows.length==0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择至少一条数据进行打印",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.printPdf417Visible = true;
      setTimeout(() => {
        _self.$refs.printPdf417.loadData(selectedRows);
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
    //打印备考表
    beforePrintPreparationTable(selectedRows){
      let _self=this;
      if (selectedRows == undefined||selectedRows.length==0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择至少一个案卷进行打印",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.PreparationTablePrintVisible = true;

      setTimeout(()=>{
        _self.$refs.PreparationTablePrint.refreshArchiveObj(selectedRows); 
      },100);

    },
    //打印封面
    beforePrintCoverpage(selectedRows){
      let _self=this;
      if (selectedRows == undefined||selectedRows.length==0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择至少一个案卷进行打印",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.PrintCoverpageVisible = true;

      setTimeout(()=>{
        _self.$refs.PrintCoverpage.refreshArchiveObj(selectedRows); 
      },100);

    },
    //打印卷内目录
    beforePrintInnerDoc(selectedRows,gridName){
      let _self=this;
      if (selectedRows == undefined||selectedRows.length==0) {
        // _self.$message('请选择一条数据进行打印');
        _self.$message({
          showClose: true,
          message: "请选择至少一个案卷进行打印",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.printVolumesVisible = true;
      setTimeout(()=>{
        _self
        .axios.post("/dc/getPrintArchiveGrid",selectedRows[0].TYPE_NAME)
        .then(function(response) {
          _self.$refs.printVolumes.isBusiness= false
          if(response.data.code=='1'){
            if(selectedRows[0].C_ARC_CLASSIC=='商务管理'){    
              _self.$refs.printVolumes.isBusiness = true
            }
            _self.$refs.printVolumes.selectedRows = selectedRows
            let printGridName=response.data.data.attributes.C_TO;
            _self.$refs.printVolumes.dialogQrcodeVisible = false
            _self.$refs.printVolumes.refreshDataGrid(selectedRows,
            printGridName); 
          }else{
            _self.$refs.printVolumes.dialogQrcodeVisible = false
             _self.$refs.printVolumes.refreshDataGrid(selectedRows,
            gridName); 
          }
        })
        .catch(function(error) {
         
          _self.$message({
            showClose: true,
            message: "操作失败",
            duration: 5000,
            type: "error"
          });
          console.log(error);
        });

        
      },100);
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
        _self.topPercent=99;
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
    beforeMount(selrow, isParent) {
      let _self = this;
      _self.mountParentDoc = isParent;
      if (selrow.length<1||selrow[0].ID == undefined) {
        _self.$message({
          showClose: true,
          message: "请至少勾选一条数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.importdialogVisible = true;
      setTimeout(()=>{
        _self.$refs.BatchFileMount.archiveObjects = selrow;
      },100);
    },
    //挂载成功触发事件
    afterMountFile(){
      if(this.mountParentDoc){
        this.loadGridData(this.currentFolder);
      }else{
        this.showInnerFile(this.selectedRow);
      }
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
          if( _self.mountParentDoc){
            _self.searchItem();
          }else{
            _self.showInnerFile(_self.selectRow);
          }
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
    onModifyChange(id){
      let _self = this
      _self.objectSrc.forEach(item => {
        if(item.id==id) {        //找到对应字段了
        if (item.controlType=='ValueSelect'||item.controlType=='Date'||item.controlType=='Select'){
          _self.modifyOption=['全部替换']
          return
        }else if(item.controlType=='TextBox'||item.controlType=='TextArea'){
          _self.modifyOption=['加前缀','加后缀','全部替换','部分替换']
        }
      }                 
      })
    },
    submitModify(){
      let ids = []
      let _self = this
      let attr=''
      if(this.isModify==true){
      for(let i=0;i<this.selectedItems.length;i++){
        ids.push(this.selectedItems[i].ID)
      }}
      if(this.isInnerModify==true){
        for(let i=0;i<this.selectedInnerItems.length;i++){
        ids.push(this.selectedInnerItems[i].ID)
      }
      }
      _self.objectSrc.forEach(item => {
        if(item.id==_self.resChoice) {        //找到对应字段了
        attr = item.attrName
        if(item.controlType=='Date'){
        _self.isDates = true
        }
        }                 
      })
      var m = new Map();
      m.set("ids",ids)
      m.set("modifyType",this.Choice)
      m.set("attr",attr)
      m.set("MFinput",_self.MFinput)
      if(this.isDates==true){
         var r=this.ChoiceInput.match(/^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2})$/); 
          if(r==null){
            this.$alert("请输入格式正确的日期\n\r日期格式：yyyy-mm-dd\n\r例    如：2021-01-01\n\r");
             return false;
          }
          var d=new Date(r[1],r[3]-1,r[4]);   
          var num = (d.getFullYear()==r[1]&&(d.getMonth()+1)==r[3]&&d.getDate()==r[4]);
          if(num==0){
           this.$alert("请输入格式正确的日期\n\r日期格式：yyyy-mm-dd\n\r例    如：2021-01-01\n\r");
          return
          }
          
      }
      m.set("modifyResult",this.ChoiceInput)
      let formdata = new FormData();
      formdata.append("metaData",JSON.stringify(m));
        axios
        .post("/exchange/doc/modifty", formdata)
        .then(function(response) {
        if(response.data.code==1){
          _self.$message("修改成功！")
          _self.modifyVisible=false
          _self.$refs.mainDataGrid.loadGridData()
          _self.$refs.leftDataGrid.loadGridData()
          _self.resChoice = ""
          _self.Choice = ""
          _self.MFinput = ""
          _self.isMF = false
          _self.ChoiceInput = ""
        }
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    getTypeNamesByMainList(keyName) {
      let _self = this;
      var m = new Map();
      let j = 0
      m.set('itemInfo',keyName);//ID 或类型
      m.set('formName',keyName);
      m.set('lang',_self.getLang());
      axios
        .post("/dc/getFormClassifications", JSON.stringify(m))
        .then(function(response) {
          let sourcedata = response.data.data
          _self.objectSrc=[]
          sourcedata.forEach(items =>{
            items.ecmFormItems.forEach( item => {
              _self.objectSrc.push(item)
            })
          })
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    beforeShowInnerFile(row) {
      // let keys = Object.keys(row)
      // console.log(row.TYPE_NAME)
      // this.getTypeNamesByMainList(row.TYPE_NAME)
      let _self = this;
      this.innerSelectedOne = [];
      _self.leftParam.childCondition = "and a.NAME='irel_children' and b.IS_HIDDEN=0"
      _self.leftParam.childUrl = "/dc/getDocuByRelationParentId"
      this.showInnerFile(row);
      if (row != null) {
        if(row.C_ITEM_TYPE!='案卷'){
          _self.childTypeName = '所有'
        }else{
          _self.getChildType(row.TYPE_NAME);
          console.log(_self.childTypeName)
          _self.$refs.childAddCondition.loadColumnInfo(_self.childTypeName);
        }
      }
    },
    //获取卷盒下的文件类型
    getChildType(fileType){
      let _self = this;
      axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: fileType,
          url: "/dc/getBoxChildType"
        }).then(function(response){
          _self.childTypeName = response.data.data
          console.log(_self.childTypeName)
        }).catch(function(error){
          console.log(error)
        })
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
             _self.$refs.leftDataGrid.loadGridData();
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
      this.ChoiceTypeName=''
      this.ChoiceTypeName = val[0].TYPE_NAME
      this.getTypeNamesByMainList(this.ChoiceTypeName)
      this.selectedItems = val;
      
    },
    // 表格行选择
    selectOutChange(val) {
      // console.log(JSON.stringify(val));
      this.selectedOutItems = val;
    },
    selectInnerChange(val) {
      this.ChoiceTypeName=''
      this.ChoiceTypeName = val[0].TYPE_NAME
      this.getTypeNamesByMainList(this.ChoiceTypeName)
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
      if(_self.AddConds!=''){
        key = key + " and "+_self.AddConds
      }
      _self.mainParam.condition=key;
      _self.mainParam.folderId=indata.id;
      var lastGridView = _self.$refs.mainDataGrid.gridViewName;
      console.log("lastGridView:" + lastGridView);
      _self.$refs.mainDataGrid.gridViewName = indata.gridView;
      _self.$refs.mainDataGrid.gridviewInfo.gridviewName = indata.gridView;
       console.log("newGridView:" + indata.gridView);
      _self.$nextTick(()=>{
        if(lastGridView != indata.gridView){
           _self.$refs.mainDataGrid.loadGridInfo();
           
        }
         _self.$refs.mainDataGrid.loadGridData();
         _self.$refs.leftDataGrid.itemDataList = [];
      },500);
      
      
    },
    beforeInnerModify(){
      this.isInnerModify = true
      this.isModify = false
      if(this.selectedInnerItems.length==0){
        this.$message("请选择至少一条文件进行修改！")
        return
      }
      this.modifyVisible = true
      this.getTypeNamesByMainList(this.ChoiceTypeName)
    },
    beforeModify(){
      this.isModify = true
      this.isInnerModify = false
      if(this.selectedItems.length==0){
        this.$message("请选择至少一条文件进行修改！")
        return
      }
      this.modifyVisible = true
      this.getTypeNamesByMainList(this.ChoiceTypeName)
    },
    onBatchImported(){
      this.handleNodeClick(this.currentFolder);
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
      _self.newChildDoc = false;
      if (_self.currentFolder.id) {
        _self.selectedItemId = "";
        _self.typeName=typeName;
        _self.propertyVisible = true;
        setTimeout(() => {
          if (_self.$refs.ShowProperty) {
            _self.$refs.ShowProperty.myItemId = "";
            _self.dialogName = typeName;
            _self.extendMap=null;
            _self.$refs.ShowProperty.parentDocId = "";
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
    newArchiveFileItem(typeName, selectedRow, copyInfo) {
      let _self = this;
      _self.newChildDoc = true;
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
             if(copyInfo){
              let mp = new Map();
              for (const key in copyInfo) {
                  mp.set(key, key);
              }
              _self.$refs.ShowProperty.setMainSubRelation(mp);
              _self.$refs.ShowProperty.setMainObject(copyInfo);
            }
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
        if(_self.newChildDoc){
          m.set("transferId", _self.parentId);
        }
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
              let msg = _self.$t('message.newFailured');
              if(response.data.message){
                msg += ":" + response.data.message;
              }
              _self.$message({
                showClose: true,
                message: msg,
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
    upgradeDesign(){
      let _self=this;
      let flag=true;
       _self.selectedItems.forEach((e)=>{
         if(e.typeName!='设计文件'){
           flag=false;
           return;
         }
       });
       if(!flag){
         _self.$message({
              showClose: true,
              message: "所选文件中包含非设计文件数据",
              duration: 2000,
              type: "warning"
            });
         return;
       }
      let ids=new Array();
      _self.selectedItems.forEach((e)=>{
        ids.push(e.ID);
      })
      _self
          .axios({
            headers: {
              "Content-Type": "application/json;charset=UTF-8"
            },
            datatype: "json",
            method: "post",
            data: JSON.stringify(ids),
            url: "/dc/upgradeDesign"
          })
          .then(function(response) {
            // _self.$message(_self.$t("message.saveSuccess"));
            if(response.data.code==1){
                _self.$message({
                  showClose: true,
                  message: "设计文件升级成功",
                  duration: 2000,
                  type: "success"
                });
                _self.searchItem();
            }else{
              _self.$message({
                  showClose: true,
                  message: "设计文件升级失败",
                  duration: 2000,
                  type: "error"
                });
            }
            
          })
          .catch(function(error) {
            console.log(error);
          });

    },
    //查询文档
    searchItem() {
      console.log(this.mainParam.condition)
      this.loadGridData(this.currentFolder);
      //  this. loadPageInfo(this.currentFolder);
    },
    searchChildItem(){
      let _self = this;
      console.log(_self.childAddConds)
      var parentCond = ' and id in (select child_id from ecm_relation where parent_id='+"'"+_self.selectRow.ID+"'"+')';
      var childCondition = _self.childAddConds+parentCond+' and IS_HIDDEN=0';
      //_self.leftParam.childCondition = _self.childAddConds+parentCond+' and IS_HIDDEN=0';
      //_self.leftParam.childUrl = "/dc/getInnerFolderDocuments"
      _self.$refs.leftDataGrid.condition = childCondition;
      _self.$refs.leftDataGrid.dataUrl = "/dc/getInnerFolderDocuments";
      _self.$refs.leftDataGrid.loadGridData();
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
      for(var i=0;i<_self.selectedItems.length;i++){
         if(_self.selectedItems[i].C_BATCH_CODING2==null || _self.selectedItems[i].C_BATCH_CODING2.length == 0){
           _self.$message({
            showClose: true,
            message: "请生成批次号后再提交！",
            duration: 5000,
            type: "warning"
          });
          return;
        }
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
    checkDC(){
      let _self = this
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
      for(var i=0;i<_self.selectedItems.length;i++){
         if(_self.selectedItems[i].C_BATCH_CODING2==null || _self.selectedItems[i].C_BATCH_CODING2.length == 0){
           _self.$message({
            showClose: true,
            message: "请生成批次号后再提交！",
            duration: 5000,
            type: "warning"
          });
          return;
        }
      }
      var m = [];
      let error =""
      let tab = _self.selectedItems;
      var i;
      for (i in tab) {
        if(tab[i].C_ARCHIVE_CODING==null||tab[i].C_ARCHIVE_CODING==''){
          let j=1
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
      axios.post("/dc/Archive/checkDC",JSON.stringify(m),{
        headers: {
            "Content-Type": "application/json;charset=UTF-8"
        }
      })
      .then(function (response) {
        let code = response.data.code;
        if (code == 1) {
          _self.penddingStorage()
        } else {
          _self.$message({
            showClose: true,
            message:response.data.message,
            duration: 2000,
            type: "warning",
          });
          return;
        }
      })
      .catch(function (error) {
        _self.$message(response.data.message);
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
      var m = [];
      let error =""
      let tab = _self.selectedItems;
      var i;
      for (i in tab) {
        if(tab[i].C_ARCHIVE_CODING==null||tab[i].C_ARCHIVE_CODING==''){
          let j=1
          error +=j+'.'+tab[i].TITLE+'<br/>'
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
      _self
          .axios({
            headers: {
              "Content-Type": "application/json;charset=UTF-8"
            },
            datatype: "json",
            method: "post",
            data: JSON.stringify(m),
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
          (tab[i]["C_ARCHIVE_CODING"] == undefined || tab[i]["C_ARCHIVE_CODING"] == "")) {
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
          _self.pieceNumVisible = false;
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
.el-form-item__content {
    line-height: 36px ;
}
</style>
