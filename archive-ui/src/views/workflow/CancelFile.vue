<template>
  <DataLayout>
    <template v-slot:header>

            <el-dialog :title="$t('application.Import')" :visible.sync="importdialogVisible" width="70%" :close-on-click-modal="false" :append-to-body="true">
                <el-form size="mini" :label-width="formLabelWidth" v-loading='uploading'>
                    <div style="height:150px;overflow-y:scroll; overflow-x:scroll;">
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
        :title="$t('application.AddFile')"
        :visible.sync="propertyVisible"
        @close="propertyVisible = false"
        :append-to-body="true"
        width="90%"
        :close-on-click-modal="false"
        v-dialogDrag
        modal-append-to-body="false" 
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
          gridViewName="DesignCancelGrid"
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
                        <el-button type="warning" @click="removeFile">{{ $t("application.delete") }}</el-button>
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
                  condition=" and a.NAME='irel_children'"
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
              <el-tab-pane label="附件列表" name="t02">
                <el-row v-if="allowEdit">
                  <el-col :span="24" style="text-align: left">
                    <el-form :inline="true" :model="filters" @submit.native.prevent>
                      <el-form-item>
                      <el-button type="primary" @click="beforeUploadFile('/dc/addAttachment4Copy')">添加附件</el-button>
                      </el-form-item>
                      <el-form-item>
                        <el-button type="warning" @click="deleteAttach">{{ $t("application.delete") }}</el-button>
                      </el-form-item>
                    </el-form>
                  </el-col>
                </el-row>
                <!--列表-->
                <DataGrid
                  ref="attach"
                  key="attach"
                  data-url="/dc/getDocuByRelationParentId"
                  :parentId="GUID"
                  v-bind:tableHeight="tableHeight"
                  v-bind:isshowOption="true"
                  v-bind:isshowSelection="true"
                  gridViewName="BorrowSequenceGrid"
                  condition=" and a.NAME='irel_children' and b.TYPE_NAME='附件'"
                  :optionWidth="1"
                  :isShowMoreOption="false"
                  :isshowCustom="false"
                  :isEditProperty="allowEdit"
                  :isShowChangeList="false"
                  :isshowicon="false"
                  :isshowPage="isShowPage"
                  @selectchange="attachSelect"
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
      selectedItems:[],
      butt:false,
      searchFileCondition:"",
      createUnit:'',
      sameCreate:true,
      sameDepartMent:true,
      selectedRemoveFiles:'',
      importdialogVisible:false,
      dialogVisible:false,
      parentId:'',
      GUID:'',
       fileList: [],
       uploading:false,
       selectedAttachment:[],
       uploadUrl:'',
    };
  },
  mounted() {
    this.getGUID()
    this.getTypeNamesByMainList("DCTypeSubContractor");
  },
  methods: {
    initTable(){
    this.$refs.attach.loadGridData();  
    this.$refs.fileList.loadGridData();
    },
            beforeUploadFile(uploadpath){
            let _self=this;
            _self.parentId = _self.GUID          
            if(_self.parentId==undefined||_self.parentId==''){
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
            uploadData() {
            let _self = this;
            let formdata = _self.getFormData();
            _self.uploading=true;
            console.log(formdata)
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
                _self.uploading=false;
                //_self.$refs.attachmentDoc.loadGridData();
                _self.$refs.attach.loadGridData();  
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
            getFormData() {
            let _self = this;
            let formdata = new FormData();
            var data = {};
            data["parentDocId"] = _self.parentId;//_self.selectedInnerItems[0].ID;//_self.selectedFileId;
            data["relationName"]='irel_children';
            data["TYPE_NAME"]='附件';
            formdata.append("metaData", JSON.stringify(data));
            _self.fileList.forEach(function(file) {
                formdata.append("uploadFile", file.raw, file.name);
            });
            return formdata;
            },
            handleChange(file, fileList) {
            this.fileList = fileList;
            console.log(this.fileList)
        },
            getGUID(){
             let _self = this
             if(this.GUID==''){
             axios.get("/dc/GUID").then(function(response) {
            _self.GUID = response.data.data
            console.log(_self.GUID)
            })}},

    relevantDocRVSelect(val){
        this.selectedRemoveFiles = val
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
    deleteAttach(){
      let ids = []
      let _self = this
      ids.push(_self.parentId)
      this.selectedAttachment.forEach(function(item){
                ids.push(item.ID)
            })
      axios.post("/exchange/doc/deleteRelations",ids).then(function(response){
        let code = response.data.code
        if(code==0){
          _self.$message("删除成功")
          _self.$refs.attach.loadGridData()
        }
      })
    },
    attachSelect(val){
      this.selectedAttachment = val
    },


    checkCreateUnit(){
      let crUnit = null
      this.createUnit = null
      this.sameCreate = true      //指示器清空
    for(let tab in this.selectedFiles){

      if(this.selectedFiles[tab].C_CREATE_UNIT==undefined){
        this.createUnit = "未定义"
        crUnit = "未定义"
      }
       if(this.selectedFiles[tab].C_CREATE_UNIT!=undefined){
        
        crUnit =  this.selectedFiles[tab].C_CREATE_UNIT           //当前编制单位指示器
        if(this.createUnit!=crUnit){
        this.sameCreate = false
      }
         this.createUnit = this.selectedFiles[tab].C_CREATE_UNIT       //全局指示器
      }

      if(crUnit!=this.currentUser().department){
        this.sameDepartMent = false
      }
    }
    },

    // select(val){
    //   this.selectedItems=val
    // },
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
        this.checkCreateUnit()
        if(this.sameCreate==false){
        this.$message("请选择相同编制单位的文件！")
        return
        }
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