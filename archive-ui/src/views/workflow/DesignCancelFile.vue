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
    <selectDC @selectchange="selectedChooseDC"  :conditionFile="searchFileCondition"></selectDC>
        <div slot="footer" class="dialog-footer">
            <el-button @click="saveFileToWorkflow" :loading="butt">{{$t('application.save')}}</el-button>
            <el-button @click="propertyVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
    </template>
    <template v-slot:main="{layout}">
      <!-- <div :style="{position:'relative',height: layout.height-startHeight+'px'}"> -->
      <div :style="{position:'relative'}">

            <el-tabs value="t01">
              <el-tab-pane :label="$t('application.FilesInWorkflow')" name="t01">
                <el-row v-if="allowEdit">
                  <el-col :span="24" style="text-align: left">
                    <el-form :inline="true" :model="filters" @submit.native.prevent>
                      <el-form-item>
                        <el-button type="primary" @click="beforeAddFile">{{ $t("application.new") }}</el-button>
                      </el-form-item>
                      <el-form-item>
                        <el-button type="warning" @click="removeFile">{{ $t("application.remove") }}</el-button>
                      </el-form-item>
                    </el-form>
                  </el-col>
                </el-row>
                <!--列表-->
                <DataGrid
                  ref="fileList"
                  key="fileList"
                  data-url="/dc/getDocuByRelationParentId"
                  :tableHeight="tableHeight"
                  :isshowOption="true"
                  :isshowSelection="true"
                  gridViewName="DesignCancelGrid"
                  condition=" and a.NAME='irel_children'"
                  :optionWidth="1"
                  :itemDataList="files"
                  :isShowMoreOption="true"
                  showOptions="查看内容,查看属性"
                  :isshowCustom="false"
                  :isEditProperty="false"
                  :isShowPropertyButton="false"
                  :isShowChangeList="false"
                  :isshowicon="false"
                  :isshowPage="isShowPage"
                  :isInitData="false"
                  @selectchange="relevantDocRVSelect"
                ></DataGrid>
              </el-tab-pane>
            </el-tabs>

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
import MountFile from '@/components/MountFile.vue';
import selectDC from"@/components/controls/selectDC.vue"
export default {
  components: {
    ShowProperty: ShowProperty,
    DataGrid: DataGrid,
    AddCondition: AddCondition,
    DataSelect: DataSelect,
    RejectButton: RejectButton,
    DataLayout: DataLayout,
    AttachmentFile: AttachmentFile,
    MountFile:MountFile,
    selectDC:selectDC
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
      tableHeight:"400px",
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
      searchFileCondition:"",
      selectedRemoveFiles:[]
    };
  },
  mounted() {
    this.searchItem()
  },
  methods: {
        selectedChooseDC(val) {
      this.selectedFiles = val;
    },

    beforeAddFile() {
      let _self=this;
      this.getEcmcfgActive(_self.workflowObj.ID,"start",function(ecmCfgActivity){
        _self.searchFileCondition=ecmCfgActivity.formCondition;
        _self.propertyVisible=true;
      });
        
    },
    relevantDocRVSelect(val){
      this.selectedRemoveFiles=val;
    },
    removeFile(){
      let _self=this;
      for(let i=0;i<_self.selectedRemoveFiles.length;i++){
        let e=_self.selectedRemoveFiles[i];
        for(let n=0;n<_self.$refs.fileList.itemDataList.length;n++){
          if(e.ID==_self.$refs.fileList.itemDataList[n].ID){
            _self.$refs.fileList.itemDataList.splice(n, 1);
            n--;
          }
        }
        _self.selectedRemoveFiles.splice(i,1);
        i--;
      }

      _self.selectedRemoveFiles.forEach(e=>{

        
        
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
                _self.$refs.fileList.itemDataList.find(function(value) {
                if(value.ID === e.ID) {
                    isContain=true;
                    return;
                    //则包含该元素
                    }
                })
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