<template>
  <DataLayout>
    <template v-slot:header>
      <el-dialog
        :title="$t('application.AddFile')"
        :visible.sync="propertyVisible"
        @close="propertyVisible = false"
        :append-to-body="true"
        width="90%"
        :close-on-click-modal="false"
        v-dialogDrag
      >
        <el-form :inline="true" :model="filters" @submit.native.prevent>
          <el-form-item>
            <el-select v-model="filters.docType">
              <el-option :label="$t('application.all')+' '+$t('application.subDC')" value></el-option>
              <el-option
                v-for="(name,nameIndex) in childrenTypes"
                :key="'Type2_'+nameIndex"
                :label="name"
                :value="name"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="filters.title"
              :placeholder="$t('application.Coding')+$t('application.or')+$t('application.Title')"
              @keyup.enter.native="searchItem"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" v-on:click="searchItem">{{$t('application.SearchData')}}</el-button>
          </el-form-item>
        </el-form>
        <DataGrid
          ref="searchDoc"
          key="searchDoc"
          data-url="/dc/getDocuments"
          v-bind:tableHeight="tableHeight"
          v-bind:isshowOption="true"
          v-bind:isshowSelection="true"
          :condition="searchFileCondition"
          gridViewName="WorkflowFileGrid"
          :optionWidth="1"
          :isShowMoreOption="false"
          :isshowCustom="false"
          :isEditProperty="false"
          :isShowChangeList="false"
          :isshowicon="false"
          @selectchange="fileSelect"
        ></DataGrid>
        <div slot="footer" class="dialog-footer">
            <el-button @click="saveFileToWorkflow" :loading="butt">{{$t('application.save')}}</el-button>
            <el-button @click="propertyVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
    </template>
    <template v-slot:main="{layout}">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <split-pane
          v-on:resize="onSplitResize"
          :min-percent="20"
          :default-percent="topPercent"
          split="horizontal"
        >
          <template slot="paneL">
            <el-tabs value="t01">
              <el-tab-pane :label="$t('application.FilesInWorkflow')" name="t01">
                <el-row v-if="allowEdit">
                  <el-col :span="24" style="text-align: left">
                    <el-form :inline="true" :model="filters" @submit.native.prevent>
                      <el-form-item>
                        <el-button type="primary" @click="beforeAddFile">{{ $t("application.new") }}</el-button>
                      </el-form-item>
                      <el-form-item>
                        <el-button type="warning">{{ $t("application.delete") }}</el-button>
                      </el-form-item>
                    </el-form>
                  </el-col>
                </el-row>
                <!--列表-->
                <DataGrid
                  ref="fileList"
                  key="fileList"
                  data-url="/dc/getDocuByRelationParentId"
                  v-bind:tableHeight="tableHeight"
                  v-bind:isshowOption="true"
                  v-bind:isshowSelection="true"
                  gridViewName="WorkflowFileGrid"
                  condition=" and a.NAME='流程文件'"
                  :optionWidth="1"
                  :itemDataList="files"
                  :isShowMoreOption="false"
                  :isshowCustom="false"
                  :isEditProperty="allowEdit"
                  :isShowChangeList="false"
                  :isshowicon="false"
                  :isshowPage="isShowPage"
                  @selectchange="relevantDocRVSelect"
                ></DataGrid>
              </el-tab-pane>
            </el-tabs>
          </template>
        </split-pane>
      </div>
    </template>
  </DataLayout>
</template>
<script type="text/javascript">
import ShowProperty from "@/components/ShowProperty";
import DataGrid from "@/components/DataGrid";
import AddCondition from "@/views/record/AddCondition";
import RejectButton from "@/components/RejectButton";
import ExcelUtil from "@/utils/excel.js";
import DataSelect from "@/components/ecm-data-select";
import DataLayout from "@/components/ecm-data-layout";
import AttachmentFile from "@/views/dc/AttachmentFile.vue";
export default {
  components: {
    ShowProperty: ShowProperty,
    DataGrid: DataGrid,
    AddCondition: AddCondition,
    DataSelect: DataSelect,
    RejectButton: RejectButton,
    DataLayout: DataLayout,
    AttachmentFile: AttachmentFile
  },
   model: {
     prop:"files",
    event: "change",
  },
  props: {
    allowEdit: { type: Boolean, default: true },
    isShowPage:{type:Boolean,default:true},
    files:{type:Array,default:[]},
    workflowObj:{type:Object,default:{}}
  },
  data() {
    return {
      // 本地存储高度名称
      topStorageName: "ReceivedDCHeight",
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 120,
      childrenTypes: [],
      propertyVisible: false,
      filters: {
        projectCode: "",
        docType: "",
        coding: "",
        title: "",
        limit: 10,
        typeName: "",
        relationName: ""
      },
      selectedFiles:[],
      butt:false,
      searchFileCondition:""
    };
  },
  mounted() {
   
    this.getTypeNamesByMainList("DCTypeSubContractor");
  },
  methods: {
    beforeAddFile() {
      let _self=this;
      this.getEcmcfgActive(this.workflowObj.ID,"start",function(ecmCfgActivity){
        _self.searchFileCondition=ecmCfgActivity.formCondition;
        _self.propertyVisible=true;
      });
        
    },
    fileSelect(val){
        this.selectedFiles=val;
    },
    saveFileToWorkflow(){
        let _self=this;
        if(_self.$refs.fileList.itemDataList==null){
            _self.$refs.fileList.itemDataList=_self.selectedFiles;
        }else{
            _self.selectedFiles.forEach(e=>{
                let isContain=false;
                _self.$refs.fileList.itemDataList.find((function(value) {
                if(value === e) {
                    isContain=true;
                    return;
                    //则包含该元素
                    }
                }))
                if(!isContain){
                    _self.$refs.fileList.itemDataList.push(e);
                }
            });
            // this.$refs.fileList.itemDataList=this.$refs.fileList.itemDataList.concat(this.selectedFiles);
        }
        _self.files=_self.$refs.fileList.itemDataList;
        _self.$emit('change',_self.$refs.fileList.itemDataList);
        this.butt=false;
        this.propertyVisible=false;
    },
    searchItem() {
      let _self = this;
      let key = " 1=1 ";
      
      if (_self.filters.docType != "") {
        key += " and TYPE_NAME = '" + _self.filters.docType + "'";
      }
      if (_self.filters.title != "") {
        key +=
          " and ( TITLE like '%"+_self.filters.title+"%' or CODING like '%" +
          _self.filters.title +
          "%' " +
          ")";
      }
      if (key != "") {
        _self.$refs.searchDoc.condition = key;
      }
      _self.$refs.searchDoc.loadGridData();
    },
    //获取类型
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
    }
  }
};
</script>
<style scoped>
</style>